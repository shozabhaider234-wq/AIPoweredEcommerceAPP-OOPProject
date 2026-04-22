package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

/**
 * An order placed by a customer.
 *
 * <p>Status transitions:
 * <pre>
 *   PENDING → CONFIRMED → SHIPPED → DELIVERED
 *                └───────→ CANCELLED
 * </pre>
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The customer who placed the order. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // Newly added attributes

    /** Full street address supplied by the customer at checkout. */
    @NotBlank
    @Column(nullable = false)
    private String shippingAddress;

    /** City / town for delivery. */
    @NotBlank
    @Column(nullable = false)
    private String shippingCity;

    /** Postal / ZIP code. */
    @Column
    private String shippingPostalCode;

    /** Customer contact phone for the delivery rider. */
    @NotBlank
    @Column(nullable = false)
    private String contactPhone;

    /** Optional delivery notes (e.g. "Ring bell twice", "Leave at gate"). */
    @Column(columnDefinition = "TEXT")
    private String deliveryNotes;

    // ── Payment ───────────────────────────────────────────────

    /**
     * Payment method chosen at checkout.
     * Currently only CASH_ON_DELIVERY is offered.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    /**
     * Payment status.
     * For COD: stays PENDING until DELIVERED, then moves to PAID.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // ── Seller cancellation ───────────────────────────────────

    /**
     * Reason provided by the seller when they cancel an order.
     * Null for customer-initiated cancellations or non-cancelled orders.
     */
    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    /**
     * Who cancelled the order — "CUSTOMER" or "SELLER".
     * Null if the order has not been cancelled.
     */
    @Column(length = 20)
    private String cancelledBy;

    /**
     * Optional note set by the seller when updating the order status.
     * Example: "Dispatched via TCS. Tracking: TCS-987654321"
     * Visible to the customer in their order detail view.
     */
    @Column(columnDefinition = "TEXT")
    private String statusNote;



    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Refreshed every time the order status changes so the customer and
     * seller can see exactly when the last update happened.
     */
    @Column
    private LocalDateTime updatedAt;



    // till here


    /** Line items in this order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // New method added for updated
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    // ── Order Status Enum ─────────────────────────────────────

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
    // New Enums
    public enum PaymentMethod {
        CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING, PAID, REFUNDED
    }
    // ── Constructors ──────────────────────────────────────────

    public Order() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public User getUser()                          { return user; }
    public void setUser(User user)                 { this.user = user; }

    public BigDecimal getTotalPrice()              { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public OrderStatus getStatus()                 { return status; }
    public void setStatus(OrderStatus status)      { this.status = status; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<OrderItem> getItems()              { return items; }
    public void setItems(List<OrderItem> items)    { this.items = items; }


    // New getters and setters
    public String getShippingAddress()                     { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingCity()                        { return shippingCity; }
    public void setShippingCity(String shippingCity)       { this.shippingCity = shippingCity; }

    public String getShippingPostalCode()                  { return shippingPostalCode; }
    public void setShippingPostalCode(String code)         { this.shippingPostalCode = code; }

    public String getContactPhone()                        { return contactPhone; }
    public void setContactPhone(String contactPhone)       { this.contactPhone = contactPhone; }

    public String getDeliveryNotes()                       { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes)     { this.deliveryNotes = deliveryNotes; }

    public PaymentMethod getPaymentMethod()                { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod pm)         { this.paymentMethod = pm; }

    public PaymentStatus getPaymentStatus()                { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus ps)         { this.paymentStatus = ps; }

    public String getCancellationReason()                  { return cancellationReason; }
    public void setCancellationReason(String reason)       { this.cancellationReason = reason; }

    public String getCancelledBy()                         { return cancelledBy; }
    public void setCancelledBy(String cancelledBy)         { this.cancelledBy = cancelledBy; }

    public String getStatusNote()                          { return statusNote; }
    public void setStatusNote(String statusNote) { this.statusNote = statusNote; }

    public LocalDateTime getUpdatedAt()                    { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)      { this.updatedAt = updatedAt; }
}