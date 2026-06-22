package com.xiaomi.shopping.agent.knowledge.ingest;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本切片器（架构.md §3.2 / 知识库Agent §2.1）。
 * <p>
 * 切片策略：按段落切，单 chunk 控制在 maxChars 内（粗略对应 token 上限），
 * 相邻 chunk 保留 overlapChars 重叠，避免跨段信息断裂。尽量在段落边界切。
 *
 * @author liyunyi
 */
@Component
public class ChunkSplitter {

    /** 段落分隔正则：连续换行或单换行均可作为段落边界 */
    private static final String PARAGRAPH_SPLIT = "\\R{1,2}";

    /**
     * 按字符数切片（含重叠）。
     *
     * @param text         原始文本
     * @param maxChars     单切片字符上限（对应 token 上限，如 400 字符 ≈ 200 token）
     * @param overlapChars 相邻切片重叠字符数
     * @return 切片列表（顺序可还原原文）
     */
    public List<String> split(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars 必须为正");
        }

        // 先按段落拆，再按 maxChars 聚合
        String[] paragraphs = text.trim().split(PARAGRAPH_SPLIT);
        StringBuilder buffer = new StringBuilder();

        for (String para : paragraphs) {
            String p = para.trim();
            if (p.isEmpty()) {
                continue;
            }
            // 单段超长：硬切（保证不超 maxChars）
            if (p.length() > maxChars) {
                flushBuffer(buffer, chunks, maxChars, overlapChars);
                chunks.addAll(hardSplit(p, maxChars, overlapChars));
                continue;
            }
            // 聚合：加入后超限则先 flush
            if (buffer.length() + p.length() + 1 > maxChars) {
                flushBuffer(buffer, chunks, maxChars, overlapChars);
                // 重叠：取上一 chunk 末尾作为新 buffer 起始
                seedOverlap(buffer, chunks, overlapChars);
            }
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(p);
        }
        flushBuffer(buffer, chunks, maxChars, overlapChars);
        return chunks;
    }

    private void flushBuffer(StringBuilder buffer, List<String> chunks,
                             int maxChars, int overlapChars) {
        if (buffer.length() > 0) {
            chunks.add(buffer.toString());
            buffer.setLength(0);
        }
    }

    /** 取已生成最后一个 chunk 的末尾 overlapChars 作为下一个 chunk 的起始（重叠）。 */
    private void seedOverlap(StringBuilder buffer, List<String> chunks, int overlapChars) {
        if (overlapChars <= 0 || chunks.isEmpty()) {
            return;
        }
        String last = chunks.get(chunks.size() - 1);
        int start = Math.max(0, last.length() - overlapChars);
        buffer.append(last, start, last.length());
    }

    /** 硬切超长段落：固定窗口 + 重叠滑动。 */
    private List<String> hardSplit(String longText, int maxChars, int overlapChars) {
        List<String> parts = new ArrayList<>();
        int step = Math.max(1, maxChars - overlapChars);
        int i = 0;
        while (i < longText.length()) {
            int end = Math.min(longText.length(), i + maxChars);
            parts.add(longText.substring(i, end));
            if (end >= longText.length()) {
                break;
            }
            i += step;
        }
        return parts;
    }
}
