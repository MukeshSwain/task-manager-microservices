package com.task.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // Import this
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets; // Import this for UTF-8
import java.security.Key;

@Component
public class JwtUtils {

    // Ideally, load this from application.properties using @Value("${jwt.secret}")
    private final String secretKey = "mdu4yr87439ynghui1nw1n/njkfnmfg3u4ygye";

    public String extractAuthId(String token){
        return extractAllClaims(token).get("id",String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT VALIDATION ERROR: " + e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Must use the same key logic
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(){
        // ❌ OLD (Incorrect for Node compatibility):
        // byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // ✅ NEW (Correct): Treat the secret as raw characters (UTF-8)
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}