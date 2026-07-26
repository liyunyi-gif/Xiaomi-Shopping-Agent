package com.xiaomi.shopping.agent.orchestrator.service;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import com.xiaomi.shopping.agent.orchestrator.entityextract.OrchestratorEntityExtractor;
import com.xiaomi.shopping.agent.orchestrator.intent.IntentRecognizer;
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
import com.xiaomi.shopping.agent.orchestrator.system.SystemCommandHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Orchestrator 主服务编排测试。
 *
 * @author liyunyi
 */
class OrchestratorServiceTest {

    @Test
    @DisplayName("低置信意图由主 Agent 澄清且不调用子节点")
    void shouldClarifyAmbiguousIntentWithoutChildCall() {
        CountingKnowledgeGateway knowledge = new CountingKnowledgeGateway();
        CountingShoppingGateway shopping = new CountingShoppingGateway(ShoppingResponse.ExecStatus.SUCCESS);
        OrchestratorService service = service(knowledge, shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "手机");

        assertTrue(reply.isNeedClarify());
        assertEquals(0, reply.getChildCalls());
        assertEquals(0, knowledge.calls);
        assertEquals(0, shopping.calls);
    }

    @Test
    @DisplayName("知识问答由 Orchestrator 调用 Knowledge 并组装回答")
    void shouldHandleKnowledgeFlow() {
        CountingKnowledgeGateway knowledge = new CountingKnowledgeGateway();
        OrchestratorService service = service(knowledge, new CountingShoppingGateway(ShoppingResponse.ExecStatus.SUCCESS));

        OrchestratorReply reply = service.handle("u1", "c1", "小米14影像规格怎么样");

        assertTrue(reply.getAnswer().contains("根据当前知识库资料"));
        assertEquals(1, knowledge.calls);
        assertEquals(1, reply.getChildCalls());
    }

    @Test
    @DisplayName("购物举手由 Orchestrator 开口澄清")
    void shouldAskClarificationWhenShoppingNeedsSlots() {
        CountingShoppingGateway shopping = new CountingShoppingGateway(ShoppingResponse.ExecStatus.NEED_CLARIFY);
        OrchestratorService service = service(new CountingKnowledgeGateway(), shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "帮我下单");

        assertTrue(reply.isNeedClarify());
        assertTrue(reply.getAnswer().contains("收货地址"));
        assertEquals(1, shopping.calls);
    }

    @Test
    @DisplayName("系统指令由 Orchestrator 自处理")
    void shouldHandleSystemCommand() {
        OrchestratorService service = service(new CountingKnowledgeGateway(), new CountingShoppingGateway(ShoppingResponse.ExecStatus.SUCCESS));

        OrchestratorReply reply = service.handle("u1", "c1", "清除我的记忆");

        assertTrue(reply.getAnswer().contains("已清除"));
        assertEquals(0, reply.getChildCalls());
    }

    @Test
    @DisplayName("未知购物动作不得默认降级为加购")
    void shouldRejectUnsupportedShoppingActionWithoutChildCall() {
        IntentRecognizer recognizer = (input, snapshot) -> IntentResult.builder()
                .primaryIntent(IntentResult.IntentType.SHOPPING)
                .secondaryIntent("unsupported-action")
                .needClarify(false)
                .entities(Set.of())
                .slots(Map.of())
                .build();
        CountingShoppingGateway shopping = new CountingShoppingGateway(ShoppingResponse.ExecStatus.SUCCESS);
        OrchestratorService service = service(recognizer, new CountingKnowledgeGateway(), shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "执行未知动作");

        assertTrue(reply.isNeedClarify());
        assertEquals("UNSUPPORTED_SHOPPING_ACTION", reply.getData().get("reason"));
        assertEquals(0, shopping.calls);
        assertEquals(0, reply.getChildCalls());
    }

    @Test
    @DisplayName("Shopping 返回空状态时仍返回结构化降级响应")
    void shouldHandleNullShoppingStatusWithoutException() {
        CountingShoppingGateway shopping = new CountingShoppingGateway(null);
        OrchestratorService service = service(new CountingKnowledgeGateway(), shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "帮我下单");

        assertTrue(reply.getAnswer().contains("有效状态"));
        assertNull(reply.getData().get("status"));
        assertEquals(1, shopping.calls);
    }

    @Test
    @DisplayName("Shopping Gateway 返回 null 时安全降级")
    void shouldHandleEmptyShoppingResponseWithoutException() {
        ShoppingGateway shopping = request -> null;
        OrchestratorService service = service(new CountingKnowledgeGateway(), shopping);

        OrchestratorReply reply = service.handle("u1", "c1", "帮我下单");

        assertTrue(reply.getAnswer().contains("没有返回结果"));
        assertEquals("EMPTY_SHOPPING_RESPONSE", reply.getData().get("reason"));
    }

    @Test
    @DisplayName("促销咨询路由到 query_promotion")
    void shouldRoutePromotionIntentToPromotionTool() {
        IntentRecognizer recognizer = (input, snapshot) -> IntentResult.builder()
                .primaryIntent(IntentResult.IntentType.SHOPPING)
                .secondaryIntent("促销咨询")
                .needClarify(false)
                .entities(Set.of())
                .slots(Map.of())
                .build();
        CountingShoppingGateway shopping = new CountingShoppingGateway(ShoppingResponse.ExecStatus.SUCCESS);
        OrchestratorService service = service(recognizer, new CountingKnowledgeGateway(), shopping);

        service.handle("u1", "c1", "查询优惠");

        assertEquals("query_promotion", shopping.lastAction);
        assertEquals(1, shopping.calls);
    }

    private OrchestratorService service(KnowledgeGateway knowledgeGateway, ShoppingGateway shoppingGateway) {
        return service(new RuleBasedIntentRecognizer(new OrchestratorEntityExtractor()), knowledgeGateway, shoppingGateway);
    }

    private OrchestratorService service(IntentRecognizer recognizer,
                                        KnowledgeGateway knowledgeGateway,
                                        ShoppingGateway shoppingGateway) {
        InMemoryShortTermMemoryService shortTerm = new InMemoryShortTermMemoryService(10);
        InMemoryLongTermMemoryService longTerm = new InMemoryLongTermMemoryService();
        InMemoryMessageArchiveService archive = new InMemoryMessageArchiveService();
        MemoryLoadService memoryLoadService = new MemoryLoadService(shortTerm, longTerm);
        return new OrchestratorService(
                recognizer,
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

    private static class CountingKnowledgeGateway implements KnowledgeGateway {
        private int calls;

        @Override
        public KnowledgeResponse ask(KnowledgeRequest request) {
            calls++;
            return KnowledgeResponse.builder()
                    .recallCount(1)
                    .topScore(0.9)
                    .hitEntities(request.getQueryEntities() == null ? Set.of() : request.getQueryEntities())
                    .results(List.of(KnowledgeResponse.RetrievalItem.builder()
                            .sourceId("doc-1")
                            .content("小米14影像规格包含高像素主摄和夜景能力")
                            .score(0.9)
                            .hitType("mock")
                            .build()))
                    .build();
        }
    }

    private static class CountingShoppingGateway implements ShoppingGateway {
        private final ShoppingResponse.ExecStatus status;
        private int calls;
        private String lastAction;

        private CountingShoppingGateway(ShoppingResponse.ExecStatus status) {
            this.status = status;
        }

        @Override
        public ShoppingResponse invoke(ShoppingRequest request) {
            calls++;
            lastAction = request.getAction();
            if (status == ShoppingResponse.ExecStatus.NEED_CLARIFY) {
                return ShoppingResponse.builder()
                        .status(status)
                        .resultData(Map.of())
                        .missingSlots(List.of("address"))
                        .build();
            }
            return ShoppingResponse.builder()
                    .status(status)
                    .resultData(Map.of("cartId", "cart-001"))
                    .missingSlots(List.of())
                    .build();
        }
    }
}
