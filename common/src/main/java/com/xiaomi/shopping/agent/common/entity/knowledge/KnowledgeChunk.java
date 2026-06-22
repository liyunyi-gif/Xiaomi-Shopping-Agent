package com.xiaomi.shopping.agent.common.entity.knowledge;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识切片表（t_knowledge_chunk）—— 关键词路 + 命中判定源。
 * <p>
 * 含 title / spec_text 字段加权列，供 rerank 字段加权（知识库Agent §5）。
 * <p>
 * 注意：tsv（TSVECTOR）由 PG 触发器自动维护，实体不映射该列，
 * 由原生 SQL（ts_rank + plainto_tsquery）在关键词路处理。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_knowledge_chunk")
public class KnowledgeChunk extends LogicDeleteEntity {

    /** 所属知识库ID */
    private Long kbId;

    /** 所属文档ID */
    private Long docId;

    /** 关联 SPU ID */
    private Long spuId;

    /** 切片序号（文档内） */
    private Integer chunkIndex;

    /** 切片文本内容 */
    private String content;

    /** 标题（rerank 字段加权） */
    private String title;

    /** 规格摘要（rerank 字段加权） */
    private String specText;

    /** 内容 hash */
    private String contentHash;

    /** 字符数 */
    private Integer charCount;

    /** token 数 */
    private Integer tokenCount;

    /** 是否启用：1 是 / 0 否 */
    private Integer enabled;
}
