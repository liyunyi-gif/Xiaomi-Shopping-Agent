package com.xiaomi.shopping.agent.shopping.mcpclient;

import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Shopping 内部工具调用结果。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {

    private ShoppingResponse.ExecStatus status;

    private Map<String, Object> data;

    private List<String> missingSlots;

    private String errorMessage;

    public static ToolResult success(Map<String, Object> data) {
        return ToolResult.builder()
                .status(ShoppingResponse.ExecStatus.SUCCESS)
                .data(data == null ? Map.of() : data)
                .missingSlots(List.of())
                .build();
    }

    public static ToolResult needClarify(List<String> missingSlots) {
        return ToolResult.builder()
                .status(ShoppingResponse.ExecStatus.NEED_CLARIFY)
                .data(Map.of())
                .missingSlots(missingSlots == null ? List.of() : missingSlots)
                .build();
    }

    public static ToolResult failed(String errorMessage) {
        return ToolResult.builder()
                .status(ShoppingResponse.ExecStatus.FAILED)
                .data(Map.of())
                .missingSlots(List.of())
                .errorMessage(errorMessage)
                .build();
    }
}
