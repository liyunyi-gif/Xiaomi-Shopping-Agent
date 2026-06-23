package com.xiaomi.shopping.agent.shopping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shopping 契约 DTO 测试（对应 CONTRACT-003/004/005）。
 *
 * @author liyunyi
 */
class ShoppingContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("CONTRACT-003 ShoppingRequest action/slots/snapshot JSON 序列化无损")
    void shouldSerializeShoppingRequestLosslessly() throws Exception {
        ShoppingRequest request = ShoppingRequest.builder()
                .action("add_cart")
                .slots(Map.of("skuId", "sku-14", "quantity", 1, "address", "武汉市洪山区"))
                .snapshot(snapshot())
                .build();

        String json = objectMapper.writeValueAsString(request);
        ShoppingRequest restored = objectMapper.readValue(json, ShoppingRequest.class);

        assertEquals("add_cart", restored.getAction());
        assertEquals("sku-14", restored.getSlots().get("skuId"));
        assertEquals("u-001", restored.getSnapshot().getUserId());
        assertNotNull(restored.getSnapshot());
    }

    @Test
    @DisplayName("CONTRACT-004 ShoppingResponse 三类状态字段契约完整")
    void shouldKeepShoppingResponseStatusContract() {
        Set<String> statuses = Arrays.stream(ShoppingResponse.ExecStatus.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        ShoppingResponse success = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.SUCCESS)
                .resultData(Map.of("cartId", "cart-001"))
                .build();
        ShoppingResponse clarify = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.NEED_CLARIFY)
                .missingSlots(java.util.List.of("address"))
                .build();
        ShoppingResponse failed = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.FAILED)
                .errorMessage("UNSUPPORTED_ACTION")
                .build();

        assertTrue(statuses.containsAll(Set.of("SUCCESS", "NEED_CLARIFY", "FAILED")));
        assertTrue(success.getResultData().containsKey("cartId"));
        assertTrue(!clarify.getMissingSlots().isEmpty());
        assertTrue(!failed.getErrorMessage().isBlank());
    }

    @Test
    @DisplayName("CONTRACT-005 SessionSnapshot 含购物流需要的上下文字段")
    void shouldKeepSessionSnapshotFields() {
        SessionSnapshot snapshot = snapshot();

        assertEquals("u-001", snapshot.getUserId());
        assertEquals("c-001", snapshot.getConversationId());
        assertEquals("TOOL", snapshot.getCurrentIntent());
        assertEquals("最近询问了小米14", snapshot.getRecentContext());
        assertEquals("sku-14", snapshot.getSelectedProducts());
        assertTrue(snapshot.getCartState().containsKey("cart-001"));
        assertEquals("sku-13,sku-14", snapshot.getBrowseHistory());
    }

    private SessionSnapshot snapshot() {
        return SessionSnapshot.builder()
                .userId("u-001")
                .conversationId("c-001")
                .currentIntent("TOOL")
                .recentContext("最近询问了小米14")
                .selectedProducts("sku-14")
                .cartState(Map.of("cart-001", 1))
                .browseHistory("sku-13,sku-14")
                .build();
    }
}
