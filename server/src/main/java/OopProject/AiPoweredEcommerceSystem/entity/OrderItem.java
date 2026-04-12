package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * A single line-item within an {@link Order}.
 *
 * <p>The unit price is snapshotted at the time of purchase so that
 * subsequent price changes do not affect historical orders.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Reference to the product.
     * We keep the FK so it is traceable, but the price is snapshotted below.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /** Unit price at the moment the order was placed. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    // ── Constructors ──────────────────────────────────────────

    public OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal price) {
        this.order    = order;
        this.product  = product;
        this.quantity = quantity;
        this.price    = price;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Order getOrder()                    { return order; }
    public void setOrder(Order order)          { this.order = order; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }

    public Integer getQuantity()               { return quantity; }
    public void setQuantity(Integer quantity)  { this.quantity = quantity; }

    public BigDecimal getPrice()               { return price; }
    public void setPrice(BigDecimal price)     { this.price = price; }
}

