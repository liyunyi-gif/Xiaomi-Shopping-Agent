package com.xiaomi.shopping.agent.orchestrator.memory;

import java.util.List;

/**
 * 短期记忆接口。
 *
 * @author liyunyi
 */
public interface ShortTermMemoryService {

    void append(String conversationId, String role, String content);

    List<ChatMessage> recent(String conversationId);

    void clear(String conversationId);
}
