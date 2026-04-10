package OopProject.AiPoweredEcommerceSystem.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating or updating a seller profile.
 */
public class SellerRequest {

    @NotBlank(message = "Store name is required")
    private String storeName;

    private String description;

    // ── Getters & Setters ─────────────────────────────────────

    public String getStoreName()                   { return storeName; }
    public void setStoreName(String storeName)     { this.storeName = storeName; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }
}
