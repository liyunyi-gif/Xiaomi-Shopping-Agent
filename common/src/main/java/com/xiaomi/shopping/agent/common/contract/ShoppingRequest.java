package com.xiaomi.shopping.agent.common.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 主 Agent → Shopping 子节点的请求（架构.md §8.2）
 * <p>
 * 注入信息：明确购买/操作意图 + 槽位（商品、数量等）+ 会话快照。
 * Shopping 是无状态能力网关，禁止意图识别与业务判断（能力正交 P5、边界红线）。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingRequest implements Serializable {

    /** 明确的操作意图（如 add_cart / place_order / query_logistics） */
    private String action;

    /** 槽位：商品/规格/数量/收货地址等（主 Agent 已澄清完成） */
    private Map<String, Object> slots;

    /** 会话快照 */
    private SessionSnapshot snapshot;
}
