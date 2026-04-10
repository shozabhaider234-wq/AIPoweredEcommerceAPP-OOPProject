package OopProject.AiPoweredEcommerceSystem.dto;

public class AuthResponse {

    /** The signed JWT — valid for the duration configured in application.properties. */
    private String token;

    /** The authenticated user's email (useful for display). */
    private String email;

    /** The authenticated user's role: ADMIN, SELLER, or CUSTOMER. */
    private String role;

    // ── Constructors ──────────────────────────────────────────

    public AuthResponse() {}

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role  = role;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String getToken()               { return token; }
    public void setToken(String token)     { this.token = token; }

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getRole()                { return role; }
    public void setRole(String role)       { this.role = role; }
}
