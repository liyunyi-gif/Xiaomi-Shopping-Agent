package com.xiaomi.shopping.agent.knowledge.service;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Knowledge 子节点对外能力端口适配器。
 *
 * @author liyunyi
 */
@Service
@RequiredArgsConstructor
public class KnowledgeGatewayAdapter implements KnowledgeGateway {

    private final KnowledgeService knowledgeService;

    @Override
    public KnowledgeResponse ask(KnowledgeRequest request) {
        return knowledgeService.ask(request);
    }
}
