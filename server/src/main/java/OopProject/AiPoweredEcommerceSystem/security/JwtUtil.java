package OopProject.AiPoweredEcommerceSystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for creating, parsing, and validating JWT tokens.
 *
 * <p>Uses the HMAC-SHA algorithm provided by the JJWT 0.12.x library.
 * The signing secret is injected from {@code application.properties}
 * so it can be rotated without recompiling.
 *
 * <p>Token lifecycle:
 * <ol>
 *   <li>On login/register → {@link #generateToken(String)} creates a signed JWT</li>
 *   <li>On every request → {@link JwtAuthFilter} calls {@link #validateToken(String)}</li>
 *   <li>If valid → {@link #getEmailFromToken(String)} extracts the subject (email)</li>
 * </ol>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** Secret key string — must be at least 32 characters for HMAC-SHA256. */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Token validity in milliseconds (default: 86 400 000 = 24 h). */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // ── Internal helpers ──────────────────────────────────────

    /**
     * Derives a {@link SecretKey} from the raw secret string.
     * Called on every operation to avoid storing the key as a field
     * (the string may be injected lazily).
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Generates a signed JWT with the user's email as the subject.
     *
     * @param email the authenticated user's email address
     * @return compact serialised token string
     */
    public String generateToken(String email) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact(); // finalizes token in header.payload.signature format and encodes everything using Base 64 Url
    }

    /**
     * Extracts the email (JWT subject) from a validated token.
     *
     * @param token compact token string from the Authorization header
     * @return the email stored as the token subject
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser() //Creates builder
                .verifyWith(getSigningKey()) // this tells use this key to verify
                .build() // creates a parser object
                .parseSignedClaims(token)
                /**
                 parseSignedClaims() actually does these step
                 Splits token
                 header.payload.signature
                 Decodes Base64
                 Verifies signature using key
                 Checks expiration
                 Validates structure
                 Extracts claims
                 */
                .getPayload(); //extracts payload

        return claims.getSubject(); // gets subject from payload
    }

    /**
     * Returns {@code true} if the token is properly signed and not expired.
     *
     * <p>Any {@link JwtException} (tampered, expired, malformed) or
     * {@link IllegalArgumentException} (null/blank) returns {@code false}.
     *
     * @param token compact token string
     * @return validity flag
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT validation failed — {}: {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or blank: {}", e.getMessage());
        }
        return false;
    }
}

