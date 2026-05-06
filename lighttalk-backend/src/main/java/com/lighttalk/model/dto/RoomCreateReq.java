package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 房间创建请求 DTO
 */
@Data
@ApiModel(description = "房间创建请求")
public class RoomCreateReq {

    @NotBlank(message = "房间名称不能为空")
    @Size(min = 2, max = 50, message = "房间名称长度必须在 2~50 位之间")
    @ApiModelProperty(value = "房间名称", required = true, example = "我的闲聊房间")
    private String name;

    @ApiModelProperty(value = "房间描述", example = "欢迎大家进来聊天~")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    @ApiModelProperty(value = "最大人数限制（默认500）", example = "500")
    private Integer maxMembers = 500;
}
