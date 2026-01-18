package com.sajid.fraudscore.component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "AzT2Zpa8oD8K9yVfxgJbzYDRTkzfwpAMpUX5JJbuE67TEv04ebGCSLfh4_NupPoP";

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long ACCESS_TOKEN_EXPIRY = 1000 * 60 * 60; // 60 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 30; // 30 days

    public String generateAccessToken(String subject, String role) {
        return Jwts.builder()
                .setSubject(subject) // username or email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }



}
