package com.app.shopsphere.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.shopsphere.enum_values.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and verifies JWTs used to authenticate requests to the API.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getSigningKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. " +
                            "Please set the JWT_SECRET environment variable or " +
                            "configure jwt.secret in application-local.properties for local development.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Long userId, UserRole role) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaims(token).getSubject());
    }

    public UserRole extractRole(String token) {
        return UserRole.valueOf(extractClaims(token).get("role", String.class));
    }

    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}