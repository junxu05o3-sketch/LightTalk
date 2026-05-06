package com.lighttalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lighttalk.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis Plus BaseMapper，自动拥有基础 CRUD 方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
