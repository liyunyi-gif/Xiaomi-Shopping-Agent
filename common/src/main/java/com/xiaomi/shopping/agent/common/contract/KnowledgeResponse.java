package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Knowledge 子节点 → 主 Agent 的响应（架构.md §8.1）
 * <p>
 * 返回结构：检索结果 + 三项置信信号（相关度分数 / 命中实体 / 召回数）。
 * <p>
 * 关键约束（P6 质量判断上移）：Knowledge <b>不返回主观质量自评</b>，
 * 只提供客观可量化信号，质量判断由主 Agent 做。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeResponse implements Serializable {

    /** 检索结果列表（每条含内容/来源/分项分数） */
    private List<RetrievalItem> results;

    /** 信号一：最高相关度分数（供主 Agent 判「不够」） */
    private double topScore;

    /** 信号二：命中的实体集合（供主 Agent 判「不全」） */
    private Set<String> hitEntities;

    /** 信号三：召回数（为 0 主 Agent 判「失败」） */
    private int recallCount;

    /**
     * 单条检索结果。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievalItem implements Serializable {
        /** 来源文档/切片标识 */
        private String sourceId;
        /** 文本内容 */
        private String content;
        /** 综合得分（rerank 后） */
        private double score;
        /** 命中类型（semantic / keyword，用于可观测） */
        private String hitType;
    }
}
