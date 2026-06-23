package com.xiaomi.shopping.agent.shopping.orchestration;

import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.shopping.mcpclient.ShoppingToolGateway;
import com.xiaomi.shopping.agent.shopping.mcpclient.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shopping 编排非联调测试（对应 test/06 MCP-001~005、test/08 CONTRACT-003/004）。
 *
 * @author liyunyi
 */
class ShoppingOrchestratorTest {

    @Test
    @DisplayName("MCP-001 add_cart 确定性路由到 add_to_cart 并透传参数")
    void shouldRouteAddCartDeterministically() {
        RecordingGateway gateway = new RecordingGateway(ToolResult.success(Map.of("cartId", "cart-001")));
        ShoppingOrchestrator orchestrator = new ShoppingOrchestrator(gateway);

        ShoppingResponse response = orchestrator.orchestrate(ShoppingRequest.builder()
                .action("add_cart")
                .slots(Map.of("skuId", "sku-14", "spec", "16GB+512GB", "qty", 2))
                .snapshot(snapshot())
                .build());

        assertEquals("add_to_cart", gateway.calls.get(0).toolName());
        assertEquals("sku-14", gateway.calls.get(0).args().get("skuId"));
        assertEquals("16GB+512GB", gateway.calls.get(0).args().get("spec"));
        assertEquals(2, gateway.calls.get(0).args().get("quantity"));
        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, response.getStatus());
        assertEquals("cart-001", response.getResultData().get("cartId"));
    }

    @Test
    @DisplayName("MCP-002 加购结果可作为下单输入，工具链每步返回给主 Agent")
    void shouldSupportAddCartThenPlaceOrderChain() {
        RecordingGateway addGateway = new RecordingGateway(ToolResult.success(Map.of("cartId", "cart-001")));
        ShoppingResponse add = new ShoppingOrchestrator(addGateway).orchestrate(ShoppingRequest.builder()
                .action("ADD_TO_CART")
                .slots(Map.of("skuId", "sku-14", "spec", "16GB+512GB"))
                .snapshot(snapshot())
                .build());

        RecordingGateway orderGateway = new RecordingGateway(ToolResult.success(Map.of("orderId", "order-001")));
        ShoppingResponse order = new ShoppingOrchestrator(orderGateway).orchestrate(ShoppingRequest.builder()
                .action("place_order")
                .slots(Map.of("cartId", add.getResultData().get("cartId"), "address", "武汉市洪山区"))
                .snapshot(snapshot())
                .build());

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, add.getStatus());
        assertEquals("place_order", orderGateway.calls.get(0).toolName());
        assertEquals("cart-001", orderGateway.calls.get(0).args().get("cartId"));
        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, order.getStatus());
        assertEquals("order-001", order.getResultData().get("orderId"));
    }

    @Test
    @DisplayName("MCP-003 缺参数或缺库存返回 NEED_CLARIFY + missingSlots")
    void shouldReturnNeedClarifyWhenMissingSlotsOrStock() {
        ShoppingOrchestrator orchestrator = new ShoppingOrchestrator(new RecordingGateway(ToolResult.needClarify(List.of("spec"))));

        ShoppingResponse response = orchestrator.orchestrate(ShoppingRequest.builder()
                .action("add_to_cart")
                .slots(Map.of("skuId", "sku-14"))
                .snapshot(snapshot())
                .build());

        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, response.getStatus());
        assertEquals(List.of("spec"), response.getMissingSlots());
        assertTrue(response.getResultData().isEmpty());
    }

    @Test
    @DisplayName("MCP-005 推荐/付费引导类 action 不调用工具并返回失败信号")
    void shouldRejectRecommendationLikeAction() {
        RecordingGateway gateway = new RecordingGateway(ToolResult.success(Map.of("cartId", "cart-001")));
        ShoppingOrchestrator orchestrator = new ShoppingOrchestrator(gateway);

        ShoppingResponse response = orchestrator.orchestrate(ShoppingRequest.builder()
                .action("recommend")
                .slots(Map.of("product", "小米14"))
                .snapshot(snapshot())
                .build());

        assertEquals(ShoppingResponse.ExecStatus.FAILED, response.getStatus());
        assertEquals("UNSUPPORTED_ACTION", response.getErrorMessage());
        assertTrue(gateway.calls.isEmpty(), "推荐类动作不应调用 MCP 工具");
    }

    @Test
    @DisplayName("CONTRACT-003/004 action 为空需举手，FAILED 必带 errorMessage")
    void shouldKeepShoppingContractFields() {
        ShoppingOrchestrator orchestrator = new ShoppingOrchestrator(new RecordingGateway(ToolResult.success(Map.of())));

        ShoppingResponse clarify = orchestrator.orchestrate(ShoppingRequest.builder()
                .action("")
                .slots(Map.of())
                .snapshot(snapshot())
                .build());
        ShoppingResponse failed = orchestrator.orchestrate(ShoppingRequest.builder()
                .action("unknown_action")
                .slots(Map.of())
                .snapshot(snapshot())
                .build());

        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, clarify.getStatus());
        assertFalse(clarify.getMissingSlots().isEmpty());
        assertEquals(ShoppingResponse.ExecStatus.FAILED, failed.getStatus());
        assertFalse(failed.getErrorMessage().isBlank());
    }

    private SessionSnapshot snapshot() {
        return SessionSnapshot.builder()
                .userId("u-001")
                .conversationId("c-001")
                .currentIntent("TOOL")
                .cartState(Map.of("cart-001", 1))
                .build();
    }

    private static class RecordingGateway implements ShoppingToolGateway {
        private final ToolResult result;
        private final List<Call> calls = new ArrayList<>();

        private RecordingGateway(ToolResult result) {
            this.result = result;
        }

        @Override
        public ToolResult invoke(String toolName, Map<String, Object> args) {
            calls.add(new Call(toolName, args));
            return result;
        }
    }

    private record Call(String toolName, Map<String, Object> args) {
    }
}
