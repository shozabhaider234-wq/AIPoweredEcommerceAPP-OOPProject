package OopProject.AiPoweredEcommerceSystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that validates the JWT Bearer token on every incoming HTTP request.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee the filter runs exactly
 * once per request, even in async dispatch scenarios.
 *
 * <p>Processing flow:
 * <pre>
 *   Request arrives
 *     ↓
 *   Extract "Authorization: Bearer <token>" header
 *     ↓ (no token → skip, continue filter chain as anonymous)
 *   jwtUtil.validateToken(token)
 *     ↓ (invalid → skip)
 *   Extract email from token
 *     ↓
 *   Load UserDetails from DB
 *     ↓
 *   Set UsernamePasswordAuthenticationToken into SecurityContext
 *     ↓
 *   Continue filter chain (controller runs with auth context set)
 * </pre>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil            = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        try {
            // 1. Extract the raw token string from the Authorization header
            String token = extractTokenFromRequest(request);

            // 2. Validate signature and expiry
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {

                // 3. Parse the subject (email) from the token payload
                String email = jwtUtil.getEmailFromToken(token);

                // 4. Load the full UserDetails (including roles) from the DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. Build Spring Security's auth token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                         // credentials — not needed post-auth
                                userDetails.getAuthorities()
                        );

                // Attach request metadata (IP, session) to the token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Publish to the SecurityContext so controllers can use it
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated user '{}' for request [{}]",
                        email, request.getRequestURI());
            }

        } catch (Exception ex) {
            // Log but do NOT rethrow — let Spring Security's own failure
            // handling produce the 401/403 response downstream.
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }

        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Parses the {@code Authorization: Bearer <token>} header.
     *
     * @return the raw token string, or {@code null} if the header is absent/invalid
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerHeader) && bearerHeader.startsWith("Bearer ")) {
            // Strip the "Bearer " prefix (7 characters)
            return bearerHeader.substring(7);
        }

        return null;
    }
}

