package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 意图识别结果（架构.md §3.1 / §8）
 * <p>
 * 一级意图三类：知识库问答 / 工具调用 / 系统指令（不可变，P1 唯一入口）。
 * 低置信度时触发澄清反问（主 Agent 唯一开口权，P2）。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {

    /** 一级意图枚举（KNOWLEDGE / TOOL / SYSTEM） */
    private IntentType primaryIntent;

    /** 二级意图（如：商品推荐/参数咨询/加购/下单/物流查询，落地清单见 init.sql 意图树） */
    private String secondaryIntent;

    /** 置信度 [0,1] */
    private double confidence;

    /** 是否需要澄清（低置信度或意图模糊时为 true） */
    private boolean needClarify;

    /** 从问题中抽取的关键实体集合（型号/参数等，供质量判断的「命中实体」信号使用） */
    private Set<String> entities;

    /**
     * 一级意图类型。
     */
    public enum IntentType {
        /** 知识库问答（检索型能力，委派 Knowledge） */
        KNOWLEDGE,
        /** 工具调用（外部调用型能力，委派 Shopping） */
        TOOL,
        /** 系统指令（清除记忆/查看历史等，主 Agent 自处理） */
        SYSTEM
    }
}
