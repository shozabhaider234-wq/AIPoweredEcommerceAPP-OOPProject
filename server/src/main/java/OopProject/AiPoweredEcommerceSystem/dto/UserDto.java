package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.User;

import java.time.LocalDateTime;

/**
 * Read-only DTO for exposing user profile data.
 *
 * <p>The password field is deliberately excluded — it must never
 * appear in any API response.
 */
public class UserDto {

    private Long          id;
    private String        name;
    private String        email;
    private String        role;
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────────

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.id        = user.getId();
        dto.name      = user.getName();
        dto.email     = user.getEmail();
        dto.role      = user.getRole().name();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    // ── Constructors ──────────────────────────────────────────

    public UserDto() {}

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }

    public String getRole()                        { return role; }
    public void setRole(String role)               { this.role = role; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
