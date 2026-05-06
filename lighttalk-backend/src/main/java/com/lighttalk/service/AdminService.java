package com.lighttalk.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lighttalk.model.dto.UserMuteReq;
import com.lighttalk.model.dto.UserQueryReq;
import com.lighttalk.model.vo.MessageVO;
import com.lighttalk.model.vo.RoomVO;
import com.lighttalk.model.vo.UserAdminVO;

import java.util.List;

/**
 * 管理后台服务接口
 */
public interface AdminService {

    /**
     * 分页查询用户
     */
    Page<UserAdminVO> pageUsers(UserQueryReq req);

    /**
     * 获取所有房间列表
     */
    List<RoomVO> listAllRooms();

    /**
     * 检索全局消息
     * @param roomId 房间ID（可选）
     * @param userId 发送者ID（可选）
     */
    List<MessageVO> listAllMessages(Long roomId, Long userId);

    /**
     * 禁言/解禁用户
     */
    void muteUser(UserMuteReq req);

    /**
     * 踢出用户 (预留接口，写 Redis)
     */
    void kickUser(Long userId, Long roomId);
}
