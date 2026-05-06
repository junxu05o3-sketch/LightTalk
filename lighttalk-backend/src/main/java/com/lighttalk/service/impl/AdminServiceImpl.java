package com.lighttalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lighttalk.common.api.ResultCode;
import com.lighttalk.common.constants.RedisKeyConstants;
import com.lighttalk.common.exception.BusinessException;
import com.lighttalk.mapper.MessageMapper;
import com.lighttalk.mapper.RoomMapper;
import com.lighttalk.mapper.UserMapper;
import com.lighttalk.model.dto.UserMuteReq;
import com.lighttalk.model.dto.UserQueryReq;
import com.lighttalk.model.entity.Message;
import com.lighttalk.model.entity.Room;
import com.lighttalk.model.entity.User;
import com.lighttalk.model.vo.MessageVO;
import com.lighttalk.model.vo.RoomVO;
import com.lighttalk.model.vo.UserAdminVO;
import com.lighttalk.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<UserAdminVO> pageUsers(UserQueryReq req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(req.getUsername())) {
            wrapper.like(User::getUsername, req.getUsername());
        }
        if (StringUtils.hasText(req.getNickname())) {
            wrapper.like(User::getNickname, req.getNickname());
        }
        if (req.getStatus() != null) {
            wrapper.eq(User::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageInfo = new Page<>(req.getCurrent(), req.getSize());
        userMapper.selectPage(pageInfo, wrapper);

        Page<UserAdminVO> voPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        List<UserAdminVO> voList = pageInfo.getRecords().stream().map(user -> {
            UserAdminVO vo = new UserAdminVO();
            BeanUtils.copyProperties(user, vo);
            // 业务约定：userId == 3 为 ADMIN
            vo.setRole(user.getId() == 3L ? "ADMIN" : "USER");
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<RoomVO> listAllRooms() {
        // 直接查库，并补充在线人数
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().orderByDesc(Room::getCreateTime));
        return rooms.stream().map(room -> {
            RoomVO vo = new RoomVO();
            BeanUtils.copyProperties(room, vo);
            String countStr = stringRedisTemplate.opsForValue().get(RedisKeyConstants.getRoomOnlineKey(room.getId()));
            vo.setOnlineCount(StringUtils.hasText(countStr) ? Long.parseLong(countStr) : 0L);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> listAllMessages(Long roomId, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) wrapper.eq(Message::getRoomId, roomId);
        if (userId != null) wrapper.eq(Message::getUserId, userId);
        wrapper.orderByDesc(Message::getCreateTime);
        wrapper.last("LIMIT 500"); // 后台检索做适当数量限制

        List<Message> messages = messageMapper.selectList(wrapper);
        return messages.stream().map(msg -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteUser(UserMuteReq req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String muteKey = RedisKeyConstants.getUserBannedKey(req.getUserId());

        if (req.getMuteDuration() > 0) {
            // 禁言
            user.setStatus(0);
            userMapper.updateById(user);
            
            // 写 Redis，Value 可以存解禁时间戳
            long expireAt = Instant.now().getEpochSecond() + req.getMuteDuration();
            stringRedisTemplate.opsForValue().set(muteKey, String.valueOf(expireAt), req.getMuteDuration(), TimeUnit.SECONDS);
            log.info("管理员禁言用户 userId={} 时长={}秒", req.getUserId(), req.getMuteDuration());
        } else {
            // 解禁
            user.setStatus(1);
            userMapper.updateById(user);
            stringRedisTemplate.delete(muteKey);
            log.info("管理员解禁用户 userId={}", req.getUserId());
        }
    }

    @Override
    public void kickUser(Long userId, Long roomId) {
        // 验证用户和房间
        if (userMapper.selectById(userId) == null || roomMapper.selectById(roomId) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户或房间不存在");
        }

        // 踢人广播约定 Key：lighttalk:room:kick:{roomId}:{userId}
        String kickKey = "lighttalk:room:kick:" + roomId + ":" + userId;
        // 写入 Redis，有效期 10 秒
        stringRedisTemplate.opsForValue().set(kickKey, "1", 10, TimeUnit.SECONDS);
        
        // 注：C++ WebSocket 网关会利用 Redis Keyspace Notifications 订阅过期/设置事件，
        // 或者直接订阅该频道频道，一旦收到指令立刻断开该用户的 socket
        log.info("管理员发起踢人指令 userId={} roomId={}", userId, roomId);
    }
}
