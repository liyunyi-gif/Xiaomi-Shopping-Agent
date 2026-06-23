package com.xiaomi.shopping.agent.orchestrator.response;

import com.xiaomi.shopping.agent.common.contract.KnowledgeResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 基于检索结果的确定性回答组装器。
 *
 * @author liyunyi
 */
@Component
public class AnswerComposer {

    public String compose(KnowledgeResponse response) {
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            return degraded();
        }
        String context = response.getResults().stream()
                .limit(3)
                .map(KnowledgeResponse.RetrievalItem::getContent)
                .collect(Collectors.joining("；"));
        return "根据当前知识库资料：" + context;
    }

    public String degraded() {
        return "抱歉，当前资料不足以确认该问题。请补充具体型号或想了解的参数，我再帮你查询。";
    }
}
