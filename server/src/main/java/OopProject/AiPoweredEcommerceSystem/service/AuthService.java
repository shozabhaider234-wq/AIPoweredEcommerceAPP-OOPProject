package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.AuthRequest;
import OopProject.AiPoweredEcommerceSystem.dto.AuthResponse;
import OopProject.AiPoweredEcommerceSystem.dto.RegisterRequest;
import OopProject.AiPoweredEcommerceSystem.dto.UserDto;
import OopProject.AiPoweredEcommerceSystem.dto.UpdateProfileRequest;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.repository.UserRepository;
import OopProject.AiPoweredEcommerceSystem.security.JwtUtil;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Handles user registration, login, profile retrieval, and profile updates.
 *
 * <p>Password security:
 * <ul>
 *   <li>Passwords are always stored as BCrypt hashes.</li>
 *   <li>The raw password is never logged or returned in any response.</li>
 * </ul>
 */
@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil             jwtUtil;
    private final AuthenticationManager authManager;
    private final SecurityUtils       securityUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authManager,
                       SecurityUtils securityUtils) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
        this.authManager     = authManager;
        this.securityUtils   = securityUtils;
    }

    // ── Register ──────────────────────────────────────────────

    /**
     * Creates a new user account and returns a JWT for immediate use.
     *
     * @param req registration data (name, email, password, role)
     * @return JWT response ready to use
     * @throws BadRequestException if the email is already registered
     */
    public AuthResponse register(RegisterRequest req) {
        // Guard: email must be unique
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }

        User user = new User(
                req.getName(),
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),   // never store plain text
                req.getRole()
        );

        userRepository.save(user);
        log.info("New user registered: {} (role={})", req.getEmail(), req.getRole());

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    // ── Login ─────────────────────────────────────────────────

    /**
     * Authenticates the user's credentials and returns a fresh JWT.
     *
     * <p>Spring Security's {@link AuthenticationManager} handles the actual
     * password comparison via BCrypt; a {@link // BadCredentialsException} is
     * thrown automatically on failure.
     *
     * @param req login credentials (email + password)
     * @return JWT response
     */
    public AuthResponse login(AuthRequest req) {
        // Delegates to DaoAuthenticationProvider → UserDetailsServiceImpl
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found: " + req.getEmail()));

        log.info("User logged in: {}", req.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    // ── Profile ───────────────────────────────────────────────

    /**
     * Returns the profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserDto getProfile() {
        return UserDto.from(securityUtils.getCurrentUser());
    }

    /**
     * Updates the name and optionally the password of the current user.
     *
     * @param req update request — newPassword may be null (no change)
     * @return updated profile
     */
    public UserDto updateProfile(UpdateProfileRequest req) {
        User user = securityUtils.getCurrentUser();
        user.setName(req.getName());

        // Only update password if a new one was provided
        if (StringUtils.hasText(req.getNewPassword())) {
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
            log.info("Password updated for user: {}", user.getEmail());
        }

        return UserDto.from(userRepository.save(user));
    }
}

