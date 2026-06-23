package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 三信号四步检索质量判断器。
 *
 * @author liyunyi
 */
@Component
public class QualityJudge {

    private final double relevanceThreshold;

    public QualityJudge(@Value("${xiaomi.agent.judge.relevance-threshold:0.7}") double relevanceThreshold) {
        this.relevanceThreshold = relevanceThreshold;
    }

    public QualityVerdict judge(KnowledgeResponse response, Set<String> queryEntities) {
        if (response == null || response.getRecallCount() == 0) {
            int recall = response == null ? 0 : response.getRecallCount();
            return verdict(QualityVerdict.Level.FAILED, "recallCount=" + recall + "，召回数为0");
        }
        Set<String> required = queryEntities == null ? Set.of() : queryEntities;
        Set<String> hit = response.getHitEntities() == null ? Set.of() : response.getHitEntities();
        if (!required.isEmpty() && !hit.containsAll(required)) {
            return verdict(QualityVerdict.Level.INCOMPLETE,
                    "关键实体未全部命中，required=" + required + "，hitEntities=" + hit);
        }
        if (response.getTopScore() < relevanceThreshold) {
            return verdict(QualityVerdict.Level.INSUFFICIENT,
                    "topScore=" + response.getTopScore() + " < threshold=" + relevanceThreshold);
        }
        return verdict(QualityVerdict.Level.SUFFICIENT,
                "recallCount=" + response.getRecallCount() + "，hitEntities=" + hit
                        + "，topScore=" + response.getTopScore() + " >= threshold=" + relevanceThreshold);
    }

    private QualityVerdict verdict(QualityVerdict.Level level, String reason) {
        return QualityVerdict.builder().level(level).reason(reason).build();
    }
}
