package com.xiaomi.shopping.agent.shopping.service;

import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ShoppingGatewayAdapter 测试。
 *
 * @author liyunyi
 */
class ShoppingGatewayAdapterTest {

    @Test
    @DisplayName("ShoppingGatewayAdapter 委托 ShoppingService")
    void shouldDelegateToShoppingService() {
        ShoppingService service = new ShoppingService(null) {
            @Override
            public ShoppingResponse invoke(ShoppingRequest request) {
                return ShoppingResponse.builder()
                        .status(ShoppingResponse.ExecStatus.SUCCESS)
                        .resultData(java.util.Map.of("delegated", true))
                        .build();
            }
        };
        ShoppingGatewayAdapter adapter = new ShoppingGatewayAdapter(service);

        ShoppingResponse response = adapter.invoke(ShoppingRequest.builder().action("add_cart").build());

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS, response.getStatus());
        assertEquals(true, response.getResultData().get("delegated"));
    }
}
