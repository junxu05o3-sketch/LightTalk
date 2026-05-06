package com.lighttalk.model.dto;

import com.lighttalk.model.vo.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应 DTO
 * 包含 JWT Token 和当前用户的基本信息（UserVO），
 * 前端将 token 存入 localStorage / Pinia，后续请求在 Header 中携带
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "登录成功响应")
public class LoginResp {

    /**
     * JWT 访问令牌
     * 前端需将此值放入请求头：Authorization: Bearer <token>
     */
    @ApiModelProperty(value = "JWT 访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    /**
     * 过期时间（秒）
     * 帮助前端判断 token 何时失效，避免无效请求
     */
    @ApiModelProperty(value = "Token 有效期（秒）", example = "86400")
    private Long expiresIn;

    /**
     * 当前登录用户基本信息（不含敏感字段）
     */
    @ApiModelProperty(value = "当前登录用户信息")
    private UserVO userInfo;
}
