package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 * 接收前端传入的登录参数，进行非空校验
 */
@Data
@ApiModel(description = "用户登录请求")
public class UserLoginReq {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true, example = "akiba_dev")
    private String username;

    /**
     * 密码（明文，由服务端 BCrypt 匹配校验）
     */
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码（明文）", required = true, example = "password123")
    private String password;
}
