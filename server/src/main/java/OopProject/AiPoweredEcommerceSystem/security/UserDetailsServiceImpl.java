package OopProject.AiPoweredEcommerceSystem.security;

import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Security integration — loads a {@link User} entity from the
 * database by email and wraps it in a {@link UserDetails} object.
 *
 * <p>This service is used in two places:
 * <ol>
 *   <li>During login — Spring Security's {@code DaoAuthenticationProvider}
 *       calls this to fetch the user and compare the encoded password.</li>
 *   <li>In {@link JwtAuthFilter} — called on every authenticated request
 *       to rebuild the security context from the JWT subject (email).</li>
 * </ol>
 *
 * <p>The granted authority is prefixed with {@code ROLE_} so Spring
 * Security's {@code hasRole('SELLER')} / {@code hasRole('ADMIN')} checks
 * work correctly with the enum values stored in the database.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor injection — no field injection used per project requirements.
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by their email address.
     *
     * @param email the "username" (email in our system)
     * @return populated {@link UserDetails} object
     * @throws UsernameNotFoundException if no user exists with this email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found with email: " + email));

        // Map our domain Role enum to a Spring Security GrantedAuthority.
        // Prefix "ROLE_" is required for hasRole() expressions to work.
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(authority)
        );
    }
}

