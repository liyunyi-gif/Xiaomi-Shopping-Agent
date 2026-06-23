package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * OrderTools 非联调测试（对应 MCP-002/MCP-003）。
 *
 * @author liyunyi
 */
class OrderToolsTest {

    private final OrderTools tools = new OrderTools();

    @Test
    @DisplayName("MCP-002 place_order 成功返回 orderId/orderNo")
    void shouldPlaceOrder() {
        ToolResult result = tools.placeOrder(Map.of("cartId", "cart-001", "address", "武汉市洪山区"));

        assertEquals(ToolResult.Status.SUCCESS, result.getStatus());
        assertFalse(result.getData().get("orderId").toString().isBlank());
        assertFalse(result.getData().get("orderNo").toString().isBlank());
    }

    @Test
    @DisplayName("MCP-003 place_order 缺地址返回 NEED_CLARIFY")
    void shouldNeedClarifyWhenAddressMissing() {
        ToolResult result = tools.placeOrder(Map.of("cartId", "cart-001"));

        assertEquals(ToolResult.Status.NEED_CLARIFY, result.getStatus());
        assertEquals(java.util.List.of("address"), result.getMissingSlots());
    }
}
