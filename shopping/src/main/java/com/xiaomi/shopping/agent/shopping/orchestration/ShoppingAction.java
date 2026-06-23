package com.xiaomi.shopping.agent.shopping.orchestration;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Shopping 子节点支持的确定性动作。
 *
 * @author liyunyi
 */
public enum ShoppingAction {

    ADD_TO_CART("add_to_cart"),
    PLACE_ORDER("place_order"),
    QUERY_LOGISTICS("query_logistics"),
    QUERY_STOCK("query_stock"),
    QUERY_PROMOTION("query_promotion");

    private static final Set<String> RECOMMENDATION_LIKE = Set.of(
            "recommend", "recommendation", "compare", "comparison", "guide_payment", "upsell",
            "推荐", "比较", "对比", "付款引导", "付费引导"
    );

    private final String toolName;

    ShoppingAction(String toolName) {
        this.toolName = toolName;
    }

    public String toolName() {
        return toolName;
    }

    public static Optional<ShoppingAction> from(String action) {
        if (action == null || action.isBlank()) {
            return Optional.empty();
        }
        String normalized = action.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "add_cart", "add_to_cart", "addtocart" -> Optional.of(ADD_TO_CART);
            case "place_order", "order", "submit_order" -> Optional.of(PLACE_ORDER);
            case "query_logistics", "logistics", "track_order" -> Optional.of(QUERY_LOGISTICS);
            case "query_stock", "stock", "inventory", "query_inventory" -> Optional.of(QUERY_STOCK);
            case "query_promotion", "promotion", "coupon", "query_coupon" -> Optional.of(QUERY_PROMOTION);
            default -> Optional.empty();
        };
    }

    public static boolean isRecommendationLike(String action) {
        if (action == null) {
            return false;
        }
        String normalized = action.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return RECOMMENDATION_LIKE.contains(normalized);
    }
}
