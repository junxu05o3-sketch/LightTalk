package com.lighttalk.controller;

import com.lighttalk.common.api.Result;
import com.lighttalk.model.dto.MessagePageReq;
import com.lighttalk.model.dto.MessagePageResp;
import com.lighttalk.model.entity.Message;
import com.lighttalk.security.SecurityUtils;
import com.lighttalk.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/messages")
@Api(tags = "消息模块", description = "聊天消息管理相关接口")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    @ApiOperation(value = "消息持久化落库", notes = "供前端或服务端异步调用记录聊天内容")
    public Result<Void> saveMessage(@RequestBody Message message) {
        // 自动填充发送者信息（如果是前端调用）
        if (message.getUserId() == null) {
            message.setUserId(SecurityUtils.getCurrentUserId());
        }
        if (message.getNickname() == null) {
            message.setNickname(SecurityUtils.getCurrentUsername());
        }
        
        messageService.saveMessage(message);
        return Result.success("消息发送成功", null);
    }

    @GetMapping("/{roomId}")
    @ApiOperation(value = "获取历史消息 (游标分页)", notes = "传 lastMsgId 向上拉取历史，返回正序数据。避免深度分页性能问题")
    public Result<MessagePageResp> getHistoryMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long lastMsgId,
            @RequestParam(defaultValue = "20") Integer pageSize) {
            
        MessagePageReq req = new MessagePageReq();
        req.setRoomId(roomId);
        req.setLastMsgId(lastMsgId);
        req.setPageSize(pageSize);
        
        return Result.success(messageService.getHistoryMessages(req));
    }
}
