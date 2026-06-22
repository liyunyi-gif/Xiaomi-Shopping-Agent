package com.xiaomi.shopping.agent.knowledge.recall.keyword;

import com.xiaomi.shopping.agent.knowledge.ingest.mapper.KeywordMapper;
import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 关键词路召回（架构.md §3.2 / 知识库Agent §4.2）。
 * <p>
 * 走 t_knowledge_chunk 的 tsv（TSVECTOR）+ ts_rank 全文检索。
 * 补语义路漏掉的精确型号词（如 "Redmi K70"）。
 *
 * @author liyunyi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordRecaller {

    private final KeywordMapper keywordMapper;

    /**
     * 关键词召回 topK（按 ts_rank 排序）。
     */
    public List<ScoredDoc> recall(String query, int topK) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<ScoredDoc> result = keywordMapper.fullTextSearch(query, topK);
            // ts_rank 归一化到 0~1（ts_rank 范围理论 0~0.1+，按经验归一）
            normalizeKwScore(result);
            return result;
        } catch (Exception e) {
            log.warn("关键词召回失败（返回空）：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 将 ts_rank（kwScore）归一化到 0~1。ts_rank 典型上限约 0.1~1，这里按 sqrt 压缩。 */
    private void normalizeKwScore(List<ScoredDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        double max = docs.stream().mapToDouble(ScoredDoc::getKwScore).max().orElse(1.0);
        if (max <= 0) {
            return;
        }
        for (ScoredDoc d : docs) {
            d.setKwScore(d.getKwScore() / max);
        }
    }
}
