package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 库存 MCP 工具。
 *
 * @author liyunyi
 */
@Service
public class StockTools {

    @Tool(name = "query_stock", description = "查询指定 SKU 库存")
    public ToolResult queryStock(Map<String, Object> args) {
        String skuId = string(args, "skuId");
        if (skuId == null) {
            return ToolResult.needClarify(List.of("skuId"));
        }
        int stock = "OUT_OF_STOCK".equalsIgnoreCase(skuId) || "0".equals(skuId) ? 0 : 100;
        return ToolResult.success(Map.of("skuId", skuId, "stock", stock));
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
