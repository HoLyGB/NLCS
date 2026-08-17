package com.chonhanh.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // Khóa bí mật để ký JWT (Phải đủ dài, ít nhất 256-bit). Thực tế nên để trong application.properties
    private final String jwtSecret = "ChoTotCloneSecretKey123456789012345678901234567890"; 
    // Thời gian sống của Token: 1 ngày (tính bằng milliseconds)
    private final int jwtExpirationMs = 86400000; 

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Hàm tạo Token từ số điện thoại (username)
    public String generateToken(String phone) {
        return Jwts.builder()
                .setSubject(phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Hàm lấy số điện thoại từ Token
    public String getPhoneFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Hàm kiểm tra Token có hợp lệ / hết hạn chưa
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT không hợp lệ: " + e.getMessage());
        }
        return false;
    }
}