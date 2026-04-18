package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.CartDto;
import OopProject.AiPoweredEcommerceSystem.entity.Cart;
import OopProject.AiPoweredEcommerceSystem.entity.CartItem;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.UserInteraction;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.CartItemRepository;
import OopProject.AiPoweredEcommerceSystem.repository.CartRepository;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.UserInteractionRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.CartServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shopping cart business logic.
 *
 * <p>Each user has exactly one persistent cart. The cart is lazily created
 * on first access. Adding a product to the cart also records an
 * ADD_TO_CART interaction for the recommendation engine.
 */
@Service
@Transactional
public class CartService extends CartServiceAbstraction {

    private final CartRepository            cartRepository;
    private final CartItemRepository        cartItemRepository;
    private final ProductRepository         productRepository;
    private final UserInteractionRepository interactionRepository;
    private final SecurityUtils             securityUtils;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserInteractionRepository interactionRepository,
                       SecurityUtils securityUtils) {
        this.cartRepository        = cartRepository;
        this.cartItemRepository    = cartItemRepository;
        this.productRepository     = productRepository;
        this.interactionRepository = interactionRepository;
        this.securityUtils         = securityUtils;
    }

    // ── Get Cart ──────────────────────────────────────────────

    /**
     * Returns the current user's cart. Creates a new empty cart if none exists.
     */
    @Override
    public CartDto getCart() {
        User user = securityUtils.getCurrentUser();
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
        return CartDto.from(cart);
    }

    // ── Add Item ──────────────────────────────────────────────

    /**
     * Adds a product to the cart (or increments quantity if already present).
     *
     * @param productId product to add
     * @param quantity  units to add (must be ≥ 1)
     * @throws BadRequestException if stock is insufficient
     */
    @Override
    public CartDto addToCart(Long productId, int quantity) {
        User user = securityUtils.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (product.getStock() < quantity) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " + product.getStock());
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        // Increment if already in cart, otherwise create new line item
        cartItemRepository.findByCartAndProduct(cart, product).ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + quantity),
                () -> cartItemRepository.save(new CartItem(cart, product, quantity))
        );

        // Track ADD_TO_CART for recommendation engine
        interactionRepository.save(new UserInteraction(
                user, product, UserInteraction.InteractionType.ADD_TO_CART));

        // Re-load cart to get fresh state with updated items
        return CartDto.from(cartRepository.findByUser(user).orElseThrow());
    }

    // ── Update Quantity ───────────────────────────────────────

    /**
     * Updates the quantity of an existing cart item.
     * Setting quantity to 0 or less removes the item entirely.
     */
    @Override
    public CartDto updateQuantity(Long cartItemId, int quantity) {
        User     user = securityUtils.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot modify another user's cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return CartDto.from(cartRepository.findByUser(user).orElseThrow());
    }

    // ── Remove Item ───────────────────────────────────────────

    /**
     * Removes a specific line item from the cart.
     */
    @Override
    public CartDto removeFromCart(Long cartItemId) {
        User     user = securityUtils.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot modify another user's cart");
        }

        cartItemRepository.delete(item);
        return CartDto.from(cartRepository.findByUser(user).orElseThrow());
    }
}
