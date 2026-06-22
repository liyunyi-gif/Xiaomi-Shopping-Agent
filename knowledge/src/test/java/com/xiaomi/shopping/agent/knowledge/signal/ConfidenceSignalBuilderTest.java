package com.xiaomi.shopping.agent.knowledge.signal;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConfidenceSignalBuilder 单测（对应 test/02 RECALL-006，P6：只给客观信号，无主观自评）。
 *
 * @author liyunyi
 */
class ConfidenceSignalBuilderTest {

    private final ConfidenceSignalBuilder builder = new ConfidenceSignalBuilder();

    @Test
    @DisplayName("RECALL-006 三信号完整：topScore/hitEntities/recallCount")
    void shouldBuildThreeSignals() {
        List<ScoredDoc> reranked = List.of(
                ScoredDoc.builder().content("小米14影像规格5000万像素").finalScore(0.92).build(),
                ScoredDoc.builder().content("小米14续航").finalScore(0.6).build()
        );
        Set<String> entities = Set.of("小米14", "5000");

        KnowledgeResponse resp = builder.build(reranked, entities);

        assertEquals(2, resp.getRecallCount(), "召回数=2");
        assertEquals(0.92, resp.getTopScore(), 0.001, "topScore=最高 finalScore");
        assertTrue(resp.getHitEntities().contains("小米14"), "命中实体应含 小米14");
        assertTrue(resp.getHitEntities().contains("5000"), "命中实体应含 5000");
    }

    @Test
    @DisplayName("RECALL-006 响应不含主观质量自评字段（P6）")
    void shouldNotContainSubjectiveFields() {
        KnowledgeResponse resp = builder.build(List.of(
                ScoredDoc.builder().content("c").finalScore(0.5).build()), Set.of());

        // 通过反射确认无 isGood/isSufficient/quality 等主观字段（契约层已保证，此处强化断言）
        assertFalse(resp.toString().toLowerCase().contains("isgood"));
        assertFalse(resp.toString().toLowerCase().contains("issufficient"));
    }

    @Test
    @DisplayName("空结果：recallCount=0, topScore=0（主 Agent 据此判 FAILED）")
    void shouldReturnZeroForEmpty() {
        KnowledgeResponse resp = builder.build(List.of(), Set.of("小米14"));
        assertEquals(0, resp.getRecallCount());
        assertEquals(0.0, resp.getTopScore(), 0.001);
        assertTrue(resp.getHitEntities().isEmpty(), "空结果无命中");
    }

    @Test
    @DisplayName("实体未出现在结果 content → hitEntities 为空（主 Agent 据此判 INCOMPLETE）")
    void shouldReturnEmptyHitWhenEntityNotInContent() {
        KnowledgeResponse resp = builder.build(
                List.of(ScoredDoc.builder().content("某无关内容").finalScore(0.8).build()),
                Set.of("小米14"));
        assertTrue(resp.getHitEntities().isEmpty(), "实体未命中应为空");
    }

    @Test
    @DisplayName("null 入参安全处理")
    void shouldHandleNullSafely() {
        KnowledgeResponse resp = builder.build(null, null);
        assertEquals(0, resp.getRecallCount());
        assertTrue(resp.getHitEntities().isEmpty());
    }
}
