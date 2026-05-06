package com.lighttalk.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象 VO
 * 返回给前端的用户基本信息，过滤掉密码等敏感字段
 */
@Data
@ApiModel(description = "用户信息")
public class UserVO {

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", example = "akiba_dev")
    private String username;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称", example = "秋叶")
    private String nickname;

    /**
     * 头像URL
     */
    @ApiModelProperty(value = "头像URL")
    private String avatar;

    /**
     * 状态：1-正常，0-禁言
     */
    @ApiModelProperty(value = "用户状态：1-正常，0-禁言", example = "1")
    private Integer status;

    /**
     * 注册时间
     */
    @ApiModelProperty(value = "注册时间")
    private LocalDateTime createTime;
}
