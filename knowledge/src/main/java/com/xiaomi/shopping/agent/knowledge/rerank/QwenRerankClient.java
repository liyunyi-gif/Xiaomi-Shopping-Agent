package com.xiaomi.shopping.agent.knowledge.rerank;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部 Rerank 客户端：硅基流动 Qwen3-Reranker-8B（知识库Agent-技术架构.md §5.2）。
 * <p>
 * POST /v1/rerank，body 含 model/query/documents/top_n/return_text，
 * 响应 results: [{index, relevance_score}]。relevance_score(0~1) 即 cross-encoder 打分。
 * <p>
 * 仅在配置了 api-key 时装配（@ConditionalOnProperty），否则 Reranker 门面直接走自研降级。
 *
 * @author liyunyi
 */
@Slf4j
@Component
@ConditionalOnExpression("'${xiaomi.agent.rerank.api-key:}' != ''")
public class QwenRerankClient {

    private final WebClient webClient;
    private final RerankProperties properties;

    @Autowired
    public QwenRerankClient(RerankProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    /**
     * 调用外部 rerank 模型对候选打分。
     *
     * @param query      查询
     * @param candidates 候选 ScoredDoc 列表（用 content 作为文档文本）
     * @param topN       返回条数
     * @return 按 relevance_score 降序、finalScore 已填充的 ScoredDoc 列表；失败抛异常由门面捕获降级
     */
    public List<ScoredDoc> rerank(String query, List<ScoredDoc> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<String> documents = candidates.stream()
                .map(d -> d.getContent() == null ? "" : d.getContent())
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel());
        body.put("query", query);
        body.put("documents", documents);
        body.put("top_n", Math.min(topN, candidates.size()));
        body.put("return_text", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = webClient.post()
                .uri("/rerank")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return mapResponse(candidates, resp);
    }

    /** 将响应 results（[{index, relevance_score}]）映射回 ScoredDoc 并按分降序。 */
    @SuppressWarnings("unchecked")
    private List<ScoredDoc> mapResponse(List<ScoredDoc> candidates, Map<String, Object> resp) {
        if (resp == null) {
            return List.of();
        }
        Object resultsObj = resp.get("results");
        if (!(resultsObj instanceof List<?> results)) {
            return List.of();
        }
        List<ScoredDoc> ranked = new ArrayList<>();
        for (Object item : results) {
            if (!(item instanceof Map<?, ?> r)) {
                continue;
            }
            Object idxObj = r.get("index");
            Object scoreObj = r.get("relevance_score");
            if (idxObj instanceof Number idx && scoreObj instanceof Number score) {
                int i = idx.intValue();
                if (i >= 0 && i < candidates.size()) {
                    ScoredDoc d = candidates.get(i);
                    d.setFinalScore(score.doubleValue());
                    d.setHitType("rerank");
                    ranked.add(d);
                }
            }
        }
        ranked.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));
        return ranked;
    }
}
