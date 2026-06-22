package com.xiaomi.shopping.agent.common.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 带逻辑删除的实体基类（created_at / updated_at / deleted）。
 * <p>
 * 大多数业务表（用户/会话/记忆/商品/知识文档/切片等）都有 deleted 逻辑删除字段。
 * 少数表（如 t_knowledge_vector 纯向量存储）无此字段，直接继承 {@link BaseEntity}。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class LogicDeleteEntity extends BaseEntity {

    /** 逻辑删除：0 未删除，1 已删除 */
    @TableLogic
    private Integer deleted;
}
