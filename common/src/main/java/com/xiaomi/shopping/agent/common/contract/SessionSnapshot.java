package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 会话快照（架构.md §8.3）
 * <p>
 * 由主 Agent 持有，注入给所有子节点，保证「记忆单点」(P3)：
 * 子节点无状态，每次调用都由主 Agent 注入快照，调用结束即释放。
 * <p>
 * 快照内容：用户标识 / 当前意图 / 已选在看商品 / 购物车 / 浏览历史。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSnapshot implements Serializable {

    /** 用户标识 */
    private String userId;

    /** 会话标识 */
    private String conversationId;

    /** 当前意图（主 Agent 识别后的明确意图） */
    private String currentIntent;

    /** 当前会话最近若干轮原文（短期记忆 ① 的轻量镜像，供子节点补全上下文） */
    private String recentContext;

    /** 已选/在看的商品（SKU 标识集合，逗号分隔或结构化） */
    private String selectedProducts;

    /** 购物车状态（商品-数量映射等） */
    private Map<String, Object> cartState;

    /** 浏览历史（最近浏览的商品标识） */
    private String browseHistory;
}
