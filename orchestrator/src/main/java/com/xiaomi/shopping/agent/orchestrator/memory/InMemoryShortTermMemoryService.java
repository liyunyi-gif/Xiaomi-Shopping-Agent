package com.xiaomi.shopping.agent.orchestrator.memory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存短期记忆实现，避免默认测试依赖 Redis。
 *
 * @author liyunyi
 */
@Service
public class InMemoryShortTermMemoryService implements ShortTermMemoryService {

    private final Map<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();
    private final int window;

    public InMemoryShortTermMemoryService(@Value("${xiaomi.agent.memory.short-term-window:10}") int window) {
        this.window = window;
    }

    @Override
    public synchronized void append(String conversationId, String role, String content) {
        List<ChatMessage> list = messages.computeIfAbsent(conversationId, key -> new ArrayList<>());
        list.add(ChatMessage.builder().role(role).content(content).timestamp(System.currentTimeMillis()).build());
        while (list.size() > window) {
            list.remove(0);
        }
    }

    @Override
    public List<ChatMessage> recent(String conversationId) {
        return List.copyOf(messages.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void clear(String conversationId) {
        messages.remove(conversationId);
    }
}
