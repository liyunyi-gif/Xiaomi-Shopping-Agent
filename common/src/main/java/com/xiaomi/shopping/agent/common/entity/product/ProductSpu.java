package com.xiaomi.shopping.agent.common.entity.product;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品 SPU 表（t_product_spu）—— 标准产品单元。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_spu")
public class ProductSpu extends LogicDeleteEntity {

    /** SPU 编码（唯一） */
    private String spuCode;

    /** 商品名称 */
    private String name;

    /** 品牌 */
    private String brand;

    /** 类目ID */
    private Long categoryId;

    /** 副标题 */
    private String subtitle;

    /** 商品描述 */
    private String description;

    /** 状态：1 在售 / 0 下架 */
    private Integer status;
}
