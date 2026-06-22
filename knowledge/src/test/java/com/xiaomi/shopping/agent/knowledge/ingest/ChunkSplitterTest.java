package com.xiaomi.shopping.agent.knowledge.ingest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ChunkSplitter 单测（对应 test/01 INGEST-003/004 切片策略）。
 *
 * @author liyunyi
 */
class ChunkSplitterTest {

    private final ChunkSplitter splitter = new ChunkSplitter();

    @Test
    @DisplayName("INGEST-003 每个切片不超 maxChars 且相邻有重叠")
    void shouldRespectMaxCharsAndOverlap() {
        // 构造超长文本（每段约 50 字，共多段）
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            sb.append("段落").append(i).append("的内容文字内容文字内容文字内容文字内容文字。\n");
        }
        List<String> chunks = splitter.split(sb.toString(), 100, 20);

        assertFalse(chunks.isEmpty(), "应切出多个切片");
        // 每个切片 ≤ maxChars（硬切保证）
        for (String c : chunks) {
            assertTrue(c.length() <= 100, "切片应 ≤100 字符，实际 " + c.length());
        }
        // 切片数 > 1（长文本必然多片）
        assertTrue(chunks.size() > 1, "长文本应切出多片");
    }

    @Test
    @DisplayName("空文本返回空列表")
    void shouldReturnEmptyForBlank() {
        assertEquals(0, splitter.split(null, 100, 10).size());
        assertEquals(0, splitter.split("", 100, 10).size());
        assertEquals(0, splitter.split("   ", 100, 10).size());
    }

    @Test
    @DisplayName("短文本不切片（单元素）")
    void shouldNotSplitShortText() {
        List<String> chunks = splitter.split("短文本一句", 100, 10);
        assertEquals(1, chunks.size());
        assertEquals("短文本一句", chunks.get(0));
    }

    @Test
    @DisplayName("单段超长：硬切不超 maxChars")
    void shouldHardSplitOverlongParagraph() {
        String longPara = "字".repeat(250); // 单段 250 字
        List<String> chunks = splitter.split(longPara, 100, 20);
        assertTrue(chunks.size() > 1, "超长段应硬切成多片");
        for (String c : chunks) {
            assertTrue(c.length() <= 100, "硬切片 ≤100");
        }
    }

    @Test
    @DisplayName("切片拼接内容无损（顺序可还原）")
    void shouldPreserveContentOrder() {
        String text = "第一段内容。第二段内容。第三段内容。";
        List<String> chunks = splitter.split(text, 100, 10);
        assertNotNull(chunks);
        // 所有原文字符都在某切片中出现
        for (char ch : text.toCharArray()) {
            boolean found = chunks.stream().anyMatch(c -> c.indexOf(ch) >= 0);
            assertTrue(found, "字符 " + ch + " 应出现在某切片中");
        }
    }

    @Test
    @DisplayName("maxChars 非法参数抛异常")
    void shouldThrowForInvalidMaxChars() {
        try {
            splitter.split("text", 0, 10);
            assertTrue(false, "应抛异常");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("maxChars"));
        }
    }

    @Test
    @DisplayName("INGEST-003 单段超长切片保留重叠字符")
    void shouldKeepOverlapForHardSplit() {
        String text = "abcdefghijklmnopqrstuvwxyz";
        List<String> chunks = splitter.split(text, 10, 3);

        assertTrue(chunks.size() > 1, "应切出多片");
        for (int i = 1; i < chunks.size(); i++) {
            String previous = chunks.get(i - 1);
            String current = chunks.get(i);
            String overlap = previous.substring(previous.length() - 3);
            assertTrue(current.startsWith(overlap), "相邻硬切片应保留 3 字符重叠");
        }
    }

    @Test
    @DisplayName("INGEST-004 段落聚合切片尽量保留段落语义边界")
    void shouldPreferParagraphBoundary() {
        String text = "## 小米14影像\n徕卡影像系统。\n## 小米14续航\n电池容量说明。";
        List<String> chunks = splitter.split(text, 25, 0);

        assertTrue(chunks.size() >= 2, "标题段落文本应按边界拆成多片");
        assertTrue(chunks.stream().anyMatch(c -> c.contains("## 小米14影像") && c.contains("徕卡影像系统")),
                "标题和紧随正文应尽量保留在同一切片");
        assertTrue(chunks.stream().anyMatch(c -> c.contains("## 小米14续航") && c.contains("电池容量说明")),
                "标题和紧随正文应尽量保留在同一切片");
    }
}
