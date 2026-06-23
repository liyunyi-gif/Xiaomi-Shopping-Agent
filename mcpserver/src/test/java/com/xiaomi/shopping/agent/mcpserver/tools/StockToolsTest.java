package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * StockTools 非联调测试。
 *
 * @author liyunyi
 */
class StockToolsTest {

    private final StockTools tools = new StockTools();

    @Test
    @DisplayName("query_stock 返回 mock 库存")
    void shouldQueryStock() {
        ToolResult result = tools.queryStock(Map.of("skuId", "sku-14"));

        assertEquals(ToolResult.Status.SUCCESS, result.getStatus());
        assertEquals(100, result.getData().get("stock"));
    }

    @Test
    @DisplayName("query_stock 缺 skuId 返回 NEED_CLARIFY")
    void shouldNeedClarifyWhenSkuMissing() {
        ToolResult result = tools.queryStock(Map.of());

        assertEquals(ToolResult.Status.NEED_CLARIFY, result.getStatus());
        assertEquals(java.util.List.of("skuId"), result.getMissingSlots());
    }
}
