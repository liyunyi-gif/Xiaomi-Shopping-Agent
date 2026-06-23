package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 主 Agent → Knowledge 子节点的请求（架构.md §8.1）
 * <p>
 * 注入信息：明确意图（知识查询）+ 原始问题 + 会话快照。
 * Knowledge 是无状态能力层，所有上下文都由主 Agent 注入。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRequest implements Serializable {

    /** 原始问题（已含会话上下文） */
    private String question;

    /** 明确意图（二级意图，如 商品推荐/参数咨询） */
    private String intent;

    /** 会话快照（提供用户/会话/上下文，供子节点补全） */
    private SessionSnapshot snapshot;

    /** 本轮重检已用的策略序号（主 Agent 传入，供 Knowledge 选择不同重写策略，对齐 P7 换策略重检） */
    private int retryAttempt;

    /** 主 Agent 抽取并显式注入的查询实体，供 Knowledge 生成命中信号并与 QualityJudge 对齐 */
    private Set<String> queryEntities;
}
