package com.xiaomi.shopping.agent.mcpserver.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MCP Server ToolResult 契约测试。
 *
 * @author liyunyi
 */
class ToolResultTest {

    @Test
    @DisplayName("MCP ToolResult SUCCESS/NEED_CLARIFY/FAILED 工厂方法字段完整")
    void shouldBuildToolResults() {
        ToolResult success = ToolResult.success(Map.of("cartId", "cart-001"));
        ToolResult clarify = ToolResult.needClarify(List.of("address"));
        ToolResult failed = ToolResult.failed("INVALID_QUANTITY");

        assertEquals(ToolResult.Status.SUCCESS, success.getStatus());
        assertEquals("cart-001", success.getData().get("cartId"));
        assertEquals(ToolResult.Status.NEED_CLARIFY, clarify.getStatus());
        assertEquals(List.of("address"), clarify.getMissingSlots());
        assertEquals(ToolResult.Status.FAILED, failed.getStatus());
        assertEquals("INVALID_QUANTITY", failed.getErrorMessage());
        assertTrue(failed.getData().isEmpty());
    }
}
