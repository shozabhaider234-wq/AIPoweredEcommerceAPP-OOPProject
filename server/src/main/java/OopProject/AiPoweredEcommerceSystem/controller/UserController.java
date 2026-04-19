package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.UpdateProfileRequest;
import OopProject.AiPoweredEcommerceSystem.dto.UserDto;
import OopProject.AiPoweredEcommerceSystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manages the profile of the currently authenticated user.
 * All endpoints require a valid JWT (authenticated).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * GET /api/users/profile
     * Returns the profile of the currently logged-in user.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(authService.getProfile()));
    }

    /**
     * PATCH /api/users/profile
     * Updates name and optionally password of the current user.
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateProfile(req)));
    }
}

