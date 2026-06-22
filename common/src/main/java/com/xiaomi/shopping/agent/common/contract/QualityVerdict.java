package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 检索质量判定结果（架构.md §5）
 * <p>
 * 主 Agent 基于三信号（相关度分数/命中实体/召回数）的 4 步组合判定。
 * 这是本架构区别于普通检索增强问答的核心（纯自研，简历重点 ★）。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityVerdict implements Serializable {

    /** 判定等级 */
    private Level level;

    /** 触发该判定的信号明细（用于可观测与调试） */
    private String reason;

    /**
     * 质量判定等级（§5.1 三信号组合判定优先级）：
     * <ol>
     *   <li>召回数为 0 → FAILED</li>
     *   <li>召回非 0，关键实体未命中 → INCOMPLETE（不全）</li>
     *   <li>实体命中，但相关度分数低于阈值 → INSUFFICIENT（不够）</li>
     *   <li>三者皆满足 → SUFFICIENT（充分），直出答案</li>
     * </ol>
     */
    public enum Level {
        /** 召回 0，失败 */
        FAILED,
        /** 实体未命中，不全 */
        INCOMPLETE,
        /** 分数低于阈值，不够 */
        INSUFFICIENT,
        /** 充分，可直出 */
        SUFFICIENT
    }

    public boolean isSufficient() {
        return level == Level.SUFFICIENT;
    }
}
