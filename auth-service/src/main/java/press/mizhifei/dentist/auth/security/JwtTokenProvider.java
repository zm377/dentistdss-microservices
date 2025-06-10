package press.mizhifei.dentist.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtKeyProvider jwtKeyProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    /**
     * Generates a JWT token for the authenticated user
     * @param authentication the authentication object containing user details
     * @return the JWT token as a string
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("email", userPrincipal.getEmail())
                .claim("roles", authorities)
                .claim("clinicId", userPrincipal.getClinicId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
    }

    /**
     * Extracts the user ID from a JWT token
     * @param token the JWT token
     * @return the user ID
     */
    public String getUserIdFromJWT(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(jwtKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token);

        Claims claims = claimsJws.getPayload();
        return claims.getSubject();
    }

    /**
     * Validates a JWT token
     * @param authToken the token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(jwtKeyProvider.getPublicKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extracts the email from a JWT token
     * @param token the JWT token
     * @return the email
     */
    public String getEmailFromJWT(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(jwtKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token);

        Claims claims = claimsJws.getPayload();
        return claims.get("email", String.class);
    }

    /**
     * Extracts the roles from a JWT token
     * @param token the JWT token
     * @return the roles as a comma-separated string
     */
    public String getRolesFromJWT(String token) {
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(jwtKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token);

        Claims claims = claimsJws.getPayload();
        return claims.get("roles", String.class);
    }
}
