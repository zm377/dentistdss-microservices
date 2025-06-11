package com.dentistdss.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dentistdss.auth.security.JwtKeyProvider;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for exposing JSON Web Key (JWK) information
 * Following OAuth 2.0 standards for JWK exposure
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class JwkController {

    private final JwtKeyProvider jwtKeyProvider;

    /**
     * Exposes the JSON Web Key Set (JWKS) endpoint
     * This endpoint provides public key information for JWT verification
     * 
     * @return JWKS response containing public key information
     */
    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyProvider.getPublicKey();
        
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", jwtKeyProvider.getKeyId());
        
        // Convert RSA public key components to Base64URL
        jwk.put("n", base64UrlEncode(publicKey.getModulus().toByteArray()));
        jwk.put("e", base64UrlEncode(publicKey.getPublicExponent().toByteArray()));
        
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(jwk));
        
        return ResponseEntity.ok(jwks);
    }

    /**
     * Converts byte array to Base64URL encoding (without padding)
     * 
     * @param bytes the byte array to encode
     * @return Base64URL encoded string
     */
    private String base64UrlEncode(byte[] bytes) {
        // Remove leading zero bytes for RSA components
        int start = 0;
        while (start < bytes.length && bytes[start] == 0) {
            start++;
        }
        
        byte[] trimmed = new byte[bytes.length - start];
        System.arraycopy(bytes, start, trimmed, 0, trimmed.length);
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(trimmed);
    }
}
