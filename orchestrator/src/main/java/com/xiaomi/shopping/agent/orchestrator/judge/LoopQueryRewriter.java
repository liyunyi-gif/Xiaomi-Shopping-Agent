package com.xiaomi.shopping.agent.orchestrator.judge;

import com.xiaomi.shopping.agent.common.contract.QualityVerdict;

import java.util.Set;

/**
 * 重检查询改写接口。
 *
 * @author liyunyi
 */
public interface LoopQueryRewriter {

    String rewrite(String query, QualityVerdict verdict, int attempt, Set<String> entities);
}
