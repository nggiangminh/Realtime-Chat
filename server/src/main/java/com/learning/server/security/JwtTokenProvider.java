package com.learning.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider để tạo và validate JWT token
 * Tuân thủ security best practices theo thiết kế
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}") String jwtSecret,
            @Value("${app.jwt.expiration:86400000}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Tạo JWT token từ user ID và email
     */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }

    /**
     * Lấy user ID từ JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    /**
     * Lấy email từ JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.get("email", String.class);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("JWT token không hợp lệ: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token đã hết hạn: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token không được hỗ trợ: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string rỗng: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi validate JWT token: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Lấy thời gian hết hạn từ token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.getExpiration();
    }

    /**
     * Kiểm tra token có hết hạn không
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}
