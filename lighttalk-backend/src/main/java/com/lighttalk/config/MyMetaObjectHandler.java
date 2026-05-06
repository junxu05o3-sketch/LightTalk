package com.lighttalk.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 字段自动填充处理器
 * 自动填充 createTime 和 updateTime 字段
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 当字段标注了 @TableField(fill = FieldFill.INSERT) 时触发
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * 当字段标注了 @TableField(fill = FieldFill.INSERT_UPDATE) 时触发
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
