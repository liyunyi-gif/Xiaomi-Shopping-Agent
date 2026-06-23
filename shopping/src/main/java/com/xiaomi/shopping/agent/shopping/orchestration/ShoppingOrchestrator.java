package com.xiaomi.shopping.agent.shopping.orchestration;

import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.shopping.mcpclient.ShoppingToolGateway;
import com.xiaomi.shopping.agent.shopping.mcpclient.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping 子节点确定性工具编排器。
 *
 * @author liyunyi
 */
@Component
@RequiredArgsConstructor
public class ShoppingOrchestrator {

    private final ShoppingToolGateway toolGateway;

    public ShoppingResponse orchestrate(ShoppingRequest request) {
        if (request == null || request.getAction() == null || request.getAction().isBlank()) {
            return needClarify(List.of("action"));
        }
        if (ShoppingAction.isRecommendationLike(request.getAction())) {
            return failed("UNSUPPORTED_ACTION");
        }
        return ShoppingAction.from(request.getAction())
                .map(action -> invokeAction(action, request))
                .orElseGet(() -> failed("UNSUPPORTED_ACTION"));
    }

    private ShoppingResponse invokeAction(ShoppingAction action, ShoppingRequest request) {
        Map<String, Object> args = buildArgs(action, request);
        ToolResult result = toolGateway.invoke(action.toolName(), args);
        return toResponse(result);
    }

    private Map<String, Object> buildArgs(ShoppingAction action, ShoppingRequest request) {
        Map<String, Object> slots = request.getSlots() == null ? Map.of() : request.getSlots();
        SessionSnapshot snapshot = request.getSnapshot();
        Map<String, Object> args = new HashMap<>();

        switch (action) {
            case ADD_TO_CART -> {
                putIfPresent(args, "skuId", ShoppingSlotUtils.stringSlot(slots, "skuId", "skuCode"));
                putIfPresent(args, "spec", ShoppingSlotUtils.stringSlot(slots, "spec", "specText"));
                args.put("quantity", ShoppingSlotUtils.intSlot(slots, 1, "quantity", "qty"));
                putIfPresent(args, "stock", ShoppingSlotUtils.first(slots, "stock"));
            }
            case PLACE_ORDER -> {
                putIfPresent(args, "address", ShoppingSlotUtils.stringSlot(slots, "address", "shippingAddress"));
                putIfPresent(args, "payMethod", ShoppingSlotUtils.stringSlot(slots, "payMethod", "paymentMethod"));
                putIfPresent(args, "cartId", ShoppingSlotUtils.stringSlot(slots, "cartId"));
                putIfPresent(args, "cartItems", ShoppingSlotUtils.first(slots, "cartItems"));
                putIfPresent(args, "cartItemIds", ShoppingSlotUtils.first(slots, "cartItemIds"));
                if (snapshot != null) {
                    putIfPresent(args, "userId", snapshot.getUserId());
                    if (!args.containsKey("cartItems")) {
                        putIfPresent(args, "cartItems", snapshot.getCartState());
                    }
                }
            }
            case QUERY_LOGISTICS -> putIfPresent(args, "orderId", ShoppingSlotUtils.stringSlot(slots, "orderId", "orderNo"));
            case QUERY_STOCK -> {
                putIfPresent(args, "skuId", ShoppingSlotUtils.stringSlot(slots, "skuId", "skuCode"));
                putIfPresent(args, "stock", ShoppingSlotUtils.first(slots, "stock"));
            }
            case QUERY_PROMOTION -> {
                putIfPresent(args, "skuId", ShoppingSlotUtils.stringSlot(slots, "skuId", "skuCode"));
                putIfPresent(args, "cartItems", ShoppingSlotUtils.first(slots, "cartItems"));
                if (snapshot != null && !args.containsKey("cartItems")) {
                    putIfPresent(args, "cartItems", snapshot.getCartState());
                }
            }
        }
        return args;
    }

    private ShoppingResponse toResponse(ToolResult result) {
        if (result == null) {
            return failed("EMPTY_TOOL_RESULT");
        }
        return ShoppingResponse.builder()
                .status(result.getStatus())
                .resultData(result.getData() == null ? Map.of() : result.getData())
                .missingSlots(result.getMissingSlots() == null ? List.of() : result.getMissingSlots())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    private ShoppingResponse needClarify(List<String> missingSlots) {
        return ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.NEED_CLARIFY)
                .resultData(Map.of())
                .missingSlots(missingSlots)
                .build();
    }

    private ShoppingResponse failed(String errorMessage) {
        return ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.FAILED)
                .resultData(Map.of())
                .missingSlots(List.of())
                .errorMessage(errorMessage)
                .build();
    }

    private void putIfPresent(Map<String, Object> args, String key, Object value) {
        if (value != null) {
            args.put(key, value);
        }
    }
}
