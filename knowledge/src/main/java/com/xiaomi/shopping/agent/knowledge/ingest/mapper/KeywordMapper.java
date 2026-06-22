package com.xiaomi.shopping.agent.knowledge.ingest.mapper;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关键词路全文检索 Mapper（原生 SQL）。
 * <p>
 * 走 t_knowledge_chunk 的 tsv（TSVECTOR）+ ts_rank 全文检索，
 * SQL 定义见 resources/mapper/KeywordMapper.xml。
 *
 * @author liyunyi
 */
@Mapper
public interface KeywordMapper {

    /**
     * 全文检索：按 query 的 ts_rank 排序返回 topK 切片。
     *
     * @param query 查询文本（口语/规范术语均可）
     * @param topK  返回条数上限
     * @return 命中切片（含 kwScore=ts_rank，content/title/specText）
     */
    List<ScoredDoc> fullTextSearch(@Param("query") String query, @Param("topK") int topK);
}
