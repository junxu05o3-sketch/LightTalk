package com.lighttalk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighttalk.common.api.Result;
import com.lighttalk.common.api.ResultCode;
import com.lighttalk.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security 配置类
 * 配置密码加密器、JWT 过滤器注入和请求授权规则
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 密码加密器
     * 使用 BCrypt 算法对密码进行加密和校验
     *
     * BCrypt 特点：
     * 1. 每次加密会随机生成盐值，相同密码加密结果不同
     * 2. 内置盐值，无需单独存储
     * 3. 计算成本可调，安全性高
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     *
     * 关键配置说明：
     * 1. 关闭 CSRF：前后端分离，Token 机制天然防 CSRF
     * 2. 关闭 Session：JWT 无状态认证，不依赖服务端 Session
     * 3. 将 JwtAuthenticationFilter 插入到 UsernamePasswordAuthenticationFilter 之前
     * 4. 配置 401/403 的 JSON 响应，避免前后端分离时被重定向到 HTML 登录页
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 关闭 CSRF
            .csrf().disable()
            // 2. 无状态 Session
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 3. 异常处理：自定义 401（未认证）和 403（无权限）响应
            .exceptionHandling()
                // 未登录/Token 无效 → 返回 JSON 401，而非重定向
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    String json = objectMapper.writeValueAsString(
                            Result.fail(ResultCode.UNAUTHORIZED)
                    );
                    response.getWriter().write(json);
                })
                // 已登录但无权限 → 返回 JSON 403
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    String json = objectMapper.writeValueAsString(
                            Result.fail(ResultCode.FORBIDDEN)
                    );
                    response.getWriter().write(json);
                })
            .and()
            // 4. 请求授权规则
            .authorizeRequests()
                // 白名单：登录/注册/Knife4j 文档路径无需认证
                .antMatchers(
                    "/auth/login",
                    "/auth/register",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v2/api-docs/**",
                    "/v3/api-docs/**",
                    "/favicon.ico"
                ).permitAll()
                // 管理后台接口需要 ADMIN 角色
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 其他所有接口必须携带有效 JWT Token
                .anyRequest().authenticated()
            .and()
            // 5. 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
            //    确保每次请求都先经过 JWT 解析，再进行 Security 鉴权
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

