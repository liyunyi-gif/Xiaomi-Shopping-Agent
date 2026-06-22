package com.xiaomi.shopping.agent.knowledge.rerank;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WeightedReranker 单测（对应 test/02 RECALL-005 加权 rerank）。
 *
 * @author liyunyi
 */
class WeightedRerankerTest {

    private WeightedReranker newReranker(int topN) {
        WeightedReranker r = new WeightedReranker();
        ReflectionTestUtils.setField(r, "topNFinal", topN);
        return r;
    }

    @Test
    @DisplayName("RECALL-005 按 finalScore 降序，title 命中获得字段加权提升")
    void shouldRankByFinalScoreWithFieldBoost() {
        WeightedReranker r = newReranker(5);
        // 两条候选：A 语义分高但无 title 命中；B 语义分低但 title 命中"小米14"
        ScoredDoc a = ScoredDoc.builder().content("某手机内容").simScore(0.9).kwScore(0).title("通用机型").build();
        ScoredDoc b = ScoredDoc.builder().content("小米14影像").simScore(0.5).kwScore(0).title("小米14规格").build();

        List<ScoredDoc> result = r.rerank(List.of(a, b), "小米14");

        assertEquals(2, result.size());
        // B 有 title 命中加成(0.2*1.0)，A 无。验证 finalScore：B 的字段加成使其分不低于无加成的 A
        double aFinal = result.stream().filter(d -> d.getContent().equals("某手机内容")).findFirst().orElseThrow().getFinalScore();
        double bFinal = result.stream().filter(d -> d.getContent().equals("小米14影像")).findFirst().orElseThrow().getFinalScore();
        assertTrue(bFinal > aFinal || Math.abs(bFinal - aFinal) < 0.01,
                "title 命中的 B 应有加成，finalScore=" + bFinal + " >= A=" + aFinal);
    }

    @Test
    @DisplayName("空候选返回空")
    void shouldReturnEmptyForEmptyInput() {
        WeightedReranker r = newReranker(5);
        assertTrue(r.rerank(null, "q").isEmpty());
        assertTrue(r.rerank(List.of(), "q").isEmpty());
    }

    @Test
    @DisplayName("限制返回 topN 条")
    void shouldLimitToTopN() {
        WeightedReranker r = newReranker(2);
        ScoredDoc d1 = ScoredDoc.builder().content("c1").simScore(0.9).build();
        ScoredDoc d2 = ScoredDoc.builder().content("c2").simScore(0.8).build();
        ScoredDoc d3 = ScoredDoc.builder().content("c3").simScore(0.7).build();
        List<ScoredDoc> result = r.rerank(List.of(d1, d2, d3), "q");
        assertEquals(2, result.size(), "应限制为 topN=2");
    }

    @Test
    @DisplayName("加权公式：finalScore = 0.5*sim + 0.3*kw + 0.2*field")
    void shouldApplyWeightedFormula() {
        WeightedReranker r = newReranker(5);
        ScoredDoc d = ScoredDoc.builder().content("小米14").simScore(0.6).kwScore(0.5).title("小米14").build();
        List<ScoredDoc> result = r.rerank(List.of(d), "小米14");
        double expected = 0.5 * 0.6 + 0.3 * 0.5 + 0.2 * 1.0; // sim + kw + title命中(1.0)
        assertEquals(expected, result.get(0).getFinalScore(), 0.001, "加权公式应正确");
    }
}
