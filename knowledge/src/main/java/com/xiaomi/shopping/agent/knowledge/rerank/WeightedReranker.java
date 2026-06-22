package com.xiaomi.shopping.agent.knowledge.rerank;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加权 Rerank（架构.md §3.2 / 知识库Agent §5）★ 简历重点（纯自研）。
 * <p>
 * 公式：finalScore = w_sem*simScore + w_kw*kwScore + w_field*fieldBoost
 * 字段加权：title 命中 > spec_text 命中（取自 t_knowledge_chunk 独立列）。
 *
 * @author liyunyi
 */
@Slf4j
@Service
public class WeightedReranker {

    /** 最终返回主 Agent 的条数（架构.md §10 TOP_N_FINAL） */
    @Value("${xiaomi.agent.knowledge.top-n-final:5}")
    private int topNFinal;

    /**
     * 加权重排。
     *
     * @param merged 双路合并去重后的候选集
     * @param query  重写后的查询（用于字段命中比对）
     * @return 按 finalScore 降序的 topN
     */
    public List<ScoredDoc> rerank(List<ScoredDoc> merged, String query) {
        if (merged == null || merged.isEmpty()) {
            return List.of();
        }
        Set<String> terms = extractTerms(query);
        return merged.stream()
                .map(d -> {
                    double fieldBoost = computeFieldBoost(d, terms);
                    double finalScore = RerankWeights.W_SEM * d.getSimScore()
                            + RerankWeights.W_KW * d.getKwScore()
                            + RerankWeights.W_FIELD * fieldBoost;
                    d.setFinalScore(finalScore);
                    return d;
                })
                .sorted(Comparator.comparingDouble(ScoredDoc::getFinalScore).reversed())
                .limit(topNFinal)
                .toList();
    }

    /**
     * 字段命中加权：title 命中 +1.0，spec_text 命中 +0.5（架构.md §5）。
     */
    double computeFieldBoost(ScoredDoc d, Set<String> terms) {
        if (terms.isEmpty()) {
            return 0;
        }
        double boost = 0;
        String title = d.getTitle();
        String spec = d.getSpecText();
        if (title != null && terms.stream().anyMatch(title::contains)) {
            boost += RerankWeights.FIELD_BOOST_TITLE;
        }
        if (spec != null && terms.stream().anyMatch(spec::contains)) {
            boost += RerankWeights.FIELD_BOOST_SPEC;
        }
        return boost;
    }

    /** 从 query 抽取词条（型号/规格/通用词）。 */
    Set<String> extractTerms(String query) {
        if (query == null || query.isBlank()) {
            return Set.of();
        }
        Set<String> terms = new java.util.HashSet<>();
        // 型号/规格词：小米14 / Redmi K70 / 16+512
        Matcher modelMatcher = Pattern.compile("[A-Za-z]*\\d+[A-Za-z+]*").matcher(query);
        while (modelMatcher.find()) {
            terms.add(modelMatcher.group());
        }
        // 通用词：中文/英文片段（按非标点切）
        Matcher wordMatcher = Pattern.compile("[\\u4e00-\\u9fa5A-Za-z]+").matcher(query);
        while (wordMatcher.find()) {
            String w = wordMatcher.group();
            if (w.length() >= 2) {
                terms.add(w);
            }
        }
        return terms;
    }
}
