package com.xiaomi.shopping.agent.orchestrator.memory;

/**
 * 长期记忆提炼接口。
 *
 * @author liyunyi
 */
public interface LongTermMemoryDistiller {

    void distillOnSessionEnd(String userId, String conversationId);
}
