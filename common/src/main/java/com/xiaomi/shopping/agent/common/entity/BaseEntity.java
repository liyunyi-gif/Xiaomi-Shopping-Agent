package com.xiaomi.shopping.agent.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类（公共字段：主键 + 创建/更新时间）。
 * <p>
 * 大多数业务表都有 created_at / updated_at，由 MyBatis-Plus 自动填充。
 * 逻辑删除字段 deleted 由 {@link LogicDeleteEntity} 提供（部分表如 t_knowledge_vector 无）。
 *
 * @author liyunyi
 */
@Data
public abstract class BaseEntity implements Serializable {

    /** 主键，雪花ID，数据库 BIGSERIAL */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
