package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CartTools 非联调测试（对应 MCP-001/MCP-003）。
 *
 * @author liyunyi
 */
class CartToolsTest {

    private final CartTools tools = new CartTools();

    @Test
    @DisplayName("MCP-001 add_to_cart 成功返回 cartId")
    void shouldAddToCart() {
        ToolResult result = tools.addToCart(Map.of("skuId", "sku-14", "spec", "16GB+512GB", "quantity", 2));

        assertEquals(ToolResult.Status.SUCCESS, result.getStatus());
        assertFalse(result.getData().get("cartId").toString().isBlank());
        assertEquals("sku-14", result.getData().get("skuId"));
        assertEquals(2, result.getData().get("quantity"));
    }

    @Test
    @DisplayName("MCP-003 add_to_cart 缺规格返回 NEED_CLARIFY")
    void shouldNeedClarifyWhenSpecMissing() {
        ToolResult result = tools.addToCart(Map.of("skuId", "sku-14"));

        assertEquals(ToolResult.Status.NEED_CLARIFY, result.getStatus());
        assertEquals(java.util.List.of("spec"), result.getMissingSlots());
    }

    @Test
    @DisplayName("MCP-003 add_to_cart 缺库存返回 NEED_CLARIFY")
    void shouldNeedClarifyWhenOutOfStock() {
        ToolResult result = tools.addToCart(Map.of("skuId", "OUT_OF_STOCK", "spec", "16GB+512GB"));

        assertEquals(ToolResult.Status.NEED_CLARIFY, result.getStatus());
        assertEquals(java.util.List.of("stock"), result.getMissingSlots());
    }

    @Test
    @DisplayName("MCP-003 add_to_cart 非法数量返回 FAILED")
    void shouldFailWhenQuantityInvalid() {
        ToolResult result = tools.addToCart(Map.of("skuId", "sku-14", "spec", "16GB+512GB", "quantity", 0));

        assertEquals(ToolResult.Status.FAILED, result.getStatus());
        assertEquals("INVALID_QUANTITY", result.getErrorMessage());
    }
}
