package com.xiaomi.shopping.agent.shopping.orchestration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shopping action/slot 规则测试。
 *
 * @author liyunyi
 */
class ShoppingActionAndSlotUtilsTest {

    @Test
    @DisplayName("MCP-001 action 别名统一映射到固定 MCP 工具")
    void shouldNormalizeActionAliases() {
        assertEquals(ShoppingAction.ADD_TO_CART, ShoppingAction.from("add_cart").orElseThrow());
        assertEquals(ShoppingAction.ADD_TO_CART, ShoppingAction.from("ADD_TO_CART").orElseThrow());
        assertEquals(ShoppingAction.ADD_TO_CART, ShoppingAction.from("add-to-cart").orElseThrow());
        assertEquals(ShoppingAction.PLACE_ORDER, ShoppingAction.from("submit_order").orElseThrow());
        assertEquals(ShoppingAction.QUERY_STOCK, ShoppingAction.from("inventory").orElseThrow());
        assertEquals(ShoppingAction.QUERY_PROMOTION, ShoppingAction.from("coupon").orElseThrow());
    }

    @Test
    @DisplayName("MCP-005 推荐/比较/付费引导类 action 识别为非 Shopping 能力")
    void shouldDetectRecommendationLikeActions() {
        assertTrue(ShoppingAction.isRecommendationLike("recommend"));
        assertTrue(ShoppingAction.isRecommendationLike("compare"));
        assertTrue(ShoppingAction.isRecommendationLike("guide_payment"));
        assertTrue(ShoppingAction.isRecommendationLike("推荐"));
    }

    @Test
    @DisplayName("CONTRACT-003 slots 支持常见别名和默认数量")
    void shouldReadSlotAliases() {
        Map<String, Object> slots = Map.of(
                "skuCode", "sku-14",
                "specText", "16GB+512GB",
                "qty", "2",
                "shippingAddress", "武汉市洪山区",
                "paymentMethod", "ONLINE"
        );

        assertEquals("sku-14", ShoppingSlotUtils.stringSlot(slots, "skuId", "skuCode"));
        assertEquals("16GB+512GB", ShoppingSlotUtils.stringSlot(slots, "spec", "specText"));
        assertEquals(2, ShoppingSlotUtils.intSlot(slots, 1, "quantity", "qty"));
        assertEquals("武汉市洪山区", ShoppingSlotUtils.stringSlot(slots, "address", "shippingAddress"));
        assertEquals("ONLINE", ShoppingSlotUtils.stringSlot(slots, "payMethod", "paymentMethod"));
        assertEquals(1, ShoppingSlotUtils.intSlot(Map.of(), 1, "quantity", "qty"));
    }
}
