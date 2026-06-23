package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * LogisticsTools 非联调测试。
 *
 * @author liyunyi
 */
class LogisticsToolsTest {

    private final LogisticsTools tools = new LogisticsTools();

    @Test
    @DisplayName("query_logistics 返回 mock 物流状态")
    void shouldQueryLogistics() {
        ToolResult result = tools.queryLogistics(Map.of("orderId", "order-001"));

        assertEquals(ToolResult.Status.SUCCESS, result.getStatus());
        assertEquals("order-001", result.getData().get("orderId"));
        assertFalse(result.getData().get("logisticsNo").toString().isBlank());
    }

    @Test
    @DisplayName("query_logistics 缺 orderId 返回 NEED_CLARIFY")
    void shouldNeedClarifyWhenOrderMissing() {
        ToolResult result = tools.queryLogistics(Map.of());

        assertEquals(ToolResult.Status.NEED_CLARIFY, result.getStatus());
        assertEquals(java.util.List.of("orderId"), result.getMissingSlots());
    }
}
