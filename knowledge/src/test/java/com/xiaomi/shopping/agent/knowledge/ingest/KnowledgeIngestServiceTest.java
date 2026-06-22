package com.xiaomi.shopping.agent.knowledge.ingest;

import com.xiaomi.shopping.agent.common.entity.knowledge.KnowledgeChunk;
import com.xiaomi.shopping.agent.knowledge.ingest.mapper.KnowledgeChunkMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KnowledgeIngestService 非联调单测（对应 Test/01 INGEST-001/002/005/008）。
 *
 * @author liyunyi
 */
class KnowledgeIngestServiceTest {

    @Test
    @DisplayName("INGEST-002 TXT/HTML 可解析为纯文本，HTML 标签被剥离")
    void shouldParseTxtAndHtmlToPlainText() {
        KnowledgeIngestService service = new KnowledgeIngestService(new ChunkSplitter(), mock(KnowledgeChunkMapper.class));

        String txt = service.parseToString(stream("小米14 徕卡 5000万像素"));
        String html = service.parseToString(stream("<html><body><h1>小米14</h1><p>徕卡 5000万像素</p></body></html>"));

        assertTrue(txt.contains("小米14"));
        assertTrue(txt.contains("徕卡"));
        assertTrue(html.contains("小米14"));
        assertTrue(html.contains("徕卡"));
        assertFalse(html.contains("<h1>"), "HTML 标签应被剥离");
    }

    @Test
    @DisplayName("INGEST-005 入库写入 chunk 文本、元数据、charCount 与连续 chunkIndex")
    void shouldPersistChunksWithMetadata() {
        KnowledgeChunkMapper mapper = mock(KnowledgeChunkMapper.class);
        when(mapper.insert(any(KnowledgeChunk.class))).thenReturn(1);
        KnowledgeIngestService service = new KnowledgeIngestService(new ChunkSplitter(), mapper);

        int count = service.ingest(stream("第一段小米14影像信息。\n第二段小米14续航信息。"),
                1L, 2L, 3L, "小米14", "16GB 512GB", 12, 0);

        ArgumentCaptor<KnowledgeChunk> captor = ArgumentCaptor.forClass(KnowledgeChunk.class);
        verify(mapper, times(count)).insert(captor.capture());
        List<KnowledgeChunk> chunks = captor.getAllValues();

        assertTrue(count > 1, "小 maxChars 应产生多切片");
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            assertEquals(1L, chunk.getKbId());
            assertEquals(2L, chunk.getDocId());
            assertEquals(3L, chunk.getSpuId());
            assertEquals(i, chunk.getChunkIndex());
            assertEquals("小米14", chunk.getTitle());
            assertEquals("16GB 512GB", chunk.getSpecText());
            assertEquals(chunk.getContent().length(), chunk.getCharCount());
            assertEquals(1, chunk.getEnabled());
        }
    }

    @Test
    @DisplayName("INGEST-008 空文本入库返回 0 切片且不写库")
    void shouldReturnZeroForBlankDocument() {
        KnowledgeChunkMapper mapper = mock(KnowledgeChunkMapper.class);
        KnowledgeIngestService service = new KnowledgeIngestService(new ChunkSplitter(), mapper);

        int count = service.ingest(stream("   "), 1L, 2L, null, "空文档", "", 100, 10);

        assertEquals(0, count);
        verify(mapper, times(0)).insert(any(KnowledgeChunk.class));
    }

    private ByteArrayInputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
