package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;

/**
 * Stores the path of a product image.
 *
 * <p>The actual file is saved on disk under {@code /uploads/products/}.
 * This entity records only the relative URL path (e.g.
 * {@code /images/products/abc123.jpg}) that the frontend can use to
 * request the image from the Spring MVC resource handler.
 */
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The relative URL path served by the static resource handler.
     * Example: {@code /images/products/product-1-hero.jpg}
     */
    @Column(nullable = false)
    private String imageUrl;

    /**
     * The product this image belongs to.
     * Cascade delete is managed by the Product side via
     * {@code CascadeType.ALL + orphanRemoval = true}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ── Constructors ──────────────────────────────────────────

    public ProductImage() {}

    public ProductImage(String imageUrl, Product product) {
        this.imageUrl = imageUrl;
        this.product  = product;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getImageUrl()                { return imageUrl; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }
}