package com.xiaomi.shopping.agent.common.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CONTRACT-001..005 跨节点契约测试。
 *
 * @author liyunyi
 */
class CrossNodeContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("CONTRACT-001 KnowledgeRequest 字段完整且 JSON 无损")
    void shouldKeepKnowledgeRequestContract() throws Exception {
        KnowledgeRequest request = KnowledgeRequest.builder()
                .question("小米14影像规格怎么样")
                .intent("参数咨询")
                .snapshot(snapshot())
                .retryAttempt(1)
                .queryEntities(Set.of("小米14", "影像"))
                .build();

        String json = objectMapper.writeValueAsString(request);
        KnowledgeRequest restored = objectMapper.readValue(json, KnowledgeRequest.class);

        assertEquals(request.getQuestion(), restored.getQuestion());
        assertEquals(request.getIntent(), restored.getIntent());
        assertEquals(1, restored.getRetryAttempt());
        assertTrue(restored.getQueryEntities().contains("小米14"));
        assertEquals("u-001", restored.getSnapshot().getUserId());
    }

    @Test
    @DisplayName("CONTRACT-002 KnowledgeResponse 只含客观信号")
    void shouldKeepKnowledgeResponseObjectiveOnly() {
        Set<String> methodNames = Arrays.stream(KnowledgeResponse.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertTrue(methodNames.containsAll(Set.of("getResults", "getTopScore", "getHitEntities", "getRecallCount")));
        assertFalse(methodNames.contains("isSufficient"));
        assertFalse(methodNames.contains("getQualityScore"));
        assertFalse(methodNames.contains("isGood"));
    }

    @Test
    @DisplayName("CONTRACT-003 ShoppingRequest action/slots/snapshot 完整")
    void shouldKeepShoppingRequestContract() throws Exception {
        ShoppingRequest request = ShoppingRequest.builder()
                .action("add_cart")
                .slots(Map.of("skuId", "小米14", "quantity", 1))
                .snapshot(snapshot())
                .build();

        ShoppingRequest restored = objectMapper.readValue(objectMapper.writeValueAsString(request), ShoppingRequest.class);

        assertEquals("add_cart", restored.getAction());
        assertEquals("小米14", restored.getSlots().get("skuId"));
        assertEquals("u-001", restored.getSnapshot().getUserId());
    }

    @Test
    @DisplayName("CONTRACT-004 ShoppingResponse 三状态字段约束")
    void shouldKeepShoppingResponseContract() {
        ShoppingResponse success = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.SUCCESS)
                .resultData(Map.of("cartId", "cart-001"))
                .missingSlots(List.of())
                .build();
        ShoppingResponse clarify = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.NEED_CLARIFY)
                .resultData(Map.of())
                .missingSlots(List.of("address"))
                .build();
        ShoppingResponse failed = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.FAILED)
                .resultData(Map.of())
                .missingSlots(List.of())
                .errorMessage("UNSUPPORTED_ACTION")
                .build();

        assertTrue(success.getResultData().containsKey("cartId"));
        assertFalse(clarify.getMissingSlots().isEmpty());
        assertFalse(failed.getErrorMessage().isBlank());
    }

    @Test
    @DisplayName("CONTRACT-005 SessionSnapshot 字段齐全")
    void shouldKeepSessionSnapshotContract() {
        SessionSnapshot snapshot = snapshot();

        assertEquals("u-001", snapshot.getUserId());
        assertEquals("c-001", snapshot.getConversationId());
        assertEquals("KNOWLEDGE", snapshot.getCurrentIntent());
        assertEquals("最近问了小米14", snapshot.getRecentContext());
        assertEquals("小米14", snapshot.getSelectedProducts());
        assertTrue(snapshot.getCartState().containsKey("cart-001"));
        assertEquals("小米13,小米14", snapshot.getBrowseHistory());
    }

    private SessionSnapshot snapshot() {
        return SessionSnapshot.builder()
                .userId("u-001")
                .conversationId("c-001")
                .currentIntent("KNOWLEDGE")
                .recentContext("最近问了小米14")
                .selectedProducts("小米14")
                .cartState(Map.of("cart-001", 1))
                .browseHistory("小米13,小米14")
                .build();
    }
}
