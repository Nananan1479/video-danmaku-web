package com.zsn.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 密钥（至少32字节，生产环境从配置读取）
    @Value("${jwt.secret}")
    private String secret;

    // 过期时间
    @Value("${jwt.expiration}")
    private long expiration;

    // 生成 Token
    public String generateToken(int userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                // 用户名&用户ID（前端字段为 username&userId）
                .setClaims(claims)
                // 签发时间（前端字段为 iat）
                .setIssuedAt(now)
                // 过期时间（前端字段为 exp）(单位为秒，前端需*1000再与Data.now比较)
                .setExpiration(expireDate)
                // 生成防伪签名（HS256为加密的算法，secret为共享密钥
                .signWith(SignatureAlgorithm.HS256, secret)
                // 拼接字符串发送到前端（格式为header.payload.signature）
                .compact();
    }

    //解析Token（旧版写法）
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 从 Token 中提取用户ID
    public int getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return (int) claims.get("userId");
    }

    // 从 Token 中提取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }
}
