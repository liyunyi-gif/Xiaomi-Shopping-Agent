package com.xiaomi.shopping.agent.common.entity.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiaomi.shopping.agent.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 向量存储表（t_knowledge_vector）—— 语义路 HNSW 召回。
 * <p>
 * 注意：embedding 为 PG VECTOR(1024) 类型，<b>不在此实体映射</b>，
 * 由 Spring AI PgVectorStore 专用路径读写（pgvector-java 处理）。
 * 此实体仅承载元数据，供 MyBatis 普通字段维护。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_knowledge_vector", autoResultMap = true)
public class KnowledgeVector extends BaseEntity {

    /** 回链切片ID */
    private Long chunkId;

    /** 所属知识库ID */
    private Long kbId;

    /** 文本内容（与切片冗余，便于向量库直接取） */
    private String content;

    /** 元数据 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    // embedding(VECTOR) 不映射，由 PgVectorStore 处理
}
