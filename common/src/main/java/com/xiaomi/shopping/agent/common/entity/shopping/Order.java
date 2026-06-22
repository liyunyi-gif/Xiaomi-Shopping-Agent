package com.xiaomi.shopping.agent.common.entity.shopping;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单表（t_order）—— Shopping place_order 操作。
 * <p>
 * status: pending/paid/shipped/done/cancelled。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends LogicDeleteEntity {

    /** 订单号（唯一） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 收货地址 */
    private String address;

    /** 支付方式 */
    private String payMethod;

    /** 状态：pending/paid/shipped/done/cancelled */
    private String status;

    /** 物流单号 */
    private String logisticsNo;
}
