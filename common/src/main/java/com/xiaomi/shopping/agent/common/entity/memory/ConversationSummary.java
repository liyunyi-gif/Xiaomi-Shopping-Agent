package com.xiaomi.shopping.agent.common.entity.memory;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话摘要表（t_conversation_summary）—— 与逐字存档分离存储。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_conversation_summary")
public class ConversationSummary extends LogicDeleteEntity {

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 最近一条消息ID */
    private Long lastMessageId;

    /** 摘要内容 */
    private String content;
}
