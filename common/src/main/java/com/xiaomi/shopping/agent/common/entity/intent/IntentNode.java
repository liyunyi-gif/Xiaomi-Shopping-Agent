package com.xiaomi.shopping.agent.common.entity.intent;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 意图树表（t_intent_node）—— 3 一级 / N 二级。
 * <p>
 * kind: 0 KNOWLEDGE / 1 SHOPPING / 2 SYSTEM（架构.md §3.1 一级意图三类）。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_intent_node")
public class IntentNode extends LogicDeleteEntity {

    /** 所属知识库ID */
    private Long kbId;

    /** 意图编码（唯一） */
    private String intentCode;

    /** 意图名称 */
    private String name;

    /** 层级：1 一级 / 2 二级 */
    private Integer level;

    /** 父意图编码 */
    private String parentCode;

    /** 类型：0 KNOWLEDGE / 1 SHOPPING / 2 SYSTEM */
    private Integer kind;

    /** 描述 */
    private String description;

    /** 示例 query */
    private String examples;

    /** 召回 Top-K */
    private Integer topK;

    /** 购物动作：ADD_TO_CART/PLACE_ORDER/QUERY_LOGISTICS/QUERY_STOCK/QUERY_PROMOTION */
    private String shoppingAction;

    /** Prompt 片段 */
    private String promptSnippet;

    /** 排序 */
    private Integer sortOrder;

    /** 是否启用 */
    private Integer enabled;
}
