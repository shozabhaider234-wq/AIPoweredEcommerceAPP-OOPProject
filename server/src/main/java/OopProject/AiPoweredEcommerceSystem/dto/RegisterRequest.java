package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/auth/register.
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Role defaults to CUSTOMER; callers may pass SELLER or ADMIN.
     * In production, restrict ADMIN creation to existing admins.
     */
    private User.Role role = User.Role.CUSTOMER;

    // ── Getters & Setters ─────────────────────────────────────

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }

    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }

    public User.Role getRole()               { return role; }
    public void setRole(User.Role role)      { this.role = role; }
}
