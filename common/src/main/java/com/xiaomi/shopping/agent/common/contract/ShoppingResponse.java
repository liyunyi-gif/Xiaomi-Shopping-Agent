package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Shopping 子节点 → 主 Agent 的响应（架构.md §8.2）
 * <p>
 * 返回结构：执行状态 + 执行结果数据 + 缺失槽位清单。
 * <p>
 * 关键约束（P4 子节点只举手不开口）：Shopping 需要澄清时<b>不直接问用户</b>，
 * 而是返回 status=NEED_CLARIFY + missingSlots 清单，由主 Agent 据此开口反问。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingResponse implements Serializable {

    /** 执行状态 */
    private ExecStatus status;

    /** 执行结果数据（如购物车标识、订单号等） */
    private Map<String, Object> resultData;

    /** 缺失槽位清单（status=NEED_CLARIFY 时声明缺哪些信息） */
    private List<String> missingSlots;

    /** 失败原因（status=FAILED 时） */
    private String errorMessage;

    /**
     * 执行状态枚举。
     */
    public enum ExecStatus {
        /** 成功 */
        SUCCESS,
        /** 需澄清（缺参数/缺库存等，由主 Agent 开口） */
        NEED_CLARIFY,
        /** 失败 */
        FAILED
    }
}
