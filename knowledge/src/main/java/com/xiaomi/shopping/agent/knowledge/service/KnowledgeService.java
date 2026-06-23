package com.xiaomi.shopping.agent.knowledge.service;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.knowledge.entityextract.EntityExtractor;
import com.xiaomi.shopping.agent.knowledge.recall.DualChannelRecaller;
import com.xiaomi.shopping.agent.knowledge.rerank.Reranker;
import com.xiaomi.shopping.agent.knowledge.rewrite.QueryRewriter;
import com.xiaomi.shopping.agent.knowledge.signal.ConfidenceSignalBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Knowledge 子节点入口服务（架构.md §8 / 知识库Agent §8）。
 * <p>
 * 请求-响应、<b>无状态</b>（P3）、不面对用户（P2）、不做意图识别（P1）、不判质量（P6）。
 * <p>
 * 流水线：查询重写 → 双路并行召回 → 加权 rerank → 生成置信信号（无主观自评）。
 *
 * @author liyunyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    /** 双路每路召回 topK（给 rerank 留候选池，架构.md §10） */
    @Value("${xiaomi.agent.knowledge.recall-topk:20}")
    private int recallTopK;

    private final QueryRewriter queryRewriter;
    private final DualChannelRecaller dualChannelRecaller;
    private final Reranker reranker;
    private final ConfidenceSignalBuilder confidenceSignalBuilder;
    private final EntityExtractor entityExtractor;

    /**
     * 主 Agent 调用入口（无状态）。
     * <p>
     * question：原始问题；queryEntities：主 Agent 抽取的实体（命中信号基准，显式注入）。
     *
     * @param question      原始问题（已含会话上下文）
     * @param snapshot      会话快照（指代消解用）
     * @param queryEntities 主 Agent 抽取的实体集合
     * @return KnowledgeResponse（结果 + 三信号，无主观自评）
     */
    public KnowledgeResponse ask(String question, SessionSnapshot snapshot,
                                 Set<String> queryEntities) {
        log.debug("Knowledge.ask question={} entities={}", question, queryEntities);

        // 1. 查询重写（受限智能，不判质量）
        String rewritten = queryRewriter.rewrite(question, snapshot);

        // 2. 双路并行召回 + 合并去重
        var merged = dualChannelRecaller.recallParallel(rewritten, recallTopK);

        // 3. 加权 rerank（外部模型主用，自研降级）
        var reranked = reranker.rerank(merged, rewritten);

        // 4. 生成置信信号（无主观自评）
        return confidenceSignalBuilder.build(reranked, queryEntities);
    }

    /**
     * 契约重载：接收 KnowledgeRequest。
     * <p>
     * 注意：架构.md §7 实体由主 Agent 抽取后注入。若调用方未单独传实体，
     * 此重载内部用 EntityExtractor 自行抽取兜底（不依赖主 Agent 也能跑）。
     */
    public KnowledgeResponse ask(KnowledgeRequest request) {
        Set<String> entities = request.getQueryEntities() == null || request.getQueryEntities().isEmpty()
                ? entityExtractor.extract(request.getQuestion())
                : request.getQueryEntities();
        return ask(request.getQuestion(), request.getSnapshot(), entities);
    }
}
