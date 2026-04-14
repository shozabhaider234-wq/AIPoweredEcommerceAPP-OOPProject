package OopProject.AiPoweredEcommerceSystem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating or updating a category.
 * Only ADMINs should be allowed to call these endpoints.
 */
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    // ── Getters & Setters ─────────────────────────────────────

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }
}
