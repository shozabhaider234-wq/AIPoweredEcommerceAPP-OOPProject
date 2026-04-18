package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.Wishlist;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.WishlistRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.WishlistServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wishlist business logic.
 *
 * <p>Users can save products to their wishlist to revisit later.
 * A product can only appear once per user (enforced by DB unique constraint
 * and checked here to give a readable error before hitting the DB).
 */
@Service
@Transactional
public class WishlistService extends WishlistServiceAbstraction {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository  productRepository;
    private final SecurityUtils      securityUtils;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository,
                           SecurityUtils securityUtils) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository  = productRepository;
        this.securityUtils      = securityUtils;
    }

    /**
     * Add a product to the current user's wishlist.
     *
     * @throws BadRequestException if the product is already in the wishlist
     */
    @Override
    public void addToWishlist(Long productId) {
        User user = securityUtils.getCurrentUser();

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BadRequestException("Product is already in your wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        wishlistRepository.save(new Wishlist(user, product));
    }

    /**
     * Remove a product from the current user's wishlist.
     * Silently succeeds if the product wasn't in the wishlist.
     */
    @Override
    public void removeFromWishlist(Long productId) {
        User user = securityUtils.getCurrentUser();
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    /**
     * Returns all products saved in the current user's wishlist.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getWishlist() {
        User user = securityUtils.getCurrentUser();
        return wishlistRepository.findByUser(user)
                .stream()
                .map(w -> ProductDto.from(w.getProduct()))
                .collect(Collectors.toList());
    }
}
