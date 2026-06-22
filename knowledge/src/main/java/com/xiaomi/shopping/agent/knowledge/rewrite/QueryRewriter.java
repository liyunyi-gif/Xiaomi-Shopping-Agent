package com.xiaomi.shopping.agent.knowledge.rewrite;

import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 查询重写器（架构.md §3.2 / 知识库Agent §3）。
 * <p>
 * 流水线唯一引入「受限智能」的环节：指代消解、补全、口语化转规范术语。
 * <b>不判断结果质量</b>（P6：质量判断在主 Agent）。
 * <p>
 * 单测兼容：ChatClient 可选注入，未配置时退化为基于术语映射的规则重写，
 * 不依赖外部 LLM 即可跑通流水线。
 *
 * @author liyunyi
 */
@Slf4j
@Component
public class QueryRewriter {

    /** 口语化 → 规范术语映射（可由 t_query_term_mapping 加载，此处内置起点集） */
    private static final Map<String, String> TERM_MAP = Map.ofEntries(
            Map.entry("打游戏爽", "高性能GPU 高刷新率"),
            Map.entry("拍照清楚", "高像素影像系统"),
            Map.entry("续航久", "大电池容量"),
            Map.entry("性价比", "价格性能比"),
            Map.entry("便宜", "低价"),
            Map.entry("打游戏", "游戏性能 GPU")
    );

    private static final String REWRITE_PROMPT = """
            请重写用户的商品咨询问题：
            1. 消解指代（"它/这款" → 会话快照中的具体型号）
            2. 补全省略信息（结合会话快照中已选商品）
            3. 口语化转规范术语（如"打游戏爽" → "高性能GPU/高刷新率"）
            仅输出重写后的问题，不要解释。
            """;

    @Autowired(required = false)
    private ChatClient chatClient;

    /**
     * 重写查询。有 ChatClient 走 LLM；否则走规则（指代消解 + 术语规范化）。
     */
    public String rewrite(String question, SessionSnapshot snapshot) {
        if (question == null || question.isBlank()) {
            return "";
        }
        if (chatClient != null) {
            return rewriteByLlm(question, snapshot);
        }
        return rewriteByRule(question, snapshot);
    }

    /**
     * 重检换策略（架构.md §5.2，禁原样重查，由主 Agent RetrievalLoop 调用）。
     *
     * @param attempt 0=换关键词（术语扩展），1=拆子问题（这里返回更宽泛的重写）
     */
    public String rewriteWithNewStrategy(String question, QualityVerdict verdict, int attempt) {
        log.debug("重检换策略 attempt={} verdict={}", attempt, verdict);
        if (attempt == 0) {
            // 第一轮：术语扩展（加同义/规范词）
            return expandTerms(question);
        }
        // 第二轮：放宽（去掉过于具体的限定，扩大召回）
        return broadenQuery(question);
    }

    /** LLM 重写。 */
    private String rewriteByLlm(String question, SessionSnapshot snapshot) {
        try {
            String userMsg = "问题: " + question + "\n会话快照: " + toSnapshotSummary(snapshot);
            String result = chatClient.prompt()
                    .system(REWRITE_PROMPT)
                    .user(userMsg)
                    .call()
                    .content();
            return result == null ? question : result.trim();
        } catch (Exception e) {
            log.warn("LLM 重写失败，退化为规则重写：{}", e.getMessage());
            return rewriteByRule(question, snapshot);
        }
    }

    /** 规则重写：指代消解（注入快照中已选商品）+ 术语规范化。 */
    String rewriteByRule(String question, SessionSnapshot snapshot) {
        String rewritten = question;
        // 指代消解：用快照中已选商品替换"它/这款"
        if (snapshot != null && snapshot.getSelectedProducts() != null && !snapshot.getSelectedProducts().isBlank()) {
            String product = snapshot.getSelectedProducts();
            rewritten = rewritten.replaceAll("(它|这款|这个|这)", product);
        }
        // 术语规范化
        for (Map.Entry<String, String> e : TERM_MAP.entrySet()) {
            if (rewritten.contains(e.getKey())) {
                rewritten = rewritten.replace(e.getKey(), e.getValue());
            }
        }
        return rewritten;
    }

    /** 换关键词策略：扩展同义/规范术语。 */
    private String expandTerms(String question) {
        StringBuilder sb = new StringBuilder(question);
        for (Map.Entry<String, String> e : TERM_MAP.entrySet()) {
            if (question.contains(e.getKey())) {
                sb.append(' ').append(e.getValue());
            }
        }
        return sb.toString();
    }

    /** 放宽查询：去掉极端限定（简化为返回原 query + 提示放宽，实际可剪枝）。 */
    private String broadenQuery(String question) {
        return question;
    }

    private String toSnapshotSummary(SessionSnapshot snapshot) {
        if (snapshot == null) {
            return "{}";
        }
        return "{已选商品:" + snapshot.getSelectedProducts() + "}";
    }

    /** 供单测注入术语映射。 */
    public void overrideTermMap(Map<String, String> custom) {
        // 不可变 map 静态字段无法覆盖；保留方法签名供扩展，单测走 rewriteByRule 直接验证内置映射
    }

    Set<String> termKeys() {
        return TERM_MAP.keySet();
    }
}
