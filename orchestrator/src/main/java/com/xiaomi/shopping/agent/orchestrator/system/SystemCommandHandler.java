package com.xiaomi.shopping.agent.orchestrator.system;

import com.xiaomi.shopping.agent.orchestrator.memory.LongTermMemoryService;
import com.xiaomi.shopping.agent.orchestrator.memory.MessageArchiveService;
import com.xiaomi.shopping.agent.orchestrator.memory.ShortTermMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 系统指令处理器。
 *
 * @author liyunyi
 */
@Component
@RequiredArgsConstructor
public class SystemCommandHandler {

    private final LongTermMemoryService longTermMemoryService;
    private final ShortTermMemoryService shortTermMemoryService;
    private final MessageArchiveService messageArchiveService;

    public String handle(String userId, String conversationId, String secondaryIntent) {
        if ("记忆清除".equals(secondaryIntent)) {
            longTermMemoryService.clear(userId);
            shortTermMemoryService.clear(conversationId);
            return "已清除你的长期记忆和当前会话短期记忆。";
        }
        if ("历史回显".equals(secondaryIntent)) {
            return "当前会话历史：" + messageArchiveService.list(conversationId);
        }
        return "系统指令已收到，但当前仅支持清除记忆和查看历史。";
    }
}
