package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 优惠 MCP 工具。
 *
 * @author liyunyi
 */
@Service
public class PromotionTools {

    @Tool(name = "query_promotion", description = "查询指定 SKU 或购物车的已有优惠信息")
    public ToolResult queryPromotion(Map<String, Object> args) {
        String skuCode = string(args, "skuCode", "skuId");
        return ToolResult.success(Map.of(
                "skuCode", skuCode == null ? "UNKNOWN" : skuCode,
                "promotion", "暂无优惠",
                "promotionType", "NONE"
        ));
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
