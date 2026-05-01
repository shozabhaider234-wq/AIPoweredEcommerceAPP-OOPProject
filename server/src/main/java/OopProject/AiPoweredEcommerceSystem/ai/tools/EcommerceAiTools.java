package com.ecommerce.ai.tools;

import com.ecommerce.ai.recommendation.RecommendationService;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring AI Tool implementations for the ecommerce chatbot.
 *
 * <p>The {@code @Tool} annotation registers each method as a callable tool
 * that the Groq LLM can invoke automatically when it determines that
 * real data is needed to answer the user's message.
 *
 * <p>Available tools:
 * <ul>
 *   <li>{@link #searchProducts}    — keyword + filter product search</li>
 *   <li>{@link #getProductDetails} — full detail for one product</li>
 *   <li>{@link #recommendProducts} — personalised recommendations for a user</li>
 *   <li>{@link #getOrderStatus}    — current status of an order</li>
 * </ul>
 *
 * <p>Tool results are plain strings — the LLM incorporates them into its
 * natural-language reply to the user.
 */
@Component
public class EcommerceAiTools {

    private static final Logger log = LoggerFactory.getLogger(EcommerceAiTools.class);

    private final ProductRepository     productRepository;
    private final OrderRepository       orderRepository;
    private final RecommendationService recommendationService;

    public EcommerceAiTools(ProductRepository productRepository,
                            OrderRepository orderRepository,
                            RecommendationService recommendationService) {
        this.productRepository    = productRepository;
        this.orderRepository      = orderRepository;
        this.recommendationService = recommendationService;
    }

    // ── Tool 1: Search Products ───────────────────────────────

    /**
     * Search for products using optional filters.
     *
     * <p>The LLM will call this when a user asks questions like:
     * <ul>
     *   <li>"Show me running shoes under $100"</li>
     *   <li>"What electronics do you have?"</li>
     *   <li>"Find me a blue jacket"</li>
     * </ul>
     *
     * @param name     keyword to match against product names (case-insensitive), or null
     * @param category category name filter, or null
     * @param minPrice minimum price filter, or null
     * @param maxPrice maximum price filter, or null
     * @return formatted string of up to 5 matching products
     */
    @Tool(description =
            "Search for products in the store. Use this to find products by name keyword, " +
            "category, or price range. All parameters are optional — pass null to skip a filter.")
    public String searchProducts(
            @ToolParam(description = "Product name keyword to search for, e.g. 'shoe', 'laptop'. Pass null to skip.")
            String name,

            @ToolParam(description = "Category name filter, e.g. 'Electronics', 'Shoes'. Pass null to skip.")
            String category,

            @ToolParam(description = "Minimum price in USD, e.g. 20.0. Pass null to skip.")
            Double minPrice,

            @ToolParam(description = "Maximum price in USD, e.g. 100.0. Pass null to skip.")
            Double maxPrice) {

        log.debug("Tool: searchProducts(name={}, category={}, min={}, max={})",
                name, category, minPrice, maxPrice);

        List<Product> results = productRepository.findAll().stream()
                .filter(p -> name == null ||
                        p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> category == null || (p.getCategory() != null &&
                        p.getCategory().getName().equalsIgnoreCase(category)))
                .filter(p -> minPrice == null ||
                        p.getPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0)
                .filter(p -> maxPrice == null ||
                        p.getPrice().compareTo(BigDecimal.valueOf(maxPrice)) <= 0)
                .filter(p -> p.getStock() > 0)   // only in-stock items
                .limit(5)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return "No products found matching the given criteria.";
        }

        return results.stream()
                .map(p -> String.format(
                        "• [ID:%d] %s — $%.2f | Stock: %d | Category: %s",
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getStock(),
                        p.getCategory() != null ? p.getCategory().getName() : "Uncategorised"))
                .collect(Collectors.joining("\n",
                        "Found " + results.size() + " product(s):\n", ""));
    }

    // ── Tool 2: Product Details ───────────────────────────────

    /**
     * Get the full details of a specific product by its ID.
     *
     * <p>The LLM will call this when a user asks about a specific product,
     * e.g. "Tell me more about product #5" or "What are the specs of the
     * Sony headphones?".
     *
     * @param productId the numeric ID of the product
     * @return formatted product detail string
     */
    @Tool(description =
            "Get full details for a specific product by its ID. " +
            "Use this after searchProducts to get more information about a product the user is interested in.")
    public String getProductDetails(
            @ToolParam(description = "The numeric ID of the product, e.g. 5")
            Long productId) {

        log.debug("Tool: getProductDetails(productId={})", productId);

        return productRepository.findById(productId)
                .map(p -> String.format(
                        "Product Details:\n" +
                        "  Name        : %s\n" +
                        "  Description : %s\n" +
                        "  Price       : $%.2f\n" +
                        "  In Stock    : %d units\n" +
                        "  Category    : %s\n" +
                        "  Sold by     : %s",
                        p.getName(),
                        p.getDescription() != null ? p.getDescription() : "No description available",
                        p.getPrice(),
                        p.getStock(),
                        p.getCategory() != null ? p.getCategory().getName() : "Uncategorised",
                        p.getSeller() != null ? p.getSeller().getStoreName() : "Unknown seller"))
                .orElse("No product found with ID: " + productId);
    }

    // ── Tool 3: Recommend Products ────────────────────────────

    /**
     * Get personalised product recommendations for a user.
     *
     * <p>The LLM will call this when a user asks for suggestions, e.g.:
     * <ul>
     *   <li>"What would you recommend for me?"</li>
     *   <li>"Show me products I might like"</li>
     *   <li>"Give me some suggestions"</li>
     * </ul>
     *
     * @param userId the ID of the user to personalise recommendations for
     * @return formatted list of recommended products
     */
    @Tool(description =
            "Get personalised product recommendations for a user based on their browsing " +
            "and purchase history. Call this when the user asks for suggestions or recommendations.")
    public String recommendProducts(
            @ToolParam(description = "The numeric user ID from the chat context")
            Long userId) {

        log.debug("Tool: recommendProducts(userId={})", userId);

        List<Product> recommendations = recommendationService.getRecommendations(userId);

        if (recommendations.isEmpty()) {
            return "No personalised recommendations available yet. " +
                   "Browse some products to help us learn your preferences!";
        }

        return recommendations.stream()
                .map(p -> String.format(
                        "• [ID:%d] %s — $%.2f [%s]",
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getCategory() != null ? p.getCategory().getName() : "General"))
                .collect(Collectors.joining("\n",
                        "Here are " + recommendations.size() + " products you might like:\n", ""));
    }

    // ── Tool 4: Order Status ──────────────────────────────────

    /**
     * Get the current status of an order.
     *
     * <p>The LLM will call this when a user asks about their order, e.g.:
     * <ul>
     *   <li>"Where is my order #12?"</li>
     *   <li>"What's the status of order 5?"</li>
     *   <li>"Has my order shipped yet?"</li>
     * </ul>
     *
     * @param orderId the numeric order ID
     * @return formatted order status string
     */
    @Tool(description =
            "Get the current status of a customer order by its ID. " +
            "Use this when the user asks about their order status, shipping, or delivery.")
    public String getOrderStatus(
            @ToolParam(description = "The numeric order ID, e.g. 12")
            Long orderId) {

        log.debug("Tool: getOrderStatus(orderId={})", orderId);

        return orderRepository.findById(orderId)
                .map(o -> String.format(
                        "Order #%d:\n" +
                        "  Status  : %s\n" +
                        "  Total   : $%.2f\n" +
                        "  Items   : %d item(s)\n" +
                        "  Placed  : %s",
                        o.getId(),
                        o.getStatus(),
                        o.getTotalPrice(),
                        o.getItems().size(),
                        o.getCreatedAt()))
                .orElse("No order found with ID: " + orderId);
    }
}
