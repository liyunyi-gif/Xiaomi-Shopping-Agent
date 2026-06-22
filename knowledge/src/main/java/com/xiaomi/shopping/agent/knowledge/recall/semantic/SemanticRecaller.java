package com.xiaomi.shopping.agent.knowledge.recall.semantic;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 语义路召回（架构.md §3.2 / 知识库Agent §4.1）。
 * <p>
 * 走 PgVectorStore + HNSW，查询 t_knowledge_vector（底层 vector_cosine_ops）。
 * similaritySearch 内部用 EmbeddingModel 自动对 query 向量化。
 * <p>
 * 单测兼容：VectorStore 可选注入，未配置时返回空（由并行编排保证关键词路兜底）。
 *
 * @author liyunyi
 */
@Slf4j
@Component
public class SemanticRecaller {

    /** 默认余弦相似度阈值（待实测标定） */
    private static final double SIMILARITY_THRESHOLD = 0.6;

    @Autowired(required = false)
    private VectorStore vectorStore;

    /**
     * 语义召回 topK。
     *
     * @param query 重写后的查询文本
     * @param topK  召回条数
     */
    public List<ScoredDoc> recall(String query, int topK) {
        if (vectorStore == null) {
            log.debug("VectorStore 未配置，语义路返回空");
            return Collections.emptyList();
        }
        try {
            SearchRequest req = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .build();
            List<Document> docs = vectorStore.similaritySearch(req);
            if (docs == null) {
                return Collections.emptyList();
            }
            return docs.stream()
                    .map(this::toScoredDoc)
                    .toList();
        } catch (Exception e) {
            log.warn("语义召回失败（返回空）：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private ScoredDoc toScoredDoc(Document doc) {
        // Document 的 score 存于 metadata（Spring AI similaritySearch 会写入 distance/score）
        double score = extractScore(doc);
        return ScoredDoc.builder()
                .content(doc.getText())
                .simScore(score)
                .hitType("semantic")
                .build();
    }

    private double extractScore(Document doc) {
        Object score = doc.getMetadata() == null ? null : doc.getMetadata().get("distance");
        if (score instanceof Number n) {
            double distance = n.doubleValue();
            // 余弦距离转相似度：similarity = 1 - distance（cosine_distance 范围 0~2）
            return Math.max(0, 1 - distance);
        }
        return 0.0;
    }
}
