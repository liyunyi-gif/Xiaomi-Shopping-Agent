package com.xiaomi.shopping.agent.mcpserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP Server 工具统一返回结构。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    private Status status;

    private Map<String, Object> data;

    private List<String> missingSlots;

    private String errorMessage;

    public static ToolResult success(Map<String, Object> data) {
        return ToolResult.builder()
                .status(Status.SUCCESS)
                .data(data == null ? Map.of() : data)
                .missingSlots(List.of())
                .build();
    }

    public static ToolResult needClarify(List<String> missingSlots) {
        return ToolResult.builder()
                .status(Status.NEED_CLARIFY)
                .data(Map.of())
                .missingSlots(missingSlots == null ? List.of() : missingSlots)
                .build();
    }

    public static ToolResult failed(String errorMessage) {
        return ToolResult.builder()
                .status(Status.FAILED)
                .data(Map.of())
                .missingSlots(List.of())
                .errorMessage(errorMessage)
                .build();
    }

    public enum Status {
        SUCCESS,
        NEED_CLARIFY,
        FAILED
    }
}
