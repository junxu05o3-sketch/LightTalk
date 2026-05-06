package com.lighttalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lighttalk.model.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间 Mapper 接口
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
}
