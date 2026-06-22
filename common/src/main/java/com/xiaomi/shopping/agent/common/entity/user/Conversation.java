package com.xiaomi.shopping.agent.common.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话列表表（t_conversation）—— ①短期记忆会话维度。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_conversation")
public class Conversation extends LogicDeleteEntity {

    /** 会话ID（业务唯一，与 user_id 联合唯一） */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    /** 最后活跃时间 */
    private LocalDateTime lastTime;
}
