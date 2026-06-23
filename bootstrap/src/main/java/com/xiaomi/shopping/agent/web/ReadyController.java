package com.xiaomi.shopping.agent.web;

import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 聚合启动就绪检查。
 *
 * @author liyunyi
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReadyController {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final DataSource dataSource;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/ready")
    public Map<String, Object> ready() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bootstrap", "UP");
        result.put("orchestrator", beanStatus(OrchestratorService.class));
        result.put("knowledgeGateway", beanStatus(KnowledgeGateway.class));
        result.put("shoppingGateway", beanStatus(ShoppingGateway.class));
        result.put("postgres", postgresStatus());
        result.put("redis", redisStatus());
        result.put("mcpserver", mcpServerStatus());
        result.put("chatModel", hasText(environment.getProperty("xiaomi.agent.chat.api-key"))
                ? "CONFIGURED" : "MISSING_KEY");
        result.put("embeddingModel", hasText(environment.getProperty("xiaomi.agent.embedding.api-key"))
                ? "CONFIGURED" : "MISSING_KEY");
        result.put("rerank", hasText(environment.getProperty("xiaomi.agent.rerank.api-key"))
                ? "CONFIGURED" : "FALLBACK");
        result.put("status", aggregateStatus(result));
        return result;
    }

    private String beanStatus(Class<?> type) {
        return applicationContext.getBeanNamesForType(type).length > 0 ? "UP" : "DOWN";
    }

    private String postgresStatus() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String redisStatus() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong) ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String mcpServerStatus() {
        String url = environment.getProperty("spring.ai.mcp.client.sse.connections.xiaomi-shopping.url",
                "http://localhost:8090");
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
            connection.setRequestMethod("GET");
            connection.getResponseCode();
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String aggregateStatus(Map<String, Object> result) {
        boolean coreDown = "DOWN".equals(result.get("orchestrator"))
                || "DOWN".equals(result.get("knowledgeGateway"))
                || "DOWN".equals(result.get("shoppingGateway"));
        if (coreDown) {
            return "DOWN";
        }
        boolean degraded = result.values().stream()
                .anyMatch(value -> "DOWN".equals(value) || "MISSING_KEY".equals(value) || "FALLBACK".equals(value));
        return degraded ? "DEGRADED" : "UP";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
