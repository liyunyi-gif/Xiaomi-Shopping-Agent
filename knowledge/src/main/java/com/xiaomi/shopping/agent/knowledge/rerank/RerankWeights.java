package com.xiaomi.shopping.agent.knowledge.rerank;

/**
 * 加权 rerank 权重常量（架构.md §5 / 知识库Agent §5）。
 * <p>
 * 起点权重 w_sem=0.5 / w_kw=0.3 / w_field=0.2，待实测标定。
 *
 * @author liyunyi
 */
public final class RerankWeights {

    /** 语义相似度权重 */
    public static final double W_SEM = 0.5;

    /** 关键词匹配权重 */
    public static final double W_KW = 0.3;

    /** 字段命中权重（标题命中>正文命中） */
    public static final double W_FIELD = 0.2;

    /** 标题命中的字段加分 */
    public static final double FIELD_BOOST_TITLE = 1.0;

    /** 规格命中的字段加分 */
    public static final double FIELD_BOOST_SPEC = 0.5;

    private RerankWeights() {
    }
}
