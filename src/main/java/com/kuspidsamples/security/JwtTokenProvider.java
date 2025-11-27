package com.kuspidsamples.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:}") // fallback empty
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}") // default 1 hour
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure key is at least 256 bits for HS512
        if (jwtSecret == null || jwtSecret.length() < 32) {
            key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // guaranteed secure
            jwtSecret = Base64.getEncoder().encodeToString(key.getEncoded());
            System.out.println("⚠️ JWT_SECRET was empty or too weak. Generated temporary secret for dev: " + jwtSecret);
        } else {
            key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
