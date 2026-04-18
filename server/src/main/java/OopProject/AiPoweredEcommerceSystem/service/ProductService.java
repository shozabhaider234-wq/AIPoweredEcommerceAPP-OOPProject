package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;
import OopProject.AiPoweredEcommerceSystem.dto.ProductRequest;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.entity.Category;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.Seller;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.UserInteraction;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.CategoryRepository;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.SellerRepository;
import OopProject.AiPoweredEcommerceSystem.repository.UserInteractionRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.ProductServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Business logic for product catalog management.
 *
 * <p>Seller operations (create / update / delete) verify that the
 * authenticated user owns the product before proceeding.
 *
 * <p>Every call to {@link #getProductById} tracks a VIEW interaction
 * for the recommendation engine when a userId is provided.
 */
@Service
@Transactional
public class ProductService extends ProductServiceAbstraction {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository         productRepository;
    private final CategoryRepository        categoryRepository;
    private final SellerRepository          sellerRepository;
    private final UserInteractionRepository interactionRepository;
    private final SecurityUtils             securityUtils;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SellerRepository sellerRepository,
                          UserInteractionRepository interactionRepository,
                          SecurityUtils securityUtils) {
        this.productRepository    = productRepository;
        this.categoryRepository   = categoryRepository;
        this.sellerRepository     = sellerRepository;
        this.interactionRepository = interactionRepository;
        this.securityUtils        = securityUtils;
    }

    // ── Create ────────────────────────────────────────────────

    /**
     * Creates a new product listing for the authenticated seller.
     *
     * @param req product data
     * @return the persisted product as a DTO
     * @throws BadRequestException if the current user has no seller profile
     */
    @Override
    public ProductDto createProduct(ProductRequest req) {
        User   user   = securityUtils.getCurrentUser();
        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException(
                        "You need a seller profile to list products. " +
                        "Create one via POST /api/sellers"));

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());
        product.setSeller(seller);

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + req.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, name='{}', seller={}",
                saved.getId(), saved.getName(), seller.getStoreName());
        return ProductDto.from(saved);
    }

    // ── Update ────────────────────────────────────────────────

    /**
     * Updates an existing product owned by the authenticated seller.
     */
    @Override
    public ProductDto updateProduct(Long productId, ProductRequest req) {
        Product product = getOwnedProductOrThrow(productId);

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStock(req.getStock());

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + req.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);   // allow un-categorising
        }

        return ProductDto.from(productRepository.save(product));
    }

    // ── Delete ────────────────────────────────────────────────

    /**
     * Deletes a product owned by the authenticated seller.
     * Cascades to ProductImage rows via JPA.
     */
    @Override
    public void deleteProduct(Long productId) {
        Product product = getOwnedProductOrThrow(productId);
        productRepository.delete(product);
        log.info("Product deleted: id={}", productId);
    }

    // ── Read ──────────────────────────────────────────────────

    /**
     * Returns the full detail page for a single product.
     *
     * <p>If a {@code userId} is provided the interaction is recorded for
     * the recommendation engine.
     *
     * @param productId the product to fetch
     * @param userId    optional — the viewing user's ID (null for anonymous)
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        // Track VIEW interaction (fire-and-forget; runs in the same TX)
        if (userId != null) {
            // Interaction requires a managed User entity — skip if user is anonymous
            try {
                // We resolve the user lazily to avoid a DB hit on every anonymous call
                User user = new User();
                user.setId(userId);
                // Use a proxy reference so we don't need a full load
                interactionRepository.save(
                        new UserInteraction(user, product, UserInteraction.InteractionType.VIEW));
            } catch (Exception e) {
                log.warn("Failed to record VIEW interaction for user {}: {}", userId, e.getMessage());
            }
        }

        return ProductDto.from(product);
    }

    /**
     * Returns a paginated list of all products, sorted by the given field.
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductDto> result = productRepository.findAll(pageable).map(ProductDto::from);
        return PagedResponse.of(result);
    }

    /**
     * Multi-field product search with optional filters.
     * All parameters are optional — if all are null, returns all products.
     *
     * @param name       keyword to match against product name (case-insensitive)
     * @param categoryId exact category ID filter
     * @param minPrice   inclusive lower price bound
     * @param maxPrice   inclusive upper price bound
     * @param page       zero-based page index
     * @param size       number of results per page
     * @param sortBy     field name to sort by (e.g. "price", "name")
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> searchProducts(String name,
                                                     Long categoryId,
                                                     BigDecimal minPrice,
                                                     BigDecimal maxPrice,
                                                     int page,
                                                     int size,
                                                     String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductDto> result = productRepository
                .searchProducts(name, categoryId, minPrice, maxPrice, pageable)
                .map(ProductDto::from);
        return PagedResponse.of(result);
    }

    // ── Private helpers ───────────────────────────────────────

    /**
     * Loads a product and verifies that the current authenticated user is
     * the seller who owns it. Throws appropriate exceptions otherwise.
     */
    private Product getOwnedProductOrThrow(Long productId) {
        User user = securityUtils.getCurrentUser();

        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new UnauthorizedException(
                        "You do not have a seller profile"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorizedException(
                    "You are not authorized to modify product id: " + productId);
        }

        return product;
    }
}
