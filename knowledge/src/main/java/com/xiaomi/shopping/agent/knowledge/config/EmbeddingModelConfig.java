package com.xiaomi.shopping.agent.knowledge.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Embedding 模型配置（架构.md / 知识库Agent-技术架构.md §2）。
 * <p>
 * 经 OpenAI 兼容协议接入硅基流动 Qwen3-Embedding-8B（dimensions=1024，与 t_knowledge_vector VECTOR(1024) 吻合）。
 * chat=百炼、embedding=硅基流动，不同服务商不同 base-url，各自独立 Bean（对齐 P5）。
 * <p>
 * 用 spring-ai-openai 核心包（非 starter），无自动装配，手动声明避免冲突。
 * 未配置 api-key 时不装配（降级为仅关键词路）。
 *
 * @author liyunyi
 */
@Configuration
@ConfigurationProperties(prefix = "xiaomi.agent.embedding")
@ConditionalOnExpression("'${xiaomi.agent.embedding.api-key:}' != ''")
public class EmbeddingModelConfig {

    /** 硅基流动 OpenAI 兼容端点 */
    private String baseUrl = "https://api.siliconflow.cn/v1";
    /** API Key */
    private String apiKey;
    /** 模型名 */
    private String model = "Qwen/Qwen3-Embedding-8B";
    /** 向量维度（Qwen3-Embedding-8B 支持 64~4096，本项目用 1024） */
    private int dimensions = 1024;

    @Bean("knowledgeEmbeddingModel")
    public OpenAiEmbeddingModel knowledgeEmbeddingModel() {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return new OpenAiEmbeddingModel(
                api,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(model)
                        .dimensions(dimensions)
                        .build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE
        );
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getDimensions() { return dimensions; }
    public void setDimensions(int dimensions) { this.dimensions = dimensions; }
}
