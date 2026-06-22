package com.xiaomi.shopping.agent.knowledge.rewrite;

import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * QueryRewriter 单测（对应 test/02 RECALL-002 查询重写·指代消解与术语规范化）。
 * <p>
 * 无 ChatClient 时走规则重写，验证指代消解 + 术语规范化。
 *
 * @author liyunyi
 */
class QueryRewriterTest {

    private final QueryRewriter rewriter = new QueryRewriter();

    @Test
    @DisplayName("RECALL-002 指代消解：'它' → 快照中已选商品")
    void shouldResolveCoreference() {
        SessionSnapshot snapshot = SessionSnapshot.builder()
                .selectedProducts("小米14")
                .build();
        String rewritten = rewriter.rewrite("它的续航多久", snapshot);
        assertTrue(rewritten.contains("小米14"), "应消解指代为 小米14: " + rewritten);
    }

    @Test
    @DisplayName("RECALL-002 术语规范化：'打游戏爽' → 高性能GPU/高刷新率")
    void shouldNormalizeColloquialTerms() {
        String rewritten = rewriter.rewrite("打游戏爽的手机", null);
        assertTrue(rewritten.contains("GPU") || rewritten.contains("刷新率"),
                "应规范化术语: " + rewritten);
    }

    @Test
    @DisplayName("空输入返回空串")
    void shouldReturnEmptyForBlank() {
        assertTrue(rewriter.rewrite(null, null).isEmpty());
        assertTrue(rewriter.rewrite("", null).isEmpty());
    }

    @Test
    @DisplayName("重检换策略：attempt=0 扩展术语")
    void shouldExpandTermsOnRetry() {
        String expanded = rewriter.rewriteWithNewStrategy("打游戏爽", null, 0);
        assertTrue(expanded.length() >= "打游戏爽".length(), "术语扩展后应更长或等长");
        assertTrue(expanded.contains("打游戏爽"), "应保留原词");
    }
}
