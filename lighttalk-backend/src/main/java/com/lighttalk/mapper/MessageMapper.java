package com.lighttalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lighttalk.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper 接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
