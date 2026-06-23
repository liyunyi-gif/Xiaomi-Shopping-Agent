package com.xiaomi.shopping.agent.orchestrator.memory;

import org.springframework.stereotype.Service;

/**
 * 默认无外部模型长期记忆提炼实现。
 *
 * @author liyunyi
 */
@Service
public class NoopLongTermMemoryDistiller implements LongTermMemoryDistiller {

    @Override
    public void distillOnSessionEnd(String userId, String conversationId) {
        // 默认测试/本地模式不调用外部 LLM。
    }
}
