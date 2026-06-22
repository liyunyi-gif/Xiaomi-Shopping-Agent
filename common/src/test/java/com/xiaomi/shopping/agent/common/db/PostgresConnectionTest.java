package com.xiaomi.shopping.agent.common.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL 连接测试。
 * <p>
 * 验证 common 模块能正确连接 Docker 中的 pgsql 服务（192.168.122.128:5432，root/root），
 * 并探测 server 版本、已安装扩展、现有数据库，为后续建业务库做准备。
 *
 * @author liyunyi
 */
@SpringBootTest
class PostgresConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("PG 连接：获取连接并验证 URL/用户名")
    void shouldConnectToPostgres() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn, "数据库连接不应为 null");
            assertTrue(conn.isValid(3), "连接应在 3s 内有效");

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("[PG] 连接成功");
            System.out.println("[PG] URL      = " + meta.getURL());
            System.out.println("[PG] 用户名    = " + meta.getUserName());
            System.out.println("[PG] 产品名    = " + meta.getDatabaseProductName());
            System.out.println("[PG] 版本      = " + meta.getDatabaseProductVersion());
            System.out.println("[PG] 驱动      = " + meta.getDriverName() + " " + meta.getDriverVersion());

            assertEquals("PostgreSQL", meta.getDatabaseProductName(), "应为 PostgreSQL");
        }
    }

    @Test
    @DisplayName("PG 连接：SELECT 1 走通")
    void shouldExecuteSelectOne() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 AS result")) {
            assertTrue(rs.next(), "应有一行结果");
            assertEquals(1, rs.getInt("result"), "SELECT 1 应返回 1");
            System.out.println("[PG] SELECT 1 = " + rs.getInt("result") + " ✓");
        }
    }

    @Test
    @DisplayName("PG 探测：已安装扩展（含 vector / pg_trgm）")
    void shouldDetectInstalledExtensions() throws Exception {
        List<String> exts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT extname FROM pg_extension ORDER BY extname")) {
            while (rs.next()) {
                exts.add(rs.getString("extname"));
            }
        }
        System.out.println("[PG] 已安装扩展 = " + exts);
        assertTrue(exts.contains("plpgsql"), "plpgsql 应默认存在");
        // vector / pg_trgm 在业务库 schema.sql 中启用；此处仅观测，不强断言
    }

    @Test
    @DisplayName("PG 探测：现有数据库列表（确认是否已建业务库）")
    void shouldListDatabases() throws Exception {
        List<String> dbs = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname")) {
            while (rs.next()) {
                dbs.add(rs.getString("datname"));
            }
        }
        System.out.println("[PG] 现有数据库 = " + dbs);
        assertNotNull(dbs, "数据库列表不应为 null");
    }
}
