package com.lighttalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lighttalk.model.entity.RoomMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间成员 Mapper 接口
 */
@Mapper
public interface RoomMemberMapper extends BaseMapper<RoomMember> {
}
