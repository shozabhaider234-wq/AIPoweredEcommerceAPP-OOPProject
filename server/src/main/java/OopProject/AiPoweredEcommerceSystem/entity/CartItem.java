package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A single line-item inside a {@link Cart}.
 *
 * <p>Holds a reference to the product and the requested quantity.
 * A unique constraint on (cart_id, product_id) prevents duplicate rows
 * for the same product in the same cart.
 */

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER )
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Must be at least 1. */
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    // ── Constructors ──────────────────────────────────────────

    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart     = cart;
        this.product  = product;
        this.quantity = quantity;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Cart getCart()                      { return cart; }
    public void setCart(Cart cart)             { this.cart = cart; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }

    public Integer getQuantity()               { return quantity; }
    public void setQuantity(Integer quantity)  { this.quantity = quantity; }
}
