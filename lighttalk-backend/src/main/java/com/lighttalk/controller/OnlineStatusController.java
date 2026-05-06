package com.lighttalk.controller;

import com.lighttalk.common.api.Result;
import com.lighttalk.common.constants.RedisKeyConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线状态控制器
 * 供前端查询用户在线状态和房间在线人数
 *
 * 注：实际的在线状态写入/下线由 C++ WebSocket 服务负责，Java 后端只做只读查询
 */
@RestController
@RequestMapping("/status")
@Api(tags = "状态统计", description = "在线人数与用户状态查询")
public class OnlineStatusController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/room/{roomId}/count")
    @ApiOperation(value = "获取房间在线人数", notes = "直接从 Redis 读取计数器")
    public Result<Long> getRoomOnlineCount(@PathVariable Long roomId) {
        String key = RedisKeyConstants.getRoomOnlineKey(roomId);
        String countStr = stringRedisTemplate.opsForValue().get(key);
        
        long count = StringUtils.hasText(countStr) ? Long.parseLong(countStr) : 0L;
        return Result.success(count);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "获取指定用户在线状态", notes = "1-在线，0-离线")
    public Result<Integer> getUserOnlineStatus(@PathVariable Long userId) {
        String key = RedisKeyConstants.getUserOnlineKey(userId);
        String statusStr = stringRedisTemplate.opsForValue().get(key);
        
        int status = "1".equals(statusStr) ? 1 : 0;
        return Result.success(status);
    }
}
