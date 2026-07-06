package com.xiaomi.shopping.agent.orchestrator.service;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.QualityVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Orchestrator 面向上层的 ChatReply 处理结果。
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
    private String clarifyQuestion;
    private QualityVerdict.Level qualityLevel;
    private int retryCount;
    private int childCalls;
    private Map<String, Object> data;
    private String traceId;
}
