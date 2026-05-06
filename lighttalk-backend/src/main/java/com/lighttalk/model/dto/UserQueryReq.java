package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户分页查询请求 DTO
 */
@Data
@ApiModel(description = "后台用户分页查询请求")
public class UserQueryReq {

    @ApiModelProperty(value = "用户名（模糊查询）", example = "junx")
    private String username;

    @ApiModelProperty(value = "昵称（模糊查询）", example = "管理员")
    private String nickname;

    @ApiModelProperty(value = "状态：1-正常, 0-禁言", example = "1")
    private Integer status;

    @ApiModelProperty(value = "当前页", example = "1")
    private Integer current = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer size = 10;
}
