package com.xiaomi.shopping.agent.common.entity.product;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品类目表（t_category）—— 10 大一级品类 + 二级。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends LogicDeleteEntity {

    /** 类目编码（唯一） */
    private String categoryCode;

    /** 类目名称 */
    private String name;

    /** 层级：1 一级 / 2 二级 */
    private Integer level;

    /** 父类目ID */
    private Long parentId;

    /** 排序 */
    private Integer sortOrder;
}
