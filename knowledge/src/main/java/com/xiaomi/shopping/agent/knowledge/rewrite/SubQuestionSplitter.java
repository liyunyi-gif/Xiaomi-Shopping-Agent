package com.xiaomi.shopping.agent.knowledge.rewrite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 子问题拆分器（架构.md §3.2 / 知识库Agent §3）。
 * <p>
 * 复杂问题拆为子问题并行检索后合并。单测兼容：无 ChatClient 时用启发式拆分（按"和/对比/VS"切）。
 *
 * @author liyunyi
 */
@Slf4j
@Component
public class SubQuestionSplitter {

    private static final String SPLIT_PROMPT = """
            将下面的商品咨询问题拆分为若干可独立检索的子问题，每行一个，不要编号不要解释：
            问题：%s
            """;

    /** 拆分的连接词（启发式，无 LLM 时用） */
    private static final List<String> SPLITTERS = Arrays.asList("和", "对比", "VS", "vs", "与", "以及", "还有");

    @Autowired(required = false)
    private ChatClient chatClient;

    /**
     * 拆分子问题。单问题返回单元素列表。
     */
    public List<String> split(String question) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }
        if (chatClient != null) {
            try {
                String out = chatClient.prompt()
                        .system(String.format(SPLIT_PROMPT, question))
                        .user(question)
                        .call()
                        .content();
                List<String> subs = parseLines(out);
                if (!subs.isEmpty()) {
                    return subs;
                }
            } catch (Exception e) {
                log.warn("LLM 子问题拆分失败，退化为启发式：{}", e.getMessage());
            }
        }
        return splitByHeuristic(question);
    }

    /** 启发式拆分：按连接词切成子问题。 */
    List<String> splitByHeuristic(String question) {
        String q = question;
        for (String sep : SPLITTERS) {
            int idx = q.indexOf(sep);
            if (idx > 0) {
                String left = q.substring(0, idx).trim();
                String right = q.substring(idx + sep.length()).trim();
                if (!left.isEmpty() && !right.isEmpty()) {
                    return Arrays.asList(left, right);
                }
            }
        }
        return Collections.singletonList(question);
    }

    private List<String> parseLines(String out) {
        if (out == null || out.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(out.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
