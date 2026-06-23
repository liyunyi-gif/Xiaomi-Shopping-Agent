package com.xiaomi.shopping.agent.orchestrator.intent;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;

/**
 * 主 Agent 意图识别入口。
 *
 * @author liyunyi
 */
public interface IntentRecognizer {

    IntentResult recognize(String userInput, SessionSnapshot snapshot);
}
