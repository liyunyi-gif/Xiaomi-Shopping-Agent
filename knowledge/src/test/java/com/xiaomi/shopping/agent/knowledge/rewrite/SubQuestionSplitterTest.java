package com.xiaomi.shopping.agent.knowledge.rewrite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SubQuestionSplitter 单测（对应 test/02 RECALL-003 子问题拆分）。
 *
 * @author liyunyi
 */
class SubQuestionSplitterTest {

    private final SubQuestionSplitter splitter = new SubQuestionSplitter();

    @Test
    @DisplayName("RECALL-003 含连接词的问题拆为多个子问题")
    void shouldSplitByConjunction() {
        List<String> subs = splitter.split("小米14的影像和续航");
        assertEquals(2, subs.size(), "应拆为 2 个子问题");
    }

    @Test
    @DisplayName("单问题返回单元素列表")
    void shouldReturnSingleForSimpleQuestion() {
        List<String> subs = splitter.split("小米14的价格");
        assertEquals(1, subs.size());
    }

    @Test
    @DisplayName("空输入返回空")
    void shouldReturnEmptyForBlank() {
        assertTrue(splitter.split(null).isEmpty());
        assertTrue(splitter.split("").isEmpty());
    }

    @Test
    @DisplayName("对比类问题拆分")
    void shouldSplitComparison() {
        List<String> subs = splitter.split("小米14对比iPhone15");
        assertEquals(2, subs.size());
    }
}
