package com.xiaomi.shopping.agent.common.entity.shopping;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 购物车项表（t_cart_item）—— Shopping add_to_cart 操作。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cart_item")
public class CartItem extends LogicDeleteEntity {

    /** 购物车ID */
    private String cartId;

    /** 用户ID */
    private Long userId;

    /** SKU ID */
    private Long skuId;

    /** 数量 */
    private Integer quantity;

    /** 是否选中 */
    private Integer selected;

    /** 加入时间 */
    @TableField("added_at")
    private LocalDateTime addedAt;
}
