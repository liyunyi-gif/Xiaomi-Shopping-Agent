package com.xiaomi.shopping.agent.shopping.service;

import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.shopping.orchestration.ShoppingOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shopping 子节点入口。
 *
 * @author liyunyi
 */
@Service
@RequiredArgsConstructor
public class ShoppingService {

    private final ShoppingOrchestrator orchestrator;

    public ShoppingResponse invoke(ShoppingRequest request) {
        return orchestrator.orchestrate(request);
    }
}
