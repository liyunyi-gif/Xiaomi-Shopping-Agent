package com.xiaomi.shopping.agent.shopping.mcpclient;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 无外部 MCP/业务接口时的本地 mock 工具网关。
 *
 * @author liyunyi
 */
@Component
public class DirectMockShoppingToolGateway implements ShoppingToolGateway {

    @Override
    public ToolResult invoke(String toolName, Map<String, Object> args) {
        return switch (toolName) {
            case "add_to_cart" -> addToCart(args);
            case "place_order" -> placeOrder(args);
            case "query_logistics" -> queryLogistics(args);
            case "query_stock" -> queryStock(args);
            case "query_promotion" -> queryPromotion(args);
            default -> ToolResult.failed("MCP_TOOL_NOT_FOUND: " + toolName);
        };
    }

    private ToolResult addToCart(Map<String, Object> args) {
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
                "quantity", quantity,
                "tool", "add_to_cart"
        ));
    }

    private ToolResult placeOrder(Map<String, Object> args) {
        String address = string(args, "address");
        if (address == null) {
            return ToolResult.needClarify(List.of("address"));
        }
        Object cartItems = args == null ? null : args.get("cartItems");
        Object cartItemIds = args == null ? null : args.get("cartItemIds");
        Object cartId = args == null ? null : args.get("cartId");
        if (cartItems == null && cartItemIds == null && cartId == null) {
            return ToolResult.needClarify(List.of("cartItems"));
        }
        return ToolResult.success(Map.of(
                "orderId", "order-" + shortId(),
                "orderNo", "XM" + System.currentTimeMillis(),
                "status", "CREATED",
                "tool", "place_order"
        ));
    }

    private ToolResult queryLogistics(Map<String, Object> args) {
        String orderId = string(args, "orderId");
        if (orderId == null) {
            return ToolResult.needClarify(List.of("orderId"));
        }
        return ToolResult.success(Map.of(
                "orderId", orderId,
                "logisticsNo", "SF" + shortId(),
                "logisticsStatus", "运输中",
                "tool", "query_logistics"
        ));
    }

    private ToolResult queryStock(Map<String, Object> args) {
        String skuId = string(args, "skuId");
        if (skuId == null) {
            return ToolResult.needClarify(List.of("skuId"));
        }
        int stock = isOutOfStock(args, skuId) ? 0 : 100;
        return ToolResult.success(Map.of("skuId", skuId, "stock", stock, "tool", "query_stock"));
    }

    private ToolResult queryPromotion(Map<String, Object> args) {
        String skuId = string(args, "skuId");
        return ToolResult.success(Map.of(
                "skuId", skuId == null ? "UNKNOWN" : skuId,
                "promotion", "暂无优惠",
                "tool", "query_promotion"
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
