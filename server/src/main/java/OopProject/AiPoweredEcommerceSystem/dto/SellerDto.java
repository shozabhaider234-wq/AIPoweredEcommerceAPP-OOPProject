package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.Seller;

/**
 * Read-only DTO for seller profile data.
 */
public class SellerDto {

    private Long   id;
    private String storeName;
    private String description;
    private Long   userId;
    private String userEmail;

    // ── Factory ───────────────────────────────────────────────

    public static SellerDto from(Seller seller) {
        SellerDto dto = new SellerDto();
        dto.id          = seller.getId();
        dto.storeName   = seller.getStoreName();
        dto.description = seller.getDescription();
        dto.userId      = seller.getUser().getId();
        dto.userEmail   = seller.getUser().getEmail();
        return dto;
    }

    // ── Constructors ──────────────────────────────────────────

    public SellerDto() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getStoreName()                   { return storeName; }
    public void setStoreName(String storeName)     { this.storeName = storeName; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getUserId()                        { return userId; }
    public void setUserId(Long userId)             { this.userId = userId; }

    public String getUserEmail()                   { return userEmail; }
    public void setUserEmail(String userEmail)     { this.userEmail = userEmail; }
}
