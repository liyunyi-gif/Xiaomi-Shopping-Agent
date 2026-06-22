package com.xiaomi.shopping.agent.knowledge.ingest;

import com.xiaomi.shopping.agent.common.entity.knowledge.KnowledgeChunk;
import com.xiaomi.shopping.agent.knowledge.ingest.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 知识库构建服务（架构.md §3.2 / 知识库Agent §2）。
 * <p>
 * 管线：Tika 解析 → 切片 → 写 t_knowledge_chunk（文本/title/spec_text）→ 写 t_knowledge_vector（向量）。
 * <p>
 * 子节点约束：无状态、不做意图识别、不判质量（仅负责入库）。
 *
 * @author liyunyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestService {

    private final ChunkSplitter chunkSplitter;
    private final KnowledgeChunkMapper chunkMapper;
    private final Tika tika = new Tika();

    /**
     * 向量存储（可选注入）。语义路向量入库用；未配置时仅写文本切片（便于单测/降级）。
     * VectorStore 默认表结构与 t_knowledge_vector 自定义列的对齐在集成阶段处理。
     */
    @Autowired(required = false)
    private VectorStore vectorStore;

    /**
     * 入库一份文档：解析→切片→落库。
     *
     * @param doc      文档输入流（PDF/Word/HTML/TXT）
     * @param kbId     知识库ID
     * @param docId    文档ID
     * @param spuId    关联 SPU ID（可空）
     * @param title    标题（rerank 字段加权列）
     * @param specText 规格摘要（rerank 字段加权列）
     * @param maxChars 单切片字符上限
     * @param overlap  相邻切片重叠字符数
     * @return 切片数
     */
    public int ingest(InputStream doc, Long kbId, Long docId, Long spuId,
                      String title, String specText,
                      int maxChars, int overlap) {
        String text = parseToString(doc);
        List<String> chunks = chunkSplitter.split(text, maxChars, overlap);
        log.info("入库：kbId={} docId={} 切片数={}", kbId, docId, chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            // ① 文本/标题/规格写入 t_knowledge_chunk（tsv 由触发器自动维护）
            KnowledgeChunk entity = new KnowledgeChunk();
            entity.setKbId(kbId);
            entity.setDocId(docId);
            entity.setSpuId(spuId);
            entity.setChunkIndex(i);
            entity.setContent(chunk);
            entity.setTitle(title);
            entity.setSpecText(specText);
            entity.setCharCount(chunk.length());
            entity.setEnabled(1);
            chunkMapper.insert(entity);
            Long chunkId = entity.getId();

            // ② 向量写入（VectorStore 可选；失败不中断整批，对齐 INGEST-008）
            writeToVectorStore(chunk, chunkId, kbId, spuId);
        }
        return chunks.size();
    }

    /** Tika 解析为纯文本。 */
    public String parseToString(InputStream doc) {
        try {
            return tika.parseToString(doc);
        } catch (Exception e) {
            throw new IllegalStateException("Tika 解析失败：" + e.getMessage(), e);
        }
    }

    /** 向量入库（VectorStore 存在时）；失败仅记录，不抛出。 */
    private void writeToVectorStore(String chunk, Long chunkId, Long kbId, Long spuId) {
        if (vectorStore == null) {
            return;
        }
        try {
            Document d = new Document(chunk, Map.of(
                    "chunk_id", String.valueOf(chunkId),
                    "kb_id", kbId == null ? "" : String.valueOf(kbId),
                    "spu_id", spuId == null ? "" : String.valueOf(spuId)
            ));
            vectorStore.add(List.of(d));
        } catch (Exception e) {
            log.warn("向量入库失败 chunkId={}（不中断整批）：{}", chunkId, e.getMessage());
        }
    }
}
