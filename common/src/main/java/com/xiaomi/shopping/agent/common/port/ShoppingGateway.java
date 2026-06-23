package com.xiaomi.shopping.agent.common.port;

import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;

/**
 * Shopping 子节点能力端口。
 *
 * @author liyunyi
 */
public interface ShoppingGateway {

    ShoppingResponse invoke(ShoppingRequest request);
}
