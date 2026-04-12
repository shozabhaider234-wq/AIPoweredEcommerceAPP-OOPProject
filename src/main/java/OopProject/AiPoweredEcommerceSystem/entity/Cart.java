package OopProject.AiPoweredEcommerceSystem.entity;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A shopping cart belonging to a single user.
 *
 * <p>Each user gets exactly one persistent cart (created on first use).
 * Items are stored in {@link CartItem} rows linked back here.
 */
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The cart owner.
     * OneToOne — each user has at most one active cart.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Line items currently in the cart. */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public User getUser()                  { return user; }
    public void setUser(User user)         { this.user = user; }

    public List<CartItem> getItems()       { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
