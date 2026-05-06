package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户注册请求 DTO
 * 接收前端传入的注册参数，并通过 Spring Validation 注解进行校验
 */
@Data
@ApiModel(description = "用户注册请求")
public class UserRegisterReq {

    /**
     * 用户名
     * 要求：非空，长度 4~20 位，建议使用字母/数字/下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在 4~20 位之间")
    @ApiModelProperty(value = "用户名", required = true, example = "akiba_dev")
    private String username;

    /**
     * 密码（明文，服务端会进行 BCrypt 加密）
     * 要求：非空，长度 6~30 位
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在 6~30 位之间")
    @ApiModelProperty(value = "密码（明文）", required = true, example = "password123")
    private String password;

    /**
     * 昵称（展示名）
     * 要求：非空，长度 2~20 位
     */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须在 2~20 位之间")
    @ApiModelProperty(value = "昵称", required = true, example = "秋叶")
    private String nickname;
}
