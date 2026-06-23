package com.xiaomi.shopping.agent.orchestrator.memory;

import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 会话快照构建器。
 *
 * @author liyunyi
 */
@Component
@RequiredArgsConstructor
public class SessionSnapshotBuilder {

    private final MemoryLoadService memoryLoadService;

    public SessionSnapshot build(String userId, String conversationId, String currentIntent) {
        return SessionSnapshot.builder()
                .userId(userId)
                .conversationId(conversationId)
                .currentIntent(currentIntent)
                .recentContext(memoryLoadService.loadDefaultContext(userId, conversationId))
                .selectedProducts(null)
                .cartState(new HashMap<>())
                .browseHistory(null)
                .build();
    }
}
