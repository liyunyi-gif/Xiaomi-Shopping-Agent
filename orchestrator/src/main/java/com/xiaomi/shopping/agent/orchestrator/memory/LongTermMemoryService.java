package com.xiaomi.shopping.agent.orchestrator.memory;

import java.util.List;

/**
 * 长期记忆接口。
 *
 * @author liyunyi
 */
public interface LongTermMemoryService {

    void save(String userId, String memory);

    List<String> load(String userId);

    void clear(String userId);

    int evictLowWeight();
}
