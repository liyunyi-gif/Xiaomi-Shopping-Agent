package com.xiaomi.shopping.agent.web;

import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorReply;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主 Agent 对话入口。
 *
 * @author liyunyi
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final OrchestratorService orchestratorService;

    /**
     * 对话接口：Web 层只做参数兜底，业务编排全部交给 Orchestrator。
     */
    @PostMapping("/chat")
    public OrchestratorReply chat(@RequestBody ChatRequest request) {
        String userId = StringUtils.hasText(request.getUserId()) ? request.getUserId() : "demo-user";
        String conversationId = StringUtils.hasText(request.getConversationId())
                ? request.getConversationId()
                : "demo-conversation";
        return orchestratorService.handle(userId, conversationId, request.getMessage());
    }

    @Data
    public static class ChatRequest {
        private String userId;
        private String conversationId;
        private String message;
    }
}
