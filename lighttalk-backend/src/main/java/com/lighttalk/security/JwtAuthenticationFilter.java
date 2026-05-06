package com.lighttalk.security;

import com.lighttalk.common.api.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 身份认证过滤器
 * 继承自 OncePerRequestFilter，保证每次请求只执行一次过滤逻辑
 *
 * 工作流程：
 * 1. 检查当前请求是否是白名单（登录/注册/文档），是则直接放行
 * 2. 从请求头 Authorization 中取出 Bearer <token>
 * 3. 调用 JwtUtils 解析并校验 Token 有效性
 * 4. 解析成功则将用户信息存入 Spring Security 的 SecurityContext
 * 5. 后续 Controller 可通过 SecurityContextHolder 获取当前登录用户
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    /** Authorization 请求头名称 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer Token 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 无需认证的白名单路径（支持 Ant 风格通配符） */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
            "/favicon.ico"
    );

    /** Ant 路径匹配器，用于白名单通配符匹配 */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 过滤器核心逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // 注意：context-path 为 /api，此处拿到的 URI 包含上下文路径
        // 白名单匹配时去掉 /api 前缀，以兼容配置灵活性
        String path = requestURI.replaceFirst("/api", "");

        // 1. 白名单放行
        if (isWhiteListed(path)) {
            log.debug("白名单放行: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 获取 Token
        String token = extractToken(request);

        // 3. Token 为空，继续传递（让 Security 的 403 逻辑处理无权限）
        if (!StringUtils.hasText(token)) {
            log.debug("请求 {} 未携带 Token", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 解析并校验 Token
        try {
            Claims claims = jwtUtils.parseToken(token);

            // 从 Token 载荷中提取用户信息
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();

            log.debug("Token 解析成功: userId={}, username={}", userId, username);

            // 5. 构建 Authentication 对象并存入 SecurityContext
            //    principal 存放 userId，方便 Controller 直接获取
            //    分配权限：如果是超级管理员 (userId = 3) 则赋予 ROLE_ADMIN，否则仅赋予 ROLE_USER
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            if (userId == 3L) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,                         // principal（当前登录用户ID）
                            null,                           // credentials（认证后置空）
                            authorities
                    );
            // 附加请求详情（IP、Session等）
            authentication.setDetails(username);

            // 将认证信息存入当前线程的 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", requestURI);
            // 清理 SecurityContext，继续传递，让 Security 返回 401
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.warn("Token 校验失败: {}, 原因: {}", requestURI, e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // 6. 继续执行过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer Token
     *
     * @param request HTTP 请求
     * @return Token 字符串，若无则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // 截取 "Bearer " 后面的 token 部分
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 判断请求路径是否在白名单中
     *
     * @param path 去掉 context-path 后的路径
     * @return true-在白名单内
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
