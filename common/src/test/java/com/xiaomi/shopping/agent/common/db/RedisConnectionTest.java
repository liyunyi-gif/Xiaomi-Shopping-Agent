package com.xiaomi.shopping.agent.common.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Redis 连接测试。
 * <p>
 * 验证 common 模块能正确连接 Docker 中的 Redis 服务（192.168.122.128:6379，无密码），
 * 并完成一次 SET/GET 往返（短期记忆 ① 的基础能力）。
 *
 * @author liyunyi
 */
@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("Redis 连接：StringRedisTemplate 注入成功")
    void shouldInjectRedisTemplate() {
        assertNotNull(redisTemplate, "StringRedisTemplate 应被注入");
        System.out.println("[Redis] StringRedisTemplate 注入成功");
    }

    @Test
    @DisplayName("Redis 连接：PING 通")
    void shouldPingRedis() throws Exception {
        String pong = redisTemplate.getConnectionFactory().getConnection().ping();
        System.out.println("[Redis] PING -> " + pong);
        assertEquals("PONG", pong, "Redis PING 应返回 PONG");
    }

    @Test
    @DisplayName("Redis 往返：SET/GET（模拟短期记忆写入）")
    void shouldSetAndGet() {
        String key = "chat:session:test-conn-test:messages";
        String value = "hello-xiaomi-agent";
        try {
            redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);
            String got = redisTemplate.opsForValue().get(key);
            System.out.println("[Redis] SET " + key + " = " + value);
            System.out.println("[Redis] GET " + key + " -> " + got);
            assertEquals(value, got, "GET 应返回刚 SET 的值");
            assertTrue(Boolean.TRUE.equals(redisTemplate.delete(key)), "清理测试 key 应成功");
            System.out.println("[Redis] 已清理测试 key");
        } catch (Exception e) {
            System.err.println("[Redis] 连接失败：" + e.getMessage());
            throw e;
        }
    }
}
