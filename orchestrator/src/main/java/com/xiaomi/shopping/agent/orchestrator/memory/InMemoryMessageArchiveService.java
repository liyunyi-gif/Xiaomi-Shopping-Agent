package com.xiaomi.shopping.agent.orchestrator.memory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存对话存档实现，避免默认测试依赖 PostgreSQL。
 *
 * @author liyunyi
 */
@Service
public class InMemoryMessageArchiveService implements MessageArchiveService {

    private final Map<String, List<ChatMessage>> archive = new ConcurrentHashMap<>();

    @Override
    public void archive(String userId, String conversationId, String role, String content) {
        archive.computeIfAbsent(conversationId, key -> new ArrayList<>())
                .add(ChatMessage.builder().role(role).content(content).timestamp(System.currentTimeMillis()).build());
    }

    @Override
    public List<ChatMessage> list(String conversationId) {
        return List.copyOf(archive.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void clear(String userId) {
        archive.clear();
    }
}
