package com.xiaomi.shopping.agent.knowledge.rerank;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Reranker 门面单测（对应 RECALL-005b：外部 rerank 不可用 → 自研加权降级）。
 *
 * @author liyunyi
 */
class RerankerTest {

    @Test
    @DisplayName("RECALL-005b 未配置外部客户端时自动降级 WeightedReranker")
    void shouldFallbackToWeightedWhenClientMissing() {
        WeightedReranker weighted = new WeightedReranker();
        ReflectionTestUtils.setField(weighted, "topNFinal", 5);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(true);
        Reranker reranker = new Reranker(weighted, properties);
        ReflectionTestUtils.setField(reranker, "topNFinal", 5);

        List<ScoredDoc> docs = sampleDocs();
        List<ScoredDoc> result = reranker.rerank(docs, "Mi14 camera");

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        // 降级后 finalScore 应由 WeightedReranker 填充为非0
        assertFalse(result.stream().allMatch(d -> d.getFinalScore() == 0.0));
    }

    @Test
    @DisplayName("RECALL-005 外部 rerank 可用时按 relevance_score 返回，不走自研降级")
    void shouldUseExternalRerankWhenAvailable() {
        WeightedReranker weighted = mock(WeightedReranker.class);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(true);
        Reranker reranker = new Reranker(weighted, properties);
        ReflectionTestUtils.setField(reranker, "topNFinal", 2);
        QwenRerankClient client = mock(QwenRerankClient.class);
        ReflectionTestUtils.setField(reranker, "qwenRerankClient", client);

        ScoredDoc first = ScoredDoc.builder().id(1L).content("Mi14 camera").finalScore(0.93).hitType("rerank").build();
        when(client.rerank(anyString(), any(), anyInt())).thenReturn(List.of(first));

        List<ScoredDoc> result = reranker.rerank(sampleDocs(), "Mi14 camera");

        assertEquals(1, result.size());
        assertEquals(0.93, result.get(0).getFinalScore(), 0.001);
        assertEquals("rerank", result.get(0).getHitType());
        verifyNoInteractions(weighted);
    }

    @Test
    @DisplayName("RECALL-005b 外部 rerank 抛异常时自动降级 WeightedReranker")
    void shouldFallbackWhenExternalThrows() {
        WeightedReranker weighted = new WeightedReranker();
        ReflectionTestUtils.setField(weighted, "topNFinal", 5);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(true);
        Reranker reranker = new Reranker(weighted, properties);
        ReflectionTestUtils.setField(reranker, "topNFinal", 5);
        QwenRerankClient client = mock(QwenRerankClient.class);
        ReflectionTestUtils.setField(reranker, "qwenRerankClient", client);
        when(client.rerank(anyString(), any(), anyInt())).thenThrow(new RuntimeException("timeout"));

        List<ScoredDoc> result = reranker.rerank(sampleDocs(), "Mi14 camera");

        assertEquals(2, result.size());
        assertFalse(result.stream().allMatch(d -> d.getFinalScore() == 0.0));
    }

    @Test
    @DisplayName("RECALL-005b 外部 rerank 返回空时自动降级 WeightedReranker")
    void shouldFallbackWhenExternalReturnsEmpty() {
        WeightedReranker weighted = new WeightedReranker();
        ReflectionTestUtils.setField(weighted, "topNFinal", 5);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(true);
        Reranker reranker = new Reranker(weighted, properties);
        ReflectionTestUtils.setField(reranker, "topNFinal", 5);
        QwenRerankClient client = mock(QwenRerankClient.class);
        ReflectionTestUtils.setField(reranker, "qwenRerankClient", client);
        when(client.rerank(anyString(), any(), anyInt())).thenReturn(List.of());

        List<ScoredDoc> result = reranker.rerank(sampleDocs(), "Mi14 camera");

        assertEquals(2, result.size());
        assertFalse(result.stream().allMatch(d -> d.getFinalScore() == 0.0));
    }

    @Test
    @DisplayName("RECALL-005b rerank.enabled=false 时不调用外部客户端，直接自研加权")
    void shouldUseWeightedWhenExternalDisabled() {
        WeightedReranker weighted = new WeightedReranker();
        ReflectionTestUtils.setField(weighted, "topNFinal", 5);
        RerankProperties properties = new RerankProperties();
        properties.setEnabled(false);
        Reranker reranker = new Reranker(weighted, properties);
        ReflectionTestUtils.setField(reranker, "topNFinal", 5);
        QwenRerankClient client = mock(QwenRerankClient.class);
        ReflectionTestUtils.setField(reranker, "qwenRerankClient", client);

        List<ScoredDoc> result = reranker.rerank(sampleDocs(), "Mi14 camera");

        assertEquals(2, result.size());
        assertFalse(result.stream().allMatch(d -> d.getFinalScore() == 0.0));
        verifyNoInteractions(client);
    }

    private List<ScoredDoc> sampleDocs() {
        return List.of(
                ScoredDoc.builder().id(1L).content("Mi14 camera").title("Mi14").simScore(0.7).kwScore(0.5).build(),
                ScoredDoc.builder().id(2L).content("Redmi battery").simScore(0.6).kwScore(0.3).build()
        );
    }
}
