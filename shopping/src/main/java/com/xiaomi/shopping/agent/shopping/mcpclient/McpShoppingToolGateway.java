package com.xiaomi.shopping.agent.shopping.mcpclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * 基于 Spring AI MCP Client 的工具网关。
 *
 * @author liyunyi
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ToolCallbackProvider.class)
public class McpShoppingToolGateway implements ShoppingToolGateway {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ToolCallbackProvider toolCallbackProvider;
    private final ObjectMapper objectMapper;

    @Override
    public ToolResult invoke(String toolName, Map<String, Object> args) {
        try {
            Optional<ToolCallback> tool = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                    .filter(callback -> toolName.equals(callback.getToolDefinition().name()))
                    .findFirst();
            if (tool.isEmpty()) {
                return ToolResult.failed("MCP_TOOL_NOT_FOUND: " + toolName);
            }
            String input = objectMapper.writeValueAsString(args == null ? Map.of() : args);
            String raw = tool.get().call(input);
            return parseResult(raw);
        } catch (Exception e) {
            log.warn("MCP 工具调用失败 toolName={} reason={}", toolName, e.getMessage());
            return ToolResult.failed("MCP_TOOL_CALL_FAILED: " + toolName);
        }
    }

    private ToolResult parseResult(String raw) throws Exception {
        if (raw == null || raw.isBlank()) {
            return ToolResult.failed("EMPTY_MCP_RESPONSE");
        }
        Map<String, Object> payload = objectMapper.readValue(raw, MAP_TYPE);
        Object status = payload.get("status");
        Map<String, Object> data = asMap(payload.get("data"));
        Object missing = payload.get("missingSlots");
        String error = payload.get("errorMessage") == null ? null : String.valueOf(payload.get("errorMessage"));
        if ("SUCCESS".equals(String.valueOf(status))) {
            return ToolResult.success(data);
        }
        if ("NEED_CLARIFY".equals(String.valueOf(status))) {
            return ToolResult.needClarify(missing instanceof Iterable<?> iterable
                    ? objectMapper.convertValue(iterable, new TypeReference<>() {})
                    : java.util.List.of());
        }
        return ToolResult.failed(error == null ? "MCP_TOOL_FAILED" : error);
    }

    private Map<String, Object> asMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.convertValue(value, MAP_TYPE);
    }
}
