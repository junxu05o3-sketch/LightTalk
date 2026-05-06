package com.lighttalk.security;

import com.lighttalk.common.api.ResultCode;
import com.lighttalk.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 安全工具类
 * 用于在 Controller 或 Service 中优雅地获取当前登录用户的上下文信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     *
     * @return userId
     * @throws BusinessException 如果未登录或获取失败，抛出未授权异常
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return username
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String) {
            return (String) auth.getDetails();
        }
        return null;
    }
}
