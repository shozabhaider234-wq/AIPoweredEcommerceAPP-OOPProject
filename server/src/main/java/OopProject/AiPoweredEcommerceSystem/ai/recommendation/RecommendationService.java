package OopProject.AiPoweredEcommerceSystem.ai.recommendation;

import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.UserInteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Recommendation engine that generates personalised product suggestions
 * based on a user's interaction history.
 *
 * <p>The algorithm uses three layers, applied in order until 10 recommendations
 * are collected:
 *
 * <ol>
 *   <li><strong>Category affinity</strong> — products from the categories the user
 *       has interacted with most (VIEW, SEARCH, ADD_TO_CART, PURCHASE).</li>
 *   <li><strong>Similar items</strong> — products in the same category as recently
 *       viewed or carted items the user has not yet purchased.</li>
 *   <li><strong>Popular fallback</strong> — highest-stock products as a catch-all
 *       when behaviour data is sparse (new users, cold start).</li>
 * </ol>
 *
 * <p>Already-purchased products are always excluded to avoid recommending
 * something the user has already bought.
 */
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    /** Maximum number of recommendations returned. */
    private static final int MAX_RECOMMENDATIONS = 10;

    private final ProductRepository         productRepository;
    private final UserInteractionRepository interactionRepository;

    public RecommendationService(ProductRepository productRepository,
                                 UserInteractionRepository interactionRepository) {
        this.productRepository    = productRepository;
        this.interactionRepository = interactionRepository;
    }

    /**
     * Generate up to {@value #MAX_RECOMMENDATIONS} personalised product recommendations.
     *
     * @param userId the user to generate recommendations for
     * @return ordered list of recommended products (most relevant first)
     */
    public List<Product> getRecommendations(Long userId) {
        log.debug("Generating recommendations for user {}", userId);

        // Build exclusion set — products the user has already purchased
        Set<Long> purchasedIds = new HashSet<>(
                interactionRepository.findPurchasedProductIdsByUserId(userId));

        // Use LinkedHashSet to preserve insertion order and deduplicate
        Set<Product> recommendations = new LinkedHashSet<>();

        // ── Layer 1: Category Affinity ────────────────────────────────
        // Find the categories the user has most interacted with and suggest
        // products from those categories that haven't been purchased yet.
        List<Long> topCategoryIds =
                interactionRepository.findTopCategoryIdsByUserId(userId);

        for (Long catId : topCategoryIds) {
            if (recommendations.size() >= MAX_RECOMMENDATIONS) break;

            int remaining = MAX_RECOMMENDATIONS - recommendations.size();
            productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null &&
                                 p.getCategory().getId().equals(catId))
                    .filter(p -> !purchasedIds.contains(p.getId()))
                    .filter(p -> !recommendations.contains(p))
                    .limit(remaining)
                    .forEach(recommendations::add);
        }

        // ── Layer 2: Similar to Recently Engaged Products ─────────────
        // Look at products the user recently viewed or added to cart,
        // and suggest other products from the same categories.
        if (recommendations.size() < MAX_RECOMMENDATIONS) {
            List<Long> recentProductIds =
                    interactionRepository.findRecentlyEngagedProductIds(
                            userId, PageRequest.of(0, 10));

            List<Product> recentProducts =
                    productRepository.findByIdIn(recentProductIds);

            for (Product seed : recentProducts) {
                if (recommendations.size() >= MAX_RECOMMENDATIONS) break;
                if (seed.getCategory() == null) continue;

                int remaining = MAX_RECOMMENDATIONS - recommendations.size();
                productRepository
                        .findByCategoryAndIdNot(
                                seed.getCategory(),
                                seed.getId(),
                                PageRequest.of(0, 5))
                        .stream()
                        .filter(p -> !purchasedIds.contains(p.getId()))
                        .filter(p -> !recommendations.contains(p))
                        .limit(remaining)
                        .forEach(recommendations::add);
            }
        }

        // ── Layer 3: Popular Fallback ─────────────────────────────────
        // Fill any remaining slots with the most popular (highest-stock) products.
        if (recommendations.size() < MAX_RECOMMENDATIONS) {
            int remaining = MAX_RECOMMENDATIONS - recommendations.size();
            productRepository.findPopularProducts(PageRequest.of(0, MAX_RECOMMENDATIONS))
                    .stream()
                    .filter(p -> !purchasedIds.contains(p.getId()))
                    .filter(p -> !recommendations.contains(p))
                    .limit(remaining)
                    .forEach(recommendations::add);
        }

        log.debug("Generated {} recommendations for user {}", recommendations.size(), userId);
        return new ArrayList<>(recommendations);
    }
}
