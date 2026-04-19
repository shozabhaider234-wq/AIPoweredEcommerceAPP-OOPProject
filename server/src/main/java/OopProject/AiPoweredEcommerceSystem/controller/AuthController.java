package OopProject.AiPoweredEcommerceSystem.controller;



import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.AuthRequest;
import OopProject.AiPoweredEcommerceSystem.dto.AuthResponse;
import OopProject.AiPoweredEcommerceSystem.dto.RegisterRequest;
import OopProject.AiPoweredEcommerceSystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles user registration and login.
 * Both endpoints are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register
     * Creates a new user account and returns a JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(req)));
    }

    /**
     * POST /api/auth/login
     * Authenticates credentials and returns a fresh JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(req)));
    }
}

