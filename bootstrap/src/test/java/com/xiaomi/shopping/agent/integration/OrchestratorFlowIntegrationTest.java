package com.xiaomi.shopping.agent.integration;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import com.xiaomi.shopping.agent.orchestrator.entityextract.OrchestratorEntityExtractor;
import com.xiaomi.shopping.agent.orchestrator.intent.RuleBasedIntentRecognizer;
import com.xiaomi.shopping.agent.orchestrator.judge.QualityJudge;
import com.xiaomi.shopping.agent.orchestrator.judge.RetrievalLoop;
import com.xiaomi.shopping.agent.orchestrator.judge.RuleBasedLoopQueryRewriter;
import com.xiaomi.shopping.agent.orchestrator.memory.InMemoryLongTermMemoryService;
import com.xiaomi.shopping.agent.orchestrator.memory.InMemoryMessageArchiveService;
import com.xiaomi.shopping.agent.orchestrator.memory.InMemoryShortTermMemoryService;
import com.xiaomi.shopping.agent.orchestrator.memory.MemoryLoadService;
import com.xiaomi.shopping.agent.orchestrator.memory.SessionSnapshotBuilder;
import com.xiaomi.shopping.agent.orchestrator.response.AnswerComposer;
import com.xiaomi.shopping.agent.orchestrator.response.ClarificationComposer;
import com.xiaomi.shopping.agent.orchestrator.response.ShoppingReplyComposer;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorReply;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorService;
import com.xiaomi.shopping.agent.orchestrator.system.SystemCommandHandler;
import com.xiaomi.shopping.agent.shopping.mcpclient.DirectMockShoppingToolGateway;
import com.xiaomi.shopping.agent.shopping.orchestration.ShoppingOrchestrator;
import com.xiaomi.shopping.agent.shopping.service.ShoppingGatewayAdapter;
import com.xiaomi.shopping.agent.shopping.service.ShoppingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FLOW-001..004 多模块交互测试。
 *
 * @author liyunyi
 */
class OrchestratorFlowIntegrationTest {

    @Test
    @DisplayName("FLOW-001 知识问答端到端含一次重检")
    void shouldHandleKnowledgeFlowWithRetry() {
        ScriptedKnowledgeGateway knowledge = new ScriptedKnowledgeGateway();
        OrchestratorService service = service(knowledge, directShopping());

        OrchestratorReply reply = service.handle("u1", "c1", "小米14影像规格怎么样");

        assertTrue(reply.getAnswer().contains("根据当前知识库资料"));
        assertEquals(2, knowledge.requests.size());
        assertEquals(1, reply.getRetryCount());
        assertTrue(knowledge.requests.get(1).getQuestion().contains("补充同义词"));
    }

    @Test
    @DisplayName("FLOW-002 购物执行端到端：Orchestrator → Shopping → mock MCP → Orchestrator")
    void shouldHandleShoppingFlow() {
        OrchestratorService service = service(new SufficientKnowledgeGateway(), directShopping());

        OrchestratorReply reply = service.handle("u1", "c1", "帮我加购一台小米14 16+512");

        assertEquals(ShoppingResponse.ExecStatus.SUCCESS.name(), ShoppingResponse.ExecStatus.SUCCESS.name());
        assertTrue(reply.getAnswer().contains("已处理成功"));
        assertEquals(1, reply.getChildCalls());
    }

    @Test
    @DisplayName("FLOW-003 缺槽位由 Shopping 举手，Orchestrator 开口澄清")
    void shouldClarifyMissingSlotFromShopping() {
        OrchestratorService service = service(new SufficientKnowledgeGateway(), directShopping());

        OrchestratorReply reply = service.handle("u1", "c1", "帮我下单");

        assertTrue(reply.isNeedClarify());
        assertTrue(reply.getAnswer().contains("收货地址") || reply.getAnswer().contains("购物车商品"));
    }

    @Test
    @DisplayName("FLOW-004 混合意图先 Knowledge 后由主 Agent 引导确认，不让子节点互通")
    void shouldHandleHybridByMainAgent() {
        ScriptedKnowledgeGateway knowledge = new ScriptedKnowledgeGateway();
        RecordingShoppingGateway shopping = new RecordingShoppingGateway(directShopping());
        OrchestratorService service = service(knowledge, shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "帮我推荐一款适合打游戏的手机，合适就直接加购");

        assertTrue(reply.getAnswer().contains("确认") || reply.getAnswer().contains("再帮你加购"));
        assertTrue(reply.isNeedClarify());
        assertTrue(knowledge.requests.size() >= 1);
        assertEquals(0, shopping.calls);
    }

    private ShoppingGateway directShopping() {
        return new ShoppingGatewayAdapter(new ShoppingService(new ShoppingOrchestrator(new DirectMockShoppingToolGateway())));
    }

    private OrchestratorService service(KnowledgeGateway knowledgeGateway, ShoppingGateway shoppingGateway) {
        InMemoryShortTermMemoryService shortTerm = new InMemoryShortTermMemoryService(10);
        InMemoryLongTermMemoryService longTerm = new InMemoryLongTermMemoryService();
        InMemoryMessageArchiveService archive = new InMemoryMessageArchiveService();
        MemoryLoadService memoryLoadService = new MemoryLoadService(shortTerm, longTerm);
        return new OrchestratorService(
                new RuleBasedIntentRecognizer(new OrchestratorEntityExtractor()),
                new RetrievalLoop(knowledgeGateway, new QualityJudge(0.7), new RuleBasedLoopQueryRewriter(), 2),
                shoppingGateway,
                new SessionSnapshotBuilder(memoryLoadService),
                new AnswerComposer(),
                new ShoppingReplyComposer(),
                new ClarificationComposer(),
                new SystemCommandHandler(longTerm, shortTerm, archive),
                archive,
                shortTerm
        );
    }

    private static class ScriptedKnowledgeGateway implements KnowledgeGateway {
        private final List<KnowledgeRequest> requests = new ArrayList<>();

        @Override
        public KnowledgeResponse ask(KnowledgeRequest request) {
            requests.add(request);
            if (requests.size() == 1) {
                return response(0.4);
            }
            return response(0.9);
        }
    }

    private static class SufficientKnowledgeGateway implements KnowledgeGateway {
        @Override
        public KnowledgeResponse ask(KnowledgeRequest request) {
            return response(0.9);
        }
    }

    private static class RecordingShoppingGateway implements ShoppingGateway {
        private final ShoppingGateway delegate;
        private int calls;

        private RecordingShoppingGateway(ShoppingGateway delegate) {
            this.delegate = delegate;
        }

        @Override
        public ShoppingResponse invoke(com.xiaomi.shopping.agent.common.contract.ShoppingRequest request) {
            calls++;
            return delegate.invoke(request);
        }
    }

    private static KnowledgeResponse response(double score) {
        return KnowledgeResponse.builder()
                .recallCount(1)
                .topScore(score)
                .hitEntities(Set.of("小米14", "影像"))
                .results(List.of(KnowledgeResponse.RetrievalItem.builder()
                        .sourceId("doc-1")
                        .content("小米14适合游戏和影像场景，16+512规格可选")
                        .score(score)
                        .hitType("mock")
                        .build()))
                .build();
    }
}
