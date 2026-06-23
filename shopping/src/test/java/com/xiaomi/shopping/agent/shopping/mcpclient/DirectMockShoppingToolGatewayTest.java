package com.xiaomi.shopping.agent.shopping.mcpclient;

import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DirectMockShoppingToolGateway 非联调测试（无外部 MCP/业务接口）。
 *
 * @author liyunyi
 */
class DirectMockShoppingToolGatewayTest {

    private final DirectMockShoppingToolGateway gateway = new DirectMockShoppingToolGateway();

    @Test
    @DisplayName("MCP-001 mock add_to_cart 成功返回 cartId 且默认数量为 1")
    void shouldMockAddToCartWithDefaultQuantity() {
        ToolResult result = gateway.invoke("add_to_cart", Map.of("skuId", "sku-14", "spec", "16GB+512GB"));

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, result.getStatus());
        assertFalse(result.getData().get("cartId").toString().isBlank());
        assertEquals(1, result.getData().get("quantity"));
        assertEquals("add_to_cart", result.getData().get("tool"));
    }

    @Test
    @DisplayName("MCP-003 mock add_to_cart 缺 skuId/spec/stock 时举手")
    void shouldMockAddToCartNeedClarifyForMissingInputs() {
        ToolResult missingSku = gateway.invoke("add_to_cart", Map.of("spec", "16GB+512GB"));
        ToolResult missingSpec = gateway.invoke("add_to_cart", Map.of("skuId", "sku-14"));
        ToolResult outOfStock = gateway.invoke("add_to_cart", Map.of("skuId", "OUT_OF_STOCK", "spec", "16GB+512GB"));

        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, missingSku.getStatus());
        assertEquals(java.util.List.of("skuId"), missingSku.getMissingSlots());
        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, missingSpec.getStatus());
        assertEquals(java.util.List.of("spec"), missingSpec.getMissingSlots());
        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, outOfStock.getStatus());
        assertEquals(java.util.List.of("stock"), outOfStock.getMissingSlots());
    }

    @Test
    @DisplayName("MCP-002 mock place_order 成功返回订单，缺地址/购物车项时举手")
    void shouldMockPlaceOrderAndClarifyMissingSlots() {
        ToolResult success = gateway.invoke("place_order", Map.of("cartId", "cart-001", "address", "武汉市洪山区"));
        ToolResult missingAddress = gateway.invoke("place_order", Map.of("cartId", "cart-001"));
        ToolResult missingItems = gateway.invoke("place_order", Map.of("address", "武汉市洪山区"));

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, success.getStatus());
        assertFalse(success.getData().get("orderId").toString().isBlank());
        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, missingAddress.getStatus());
        assertEquals(java.util.List.of("address"), missingAddress.getMissingSlots());
        assertEquals(ShoppingResponse.ExecStatus.NEED_CLARIFY, missingItems.getStatus());
        assertEquals(java.util.List.of("cartItems"), missingItems.getMissingSlots());
    }

    @Test
    @DisplayName("MCP 工具查询类 mock 结果完整，未知工具返回 FAILED")
    void shouldMockQueryToolsAndFailUnknownTool() {
        ToolResult logistics = gateway.invoke("query_logistics", Map.of("orderId", "order-001"));
        ToolResult stock = gateway.invoke("query_stock", Map.of("skuId", "OUT_OF_STOCK"));
        ToolResult promotion = gateway.invoke("query_promotion", Map.of("skuId", "sku-14"));
        ToolResult unknown = gateway.invoke("not_exists", Map.of());

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, logistics.getStatus());
        assertTrue(logistics.getData().containsKey("logisticsNo"));
        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, stock.getStatus());
        assertEquals(0, stock.getData().get("stock"));
        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, promotion.getStatus());
        assertEquals("暂无优惠", promotion.getData().get("promotion"));
        assertEquals(ShoppingResponse.ExecStatus.FAILED, unknown.getStatus());
        assertTrue(unknown.getErrorMessage().contains("MCP_TOOL_NOT_FOUND"));
    }
}
