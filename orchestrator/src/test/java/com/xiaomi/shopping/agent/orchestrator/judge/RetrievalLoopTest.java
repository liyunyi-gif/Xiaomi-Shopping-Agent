package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUDGE-005..007 重检循环测试。
 *
 * @author liyunyi
 */
class RetrievalLoopTest {

    @Test
    @DisplayName("JUDGE-005 不够→换策略重检→充分")
    void shouldRetryWithNewStrategyUntilSufficient() {
        ScriptedKnowledgeGateway gateway = new ScriptedKnowledgeGateway(List.of(
                resp(3, 0.4, Set.of("小米14")),
                resp(3, 0.9, Set.of("小米14"))
        ));
        RetrievalLoop loop = new RetrievalLoop(gateway, new QualityJudge(0.7), new RuleBasedLoopQueryRewriter(), 2);

        RetrievalLoopResult result = loop.retrieve("小米14影像规格怎么样", "参数咨询", null, Set.of("小米14"));

        assertEquals(QualityVerdict.Level.SUFFICIENT, result.getVerdict().getLevel());
        assertEquals(1, result.getRetryCount());
        assertEquals(2, gateway.requests.size());
        assertEquals(1, gateway.requests.get(1).getRetryAttempt());
        assertNotEquals(gateway.requests.get(0).getQuestion(), gateway.requests.get(1).getQuestion());
    }

    @Test
    @DisplayName("JUDGE-006 达上限仍不够则退化")
    void shouldDegradeWhenMaxRetriesReached() {
        ScriptedKnowledgeGateway gateway = new ScriptedKnowledgeGateway(List.of(
                resp(1, 0.2, Set.of("小米14")),
                resp(1, 0.3, Set.of("小米14")),
                resp(1, 0.4, Set.of("小米14"))
        ));
        RetrievalLoop loop = new RetrievalLoop(gateway, new QualityJudge(0.7), new RuleBasedLoopQueryRewriter(), 2);

        RetrievalLoopResult result = loop.retrieve("小米14影像规格怎么样", "参数咨询", null, Set.of("小米14"));

        assertTrue(result.isDegraded());
        assertEquals(2, result.getRetryCount());
        assertEquals(3, gateway.requests.size());
    }

    private KnowledgeResponse resp(int recall, double topScore, Set<String> hitEntities) {
        return KnowledgeResponse.builder()
                .recallCount(recall)
                .topScore(topScore)
                .hitEntities(hitEntities)
                .results(List.of(KnowledgeResponse.RetrievalItem.builder()
                        .sourceId("doc")
                        .content("小米14影像资料")
                        .score(topScore)
                        .hitType("semantic")
                        .build()))
                .build();
    }

    private static class ScriptedKnowledgeGateway implements KnowledgeGateway {
        private final List<KnowledgeResponse> responses;
        private final List<KnowledgeRequest> requests = new ArrayList<>();

        private ScriptedKnowledgeGateway(List<KnowledgeResponse> responses) {
            this.responses = responses;
        }

        @Override
        public KnowledgeResponse ask(KnowledgeRequest request) {
            requests.add(request);
            return responses.get(Math.min(requests.size() - 1, responses.size() - 1));
        }
    }
}
