package com.xiaomi.shopping.agent.web;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorReply;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ChatController Web 入口测试。
 *
 * @author liyunyi
 */
class ChatControllerTest {

    @Test
    @DisplayName("POST /api/chat 默认会话参数并委托 OrchestratorService")
    void shouldDelegateToOrchestratorService() throws Exception {
        OrchestratorService orchestratorService = mock(OrchestratorService.class);
        when(orchestratorService.handle(eq("demo-user"), eq("demo-conversation"), eq("小米14有什么参数")))
                .thenReturn(OrchestratorReply.builder()
                        .answer("根据当前知识库资料：小米14参数")
                        .intent(IntentResult.IntentType.KNOWLEDGE)
                        .childCalls(1)
                        .build());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(orchestratorService)).build();

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"小米14有什么参数\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("根据当前知识库资料：小米14参数"))
                .andExpect(jsonPath("$.intent").value("KNOWLEDGE"));

        verify(orchestratorService).handle("demo-user", "demo-conversation", "小米14有什么参数");
    }
}
