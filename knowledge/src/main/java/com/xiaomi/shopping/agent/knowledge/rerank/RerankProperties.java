package com.xiaomi.shopping.agent.knowledge.rerank;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rerank 配置属性（硅基流动 Qwen3-Reranker-8B）。
 *
 * @author liyunyi
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xiaomi.agent.rerank")
public class RerankProperties {

    /** 硅基流动端点（OpenAI 兼容 base-url） */
    private String baseUrl = "https://api.siliconflow.cn/v1";

    /** API Key（与 embedding 共用硅基流动 key） */
    private String apiKey;

    /** 模型名 */
    private String model = "Qwen/Qwen3-Reranker-8B";

    /** 是否启用外部 rerank（false 时直接用自研加权降级） */
    private boolean enabled = true;

    /** 外部 rerank 失败重试次数 */
    private int retry = 1;
}
