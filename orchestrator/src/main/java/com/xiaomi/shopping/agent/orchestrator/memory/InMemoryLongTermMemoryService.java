package com.xiaomi.shopping.agent.orchestrator.memory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存长期记忆实现，避免默认测试依赖 PostgreSQL。
 *
 * @author liyunyi
 */
@Service
public class InMemoryLongTermMemoryService implements LongTermMemoryService {

    private final Map<String, List<String>> memories = new ConcurrentHashMap<>();

    @Override
    public void save(String userId, String memory) {
        memories.computeIfAbsent(userId, key -> new ArrayList<>()).add(memory);
    }

    @Override
    public List<String> load(String userId) {
        return List.copyOf(memories.getOrDefault(userId, List.of()));
    }

    @Override
    public void clear(String userId) {
        memories.remove(userId);
    }

    @Override
    public int evictLowWeight() {
        return 0;
    }
}
