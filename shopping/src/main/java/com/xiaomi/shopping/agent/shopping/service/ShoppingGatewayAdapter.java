package com.xiaomi.shopping.agent.shopping.service;

import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shopping 子节点对外能力端口适配器。
 *
 * @author liyunyi
 */
@Service
@RequiredArgsConstructor
public class ShoppingGatewayAdapter implements ShoppingGateway {

    private final ShoppingService shoppingService;

    @Override
    public ShoppingResponse invoke(ShoppingRequest request) {
        return shoppingService.invoke(request);
    }
}
