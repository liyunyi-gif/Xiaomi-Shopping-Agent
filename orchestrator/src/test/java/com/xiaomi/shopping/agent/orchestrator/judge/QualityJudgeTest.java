package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUDGE-001..004/008 质量判断测试。
 *
 * @author liyunyi
 */
class QualityJudgeTest {

    private final QualityJudge judge = new QualityJudge(0.7);

    @Test
    @DisplayName("JUDGE-001 召回数为0判 FAILED")
    void shouldJudgeFailedWhenNoRecall() {
        QualityVerdict verdict = judge.judge(resp(0, 0, Set.of()), Set.of("小米14"));

        assertEquals(QualityVerdict.Level.FAILED, verdict.getLevel());
        assertTrue(verdict.getReason().contains("recallCount=0"));
    }

    @Test
    @DisplayName("JUDGE-002 实体未命中判 INCOMPLETE")
    void shouldJudgeIncompleteWhenEntityMissing() {
        QualityVerdict verdict = judge.judge(resp(5, 0.9, Set.of("影像")), Set.of("小米14", "影像"));

        assertEquals(QualityVerdict.Level.INCOMPLETE, verdict.getLevel());
        assertTrue(verdict.getReason().contains("关键实体"));
    }

    @Test
    @DisplayName("JUDGE-003 相关度低于阈值判 INSUFFICIENT")
    void shouldJudgeInsufficientWhenScoreLow() {
        QualityVerdict verdict = judge.judge(resp(5, 0.4, Set.of("小米14")), Set.of("小米14"));

        assertEquals(QualityVerdict.Level.INSUFFICIENT, verdict.getLevel());
        assertTrue(verdict.getReason().contains("0.4"));
        assertTrue(verdict.getReason().contains("0.7"));
    }

    @Test
    @DisplayName("JUDGE-004 三信号满足判 SUFFICIENT")
    void shouldJudgeSufficient() {
        QualityVerdict verdict = judge.judge(resp(5, 0.85, Set.of("小米14")), Set.of("小米14"));

        assertEquals(QualityVerdict.Level.SUFFICIENT, verdict.getLevel());
        assertTrue(verdict.isSufficient());
    }

    @Test
    @DisplayName("JUDGE-008 判定理由来自可量化信号")
    void shouldRecordMeasurableSignals() {
        QualityVerdict verdict = judge.judge(resp(3, 0.8, Set.of("小米14")), Set.of("小米14"));

        assertTrue(verdict.getReason().contains("recallCount=3"));
        assertTrue(verdict.getReason().contains("topScore=0.8"));
    }

    private KnowledgeResponse resp(int recall, double topScore, Set<String> hitEntities) {
        return KnowledgeResponse.builder()
                .recallCount(recall)
                .topScore(topScore)
                .hitEntities(hitEntities)
                .results(List.of(KnowledgeResponse.RetrievalItem.builder()
                        .sourceId("doc-1")
                        .content("小米14影像规格资料")
                        .score(topScore)
                        .hitType("keyword")
                        .build()))
                .build();
    }
}
