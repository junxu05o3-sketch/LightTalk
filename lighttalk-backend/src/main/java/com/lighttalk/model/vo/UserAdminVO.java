package com.lighttalk.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员视角的用户视图 VO
 */
@Data
@ApiModel(description = "管理员视角用户信息")
public class UserAdminVO {

    @ApiModelProperty(value = "用户ID")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "用户权限：ADMIN / USER")
    private String role;

    @ApiModelProperty(value = "状态：1-正常, 0-禁言")
    private Integer status;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "注册时间")
    private LocalDateTime createTime;
}
