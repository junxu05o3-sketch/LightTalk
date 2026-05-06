package com.lighttalk.service;

import com.lighttalk.model.dto.RoomCreateReq;
import com.lighttalk.model.vo.RoomVO;

import java.util.List;

/**
 * 房间服务接口
 */
public interface RoomService {

    /**
     * 创建房间
     *
     * @param req 房间创建请求
     */
    void createRoom(RoomCreateReq req);

    /**
     * 获取房间列表
     * 会从 Redis 中同步拉取每个房间的实时在线人数
     *
     * @return 房间 VO 列表
     */
    List<RoomVO> listRooms();

    /**
     * 加入房间
     *
     * @param roomId 房间ID
     */
    void joinRoom(Long roomId);
}
