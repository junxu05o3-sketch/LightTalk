package com.lighttalk.controller;

import com.lighttalk.common.api.Result;
import com.lighttalk.model.dto.LoginResp;
import com.lighttalk.model.dto.UserLoginReq;
import com.lighttalk.model.dto.UserRegisterReq;
import com.lighttalk.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 提供用户注册与登录接口
 *
 * 接口路径（context-path = /api）：
 *   POST /api/auth/register  → 用户注册
 *   POST /api/auth/login     → 用户登录
 *
 * ─────────────────────────────────────────────────
 * 💡 如何在其他 Controller 中获取当前登录用户 ID？
 * ─────────────────────────────────────────────────
 * JwtAuthenticationFilter 在解析 Token 成功后，会将 userId（Long 类型）
 * 存入 SecurityContext 的 Authentication.principal 中。
 *
 * 方式一：直接从 SecurityContextHolder 中取（推荐）
 *   Long userId = (Long) SecurityContextHolder.getContext()
 *                        .getAuthentication().getPrincipal();
 *
 * 方式二：抽取工具方法（可封装到 SecurityUtils 中复用）
 *   public static Long getCurrentUserId() {
 *       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *       if (auth != null && auth.getPrincipal() instanceof Long) {
 *           return (Long) auth.getPrincipal();
 *       }
 *       throw new BusinessException(ResultCode.UNAUTHORIZED);
 *   }
 * ─────────────────────────────────────────────────
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证模块", description = "用户注册与登录相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     * POST /api/auth/register
     *
     * @param req 注册请求（用户名/密码/昵称，含参数校验）
     * @return 注册成功提示
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户名唯一，密码会进行 BCrypt 加密后存储")
    public Result<Void> register(@Validated @RequestBody UserRegisterReq req) {
        authService.register(req);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     *
     * @param req 登录请求（用户名/密码）
     * @return 包含 JWT Token 和用户基本信息的 LoginResp
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "登录成功后返回 JWT Token，后续请求需在 Header 中携带：Authorization: Bearer <token>")
    public Result<LoginResp> login(@Validated @RequestBody UserLoginReq req) {
        LoginResp resp = authService.login(req);
        return Result.success(resp);
    }
}
