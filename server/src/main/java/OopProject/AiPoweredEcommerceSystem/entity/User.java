package OopProject.AiPoweredEcommerceSystem.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Represents an application user.
 *
 * <p>A user may have one of three roles:
 * <ul>
 *   <li>ADMIN  — full platform access</li>
 *   <li>SELLER — can manage products / orders for their store</li>
 *   <li>CUSTOMER — can browse, purchase, and review</li>
 * </ul>
 *
 * <p>The password field stores a BCrypt hash — never plain text.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    // ── Primary Key ──────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Fields ────────────────────────────────────────────────
    @NotBlank  // it validates that the field must not be empty string
    @Column(nullable = false) // it puts column level constraint that column value cannot be null
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password — never stored in plain text. */
    @NotBlank
    @Column(nullable = false)
    private String password;

    /**
     * User role stored as a string enum.
     * Possible values: ADMIN, SELLER, CUSTOMER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Lifecycle ─────────────────────────────────────────────

    /** Automatically sets createdAt before the entity is first persisted. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Role Enum ─────────────────────────────────────────────

    public enum Role {
        ADMIN, SELLER, CUSTOMER
    }

    // ── Constructors ──────────────────────────────────────────

    public User() {}

    public User(String name, String email, String password, Role role) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getName()                { return name; }
    public void setName(String name)       { this.name = name; }

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getPassword()            { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole()                  { return role; }
    public void setRole(Role role)         { this.role = role; }

    public LocalDateTime getCreatedAt()    { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

