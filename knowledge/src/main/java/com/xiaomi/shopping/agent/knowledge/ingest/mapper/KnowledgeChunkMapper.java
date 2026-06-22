package com.xiaomi.shopping.agent.knowledge.ingest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaomi.shopping.agent.common.entity.knowledge.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识切片 Mapper（写 t_knowledge_chunk：文本/title/spec_text）。
 * <p>
 * tsv 列由 PG 触发器自动维护，此处不涉及；embedding 在 t_knowledge_vector，由 VectorStore 处理。
 *
 * @author liyunyi
 */
@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {
}
