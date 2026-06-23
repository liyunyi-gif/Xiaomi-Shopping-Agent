package com.xiaomi.shopping.agent.orchestrator.memory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MEM-001..008 三层记忆非外部依赖测试。
 *
 * @author liyunyi
 */
class MemoryServiceTest {

    @Test
    @DisplayName("MEM-001/006/007 存档与默认上下文分层，存档不进默认上下文")
    void shouldSeparateArchiveFromDefaultContext() {
        InMemoryShortTermMemoryService shortTerm = new InMemoryShortTermMemoryService(10);
        InMemoryLongTermMemoryService longTerm = new InMemoryLongTermMemoryService();
        InMemoryMessageArchiveService archive = new InMemoryMessageArchiveService();
        MemoryLoadService loader = new MemoryLoadService(shortTerm, longTerm);

        archive.archive("u1", "c1", "user", "逐字历史很长");
        longTerm.save("u1", "预算5000以内");
        shortTerm.append("c1", "user", "最近问小米14");

        String context = loader.loadDefaultContext("u1", "c1");

        assertEquals(1, archive.list("c1").size());
        assertTrue(context.contains("预算5000以内"));
        assertTrue(context.contains("最近问小米14"));
        assertFalse(context.contains("逐字历史很长"));
    }

    @Test
    @DisplayName("MEM-003 短期记忆只保留最近N轮")
    void shouldKeepRecentWindow() {
        InMemoryShortTermMemoryService shortTerm = new InMemoryShortTermMemoryService(3);

        for (int i = 0; i < 5; i++) {
            shortTerm.append("c1", "user", "msg" + i);
        }

        assertEquals(3, shortTerm.recent("c1").size());
        assertEquals("msg2", shortTerm.recent("c1").get(0).getContent());
    }

    @Test
    @DisplayName("MEM-004/008 长期记忆跨会话加载并支持手动清除")
    void shouldLoadAndClearLongTermMemory() {
        InMemoryLongTermMemoryService longTerm = new InMemoryLongTermMemoryService();

        longTerm.save("u1", "爱打游戏");
        assertTrue(longTerm.load("u1").contains("爱打游戏"));

        longTerm.clear("u1");
        assertTrue(longTerm.load("u1").isEmpty());
        assertEquals(0, longTerm.evictLowWeight());
    }

    @Test
    @DisplayName("MEM-002 快照由主 Agent 构造注入")
    void shouldBuildSnapshotFromMainAgentMemory() {
        InMemoryShortTermMemoryService shortTerm = new InMemoryShortTermMemoryService(10);
        InMemoryLongTermMemoryService longTerm = new InMemoryLongTermMemoryService();
        longTerm.save("u1", "大内存偏好");
        SessionSnapshotBuilder builder = new SessionSnapshotBuilder(new MemoryLoadService(shortTerm, longTerm));

        var snapshot = builder.build("u1", "c1", "KNOWLEDGE");

        assertEquals("u1", snapshot.getUserId());
        assertEquals("c1", snapshot.getConversationId());
        assertTrue(snapshot.getRecentContext().contains("大内存偏好"));
    }
}
