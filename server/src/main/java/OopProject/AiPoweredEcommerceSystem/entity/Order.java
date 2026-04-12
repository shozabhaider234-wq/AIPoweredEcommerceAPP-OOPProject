package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Line items in this order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Order Status Enum ─────────────────────────────────────

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
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
}
