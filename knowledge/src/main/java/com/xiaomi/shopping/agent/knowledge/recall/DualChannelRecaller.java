package com.xiaomi.shopping.agent.knowledge.recall;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import com.xiaomi.shopping.agent.knowledge.recall.keyword.KeywordRecaller;
import com.xiaomi.shopping.agent.knowledge.recall.semantic.SemanticRecaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 双路召回并行编排（架构.md §3.2 / 知识库Agent §4.3）★ 简历重点。
 * <p>
 * 语义路 + 关键词路 CompletableFuture 并行召回，墙钟时间 ≈ 较慢一路（非两路之和）。
 * 合并去重（按 chunk id），已存在则保留语义分。
 *
 * @author liyunyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DualChannelRecaller {

    private final SemanticRecaller semanticRecaller;
    private final KeywordRecaller keywordRecaller;

    /**
     * 双路并行召回 + 合并去重。
     *
     * @param query 重写后的查询
     * @param topK  每路召回条数上限
     * @return 合并去重后的候选集
     */
    public List<ScoredDoc> recallParallel(String query, int topK) {
        CompletableFuture<List<ScoredDoc>> semFuture =
                CompletableFuture.supplyAsync(() -> safeRecall(() -> semanticRecaller.recall(query, topK), "semantic"));
        CompletableFuture<List<ScoredDoc>> kwFuture =
                CompletableFuture.supplyAsync(() -> safeRecall(() -> keywordRecaller.recall(query, topK), "keyword"));

        CompletableFuture.allOf(semFuture, kwFuture).join();

        return mergeAndDedup(semFuture.join(), kwFuture.join());
    }

    /** 包装异常，确保单路失败不影响另一路。 */
    private List<ScoredDoc> safeRecall(Supplier<List<ScoredDoc>> supplier, String channel) {
        try {
            List<ScoredDoc> result = supplier.get();
            if (result == null) {
                log.warn("{} 路召回返回 null，按空处理", channel);
                return List.of();
            }
            return result;
        } catch (Exception e) {
            log.warn("{} 路召回异常，按空处理：{}", channel, e.getMessage());
            return List.of();
        }
    }

    /**
     * 合并去重：按 chunk id（无 id 时按 content）。已存在则合并分（保留语义分，叠加关键词分）。
     */
    List<ScoredDoc> mergeAndDedup(List<ScoredDoc> semantic, List<ScoredDoc> keyword) {
        Map<String, ScoredDoc> merged = new HashMap<>();
        // 语义路优先入
        for (ScoredDoc d : semantic) {
            merged.put(keyOf(d), d);
        }
        // 关键词路并入：已存在则叠加 kwScore + 补 title/specText（关键词路来自 chunk 表，有这些列）
        for (ScoredDoc d : keyword) {
            merged.merge(keyOf(d), d, (existing, incoming) -> {
                existing.setKwScore(Math.max(existing.getKwScore(), incoming.getKwScore()));
                // 补全字段（语义路 ScoredDoc 可能缺 title/specText/id）
                if (existing.getTitle() == null) {
                    existing.setTitle(incoming.getTitle());
                }
                if (existing.getSpecText() == null) {
                    existing.setSpecText(incoming.getSpecText());
                }
                if (existing.getId() == null) {
                    existing.setId(incoming.getId());
                }
                if (existing.getSpuId() == null) {
                    existing.setSpuId(incoming.getSpuId());
                }
                return existing;
            });
        }
        return new ArrayList<>(merged.values());
    }

    /** 去重键：优先用 chunk id，无则用 content（语义路无 id 时）。 */
    private String keyOf(ScoredDoc d) {
        return d.getId() != null ? "id:" + d.getId() : "c:" + (d.getContent() == null ? "" : d.getContent().hashCode());
    }
}
