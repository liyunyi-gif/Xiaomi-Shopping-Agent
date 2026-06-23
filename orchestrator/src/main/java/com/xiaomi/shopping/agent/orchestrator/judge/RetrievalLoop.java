package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 主 Agent 重检循环状态机。
 *
 * @author liyunyi
 */
@Component
public class RetrievalLoop {

    private final KnowledgeGateway knowledgeGateway;
    private final QualityJudge qualityJudge;
    private final LoopQueryRewriter queryRewriter;
    private final int maxRetries;

    public RetrievalLoop(KnowledgeGateway knowledgeGateway,
                         QualityJudge qualityJudge,
                         LoopQueryRewriter queryRewriter,
                         @Value("${xiaomi.agent.retry.max-attempts:2}") int maxRetries) {
        this.knowledgeGateway = knowledgeGateway;
        this.qualityJudge = qualityJudge;
        this.queryRewriter = queryRewriter;
        this.maxRetries = maxRetries;
    }

    public RetrievalLoopResult retrieve(String question, String intent,
                                        SessionSnapshot snapshot, Set<String> entities) {
        String currentQuery = question;
        KnowledgeResponse lastResponse = null;
        QualityVerdict lastVerdict = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            KnowledgeRequest request = KnowledgeRequest.builder()
                    .question(currentQuery)
                    .intent(intent)
                    .snapshot(snapshot)
                    .retryAttempt(attempt)
                    .queryEntities(entities)
                    .build();
            lastResponse = knowledgeGateway.ask(request);
            lastVerdict = qualityJudge.judge(lastResponse, entities);
            if (lastVerdict.isSufficient()) {
                return RetrievalLoopResult.builder()
                        .response(lastResponse)
                        .verdict(lastVerdict)
                        .retryCount(attempt)
                        .degraded(false)
                        .build();
            }
            if (attempt < maxRetries) {
                currentQuery = queryRewriter.rewrite(currentQuery, lastVerdict, attempt, entities);
            }
        }
        return RetrievalLoopResult.builder()
                .response(lastResponse)
                .verdict(lastVerdict)
                .retryCount(maxRetries)
                .degraded(true)
                .build();
    }
}
