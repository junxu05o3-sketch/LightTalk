package com.lighttalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lighttalk.common.api.ResultCode;
import com.lighttalk.common.constants.RedisKeyConstants;
import com.lighttalk.common.exception.BusinessException;
import com.lighttalk.mapper.MessageMapper;
import com.lighttalk.model.dto.MessagePageReq;
import com.lighttalk.model.dto.MessagePageResp;
import com.lighttalk.model.entity.Message;
import com.lighttalk.model.vo.MessageVO;
import com.lighttalk.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(Message message) {
        // === 禁言防卫机制：落库前检查 Redis ===
        if (message.getUserId() != null) {
            String muteKey = RedisKeyConstants.getUserBannedKey(message.getUserId());
            String isMuted = stringRedisTemplate.opsForValue().get(muteKey);
            if (StringUtils.hasText(isMuted)) {
                log.warn("被禁言用户尝试发消息，已拦截: userId={}", message.getUserId());
                throw new BusinessException(ResultCode.USER_BANNED, "您已被管理员禁言，暂时无法发言");
            }
        }
        // =====================================

        messageMapper.insert(message);
        log.debug("消息持久化成功: msgId={}, roomId={}", message.getId(), message.getRoomId());
    }

    @Override
    public MessagePageResp getHistoryMessages(MessagePageReq req) {
        // 构建游标分页查询条件
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getRoomId, req.getRoomId());
        
        // 如果传了 lastMsgId，则查询 id < lastMsgId 的消息（更早的历史消息）
        if (req.getLastMsgId() != null) {
            wrapper.lt(Message::getId, req.getLastMsgId());
        }
        
        // 按 ID 倒序（时间倒序），拉取 pageSize 条最新消息
        wrapper.orderByDesc(Message::getId);
        wrapper.last("LIMIT " + req.getPageSize());
        
        List<Message> messages = messageMapper.selectList(wrapper);
        
        MessagePageResp resp = new MessagePageResp();
        
        if (messages.isEmpty()) {
            resp.setMessages(Collections.emptyList());
            resp.setHasMore(false);
            return resp;
        }

        // 获取拉取到的最旧的一条消息的 ID（作为下一次请求的游标）
        // 因为查出来是倒序，所以最后一条元素就是最旧的
        Long oldestMsgId = messages.get(messages.size() - 1).getId();
        resp.setLastMsgId(oldestMsgId);
        
        // 判断是否还有更多：如果拉取数量 < pageSize，通常说明已经没有更多记录了
        resp.setHasMore(messages.size() == req.getPageSize());

        // 将数据库查询的倒序列表反转为正序（聊天界面通常新消息在底部，向上滚拉取历史也需正序展示）
        Collections.reverse(messages);

        // 组装 VO
        List<MessageVO> voList = messages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            return vo;
        }).collect(Collectors.toList());

        resp.setMessages(voList);
        return resp;
    }
}
