package com.lighttalk.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 对应数据库表: message
 */
@Data
@TableName("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间ID
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 发送者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 发送者昵称（冗余存储）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型: text-文本, system-系统消息
     */
    @TableField("type")
    private String type;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
