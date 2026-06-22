package com.xiaomi.shopping.agent.common.entity.memory;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 长期记忆表（t_user_longterm_memory）—— ②提炼画像/偏好/决策/已澄清槽位。
 * <p>
 * weight 支撑低权重淘汰，防记忆无限膨胀（架构.md §6.4 遗忘机制）。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_longterm_memory")
public class UserLongtermMemory extends LogicDeleteEntity {

    /** 用户ID */
    private Long userId;

    /** 记忆类型：profile/preference/decision/slot */
    private String memType;

    /** 记忆内容（提炼后结构化状态） */
    private String content;

    /** 权重（低权重淘汰） */
    private Double weight;

    /** 来源会话ID */
    private String sourceConversation;
}
