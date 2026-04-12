package OopProject.AiPoweredEcommerceSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for PATCH /api/users/profile.
 *
 * <p>Only name and password may be updated by the user themselves.
 * Email and role changes require admin action.
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    /**
     * New password — optional. If omitted (null), the existing password is kept.
     * If provided it must be at least 6 characters.
     */
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    // ── Getters & Setters ─────────────────────────────────────

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getNewPassword()                 { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
