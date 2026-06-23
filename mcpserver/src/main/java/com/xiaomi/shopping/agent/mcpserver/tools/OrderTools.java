package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 下单 MCP 工具。
 *
 * @author liyunyi
 */
@Service
public class OrderTools {

    @Tool(name = "place_order", description = "基于购物车项和收货地址创建订单")
    public ToolResult placeOrder(Map<String, Object> args) {
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
        String orderId = "order-" + shortId();
        return ToolResult.success(Map.of(
                "orderId", orderId,
                "orderNo", "XM" + shortId().toUpperCase(),
                "status", "CREATED"
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

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
