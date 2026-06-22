package com.xiaomi.shopping.agent.knowledge.signal;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 置信信号生成（架构.md §5.1 / 知识库Agent §6）★ 对齐 P6。
 * <p>
 * 从 rerank 结果算出三信号（相关度分数/命中实体/召回数），交主 Agent 判定充分性。
 * <b>不返回任何主观质量自评</b>——充分性是主 Agent QualityJudge 的职责（P6）。
 *
 * @author liyunyi
 */
@Component
public class ConfidenceSignalBuilder {

    /**
     * 构造 KnowledgeResponse（含三信号）。
     *
     * @param reranked      rerank 后的结果
     * @param queryEntities 主 Agent 抽取的实体（命中比对基准）
     */
    public KnowledgeResponse build(List<ScoredDoc> reranked, Set<String> queryEntities) {
        int recallCount = reranked == null ? 0 : reranked.size();

        // 信号一：相关度分数（rerank 最高 finalScore）
        double topScore = (reranked == null || reranked.isEmpty()) ? 0.0 : reranked.get(0).getFinalScore();

        // 信号三：命中实体（query 实体是否出现在结果 content 中）
        Set<String> hitEntities = (reranked == null) ? Set.of() : reranked.stream()
                .filter(d -> d.getContent() != null)
                .flatMap(d -> (queryEntities == null ? Set.<String>of() : queryEntities).stream()
                        .filter(e -> d.getContent().contains(e)))
                .collect(Collectors.toSet());

        List<KnowledgeResponse.RetrievalItem> items = (reranked == null) ? List.of() : reranked.stream()
                .map(ScoredDoc::toRetrievalItem)
                .toList();

        return KnowledgeResponse.builder()
                .results(items)
                .topScore(topScore)
                .hitEntities(hitEntities)
                .recallCount(recallCount)
                .build();
    }
}
