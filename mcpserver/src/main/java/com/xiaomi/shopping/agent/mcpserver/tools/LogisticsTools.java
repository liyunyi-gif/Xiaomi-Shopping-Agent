package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 物流 MCP 工具。
 *
 * @author liyunyi
 */
@Service
public class LogisticsTools {

    @Tool(name = "query_logistics", description = "查询订单物流状态")
    public ToolResult queryLogistics(Map<String, Object> args) {
        String orderId = string(args, "orderId");
        if (orderId == null) {
            return ToolResult.needClarify(List.of("orderId"));
        }
        return ToolResult.success(Map.of(
                "orderId", orderId,
                "logisticsNo", "SF" + UUID.randomUUID().toString().substring(0, 8),
                "logisticsStatus", "运输中"
        ));
    }

    private String string(Map<String, Object> args, String name) {
        Object value = args == null ? null : args.get(name);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
