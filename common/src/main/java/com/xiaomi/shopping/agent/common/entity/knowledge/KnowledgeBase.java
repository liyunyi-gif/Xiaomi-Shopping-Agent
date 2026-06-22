package com.xiaomi.shopping.agent.common.entity.knowledge;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库表（t_knowledge_base）。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_knowledge_base")
public class KnowledgeBase extends LogicDeleteEntity {

    /** 知识库名称 */
    private String name;

    /** Embedding 模型 */
    private String embeddingModel;

    /** 集合名（唯一，pgvector collection） */
    private String collectionName;

    /** 描述 */
    private String description;
}
