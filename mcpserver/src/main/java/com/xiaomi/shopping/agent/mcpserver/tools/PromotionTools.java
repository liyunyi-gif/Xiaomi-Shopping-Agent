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
        String skuId = string(args, "skuId");
        return ToolResult.success(Map.of(
                "skuId", skuId == null ? "UNKNOWN" : skuId,
                "promotion", "暂无优惠",
                "promotionType", "NONE"
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
