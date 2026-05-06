package com.lighttalk.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息视图对象 VO
 */
@Data
@ApiModel(description = "历史消息返回对象")
public class MessageVO {

    @ApiModelProperty(value = "消息ID", example = "1024")
    private Long id;

    @ApiModelProperty(value = "房间ID", example = "1")
    private Long roomId;

    @ApiModelProperty(value = "发送者ID", example = "2")
    private Long userId;

    @ApiModelProperty(value = "发送者昵称")
    private String nickname;

    @ApiModelProperty(value = "发送者头像")
    private String avatar;

    @ApiModelProperty(value = "消息内容")
    private String content;

    @ApiModelProperty(value = "消息类型：text/system", example = "text")
    private String type;

    @ApiModelProperty(value = "发送时间")
    private LocalDateTime createTime;
}
