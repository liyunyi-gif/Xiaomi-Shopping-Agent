package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 规则版重检改写器，保证每次重检改变策略。
 *
 * @author liyunyi
 */
@Component
public class RuleBasedLoopQueryRewriter implements LoopQueryRewriter {

    @Override
    public String rewrite(String query, QualityVerdict verdict, int attempt, Set<String> entities) {
        String entityText = entities == null || entities.isEmpty()
                ? ""
                : entities.stream().collect(Collectors.joining(" "));
        String reason = verdict == null || verdict.getReason() == null ? "信息不足" : verdict.getReason();
        String suffix = attempt % 2 == 0
                ? " 补充同义词和关键参数检索 " + entityText
                : " 拆分子问题重点检索 " + entityText;
        String rewritten = (query == null ? "" : query) + suffix + "（上轮原因：" + reason + "）";
        return rewritten.equals(query) ? rewritten + " 重检" : rewritten;
    }
}
