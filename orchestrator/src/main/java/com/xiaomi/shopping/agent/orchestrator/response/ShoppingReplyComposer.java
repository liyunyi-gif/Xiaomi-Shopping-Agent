package com.xiaomi.shopping.agent.orchestrator.response;

import com.xiaomi.shopping.agent.common.contract.ShoppingResponse;
import org.springframework.stereotype.Component;

/**
 * Shopping 结果转述器。
 *
 * @author liyunyi
 */
@Component
public class ShoppingReplyComposer {

    public String compose(ShoppingResponse response) {
        if (response == null) {
            return "购物操作没有返回结果，请稍后重试。";
        }
        return switch (response.getStatus()) {
            case SUCCESS -> "已处理成功：" + response.getResultData();
            case NEED_CLARIFY -> "还需要补充信息：" + response.getMissingSlots();
            case FAILED -> "操作失败：" + response.getErrorMessage();
        };
    }
}
