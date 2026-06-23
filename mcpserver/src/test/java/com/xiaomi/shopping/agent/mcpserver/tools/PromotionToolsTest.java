package com.xiaomi.shopping.agent.mcpserver.tools;

import com.xiaomi.shopping.agent.mcpserver.model.ToolResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PromotionTools 非联调测试。
 *
 * @author liyunyi
 */
class PromotionToolsTest {

    private final PromotionTools tools = new PromotionTools();

    @Test
    @DisplayName("query_promotion 只返回优惠查询结果，不做付费引导")
    void shouldQueryPromotionWithoutUpsell() {
        ToolResult result = tools.queryPromotion(Map.of("skuId", "sku-14"));

        assertEquals(ToolResult.Status.SUCCESS, result.getStatus());
        assertEquals("sku-14", result.getData().get("skuId"));
        assertEquals("NONE", result.getData().get("promotionType"));
        assertEquals("暂无优惠", result.getData().get("promotion"));
    }
}
