package com.xiaomi.shopping.agent.knowledge.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Knowledge 契约单测（对应 Test/08 CONTRACT-001/002/005 与 P6）。
 *
 * @author liyunyi
 */
class KnowledgeContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("CONTRACT-001 KnowledgeRequest 字段完整且 JSON 序列化无损")
    void shouldSerializeKnowledgeRequestLosslessly() throws Exception {
        SessionSnapshot snapshot = SessionSnapshot.builder()
                .userId("u1")
                .conversationId("c1")
                .currentIntent("参数咨询")
                .recentContext("用户刚刚在看小米14")
                .selectedProducts("小米14")
                .cartState(Map.of("小米14", 1))
                .browseHistory("小米14,Redmi K70")
                .build();
        KnowledgeRequest request = KnowledgeRequest.builder()
                .question("小米14拍照怎么样")
                .intent("商品参数咨询")
                .snapshot(snapshot)
                .retryAttempt(1)
                .build();

        String json = objectMapper.writeValueAsString(request);
        KnowledgeRequest restored = objectMapper.readValue(json, KnowledgeRequest.class);

        assertEquals("小米14拍照怎么样", restored.getQuestion());
        assertEquals("商品参数咨询", restored.getIntent());
        assertEquals(1, restored.getRetryAttempt());
        assertNotNull(restored.getSnapshot());
        assertEquals("小米14", restored.getSnapshot().getSelectedProducts());
    }

    @Test
    @DisplayName("CONTRACT-002 KnowledgeResponse 只含客观三信号，不含主观质量自评字段")
    void shouldOnlyContainObjectiveKnowledgeResponseFields() {
        Set<String> fieldNames = Arrays.stream(KnowledgeResponse.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertTrue(fieldNames.contains("results"));
        assertTrue(fieldNames.contains("topScore"));
        assertTrue(fieldNames.contains("hitEntities"));
        assertTrue(fieldNames.contains("recallCount"));
        assertFalse(fieldNames.contains("isGood"));
        assertFalse(fieldNames.contains("isSufficient"));
        assertFalse(fieldNames.contains("quality"));
        assertFalse(fieldNames.contains("qualityScore"));
        assertFalse(fieldNames.contains("sufficient"));
    }

    @Test
    @DisplayName("CONTRACT-002 RetrievalItem 字段包含 sourceId/content/score/hitType")
    void shouldExposeRetrievalItemFields() {
        Set<String> fieldNames = Arrays.stream(KnowledgeResponse.RetrievalItem.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertTrue(fieldNames.contains("sourceId"));
        assertTrue(fieldNames.contains("content"));
        assertTrue(fieldNames.contains("score"));
        assertTrue(fieldNames.contains("hitType"));
    }

    @Test
    @DisplayName("CONTRACT-005 SessionSnapshot 字段齐全")
    void shouldContainSessionSnapshotFields() {
        Set<String> fieldNames = Arrays.stream(SessionSnapshot.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertTrue(fieldNames.contains("userId"));
        assertTrue(fieldNames.contains("conversationId"));
        assertTrue(fieldNames.contains("currentIntent"));
        assertTrue(fieldNames.contains("recentContext"));
        assertTrue(fieldNames.contains("selectedProducts"));
        assertTrue(fieldNames.contains("cartState"));
        assertTrue(fieldNames.contains("browseHistory"));
    }
}
