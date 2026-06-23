package com.xiaomi.shopping.agent.mcpserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MCP Server 上下文和工具注册测试。
 *
 * @author liyunyi
 */
@SpringBootTest
class McpServerApplicationContextTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    @DisplayName("mcpserver Spring 上下文加载并注册五个购物工具")
    void shouldLoadContextAndRegisterTools() {
        Set<String> toolNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertTrue(toolNames.contains("add_to_cart"));
        assertTrue(toolNames.contains("place_order"));
        assertTrue(toolNames.contains("query_logistics"));
        assertTrue(toolNames.contains("query_stock"));
        assertTrue(toolNames.contains("query_promotion"));
    }
}
