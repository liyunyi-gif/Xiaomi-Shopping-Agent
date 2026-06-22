package com.xiaomi.shopping.agent.common.entity.memory;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话存档表（t_message）—— ③逐字历史，实时落盘。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_message")
public class Message extends LogicDeleteEntity {

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 角色：user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 命中的意图（仅 assistant） */
    private String intent;

    /** 检索质量判定 SUFFICIENT/INCOMPLETE/INSUFFICIENT/FAILURE */
    private String qualityVerdict;

    /** 重检次数 */
    private Integer retryCount;

    /** 思考过程内容（LLM 推理链） */
    private String thinkingContent;

    /** 消耗 token 数 */
    private Integer tokensUsed;
}
