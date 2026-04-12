package OopProject.AiPoweredEcommerceSystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Product category (e.g. "Electronics", "Shoes", "Books").
 *
 * <p>Categories are managed by ADMINs and referenced by products.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Constructors ──────────────────────────────────────────

    public Category() {}

    public Category(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }
}