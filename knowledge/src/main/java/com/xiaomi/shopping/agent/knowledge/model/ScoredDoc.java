package com.xiaomi.shopping.agent.knowledge.model;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 召回文档（双路召回 + rerank 的内部统一模型）。
 * <p>
 * 语义路与关键词路召回结果统一封装为 ScoredDoc，rerank 在其上叠加 finalScore。
 * title/specText 来自 t_knowledge_chunk 的独立列（rerank 字段加权用），
 * content 来自 t_knowledge_chunk.content（命中实体判定用）。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredDoc implements Serializable {

    /** 切片ID（t_knowledge_chunk.id，去重键） */
    private Long id;

    /** 命中来源：semantic / keyword */
    private String hitType;

    /** 文本内容（content 列，命中实体比对基准） */
    private String content;

    /** 标题（title 列，rerank 字段加权） */
    private String title;

    /** 规格摘要（spec_text 列，rerank 字段加权） */
    private String specText;

    /** 关联 SPU ID */
    private Long spuId;

    /** 语义相似度分（语义路召回时填充，0~1） */
    private double simScore;

    /** 关键词匹配分（关键词路 ts_rank 归一化，0~1） */
    private double kwScore;

    /** rerank 综合分（WeightedReranker 计算后填充） */
    private double finalScore;

    /**
     * 构造响应用的 RetrievalItem（转 common 契约）。
     */
    public KnowledgeResponse.RetrievalItem toRetrievalItem() {
        return KnowledgeResponse.RetrievalItem.builder()
                .sourceId(id == null ? null : String.valueOf(id))
                .content(content)
                .score(finalScore)
                .hitType(hitType)
                .build();
    }
}
