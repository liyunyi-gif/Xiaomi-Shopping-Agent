package com.xiaomi.shopping.agent.knowledge.rerank;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Rerank 门面（知识库Agent-技术架构.md §5.1）★ 简历重点。
 * <p>
 * 策略：外部 Qwen3-Reranker-8B 主用（cross-encoder 精度高）；
 * 外部不可用/失败/未配置时自动降级到自研 {@link WeightedReranker} 加权打分，保证可用性。
 * 下游（KnowledgeService）对两种实现无感，统一拿 finalScore 降序列表。
 *
 * @author liyunyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Reranker {

    private final WeightedReranker weightedReranker;
    private final RerankProperties properties;

    /** 外部 rerank 客户端（可选：未配置 api-key 时不装配，为 null → 直接降级） */
    @Autowired(required = false)
    private QwenRerankClient qwenRerankClient;

    /** rerank 后最终返回条数 */
    @Value("${xiaomi.agent.knowledge.top-n-final:5}")
    private int topNFinal;

    /**
     * 重排：优先外部模型，失败则降级自研加权。
     *
     * @param merged 双路召回合并去重后的候选
     * @param query  重写后的查询（自研加权用）
     * @return 按 finalScore 降序、限 topNFinal 的列表
     */
    public List<ScoredDoc> rerank(List<ScoredDoc> merged, String query) {
        if (merged == null || merged.isEmpty()) {
            return List.of();
        }
        // 外部 rerank 主用
        if (properties.isEnabled() && qwenRerankClient != null) {
            try {
                List<ScoredDoc> ranked = qwenRerankClient.rerank(query, merged, topNFinal);
                if (!ranked.isEmpty()) {
                    return ranked;
                }
                log.debug("外部 rerank 返回空，降级自研加权");
            } catch (Exception e) {
                log.warn("外部 rerank 失败，降级自研加权：{}", e.getMessage());
            }
        }
        // 降级：自研加权
        return weightedReranker.rerank(merged, query);
    }
}
