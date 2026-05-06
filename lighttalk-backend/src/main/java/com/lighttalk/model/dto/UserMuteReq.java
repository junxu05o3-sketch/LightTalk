package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户禁言请求 DTO
 */
@Data
@ApiModel(description = "后台用户禁言请求")
public class UserMuteReq {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "目标用户ID", required = true, example = "2")
    private Long userId;

    @NotNull(message = "禁言时长不能为空")
    @ApiModelProperty(value = "禁言时长（秒），传 0 则表示解除禁言", required = true, example = "3600")
    private Long muteDuration;
}
