package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.OrderDto;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.entity.Cart;
import OopProject.AiPoweredEcommerceSystem.entity.CartItem;
import OopProject.AiPoweredEcommerceSystem.entity.Order;
import OopProject.AiPoweredEcommerceSystem.entity.OrderItem;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.UserInteraction;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.CartRepository;
import OopProject.AiPoweredEcommerceSystem.repository.OrderRepository;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.UserInteractionRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.OrderServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Order placement and management.
 *
 * <p>Placing an order:
 * <ol>
 *   <li>Reads all items from the user's cart</li>
 *   <li>Validates stock for every item</li>
 *   <li>Deducts stock and snapshots the unit price</li>
 *   <li>Saves the Order + OrderItems in one transaction</li>
 *   <li>Records PURCHASE interactions for the recommendation engine</li>
 *   <li>Clears the cart</li>
 * </ol>
 */
@Service
@Transactional
public class OrderService extends OrderServiceAbstraction{

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository           orderRepository;
    private final CartRepository            cartRepository;
    private final ProductRepository         productRepository;
    private final UserInteractionRepository interactionRepository;
    private final SecurityUtils             securityUtils;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        UserInteractionRepository interactionRepository,
                        SecurityUtils securityUtils) {
        this.orderRepository        = orderRepository;
        this.cartRepository         = cartRepository;
        this.productRepository      = productRepository;
        this.interactionRepository  = interactionRepository;
        this.securityUtils          = securityUtils;
    }

    // ── Place Order ───────────────────────────────────────────

    /**
     * Converts the authenticated user's cart into a confirmed order.
     *
     * @throws BadRequestException if the cart is empty or any item has insufficient stock
     */
    @Override
    public OrderDto placeOrder() {
        User user = securityUtils.getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("You have no active cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product p = cartItem.getProduct();

            // Validate stock
            if (p.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for '" + p.getName() +
                                "'. Available: " + p.getStock() +
                                ", Requested: " + cartItem.getQuantity());
            }

            // Deduct stock
            p.setStock(p.getStock() - cartItem.getQuantity());
            productRepository.save(p);

            // Snapshot the unit price at purchase time
            OrderItem orderItem = new OrderItem(order, p, cartItem.getQuantity(), p.getPrice());
            order.getItems().add(orderItem);

            total = total.add(p.getPrice().multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())));

            // Track PURCHASE for recommendation engine
            interactionRepository.save(new UserInteraction(
                    user, p, UserInteraction.InteractionType.PURCHASE));
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        // Clear the cart after successful order placement
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order #{} placed by user {} | total=${}", saved.getId(), user.getEmail(), total);
        return OrderDto.from(saved);
    }

    // ── Cancel Order ──────────────────────────────────────────

    /**
     * Cancels a PENDING order.
     *
     * @throws BadRequestException if the order is not in PENDING status
     */
    @Override
    public OrderDto cancelOrder(Long orderId) {
        User  user  = securityUtils.getCurrentUser();
        Order order = getOwnOrderOrThrow(orderId, user);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        log.info("Order #{} cancelled by user {}", orderId, user.getEmail());
        return OrderDto.from(orderRepository.save(order));
    }

    // ── Read ──────────────────────────────────────────────────

    /** Full order detail by ID (must belong to the current user). */
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        User user = securityUtils.getCurrentUser();
        return OrderDto.from(getOwnOrderOrThrow(orderId, user));
    }

    /** Paginated order history for the current user, newest first. */
    @Transactional(readOnly = true)
    public PagedResponse<OrderDto> getOrderHistory(int page, int size) {
        User     user     = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.of(
                orderRepository.findByUser(user, pageable).map(OrderDto::from));
    }

    // ── Private helpers ───────────────────────────────────────

    private Order getOwnOrderOrThrow(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to access order #" + orderId);
        }
        return order;
    }
}
