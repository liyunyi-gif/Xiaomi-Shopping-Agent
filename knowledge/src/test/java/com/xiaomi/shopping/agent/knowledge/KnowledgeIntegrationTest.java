package com.xiaomi.shopping.agent.knowledge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.entity.knowledge.KnowledgeChunk;
import com.xiaomi.shopping.agent.knowledge.ingest.mapper.KnowledgeChunkMapper;
import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import com.xiaomi.shopping.agent.knowledge.recall.keyword.KeywordRecaller;
import com.xiaomi.shopping.agent.knowledge.service.KnowledgeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 知识库关键词路 + 入库 集成单测（连业务库 xiaomi_agent）。
 * <p>
 * 覆盖 test/01 INGEST-005/006（入库→tsv 触发器→全文检索）+ test/02 RECALL-004（关键词路 ts_rank）。
 * 用英文/型号词内容，规避中文 simple 配置不分词问题。
 *
 * @author liyunyi
 */
@SpringBootTest
@ActiveProfiles("local")
class KnowledgeIntegrationTest {

    @Autowired
    private KnowledgeChunkMapper chunkMapper;

    @Autowired
    private KeywordRecaller keywordRecaller;

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("INGEST-006 + RECALL-004 入库后 tsv 触发器维护，关键词路 ts_rank 可检索")
    void shouldInsertAndFullTextSearch() {
        // 1. 插入测试切片（含型号词 Redmi K70）
        Long chunkId = insertTestChunk("Redmi K70 Pro specs snapdragon processor",
                "Redmi K70 Pro", "16GB 512GB");

        try {
            // 2. 等待 tsv 触发器（同事务内已生效）
            // 3. 关键词路检索
            List<ScoredDoc> result = keywordRecaller.recall("Redmi K70", 5);
            assertFalse(result.isEmpty(), "关键词路应命中 Redmi K70 切片: " + result);
            assertTrue(result.stream().anyMatch(d -> d.getContent() != null && d.getContent().contains("Redmi K70")),
                    "命中内容应含 Redmi K70");
        } finally {
            // 清理
            jdbcTemplate.update("DELETE FROM t_knowledge_chunk WHERE id = ?", chunkId);
        }
    }

    @Test
    @DisplayName("RECALL-006 端到端流水线返回三信号（无主观自评）")
    void shouldRunPipelineAndReturnSignals() {
        Long chunkId = insertTestChunk("Mi14 camera leica 5000MP image sensor",
                "Mi14 camera", "leica");
        try {
            KnowledgeResponse resp = knowledgeService.ask("Mi14 camera", null, Set.of("Mi14"));
            // 返回结果 + 三信号字段齐全
            assertTrue(resp.getRecallCount() >= 0, "召回数应为非负");
            assertTrue(resp.getTopScore() >= 0.0, "topScore 应为非负");
            assertTrue(resp.getHitEntities() != null, "hitEntities 不应为 null");
            // 结果中应含刚插入的内容
            assertFalse(resp.getResults().isEmpty(), "应召回结果");
        } finally {
            jdbcTemplate.update("DELETE FROM t_knowledge_chunk WHERE id = ?", chunkId);
        }
    }

    private Long insertTestChunk(String content, String title, String specText) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setKbId(1L);
        chunk.setDocId(1L);
        chunk.setSpuId(1L);
        chunk.setChunkIndex(0);
        chunk.setContent(content);
        chunk.setTitle(title);
        chunk.setSpecText(specText);
        chunk.setCharCount(content.length());
        chunk.setEnabled(1);
        chunkMapper.insert(chunk);
        return chunk.getId();
    }
}
