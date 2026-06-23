package com.xiaomi.shopping.agent.orchestrator.response;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 澄清问题组装器。
 *
 * @author liyunyi
 */
@Component
public class ClarificationComposer {

    private static final Map<String, String> SLOT_NAMES = Map.of(
            "action", "你想执行的购物操作",
            "skuId", "商品型号",
            "spec", "商品规格",
            "stock", "库存/可购买商品",
            "address", "收货地址",
            "cartItems", "购物车商品",
            "orderId", "订单号"
    );

    public String intentClarification() {
        return "你是想了解商品信息，还是想进行加购、下单、查物流等购物操作？";
    }

    public String missingSlots(List<String> missingSlots) {
        List<String> slots = missingSlots == null ? List.of() : missingSlots;
        if (slots.isEmpty()) {
            return "还需要补充必要信息后才能继续。";
        }
        String names = slots.stream().map(slot -> SLOT_NAMES.getOrDefault(slot, slot)).toList().toString();
        return "还需要补充" + names + "，请告诉我后我再继续处理。";
    }
}
