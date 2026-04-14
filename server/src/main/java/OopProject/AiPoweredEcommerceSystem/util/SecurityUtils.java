package OopProject.AiPoweredEcommerceSystem.util;

import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility component that provides convenient access to the currently
 * authenticated user throughout the service layer.
 *
 * <p>Usage in any Spring-managed bean:
 * <pre>
 *   private final SecurityUtils securityUtils;
 *   // ...
 *   User currentUser = securityUtils.getCurrentUser();
 * </pre>
 *
 * <p>This keeps service methods clean — they don't need to accept a
 * {@code Principal} or {@code Authentication} parameter from the controller.
 */
@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the full {@link User} entity for the currently authenticated principal.
     *
     * <p>The principal name is the user's email (set in
     * {@link OopProject.AiPoweredEcommerceSystem.security.UserDetailsServiceImpl}).
     * We look up the full entity so service methods have access to all fields (id, role, etc.).
     *
     * @return the authenticated user
     * @throws ResourceNotFoundException if the authenticated email has no matching DB record
     *         (should not happen in normal operation)
     * @throws IllegalStateException if called outside an authenticated security context
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "SecurityUtils.getCurrentUser() called without an active authentication");
        }

        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found in database: " + email));
    }

    /**
     * Returns the email of the currently authenticated principal without
     * hitting the database.
     *
     * <p>Useful for logging, audit trails, or quick checks where the full
     * User entity is not needed.
     *
     * @return authenticated user's email
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return auth.getName();
    }
}
