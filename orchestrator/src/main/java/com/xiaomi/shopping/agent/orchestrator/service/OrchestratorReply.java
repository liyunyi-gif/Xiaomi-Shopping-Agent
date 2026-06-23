package com.xiaomi.shopping.agent.orchestrator.service;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Orchestrator 面向上层的处理结果。
 *
 * @author liyunyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestratorReply {

    private String answer;
    private IntentResult.IntentType intent;
    private boolean needClarify;
    private QualityVerdict.Level qualityLevel;
    private int retryCount;
    private int childCalls;
}
