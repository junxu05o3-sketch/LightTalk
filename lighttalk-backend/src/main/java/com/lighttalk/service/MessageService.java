package com.lighttalk.service;

import com.lighttalk.model.dto.MessagePageReq;
import com.lighttalk.model.dto.MessagePageResp;
import com.lighttalk.model.entity.Message;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 消息持久化落库
     *
     * @param message 消息实体
     */
    void saveMessage(Message message);

    /**
     * 游标分页拉取历史消息
     *
     * @param req 分页请求参数
     * @return 分页响应（含最旧 lastMsgId 和正序消息列表）
     */
    MessagePageResp getHistoryMessages(MessagePageReq req);
}
