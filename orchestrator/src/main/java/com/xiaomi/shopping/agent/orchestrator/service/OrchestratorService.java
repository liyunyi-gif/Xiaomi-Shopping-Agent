package com.xiaomi.shopping.agent.orchestrator.service;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        String traceId = "trace-" + UUID.randomUUID();
        messageArchiveService.archive(userId, conversationId, "user", userInput);
        SessionSnapshot snapshot = snapshotBuilder.build(userId, conversationId, null);
        IntentResult intent = intentRecognizer.recognize(userInput, snapshot);
        OrchestratorReply reply;
        if (intent.isNeedClarify()) {
            String question = clarificationComposer.intentClarification();
            reply = OrchestratorReply.builder()
                    .answer(question)
                    .intent(intent.getPrimaryIntent())
                    .needClarify(true)
                    .clarifyQuestion(question)
                    .childCalls(0)
                    .data(Map.of("reason", "LOW_CONFIDENCE_OR_AMBIGUOUS_INTENT"))
                    .traceId(traceId)
                    .build();
        } else {
            reply = switch (intent.getPrimaryIntent()) {
                case KNOWLEDGE -> handleKnowledge(userInput, snapshot, intent, traceId);
                case SHOPPING -> handleShopping(snapshot, intent, traceId);
                case SYSTEM -> handleSystem(userId, conversationId, intent, traceId);
            };
        }
        messageArchiveService.archive(userId, conversationId, "assistant", reply.getAnswer());
        shortTermMemoryService.append(conversationId, "user", userInput);
        shortTermMemoryService.append(conversationId, "assistant", reply.getAnswer());
        return reply;
    }

    private OrchestratorReply handleKnowledge(String userInput, SessionSnapshot snapshot, IntentResult intent, String traceId) {
        if (isHybridShoppingRequest(userInput)) {
            RetrievalLoopResult result = retrievalLoop.retrieve(userInput, intent.getSecondaryIntent(), snapshot, intent.getEntities());
            String question = "如果你确认这款商品和规格，我再帮你加购。";
            String answer = result.isDegraded()
                    ? answerComposer.degraded()
                    : answerComposer.compose(result.getResponse()) + "。" + question;
            return OrchestratorReply.builder()
                    .answer(answer)
                    .intent(IntentResult.IntentType.KNOWLEDGE)
                    .needClarify(true)
                    .clarifyQuestion(question)
                    .qualityLevel(result.getVerdict().getLevel())
                    .retryCount(result.getRetryCount())
                    .childCalls(result.getRetryCount() + 1)
                    .data(knowledgeData(result))
                    .traceId(traceId)
                    .build();
        }
        RetrievalLoopResult result = retrievalLoop.retrieve(userInput, intent.getSecondaryIntent(), snapshot, intent.getEntities());
        String answer = result.isDegraded() ? answerComposer.degraded() : answerComposer.compose(result.getResponse());
        return OrchestratorReply.builder()
                .answer(answer)
                .intent(IntentResult.IntentType.KNOWLEDGE)
                .needClarify(result.isDegraded())
                .clarifyQuestion(result.isDegraded() ? answer : null)
                .qualityLevel(result.getVerdict().getLevel())
                .retryCount(result.getRetryCount())
                .childCalls(result.getRetryCount() + 1)
                .data(knowledgeData(result))
                .traceId(traceId)
                .build();
    }

    private OrchestratorReply handleShopping(SessionSnapshot snapshot, IntentResult intent, String traceId) {
        String action = toShoppingAction(intent.getSecondaryIntent());
        if (action == null) {
            String question = clarificationComposer.intentClarification();
            return OrchestratorReply.builder()
                    .answer(question)
                    .intent(IntentResult.IntentType.SHOPPING)
                    .needClarify(true)
                    .clarifyQuestion(question)
                    .childCalls(0)
                    .data(Map.of(
                            "reason", "UNSUPPORTED_SHOPPING_ACTION",
                            "secondaryIntent", String.valueOf(intent.getSecondaryIntent())
                    ))
                    .traceId(traceId)
                    .build();
        }
        ShoppingResponse response = shoppingGateway.invoke(ShoppingRequest.builder()
                .action(action)
                .slots(intent.getSlots() == null ? Map.of() : intent.getSlots())
                .snapshot(snapshot)
                .build());
        boolean needClarify = response != null && response.getStatus() == ShoppingResponse.ExecStatus.NEED_CLARIFY;
        String answer = response == null
                ? shoppingReplyComposer.compose(null)
                : needClarify
                ? clarificationComposer.missingSlots(response.getMissingSlots())
                : shoppingReplyComposer.compose(response);
        return OrchestratorReply.builder()
                .answer(answer)
                .intent(IntentResult.IntentType.SHOPPING)
                .needClarify(needClarify)
                .clarifyQuestion(needClarify ? answer : null)
                .childCalls(1)
                .data(shoppingData(response))
                .traceId(traceId)
                .build();
    }

    private OrchestratorReply handleSystem(String userId, String conversationId, IntentResult intent, String traceId) {
        return OrchestratorReply.builder()
                .answer(systemCommandHandler.handle(userId, conversationId, intent.getSecondaryIntent()))
                .intent(IntentResult.IntentType.SYSTEM)
                .childCalls(0)
                .data(systemData(intent.getSecondaryIntent()))
                .traceId(traceId)
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
        if ("加购".equals(secondaryIntent)) {
            return "add_to_cart";
        }
        if ("促销咨询".equals(secondaryIntent)) {
            return "query_promotion";
        }
        return null;
    }

    private Map<String, Object> knowledgeData(RetrievalLoopResult result) {
        KnowledgeResponse response = result.getResponse();
        if (response == null) {
            return Map.of("references", List.of(), "degraded", result.isDegraded());
        }
        List<Map<String, Object>> references = response.getResults() == null ? List.of() : response.getResults().stream()
                .filter(item -> item != null)
                .map(item -> {
                    Map<String, Object> reference = new LinkedHashMap<>();
                    reference.put("sourceId", item.getSourceId());
                    reference.put("content", item.getContent());
                    reference.put("score", item.getScore());
                    reference.put("hitType", item.getHitType());
                    return reference;
                })
                .toList();
        return Map.of(
                "references", references,
                "topScore", response.getTopScore(),
                "hitEntities", response.getHitEntities() == null ? List.of() : response.getHitEntities(),
                "recallCount", response.getRecallCount(),
                "degraded", result.isDegraded()
        );
    }

    private Map<String, Object> shoppingData(ShoppingResponse response) {
        if (response == null) {
            return Map.of("status", "FAILED", "reason", "EMPTY_SHOPPING_RESPONSE", "missingSlots", List.of());
        }
        Map<String, Object> resultData = response.getResultData() == null ? Map.of() : response.getResultData();
        Object rawTool = resultData.get("tool");
        String tool = rawTool == null ? "" : String.valueOf(rawTool);
        String key = switch (tool) {
            case "add_to_cart" -> "cart";
            case "place_order" -> "order";
            case "query_stock" -> "stock";
            case "query_logistics" -> "logistics";
            case "query_promotion" -> "promotion";
            default -> "result";
        };
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", response.getStatus());
        data.put("tool", tool);
        data.put(key, resultData);
        data.put("missingSlots", response.getMissingSlots() == null ? List.of() : response.getMissingSlots());
        return data;
    }

    private Map<String, Object> systemData(String secondaryIntent) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("secondaryIntent", secondaryIntent);
        return data;
    }

    private boolean isHybridShoppingRequest(String input) {
        return input != null && (input.contains("推荐") || input.contains("适合"))
                && (input.contains("加购") || input.contains("下单") || input.contains("购买"));
    }
}
