package com.xiaomi.shopping.agent.common.entity.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品 SKU 表（t_product_sku）—— 规格单元，加购粒度。
 * <p>
 * spec_json 为 PG JSONB 类型，通过 JacksonTypeHandler 映射为 Map。
 * stock 用于 Shopping 加购缺库存判定。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_product_sku", autoResultMap = true)
public class ProductSku extends LogicDeleteEntity {

    /** SKU 编码（唯一） */
    private String skuCode;

    /** 所属 SPU ID */
    private Long spuId;

    /** 规格摘要（如 16GB+512GB 黑色） */
    private String specInfo;

    /** 规格明细 JSONB（内存/存储/颜色等） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> specJson;

    /** 价格 */
    private BigDecimal price;

    /** 库存（Shopping 加购缺库存判定用） */
    private Integer stock;

    /** 状态：1 在售 / 0 下架 */
    private Integer status;
}
