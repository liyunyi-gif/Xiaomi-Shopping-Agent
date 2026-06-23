package com.xiaomi.shopping.agent.shopping.service;

import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.shopping.orchestration.ShoppingOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ShoppingService 子节点入口测试。
 *
 * @author liyunyi
 */
class ShoppingServiceTest {

    @Test
    @DisplayName("FLOW-002 ShoppingService 只委托确定性编排并返回结构化结果")
    void shouldDelegateToOrchestrator() {
        ShoppingOrchestrator orchestrator = mock(ShoppingOrchestrator.class);
        ShoppingService service = new ShoppingService(orchestrator);
        ShoppingRequest request = ShoppingRequest.builder()
                .action("add_cart")
                .slots(Map.of("skuId", "sku-14", "spec", "16GB+512GB"))
                .build();
        ShoppingResponse expected = ShoppingResponse.builder()
                .status(ShoppingResponse.ExecStatus.SUCCESS)
                .resultData(Map.of("cartId", "cart-001"))
                .build();
        when(orchestrator.orchestrate(request)).thenReturn(expected);

        ShoppingResponse actual = service.invoke(request);

        assertEquals(expected, actual);
        verify(orchestrator, times(1)).orchestrate(request);
    }
}
