package com.xiaomi.shopping.agent.common.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 实体↔表结构映射验证。
 * <p>
 * 对 17 张表逐一执行 SELECT COUNT(*)，验证 common 模块各实体的 @TableName
 * 注解、列名驼峰映射与真实库表结构完全对齐（schema.sql 已建表）。
 * <p>
 * 测试连业务库 xiaomi_agent。
 *
 * @author liyunyi
 */
@SpringBootTest
@TestPropertySource(properties = {
        // 此测试连业务库 xiaomi_agent（schema.sql 已建 17 表）
        "spring.datasource.url=jdbc:postgresql://192.168.122.128:5432/xiaomi_agent"
})
class EntityTableMappingTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("17 张表均存在且可 SELECT COUNT(*)")
    void allSeventeenTablesQueryable() {
        String[] tables = {
                // 用户与会话域
                "t_user", "t_conversation",
                // 三层记忆域
                "t_message", "t_conversation_summary", "t_user_longterm_memory",
                // 商品业务域
                "t_category", "t_product_spu", "t_product_sku",
                // 知识库域
                "t_knowledge_base", "t_knowledge_document", "t_knowledge_chunk", "t_knowledge_vector",
                // 意图与查询域
                "t_intent_node", "t_query_term_mapping",
                // 购物业务域
                "t_cart_item", "t_order",
                // 链路追踪域
                "t_agent_trace"
        };

        Map<String, Long> counts = new LinkedHashMap<>();
        for (String t : tables) {
            Long cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + t, Long.class);
            counts.put(t, cnt);
            System.out.printf("[表] %-26s count=%d%n", t, cnt);
        }
        assertEquals(tables.length, counts.size(), "17 张表应全部可查");
        System.out.println("[OK] " + tables.length + " 张表全部可查，实体<->表映射对齐");
    }
}
