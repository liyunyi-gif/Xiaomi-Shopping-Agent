package com.xiaomi.shopping.agent.orchestrator.service;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.common.contract.ShoppingRequest;
import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import com.xiaomi.shopping.agent.orchestrator.intent.IntentRecognizer;
import com.xiaomi.shopping.agent.orchestrator.judge.RetrievalLoop;
import com.xiaomi.shopping.agent.orchestrator.judge.RetrievalLoopResult;
import com.xiaomi.shopping.agent.orchestrator.memory.MessageArchiveService;
import com.xiaomi.shopping.agent.orchestrator.memory.SessionSnapshotBuilder;
import com.xiaomi.shopping.agent.orchestrator.memory.ShortTermMemoryService;
import com.xiaomi.shopping.agent.orchestrator.response.AnswerComposer;
import com.xiaomi.shopping.agent.orchestrator.response.ClarificationComposer;
import com.xiaomi.shopping.agent.orchestrator.response.ShoppingReplyComposer;
import com.xiaomi.shopping.agent.orchestrator.system.SystemCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Orchestrator 主 Agent 核心服务。
 *
 * @author liyunyi
 */
@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final IntentRecognizer intentRecognizer;
    private final RetrievalLoop retrievalLoop;
    private final ShoppingGateway shoppingGateway;
    private final SessionSnapshotBuilder snapshotBuilder;
    private final AnswerComposer answerComposer;
    private final ShoppingReplyComposer shoppingReplyComposer;
    private final ClarificationComposer clarificationComposer;
    private final SystemCommandHandler systemCommandHandler;
    private final MessageArchiveService messageArchiveService;
    private final ShortTermMemoryService shortTermMemoryService;

    public OrchestratorReply handle(String userId, String conversationId, String userInput) {
        messageArchiveService.archive(userId, conversationId, "user", userInput);
        SessionSnapshot snapshot = snapshotBuilder.build(userId, conversationId, null);
        IntentResult intent = intentRecognizer.recognize(userInput, snapshot);
        OrchestratorReply reply;
        if (intent.isNeedClarify()) {
            reply = OrchestratorReply.builder()
                    .answer(clarificationComposer.intentClarification())
                    .intent(intent.getPrimaryIntent())
                    .needClarify(true)
                    .childCalls(0)
                    .build();
        } else {
            reply = switch (intent.getPrimaryIntent()) {
                case KNOWLEDGE -> handleKnowledge(userInput, snapshot, intent);
                case TOOL -> handleShopping(snapshot, intent);
                case SYSTEM -> handleSystem(userId, conversationId, intent);
            };
        }
        messageArchiveService.archive(userId, conversationId, "assistant", reply.getAnswer());
        shortTermMemoryService.append(conversationId, "user", userInput);
        shortTermMemoryService.append(conversationId, "assistant", reply.getAnswer());
        return reply;
    }

    private OrchestratorReply handleKnowledge(String userInput, SessionSnapshot snapshot, IntentResult intent) {
        if (isHybridShoppingRequest(userInput)) {
            RetrievalLoopResult result = retrievalLoop.retrieve(userInput, intent.getSecondaryIntent(), snapshot, intent.getEntities());
            String answer = result.isDegraded()
                    ? answerComposer.degraded()
                    : answerComposer.compose(result.getResponse()) + "。如果你确认这款商品和规格，我再帮你加购。";
            return OrchestratorReply.builder()
                    .answer(answer)
                    .intent(IntentResult.IntentType.KNOWLEDGE)
                    .needClarify(true)
                    .qualityLevel(result.getVerdict().getLevel())
                    .retryCount(result.getRetryCount())
                    .childCalls(result.getRetryCount() + 1)
                    .build();
        }
        RetrievalLoopResult result = retrievalLoop.retrieve(userInput, intent.getSecondaryIntent(), snapshot, intent.getEntities());
        String answer = result.isDegraded() ? answerComposer.degraded() : answerComposer.compose(result.getResponse());
        return OrchestratorReply.builder()
                .answer(answer)
                .intent(IntentResult.IntentType.KNOWLEDGE)
                .needClarify(result.isDegraded())
                .qualityLevel(result.getVerdict().getLevel())
                .retryCount(result.getRetryCount())
                .childCalls(result.getRetryCount() + 1)
                .build();
    }

    private OrchestratorReply handleShopping(SessionSnapshot snapshot, IntentResult intent) {
        ShoppingResponse response = shoppingGateway.invoke(ShoppingRequest.builder()
                .action(toShoppingAction(intent.getSecondaryIntent()))
                .slots(intent.getSlots() == null ? Map.of() : intent.getSlots())
                .snapshot(snapshot)
                .build());
        boolean needClarify = response.getStatus() == ShoppingResponse.ExecStatus.NEED_CLARIFY;
        String answer = needClarify
                ? clarificationComposer.missingSlots(response.getMissingSlots())
                : shoppingReplyComposer.compose(response);
        return OrchestratorReply.builder()
                .answer(answer)
                .intent(IntentResult.IntentType.TOOL)
                .needClarify(needClarify)
                .childCalls(1)
                .build();
    }

    private OrchestratorReply handleSystem(String userId, String conversationId, IntentResult intent) {
        return OrchestratorReply.builder()
                .answer(systemCommandHandler.handle(userId, conversationId, intent.getSecondaryIntent()))
                .intent(IntentResult.IntentType.SYSTEM)
                .childCalls(0)
                .build();
    }

    private String toShoppingAction(String secondaryIntent) {
        if ("下单".equals(secondaryIntent)) {
            return "place_order";
        }
        if ("物流查询".equals(secondaryIntent)) {
            return "query_logistics";
        }
        if ("库存查询".equals(secondaryIntent)) {
            return "query_stock";
        }
        return "add_cart";
    }

    private boolean isHybridShoppingRequest(String input) {
        return input != null && (input.contains("推荐") || input.contains("适合"))
                && (input.contains("加购") || input.contains("下单") || input.contains("购买"));
    }
}
