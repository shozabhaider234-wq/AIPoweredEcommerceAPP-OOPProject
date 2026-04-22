package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.OrderDto;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.PlaceOrderRequest;
import OopProject.AiPoweredEcommerceSystem.dto.UpdateOrderStatusRequest;
import OopProject.AiPoweredEcommerceSystem.dto.SellerCancelOrderRequest;
import OopProject.AiPoweredEcommerceSystem.entity.*;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.*;
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
 * Order placement and management — v2.
 *
 * <p>Customer flow:
 * <ol>
 *   <li>Customer POSTs with shipping details + CASH_ON_DELIVERY payment</li>
 *   <li>Stock is validated and deducted</li>
 *   <li>Order is saved with PENDING status and all shipping info</li>
 *   <li>Cart is cleared</li>
 *   <li>Customer can cancel their own PENDING order</li>
 * </ol>
 *
 * <p>Seller flow:
 * <ol>
 *   <li>Seller sees all orders containing their products (with customer shipping details)</li>
 *   <li>Seller advances status: PENDING → CONFIRMED → SHIPPED → DELIVERED</li>
 *   <li>Seller can cancel with a mandatory reason at any time before DELIVERED</li>
 *   <li>On DELIVERED: payment status is automatically set to PAID (COD collected)</li>
 * </ol>
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository           orderRepository;
    private final CartRepository            cartRepository;
    private final ProductRepository         productRepository;
    private final UserInteractionRepository interactionRepository;
    private final SecurityUtils             securityUtils;
    // -------------------Newly added-----------------------
    private final SellerRepository sellerRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        UserInteractionRepository interactionRepository,
                        SecurityUtils securityUtils,
                        SellerRepository sellerRepository) {
        this.orderRepository        = orderRepository;
        this.cartRepository         = cartRepository;
        this.productRepository      = productRepository;
        this.interactionRepository  = interactionRepository;
        this.securityUtils          = securityUtils;
        // Added
        this.sellerRepository=sellerRepository;
    }

    // ── Place Order ───────────────────────────────────────────

    /**
     * Converts the authenticated user's cart into a confirmed order.
     *
     * @throws BadRequestException if the cart is empty or any item has insufficient stock
     */

    /**
     Previous function

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
     }*/

    //Newly made function for Placing order

    // ══════════════════════════════════════════════════════════
    //   CUSTOMER — Place Order
    // ══════════════════════════════════════════════════════════

    /**
     * Converts the authenticated user's cart into a placed order.
     *
     * <p>Validates:
     * <ul>
     *   <li>Cart is not empty</li>
     *   <li>Payment method is CASH_ON_DELIVERY (only supported method)</li>
     *   <li>Every item has sufficient stock</li>
     * </ul>
     *
     * @param req shipping details and payment method supplied by the customer
     */

    public OrderDto placeOrder(PlaceOrderRequest req) {
        User user = securityUtils.getCurrentUser();

        // ── Validate payment method ───────────────────────────
        if (!"CASH_ON_DELIVERY".equalsIgnoreCase(req.getPaymentMethod())) {
            throw new BadRequestException(
                    "Only CASH_ON_DELIVERY is supported at this time. " +
                            "Please select 'CASH_ON_DELIVERY' as your payment method.");
        }

        // ── Load cart ─────────────────────────────────────────
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("You have no active cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place an order with an empty cart");
        }

        // ── Build Order with shipping details ─────────────────
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(Order.PaymentMethod.CASH_ON_DELIVERY);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        // Shipping info — captured permanently on the order
        order.setShippingAddress(req.getShippingAddress());
        order.setShippingCity(req.getShippingCity());
        order.setShippingPostalCode(req.getShippingPostalCode());
        order.setContactPhone(req.getContactPhone());
        order.setDeliveryNotes(req.getDeliveryNotes());

        BigDecimal total = BigDecimal.ZERO;

        // ── Process each cart item ────────────────────────────
        for (CartItem cartItem : cart.getItems()) {
            Product p = cartItem.getProduct();

            if (p.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for '" + p.getName() +
                                "'. Available: " + p.getStock() +
                                ", Requested: " + cartItem.getQuantity());
            }

            // Deduct stock
            p.setStock(p.getStock() - cartItem.getQuantity());
            productRepository.save(p);

            // Snapshot unit price at time of purchase
            OrderItem orderItem = new OrderItem(order, p, cartItem.getQuantity(), p.getPrice());
            order.getItems().add(orderItem);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Track PURCHASE for recommendation engine
            interactionRepository.save(
                    new UserInteraction(user, p, UserInteraction.InteractionType.PURCHASE));
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order #{} placed by {} | total={} | ship-to={}, {} | payment=COD",
                saved.getId(), user.getEmail(), total,
                req.getShippingAddress(), req.getShippingCity());

        return OrderDto.from(saved);
    }


    // ── Cancel Order ──────────────────────────────────────────

    /**
     * Cancels a PENDING order.
     *Allows a customer to cancel their own PENDING order.
     *Stock is restored when the order is cancelled.
     * @throws BadRequestException if the order is not in PENDING status
     */
    public OrderDto cancelOrder(Long orderId) {
        User  user  = securityUtils.getCurrentUser();
        Order order = getOwnOrderOrThrow(orderId, user);

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        //----------- Newly added---------------------

        // Restore stock
        restoreStock(order);
        //----------------------------------------------------
        order.setStatus(Order.OrderStatus.CANCELLED);
        log.info("Order #{} cancelled by user {}", orderId, user.getEmail());
        return OrderDto.from(orderRepository.save(order));
    }

    // ── Read ──────────────────────────────────────────────────

    /** Full order detail by ID (must belong to the current user). */
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

    //-----------Newly Added--------------------
    // ══════════════════════════════════════════════════════════
    //   SELLER — View Orders
    // ══════════════════════════════════════════════════════════

    /**
     * Returns all orders that contain at least one product from this seller's store.
     * Full shipping details and customer contact info are included so the seller
     * knows exactly where and how to deliver.
     *
     * @param page zero-based page index
     * @param size results per page
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrderDto> getSellerOrders(int page, int size) {
        Seller   seller   = getAuthenticatedSeller();
        Pageable pageable = PageRequest.of(page, size);
        return PagedResponse.of(
                orderRepository.findDistinctByItems_Product_Seller_Id(seller.getId(), pageable)
                        .map(OrderDto::from));

    }
    /**
     * Returns the full detail of one order, verifying the seller has a product in it.
     */
    @Transactional(readOnly = true)
    public OrderDto getSellerOrderById(Long orderId) {
        Seller seller = getAuthenticatedSeller();
        Order  order  = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        assertSellerOwnsOrder(seller, order);
        return OrderDto.from(order);
    }

    // ══════════════════════════════════════════════════════════
    //   SELLER — Update Status
    // ══════════════════════════════════════════════════════════

    /**
     * Allows the seller to advance the order status.
     *
     * <p>Valid transitions the seller may request:
     * <pre>
     *   PENDING   → CONFIRMED
     *   CONFIRMED → SHIPPED
     *   SHIPPED   → DELIVERED
     * </pre>
     *
     * <p>When the order reaches DELIVERED, the payment status is automatically
     * flipped to PAID (cash has been collected on delivery).
     *
     * @param orderId the order to update
     * @param req     the new status + optional note (e.g. tracking number)
     */
    public OrderDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest req) {
        Seller seller = getAuthenticatedSeller();
        Order  order  = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        assertSellerOwnsOrder(seller, order);

        // Parse and validate requested status
        Order.OrderStatus requestedStatus;
        try {
            requestedStatus = Order.OrderStatus.valueOf(req.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid status '" + req.getStatus() + "'. " +
                            "Valid values: CONFIRMED, SHIPPED, DELIVERED");
        }

        // Sellers cannot set PENDING or CANCELLED via this endpoint
        if (requestedStatus == Order.OrderStatus.PENDING ||
                requestedStatus == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Sellers cannot set status to " + requestedStatus +
                            ". Use the cancel endpoint to cancel an order.");
        }

        // Enforce forward-only progression
        validateStatusTransition(order.getStatus(), requestedStatus);

        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(requestedStatus);

        // Store the optional seller note (e.g. tracking number)
        if (req.getStatusNote() != null && !req.getStatusNote().isBlank()) {
            order.setStatusNote(req.getStatusNote());
        }

        // COD: mark as PAID when delivered — cash collected by rider
        if (requestedStatus == Order.OrderStatus.DELIVERED) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            log.info("Order #{} delivered — COD payment marked as PAID", orderId);
        }

        log.info("Order #{} status updated {} → {} by seller '{}' | note: {}",
                orderId, previousStatus, requestedStatus,
                seller.getStoreName(),
                req.getStatusNote() != null ? req.getStatusNote() : "(none)");

        return OrderDto.from(orderRepository.save(order));
    }

    // ══════════════════════════════════════════════════════════
    //   SELLER — Cancel Order (with mandatory reason)
    // ══════════════════════════════════════════════════════════

    /**
     * Allows the seller to cancel an order at any point before DELIVERED.
     * A non-blank cancellation reason is required so the customer understands why.
     * Stock is automatically restored.
     *
     * @param orderId the order to cancel
     * @param req     must contain a non-blank reason (10–500 chars)
     */
    public OrderDto sellerCancelOrder(Long orderId, SellerCancelOrderRequest req) {
        Seller seller = getAuthenticatedSeller();
        Order  order  = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        assertSellerOwnsOrder(seller, order);

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "Cannot cancel an order that has already been delivered.");
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Order #" + orderId + " is already cancelled.");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledBy("SELLER");
        order.setCancellationReason(req.getReason());

        // Restore stock so products are available again
        restoreStock(order);

        log.info("Order #{} cancelled by seller '{}' — reason: {}",
                orderId, seller.getStoreName(), req.getReason());

        return OrderDto.from(orderRepository.save(order));
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

    // -------------Newly added---------------------
    /**
     * Restores the stock of every product in a cancelled order.
     * Called both on customer and seller cancellation.
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            productRepository.save(p);
            log.debug("Stock restored for product '{}': +{} units", p.getName(), item.getQuantity());
        }
    }
    private Seller getAuthenticatedSeller() {
        User user = securityUtils.getCurrentUser();
        return sellerRepository.findByUser(user)
                .orElseThrow(() -> new UnauthorizedException(
                        "You must have a seller profile to manage orders"));
    }

    private void assertSellerOwnsOrder(Seller seller, Order order) {
        boolean owns = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller() != null &&
                        item.getProduct().getSeller().getId().equals(seller.getId()));
        if (!owns) {
            throw new UnauthorizedException(
                    "Order #" + order.getId() + " does not contain any of your products.");
        }
    }

    private void validateStatusTransition(Order.OrderStatus current,
                                          Order.OrderStatus requested) {
        // Build the allowed next step map
        java.util.Map<Order.OrderStatus, Order.OrderStatus> nextStep = new java.util.HashMap<>();
        nextStep.put(Order.OrderStatus.PENDING,   Order.OrderStatus.CONFIRMED);
        nextStep.put(Order.OrderStatus.CONFIRMED, Order.OrderStatus.SHIPPED);
        nextStep.put(Order.OrderStatus.SHIPPED,   Order.OrderStatus.DELIVERED);

        Order.OrderStatus allowed = nextStep.get(current);

        if (allowed == null) {
            throw new BadRequestException(
                    "Cannot update status — order is in a terminal state: " + current);
        }
        if (!allowed.equals(requested)) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " → " + requested +
                            ". The next allowed status is: " + allowed);
        }
    }
}