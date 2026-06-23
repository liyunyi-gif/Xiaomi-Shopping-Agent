package com.xiaomi.shopping.agent.orchestrator.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 默认上下文加载服务：长期记忆 + 短期记忆，不注入存档。
 *
 * @author liyunyi
 */
@Service
@RequiredArgsConstructor
public class MemoryLoadService {

    private final ShortTermMemoryService shortTermMemoryService;
    private final LongTermMemoryService longTermMemoryService;

    public String loadDefaultContext(String userId, String conversationId) {
        String longTerm = String.join("；", longTermMemoryService.load(userId));
        String shortTerm = shortTermMemoryService.recent(conversationId).stream()
                .map(message -> message.getRole() + ":" + message.getContent())
                .collect(Collectors.joining("\n"));
        if (longTerm.isBlank()) {
            return shortTerm;
        }
        if (shortTerm.isBlank()) {
            return longTerm;
        }
        return "长期记忆：" + longTerm + "\n短期记忆：\n" + shortTerm;
    }
}
