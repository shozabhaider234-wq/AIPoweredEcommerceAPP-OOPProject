package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.Category;

/**
 * Read-only DTO for category data returned in API responses.
 */
public class CategoryDto {

    private Long   id;
    private String name;
    private String description;

    // ── Factory ───────────────────────────────────────────────

    public static CategoryDto from(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.id          = category.getId();
        dto.name        = category.getName();
        dto.description = category.getDescription();
        return dto;
    }

    // ── Constructors ──────────────────────────────────────────

    public CategoryDto() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }
}
