package com.questboard.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    public static final String CLAIM_ROLE    = "role";
    public static final String CLAIM_USERNAME= "username";
    public static final String CLAIM_TYPE    = "type";  // "access" | "refresh"

    private final SecretKey signingKey;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessExpMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpMs) {
        // Garante pelo menos 32 bytes (256 bits) — HS256
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            // SHA-256 do segredo → 32 bytes exatos
            try {
                keyBytes = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(secret.getBytes(StandardCharsets.UTF_8));
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 não disponível", e);
            }
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String generateAccessToken(UUID userId, String username, String role) {
        return buildToken(Map.of(
                CLAIM_USERNAME, username,
                CLAIM_ROLE, role,
                CLAIM_TYPE, "access"
        ), userId, accessExpMs);
    }

    public String generateRefreshToken(UUID userId, String username) {
        return buildToken(Map.of(
                CLAIM_USERNAME, username,
                CLAIM_TYPE, "refresh"
        ), userId, refreshExpMs);
    }

    private String buildToken(Map<String, Object> claims, UUID subject, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject.toString())
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getSubject());
    }

    public String extractUsername(String token) {
        return parse(token).get(CLAIM_USERNAME, String.class);
    }

    public String extractRole(String token) {
        return parse(token).get(CLAIM_ROLE, String.class);
    }

    public String extractType(String token) {
        return parse(token).get(CLAIM_TYPE, String.class);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractType(token));
    }

    public long getAccessExpirationMs() { return accessExpMs; }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}