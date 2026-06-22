package com.xiaomi.shopping.agent.common.entity.intent;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 术语归一化映射表（t_query_term_mapping）—— 查询重写口语→规范。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_query_term_mapping")
public class QueryTermMapping extends LogicDeleteEntity {

    /** 领域 */
    private String domain;

    /** 源术语（口语/别名） */
    private String sourceTerm;

    /** 目标术语（规范） */
    private String targetTerm;

    /** 匹配类型：1 精确 / 2 模糊 */
    private Integer matchType;

    /** 优先级 */
    private Integer priority;

    /** 是否启用 */
    private Integer enabled;
}
