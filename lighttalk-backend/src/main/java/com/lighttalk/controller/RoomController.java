package com.lighttalk.controller;

import com.lighttalk.common.api.Result;
import com.lighttalk.model.dto.RoomCreateReq;
import com.lighttalk.model.vo.RoomVO;
import com.lighttalk.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间控制器
 */
@RestController
@RequestMapping("/rooms")
@Api(tags = "房间模块", description = "房间管理相关接口")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @ApiOperation(value = "创建房间", notes = "需登录认证，房间名不能重复")
    public Result<Void> createRoom(@Validated @RequestBody RoomCreateReq req) {
        roomService.createRoom(req);
        return Result.success("创建房间成功", null);
    }

    @GetMapping
    @ApiOperation(value = "获取房间列表", notes = "返回房间列表及Redis实时在线人数统计")
    public Result<List<RoomVO>> listRooms() {
        return Result.success(roomService.listRooms());
    }

    @PostMapping("/{roomId}/join")
    @ApiOperation(value = "加入房间", notes = "在数据库 room_member 表增加记录")
    public Result<Void> joinRoom(@PathVariable Long roomId) {
        roomService.joinRoom(roomId);
        return Result.success("加入房间成功", null);
    }
}
