package com.xiaomi.shopping.agent.orchestrator.memory;

import java.util.List;

/**
 * 存档接口。
 *
 * @author liyunyi
 */
public interface MessageArchiveService {

    void archive(String userId, String conversationId, String role, String content);

    List<ChatMessage> list(String conversationId);

    void clear(String userId);
}
