package OopProject.AiPoweredEcommerceSystem.config;

import OopProject.AiPoweredEcommerceSystem.security.JwtAuthFilter;

import OopProject.AiPoweredEcommerceSystem.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Stateless sessions — no HTTP session is created; auth state lives in the JWT.</li>
 *   <li>CSRF disabled — safe for stateless REST APIs that don't use cookies for auth.</li>
 *   <li>{@code @EnableMethodSecurity} enables {@code @PreAuthorize("hasRole('SELLER')")}
 *       annotations on controller and service methods.</li>
 *   <li>The JWT filter is inserted <em>before</em> the default username/password filter.</li>
 * </ul>
 *
 * <p>Public endpoints (no JWT required):
 * <ul>
 *   <li>{@code POST /api/auth/**}          — register and login</li>
 *   <li>{@code GET  /api/products/**}      — browse catalogue</li>
 *   <li>{@code GET  /api/categories/**}    — browse categories</li>
 *   <li>{@code /images/**}                 — static product images</li>
 * </ul>
 *
 * <p>All other endpoints require a valid JWT in the {@code Authorization: Bearer <token>} header.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // activates @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter      = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // ── Filter Chain ──────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(csrf -> csrf.disable())

                // Stateless session — Spring Security won't create an HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Endpoint access rules
                .authorizeHttpRequests(auth -> auth
                        // ── Public (no token required) ─────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/api/seller/orders/**").hasRole("SELLER")

                        // ── Swagger / Actuator (optional, enable as needed) ──
                        // .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // .requestMatchers("/actuator/health").permitAll()

                        // ── Everything else needs a valid JWT ──────────
                        .anyRequest().authenticated()
                )

                // Register the JWT filter before Spring's default auth filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Authentication Provider ───────────────────────────────

    /**
     * Wires our custom {@link UserDetailsServiceImpl} with BCrypt password comparison.
     * Spring Security will use this provider when {@link AuthenticationManager#authenticate}
     * is called during login.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ── Beans ─────────────────────────────────────────────────

    /**
     * BCrypt password encoder with strength 10 (default).
     * Injected into AuthService to hash new passwords at registration.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean so it
     * can be injected into {@link // OopProject.AiPoweredEcommerceSystem.service.AuthService}.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
