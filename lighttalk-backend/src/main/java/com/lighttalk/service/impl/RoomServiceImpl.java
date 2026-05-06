package com.lighttalk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lighttalk.common.api.ResultCode;
import com.lighttalk.common.constants.RedisKeyConstants;
import com.lighttalk.common.exception.BusinessException;
import com.lighttalk.mapper.RoomMapper;
import com.lighttalk.mapper.RoomMemberMapper;
import com.lighttalk.mapper.UserMapper;
import com.lighttalk.model.dto.RoomCreateReq;
import com.lighttalk.model.entity.Room;
import com.lighttalk.model.entity.RoomMember;
import com.lighttalk.model.entity.User;
import com.lighttalk.model.vo.RoomVO;
import com.lighttalk.security.SecurityUtils;
import com.lighttalk.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 房间服务实现类
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomMemberMapper roomMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRoom(RoomCreateReq req) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 校验房间名是否存在
        boolean exists = roomMapper.exists(
                new LambdaQueryWrapper<Room>().eq(Room::getName, req.getName())
        );
        if (exists) {
            throw new BusinessException(ResultCode.ROOM_EXISTS);
        }

        // 创建房间实体
        Room room = new Room();
        room.setName(req.getName());
        room.setDescription(req.getDescription());
        room.setOwnerId(userId);
        room.setMaxMembers(req.getMaxMembers());
        room.setStatus(1); // 1-正常
        
        roomMapper.insert(room);

        // 创建者自动加入房间并设为房主(role=1)
        RoomMember member = new RoomMember();
        member.setRoomId(room.getId());
        member.setUserId(userId);
        member.setRole(1); // 房主
        roomMemberMapper.insert(member);

        log.info("用户 {} 创建了房间: {}", userId, room.getName());
    }

    @Override
    public List<RoomVO> listRooms() {
        // 1. 查询所有正常状态的房间（实际项目中可加分页）
        List<Room> rooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, 1).orderByDesc(Room::getCreateTime)
        );

        if (rooms.isEmpty()) {
            return List.of();
        }

        // 2. 批量获取房主昵称
        Set<Long> ownerIds = rooms.stream().map(Room::getOwnerId).collect(Collectors.toSet());
        List<User> owners = userMapper.selectBatchIds(ownerIds);
        Map<Long, String> ownerNicknameMap = owners.stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 3. 组装 VO 并从 Redis 中读取在线人数
        return rooms.stream().map(room -> {
            RoomVO vo = new RoomVO();
            BeanUtils.copyProperties(room, vo);
            
            // 设置房主昵称
            vo.setOwnerNickname(ownerNicknameMap.getOrDefault(room.getOwnerId(), "未知用户"));
            
            // 从 Redis 拉取在线人数
            String onlineKey = RedisKeyConstants.getRoomOnlineKey(room.getId());
            String countStr = stringRedisTemplate.opsForValue().get(onlineKey);
            long onlineCount = StringUtils.hasText(countStr) ? Long.parseLong(countStr) : 0L;
            vo.setOnlineCount(onlineCount);
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinRoom(Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 检查房间是否存在且正常
        Room room = roomMapper.selectById(roomId);
        if (room == null || room.getStatus() != 1) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 2. 尝试插入房间成员记录
        RoomMember member = new RoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setRole(0); // 0-普通成员
        
        try {
            // 利用数据库 (room_id, user_id) 唯一索引防止重复加入
            roomMemberMapper.insert(member);
            log.info("用户 {} 加入了房间 {}", userId, roomId);
        } catch (DuplicateKeyException e) {
            // 如果捕获到重复键异常，说明已经加入过了
            log.info("用户 {} 尝试重复加入房间 {}", userId, roomId);
            throw new BusinessException(ResultCode.ALREADY_IN_ROOM);
        }
    }
}
