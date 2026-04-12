package OopProject.AiPoweredEcommerceSystem.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a seller's store profile.
 *
 * <p>A Seller is a 1-to-1 extension of a User whose role is SELLER.
 * The relationship is modelled with a foreign-key column (user_id) rather
 * than a shared primary key so that the two tables can evolve independently.
 */
@Entity
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name shown on the storefront. */
    @NotBlank
    @Column(nullable = false)
    private String storeName;

    /** Optional long-form description of the store. */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The owning user account.
     * EAGER is acceptable here — a seller object is rarely loaded
     * without needing user details (e.g. email, name).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ── Constructors ──────────────────────────────────────────

    public Seller() {}

    public Seller(String storeName, String description, User user) {
        this.storeName   = storeName;
        this.description = description;
        this.user        = user;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getStoreName()                 { return storeName; }
    public void setStoreName(String storeName)   { this.storeName = storeName; }

    public String getDescription()               { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser()                        { return user; }
    public void setUser(User user)               { this.user = user; }
}

