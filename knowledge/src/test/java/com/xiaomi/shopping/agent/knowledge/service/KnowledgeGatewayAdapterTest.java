package com.xiaomi.shopping.agent.knowledge.service;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * KnowledgeGatewayAdapter 测试。
 *
 * @author liyunyi
 */
class KnowledgeGatewayAdapterTest {

    @Test
    @DisplayName("KnowledgeGatewayAdapter 委托 KnowledgeService")
    void shouldDelegateToKnowledgeService() {
        KnowledgeService service = new KnowledgeService(null, null, null, null, null) {
            @Override
            public KnowledgeResponse ask(KnowledgeRequest request) {
                return KnowledgeResponse.builder()
                        .recallCount(1)
                        .topScore(0.9)
                        .hitEntities(Set.of("小米14"))
                        .results(List.of())
                        .build();
            }
        };
        KnowledgeGatewayAdapter adapter = new KnowledgeGatewayAdapter(service);

        KnowledgeResponse response = adapter.ask(KnowledgeRequest.builder().question("小米14").build());

        assertEquals(1, response.getRecallCount());
        assertEquals(0.9, response.getTopScore());
    }
}
