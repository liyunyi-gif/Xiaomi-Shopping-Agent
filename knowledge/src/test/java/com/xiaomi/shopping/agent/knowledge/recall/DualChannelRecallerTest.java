package com.xiaomi.shopping.agent.knowledge.recall;

import com.xiaomi.shopping.agent.knowledge.model.ScoredDoc;
import com.xiaomi.shopping.agent.knowledge.recall.keyword.KeywordRecaller;
import com.xiaomi.shopping.agent.knowledge.recall.semantic.SemanticRecaller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DualChannelRecaller 单测（对应 test/02 RECALL-001 双路并行召回 + 合并去重）。
 *
 * @author liyunyi
 */
class DualChannelRecallerTest {

    @Test
    @DisplayName("RECALL-001 双路并行召回后合并去重（按 chunk id）")
    void shouldMergeAndDedupByChunkId() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);

        // 语义路：id=1,2；关键词路：id=2,3（id=2 重复，应去重）
        when(sem.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(1L).content("c1").simScore(0.8).hitType("semantic").build(),
                ScoredDoc.builder().id(2L).content("c2").simScore(0.7).hitType("semantic").build()
        ));
        when(kw.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(2L).content("c2").kwScore(0.9).title("小米14").build(),
                ScoredDoc.builder().id(3L).content("c3").kwScore(0.5).build()
        ));

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        List<ScoredDoc> merged = recaller.recallParallel("小米14", 20);

        assertEquals(3, merged.size(), "应去重后剩 3 条（id=1,2,3）");
    }

    @Test
    @DisplayName("合并时保留语义分、叠加关键词分、补全字段")
    void shouldKeepSemanticAndSupplementFields() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);

        // 同 id=1：语义路有 simScore 无 title；关键词路有 title
        when(sem.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(1L).content("c1").simScore(0.8).build()
        ));
        when(kw.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(1L).content("c1").kwScore(0.9).title("小米14").specText("16+512").build()
        ));

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        List<ScoredDoc> merged = recaller.recallParallel("q", 20);

        assertEquals(1, merged.size());
        ScoredDoc d = merged.get(0);
        assertEquals(0.8, d.getSimScore(), 0.001, "语义分应保留");
        assertEquals(0.9, d.getKwScore(), 0.001, "关键词分应叠加");
        assertEquals("小米14", d.getTitle(), "title 应从关键词路补全");
        assertEquals("16+512", d.getSpecText(), "specText 应从关键词路补全");
    }

    @Test
    @DisplayName("单路返回空不影响另一路")
    void shouldHandleEmptyChannel() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);

        when(sem.recall(anyString(), anyInt())).thenReturn(List.of());
        when(kw.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(1L).content("c1").kwScore(0.5).build()
        ));

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        List<ScoredDoc> merged = recaller.recallParallel("q", 20);

        assertEquals(1, merged.size(), "语义路空时关键词路结果应保留");
    }

    @Test
    @DisplayName("单路返回 null 按空处理不抛异常")
    void shouldHandleNullChannel() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);
        when(sem.recall(anyString(), anyInt())).thenReturn(null);
        when(kw.recall(anyString(), anyInt())).thenReturn(null);

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        List<ScoredDoc> merged = recaller.recallParallel("q", 20);
        assertEquals(0, merged.size());
    }

    @Test
    @DisplayName("RECALL-001 单路异常不影响另一路结果返回")
    void shouldIsolateChannelException() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);
        when(sem.recall(anyString(), anyInt())).thenThrow(new RuntimeException("semantic down"));
        when(kw.recall(anyString(), anyInt())).thenReturn(List.of(
                ScoredDoc.builder().id(1L).content("Redmi K70 specs").kwScore(0.8).build()
        ));

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        List<ScoredDoc> merged = recaller.recallParallel("Redmi K70", 20);

        assertEquals(1, merged.size(), "语义路异常时关键词路结果仍应保留");
        assertEquals("Redmi K70 specs", merged.get(0).getContent());
    }

    @Test
    @DisplayName("RECALL-001 双路召回都会以同一个 topK 触发")
    void shouldCallBothChannelsWithTopK() {
        SemanticRecaller sem = mock(SemanticRecaller.class);
        KeywordRecaller kw = mock(KeywordRecaller.class);
        when(sem.recall(anyString(), anyInt())).thenReturn(List.of());
        when(kw.recall(anyString(), anyInt())).thenReturn(List.of());

        DualChannelRecaller recaller = new DualChannelRecaller(sem, kw);
        recaller.recallParallel("小米14", 7);

        verify(sem).recall("小米14", 7);
        verify(kw).recall("小米14", 7);
    }
}
