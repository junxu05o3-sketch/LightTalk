package com.lighttalk.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间视图对象 VO
 */
@Data
@ApiModel(description = "房间信息返回对象")
public class RoomVO {

    @ApiModelProperty(value = "房间ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "房间名称")
    private String name;

    @ApiModelProperty(value = "房间描述")
    private String description;

    @ApiModelProperty(value = "房主ID")
    private Long ownerId;

    @ApiModelProperty(value = "房主昵称")
    private String ownerNickname;

    @ApiModelProperty(value = "最大人数")
    private Integer maxMembers;

    @ApiModelProperty(value = "房间状态：1-正常, 0-关闭")
    private Integer status;

    @ApiModelProperty(value = "在线人数 (Redis获取)", example = "42")
    private Long onlineCount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
