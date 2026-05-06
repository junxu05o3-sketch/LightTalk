package com.lighttalk.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 提供 Token 的生成、解析、校验功能
 *
 * JWT 结构说明：
 * Header:  { "alg": "HS256", "typ": "JWT" }
 * Payload: { "userId": 1, "username": "admin", "exp": 1710000000 }
 * Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT 密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 过期时间（秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 签名密钥对象
     * 使用 HMAC-SHA256 算法
     */
    private Key key;

    /**
     * 初始化密钥
     * 在 Bean 创建后自动调用
     */
    @PostConstruct
    public void init() {
        // 确保密钥长度足够（HS256 要求至少 256 位 / 32 字节）
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username) {
        // 当前时间
        Date now = new Date();
        // 过期时间
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        // 自定义 Claims（载荷信息）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 构建 JWT
        return Jwts.builder()
                .setClaims(claims)                          // 设置载荷
                .setSubject(username)                        // 设置主题（用户名）
                .setIssuedAt(now)                           // 设置签发时间
                .setExpiration(expiryDate)                  // 设置过期时间
                .signWith(key, SignatureAlgorithm.HS256)    // 使用 HS256 算法签名
                .compact();                                 // 生成 Token
    }

    /**
     * 从 Token 中解析 Claims（载荷信息）
     *
     * @param token JWT Token
     * @return Claims 对象，包含用户信息
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)      // 设置签名密钥
                    .build()
                    .parseClaimsJws(token)   // 解析 Token
                    .getBody();              // 获取载荷
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 Token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("Token 签名无效: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Token 为空: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效, false-无效
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
     * 校验 Token 是否过期
     *
     * @param token JWT Token
     * @return true-已过期, false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取 Token 过期时间（秒）
     *
     * @return 过期时间
     */
    public Long getExpiration() {
        return expiration;
    }
}
