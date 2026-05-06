package com.lighttalk.model.dto;

import com.lighttalk.model.vo.MessageVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 消息分页响应 DTO
 */
@Data
@ApiModel(description = "消息游标分页响应")
public class MessagePageResp {

    @ApiModelProperty(value = "消息列表（按时间正序排列）")
    private List<MessageVO> messages;

    @ApiModelProperty(value = "最旧的一条消息ID（供下次分页使用），如果是最后一页(无更早消息)则可为null")
    private Long lastMsgId;
    
    @ApiModelProperty(value = "是否还有更多历史消息")
    private Boolean hasMore;
}
