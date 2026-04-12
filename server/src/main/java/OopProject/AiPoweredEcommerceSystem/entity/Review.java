package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * A product review submitted by a customer.
 *
 * <p>A unique constraint ensures a user can review each product only once.
 */
@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Rating from 1 (worst) to 5 (best). */
    @Min(1) @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // ── Constructors ──────────────────────────────────────────

    public Review() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }

    public Integer getRating()                 { return rating; }
    public void setRating(Integer rating)      { this.rating = rating; }

    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }
}

