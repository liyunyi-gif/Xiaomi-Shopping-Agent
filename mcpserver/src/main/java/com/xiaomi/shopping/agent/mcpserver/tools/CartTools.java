package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 购物车 MCP 工具。
 *
 * @author liyunyi
 */
@Service
public class CartTools {

    @Tool(name = "add_to_cart", description = "将指定 SKU 和规格加入购物车，缺参数或缺库存时返回 NEED_CLARIFY")
    public ToolResult addToCart(Map<String, Object> args) {
        String skuId = string(args, "skuId");
        String spec = string(args, "spec");
        int quantity = integer(args, "quantity", 1);
        if (skuId == null) {
            return ToolResult.needClarify(List.of("skuId"));
        }
        if (spec == null) {
            return ToolResult.needClarify(List.of("spec"));
        }
        if (quantity <= 0) {
            return ToolResult.failed("INVALID_QUANTITY");
        }
        if (isOutOfStock(args, skuId)) {
            return ToolResult.needClarify(List.of("stock"));
        }
        return ToolResult.success(Map.of(
                "cartId", "cart-" + shortId(),
                "skuId", skuId,
                "spec", spec,
                "quantity", quantity
        ));
    }

    private boolean isOutOfStock(Map<String, Object> args, String skuId) {
        Object stock = args == null ? null : args.get("stock");
        return "OUT_OF_STOCK".equalsIgnoreCase(skuId)
                || "0".equals(skuId)
                || (stock instanceof Number number && number.intValue() <= 0);
    }

    private String string(Map<String, Object> args, String name) {
        Object value = args == null ? null : args.get(name);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private int integer(Map<String, Object> args, String name, int defaultValue) {
        Object value = args == null ? null : args.get(name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
