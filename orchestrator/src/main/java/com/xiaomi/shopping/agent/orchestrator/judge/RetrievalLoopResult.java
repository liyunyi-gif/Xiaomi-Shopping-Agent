package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重检循环结果。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalLoopResult {

    private KnowledgeResponse response;
    private QualityVerdict verdict;
    private int retryCount;
    private boolean degraded;
}
