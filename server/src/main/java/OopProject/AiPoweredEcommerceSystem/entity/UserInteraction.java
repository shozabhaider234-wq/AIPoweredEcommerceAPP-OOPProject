package OopProject.AiPoweredEcommerceSystem.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks a user's interaction with a product for the recommendation engine.
 *
 * <p>Interaction types (in ascending order of intent strength):
 * <ol>
 *   <li>VIEW     — user opened the product detail page</li>
 *   <li>SEARCH   — product appeared in and was clicked from a search result</li>
 *   <li>ADD_TO_CART — user added to cart (strong buying signal)</li>
 *   <li>PURCHASE — user completed a purchase (strongest signal)</li>
 * </ol>
 *
 * <p>The recommendation engine queries this table to identify which categories
 * and products a user is most interested in.
 */
@Entity
@Table(name = "user_interactions",
        indexes = {
                @Index(name = "idx_ui_user",    columnList = "user_id"),
                @Index(name = "idx_ui_product", columnList = "product_id")
                //@Index creates index for the values so that the db knows where a particular value is stored otherwise for a where query it will check each row one by one
        })
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType interactionType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ── Interaction Type Enum ─────────────────────────────────

    public enum InteractionType {
        VIEW, SEARCH, ADD_TO_CART, PURCHASE
    }

    // ── Constructors ──────────────────────────────────────────

    public UserInteraction() {}

    public UserInteraction(User user, Product product, InteractionType interactionType) {
        this.user            = user;
        this.product         = product;
        this.interactionType = interactionType;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                                          { return id; }
    public void setId(Long id)                                   { this.id = id; }

    public User getUser()                                        { return user; }
    public void setUser(User user)                               { this.user = user; }

    public Product getProduct()                                  { return product; }
    public void setProduct(Product product)                      { this.product = product; }

    public InteractionType getInteractionType()                  { return interactionType; }
    public void setInteractionType(InteractionType type)         { this.interactionType = type; }

    public LocalDateTime getTimestamp()                          { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp)            { this.timestamp = timestamp; }
}

