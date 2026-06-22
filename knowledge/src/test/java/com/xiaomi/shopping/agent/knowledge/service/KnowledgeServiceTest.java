package com.xiaomi.shopping.agent.knowledge.service;

import com.xiaomi.shopping.agent.common.contract.KnowledgeRequest;
import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.knowledge.entityextract.EntityExtractor;
import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import com.xiaomi.shopping.agent.knowledge.recall.DualChannelRecaller;
import com.xiaomi.shopping.agent.knowledge.rerank.Reranker;
import com.xiaomi.shopping.agent.knowledge.rewrite.QueryRewriter;
import com.xiaomi.shopping.agent.knowledge.signal.ConfidenceSignalBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KnowledgeService 编排单测（对应 Test/02 RECALL-006、Test/07 FLOW-001 中 Knowledge 非联调部分）。
 *
 * @author liyunyi
 */
class KnowledgeServiceTest {

    @Test
    @DisplayName("RECALL-006 KnowledgeService 串联重写→双路召回→rerank→三信号")
    void shouldOrchestrateKnowledgePipeline() {
        QueryRewriter queryRewriter = mock(QueryRewriter.class);
        DualChannelRecaller dualChannelRecaller = mock(DualChannelRecaller.class);
        Reranker reranker = mock(Reranker.class);
        ConfidenceSignalBuilder signalBuilder = mock(ConfidenceSignalBuilder.class);
        EntityExtractor entityExtractor = mock(EntityExtractor.class);
        KnowledgeService service = new KnowledgeService(queryRewriter, dualChannelRecaller, reranker, signalBuilder, entityExtractor);
        ReflectionTestUtils.setField(service, "recallTopK", 20);

        List<ScoredDoc> recalled = List.of(ScoredDoc.builder().id(1L).content("Mi14 camera").build());
        List<ScoredDoc> reranked = List.of(ScoredDoc.builder().id(1L).content("Mi14 camera").finalScore(0.88).build());
        KnowledgeResponse expected = KnowledgeResponse.builder()
                .recallCount(1)
                .topScore(0.88)
                .hitEntities(Set.of("Mi14"))
                .results(List.of(KnowledgeResponse.RetrievalItem.builder()
                        .sourceId("1").content("Mi14 camera").score(0.88).hitType("keyword").build()))
                .build();

        when(queryRewriter.rewrite("Mi14 camera", null)).thenReturn("Mi14 camera rewritten");
        when(dualChannelRecaller.recallParallel("Mi14 camera rewritten", 20)).thenReturn(recalled);
        when(reranker.rerank(recalled, "Mi14 camera rewritten")).thenReturn(reranked);
        when(signalBuilder.build(reranked, Set.of("Mi14"))).thenReturn(expected);

        KnowledgeResponse actual = service.ask("Mi14 camera", null, Set.of("Mi14"));

        assertEquals(expected, actual);
        verify(queryRewriter).rewrite("Mi14 camera", null);
        verify(dualChannelRecaller).recallParallel("Mi14 camera rewritten", 20);
        verify(reranker).rerank(recalled, "Mi14 camera rewritten");
        verify(signalBuilder).build(reranked, Set.of("Mi14"));
    }

    @Test
    @DisplayName("CONTRACT-001 KnowledgeRequest 入口会兜底抽取实体并返回客观信号")
    void shouldAcceptKnowledgeRequestAndExtractEntities() {
        QueryRewriter queryRewriter = mock(QueryRewriter.class);
        DualChannelRecaller dualChannelRecaller = mock(DualChannelRecaller.class);
        Reranker reranker = mock(Reranker.class);
        ConfidenceSignalBuilder signalBuilder = mock(ConfidenceSignalBuilder.class);
        EntityExtractor entityExtractor = mock(EntityExtractor.class);
        KnowledgeService service = new KnowledgeService(queryRewriter, dualChannelRecaller, reranker, signalBuilder, entityExtractor);
        ReflectionTestUtils.setField(service, "recallTopK", 5);

        KnowledgeRequest request = KnowledgeRequest.builder()
                .question("小米14拍照怎么样")
                .intent("参数咨询")
                .retryAttempt(1)
                .build();
        Set<String> entities = Set.of("小米14");
        when(entityExtractor.extract("小米14拍照怎么样")).thenReturn(entities);
        when(queryRewriter.rewrite("小米14拍照怎么样", null)).thenReturn("小米14 影像");
        when(dualChannelRecaller.recallParallel("小米14 影像", 5)).thenReturn(List.of());
        when(reranker.rerank(List.of(), "小米14 影像")).thenReturn(List.of());
        when(signalBuilder.build(List.of(), entities)).thenReturn(KnowledgeResponse.builder()
                .results(List.of()).recallCount(0).topScore(0.0).hitEntities(Set.of()).build());

        KnowledgeResponse response = service.ask(request);

        assertEquals(0, response.getRecallCount());
        assertEquals(0.0, response.getTopScore(), 0.001);
        assertTrue(response.getHitEntities().isEmpty());
        verify(entityExtractor).extract("小米14拍照怎么样");
    }
}
