package com.xiaomi.shopping.agent.orchestrator.chatclient;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 主 Agent ChatClient 的 ChatModel 配置（架构.md §2 / 主Agent-技术架构.md §2）。
 * <p>
 * 经 OpenAI 兼容协议接入阿里云百炼 qwen-plus-2025-07-28。
 * chat 与 embedding 来自不同服务商（chat=百炼，embedding=硅基流动），各自独立 Bean。
 * <p>
 * 关闭自动装配：使用 spring-ai-openai 核心包（非 starter），无 AutoConfiguration，
 * 完全手动声明，天然避免与 embedding 端点冲突。
 * <p>
 * 未配置 api-key 时不装配 Bean（降级），便于单测/无 LLM 环境运行。
 *
 * @author liyunyi
 */
@Configuration
@ConfigurationProperties(prefix = "xiaomi.agent.chat")
@ConditionalOnExpression("'${xiaomi.agent.chat.api-key:}' != ''")
public class OrchestratorChatModelConfig {

    /** 百炼 OpenAI 兼容端点 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    /** API Key（从 application-local.yml / 环境变量注入） */
    private String apiKey;
    /** 模型名 */
    private String model = "qwen-plus-2025-07-28";

    @Bean("orchestratorChatModel")
    public OpenAiChatModel orchestratorChatModel() {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .build())
                .build();
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
