package com.lighttalk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lighttalk.common.api.Result;
import com.lighttalk.model.dto.UserMuteReq;
import com.lighttalk.model.dto.UserQueryReq;
import com.lighttalk.model.vo.MessageVO;
import com.lighttalk.model.vo.RoomVO;
import com.lighttalk.model.vo.UserAdminVO;
import com.lighttalk.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台控制器
 * 已在 SecurityConfig 中配置了仅有 ROLE_ADMIN 权限的用户可访问该路径 (/admin/**)
 */
@RestController
@RequestMapping("/admin")
@Api(tags = "管理后台", description = "供超级管理员调用的用户、房间、消息审查接口")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/users/page")
    @ApiOperation(value = "分页查询用户", notes = "支持按用户名、昵称模糊查询及状态筛选")
    public Result<Page<UserAdminVO>> pageUsers(@RequestBody UserQueryReq req) {
        return Result.success(adminService.pageUsers(req));
    }

    @GetMapping("/rooms")
    @ApiOperation(value = "获取所有房间元数据", notes = "供管理员审查全服房间情况")
    public Result<List<RoomVO>> listAllRooms() {
        return Result.success(adminService.listAllRooms());
    }

    @GetMapping("/messages")
    @ApiOperation(value = "全局消息检索", notes = "可按房间ID或用户ID精确检索")
    public Result<List<MessageVO>> listAllMessages(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long userId) {
        return Result.success(adminService.listAllMessages(roomId, userId));
    }

    @PostMapping("/users/mute")
    @ApiOperation(value = "禁言/解禁用户", notes = "修改DB并向Redis写入惩罚键（支持双端秒级拦截），传 0 则解禁")
    public Result<Void> muteUser(@Validated @RequestBody UserMuteReq req) {
        adminService.muteUser(req);
        return Result.success("操作成功", null);
    }

    @PostMapping("/users/kick")
    @ApiOperation(value = "踢出用户预留接口", notes = "向Redis写入短效Key，供C++端监听并主动断开WebSocket长连接")
    public Result<Void> kickUser(@RequestParam Long userId, @RequestParam Long roomId) {
        adminService.kickUser(userId, roomId);
        return Result.success("踢人指令已发出", null);
    }
}
