package com.lighttalk.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间实体类
 * 对应数据库表: room
 */
@Data
@TableName("room")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 房间ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间名称
     */
    @TableField("name")
    private String name;

    /**
     * 房间描述
     */
    @TableField("description")
    private String description;

    /**
     * 房主用户ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 最大成员数
     */
    @TableField("max_members")
    private Integer maxMembers;

    /**
     * 房间状态: 1-正常, 0-已关闭
     */
    @TableField("status")
    private Integer status;

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
