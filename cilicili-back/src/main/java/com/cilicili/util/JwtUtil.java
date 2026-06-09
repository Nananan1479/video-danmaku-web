package com.cilicili.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成或解析JWT-Token
 *
 *
 * @author Nananan1479
 * @date 2026/5/25 14:43

 * @return null
 */
@Component
public class JwtUtil {

    // 密钥（至少32字节，生产环境从配置读取）
    @Value("${jwt.secret}")
    private String secret;

    // 过期时间
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 Token（含用户角色）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色（0=普通用户，1=管理员）
     *
     * @author Nananan1479
     * @date 2026/5/25 14:38
     * @return java.lang.String
     */
    public String generateToken(long userId, String username, int role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

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

    /**
     * 解析Token（旧版写法）
     *
     * @param token
     *
     * @author Nananan1479
     * @date 2026/5/25 14:38

     * @return io.jsonwebtoken.Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token
     *
     * @author Nananan1479
     * @date 2026/5/25 14:39

     * @return boolean
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中提取用户ID
     *

     * @param token
     *
     * @author Nananan1479
     * @date 2026/5/25 14:39

     * @return int
     */
    public long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            return 0;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        return ((Number) userIdObj).longValue();
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token
     *
     * @author Nananan1479
     * @date 2026/5/25 14:41

     * @return java.lang.String
     */
    /**
     * 从 Token 中提取角色
     */
    public int getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        Object roleObj = claims.get("role");
        if (roleObj == null) {
            return 0;
        }
        if (roleObj instanceof Integer) {
            return (Integer) roleObj;
        }
        return ((Number) roleObj).intValue();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }
}
