package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A product listed in the catalogue.
 *
 * <p>Key relationships:
 * <ul>
 *   <li>Many-to-One → {@link Seller}  (the listing seller)</li>
 *   <li>Many-to-One → {@link Category}</li>
 *   <li>One-to-Many → {@link ProductImage} (cascade delete)</li>
 * </ul>
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Available stock units. Zero means out-of-stock. */
    @Min(0)
    @Column(nullable = false)
    private Integer stock = 0;

    /**
     * The seller who listed this product.
     * LAZY — seller info is not always required when listing products.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    public Category category;

    /**
     * Images for this product.
     * CascadeType.ALL + orphanRemoval ensures images are deleted
     * when the product is deleted.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────

    public Product() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice()                   { return price; }
    public void setPrice(BigDecimal price)         { this.price = price; }

    public Integer getStock()                      { return stock; }
    public void setStock(Integer stock)            { this.stock = stock; }

    public Seller getSeller()                      { return seller; }
    public void setSeller(Seller seller)           { this.seller = seller; }

    public Category getCategory()                  { return category; }
    public void setCategory(Category category)     { this.category = category; }

    public List<ProductImage> getImages()          { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
}
