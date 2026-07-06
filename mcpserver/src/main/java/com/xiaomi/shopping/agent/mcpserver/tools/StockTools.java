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
        String skuCode = string(args, "skuCode", "skuId");
        if (skuCode == null) {
            return ToolResult.needClarify(List.of("skuCode"));
        }
        int stock = "OUT_OF_STOCK".equalsIgnoreCase(skuCode) || "0".equals(skuCode) ? 0 : 100;
        return ToolResult.success(Map.of("skuCode", skuCode, "stock", stock));
    }

    private String string(Map<String, Object> args, String... names) {
        if (args == null) {
            return null;
        }
        for (String name : names) {
            Object value = args.get(name);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }
}
