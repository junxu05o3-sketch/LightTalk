package com.lighttalk.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间成员实体类
 * 对应数据库表: room_member
 */
@Data
@TableName("room_member")
public class RoomMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 房间内昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 角色: 0-普通成员, 1-房主, 2-管理员
     */
    @TableField("role")
    private Integer role;

    /**
     * 加入时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
