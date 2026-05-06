package com.lighttalk.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 消息分页请求 DTO (游标分页)
 */
@Data
@ApiModel(description = "消息分页请求")
public class MessagePageReq {

    @NotNull(message = "房间ID不能为空")
    @ApiModelProperty(value = "房间ID", required = true, example = "1")
    private Long roomId;

    @ApiModelProperty(value = "游标(最后一条消息的ID)，如果是首次拉取传空即可", example = "1024")
    private Long lastMsgId;

    @Min(value = 1, message = "分页大小最小为1")
    @Max(value = 100, message = "分页大小最大为100")
    @ApiModelProperty(value = "每次拉取条数（1-100）", example = "20")
    private Integer pageSize = 20;
}
