package com.xiaomi.shopping.agent.web;

import com.xiaomi.shopping.agent.common.port.KnowledgeGateway;
import com.xiaomi.shopping.agent.common.port.ShoppingGateway;
import com.xiaomi.shopping.agent.orchestrator.service.OrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ReadyController 聚合就绪检查测试。
 *
 * @author liyunyi
 */
class ReadyControllerTest {

    @Test
    @DisplayName("GET /api/ready 汇总核心 Bean、基础设施与模型密钥状态")
    void shouldAggregateReadinessStatus() throws Exception {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeanNamesForType(OrchestratorService.class)).thenReturn(new String[]{"orchestratorService"});
        when(context.getBeanNamesForType(KnowledgeGateway.class)).thenReturn(new String[]{"knowledgeGateway"});
        when(context.getBeanNamesForType(ShoppingGateway.class)).thenReturn(new String[]{"shoppingGateway"});

        Environment environment = mock(Environment.class);
        when(environment.getProperty("xiaomi.agent.chat.api-key")).thenReturn("");
        when(environment.getProperty("xiaomi.agent.embedding.api-key")).thenReturn("sk-embedding");
        when(environment.getProperty("xiaomi.agent.rerank.api-key")).thenReturn("");
        when(environment.getProperty("spring.ai.mcp.client.sse.connections.xiaomi-shopping.url", "http://localhost:8090"))
                .thenReturn("http://127.0.0.1:1");

        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection redisConnection = mock(RedisConnection.class);
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        ReadyController controller = new ReadyController(context, environment, dataSource, redisTemplate);

        Map<String, Object> ready = controller.ready();

        assertEquals("UP", ready.get("bootstrap"));
        assertEquals("UP", ready.get("orchestrator"));
        assertEquals("UP", ready.get("knowledgeGateway"));
        assertEquals("UP", ready.get("shoppingGateway"));
        assertEquals("UP", ready.get("postgres"));
        assertEquals("UP", ready.get("redis"));
        assertEquals("MISSING_KEY", ready.get("chatModel"));
        assertEquals("CONFIGURED", ready.get("embeddingModel"));
        assertEquals("FALLBACK", ready.get("rerank"));
        assertTrue(ready.get("status").equals("DEGRADED") || ready.get("status").equals("UP"));
    }
}
