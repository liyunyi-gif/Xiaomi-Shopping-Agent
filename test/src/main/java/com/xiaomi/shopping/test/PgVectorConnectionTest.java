package com.xiaomi.shopping.test;

import com.pgvector.PGvector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PostgreSQL + pgvector 连接与可用性测试。
 *
 * 验证项（逐步）：
 *   1. JDBC 连通性（账号/密码/网络）
 *   2. PostgreSQL 版本
 *   3. pgvector 扩展是否可用（CREATE EXTENSION vector）
 *   4. 建 vector 列测试表 + 插入向量
 *   5. HNSW 索引创建
 *   6. 向量相似度查询（余弦距离 <=>）
 *
 * 连接参数可通过环境变量覆盖，默认对应用户提供的 Docker 服务：
 *   PG_HOST (默认 192.168.122.128)
 *   PG_PORT (默认 5432)
 *   PG_DB   (默认 postgres)
 *   PG_USER (默认 root)
 *   PG_PASSWORD (默认 root)
 */
public class PgVectorConnectionTest {

    private static final String HOST = env("PG_HOST", "192.168.122.128");
    private static final String PORT = env("PG_PORT", "5432");
    private static final String DB   = env("PG_DB", "postgres");
    private static final String USER = env("PG_USER", "root");
    private static final String PWD  = env("PG_PASSWORD", "root");

    private static final String URL = String.format(
            "jdbc:postgresql://%s:%s/%s?connectTimeout=5&socketTimeout=10", HOST, PORT, DB);

    public static void main(String[] args) {
        printHeader();
        int pass = 0, fail = 0;

        // —— 1. 连接测试 ——
        System.out.println("\n[1/6] 测试 JDBC 连接 ...");
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD)) {
            ok("连接成功：" + URL.replaceAll("root", "***"));

            // —— 2. 版本 ——
            System.out.println("\n[2/6] 查询 PostgreSQL 版本 ...");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT version();")) {
                if (rs.next()) ok("PostgreSQL 版本：" + rs.getString(1));
            }

            // —— 3. pgvector 扩展 ——
            System.out.println("\n[3/6] 检查 pgvector 扩展 ...");
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE EXTENSION IF NOT EXISTS vector;");
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT extversion FROM pg_extension WHERE extname='vector';")) {
                if (rs.next()) {
                    ok("pgvector 扩展已安装，版本：" + rs.getString(1));
                } else {
                    fail("pgvector 扩展未安装（CREATE EXTENSION vector 未能生效）");
                }
            }
            // 注册 PGvector 类型映射（后续读写向量用）
            PGvector.addVectorType(conn);

            // —— 4. 建表 + 插入向量 ——
            System.out.println("\n[4/6] 建 vector 测试表并插入数据 ...");
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS test_vector_conn;");
                st.execute("CREATE TABLE test_vector_conn (id bigserial PRIMARY KEY, content text, embedding vector(3));");
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO test_vector_conn (content, embedding) VALUES (?, ?);")) {
                ps.setString(1, "小米14");
                ps.setObject(2, new PGvector(new float[]{1.0f, 1.2f, 0.8f}));
                ps.executeUpdate();
                ps.setString(1, "小米14 Pro");
                ps.setObject(2, new PGvector(new float[]{1.1f, 1.3f, 0.9f}));
                ps.executeUpdate();
                ps.setString(1, "Redmi K70");
                ps.setObject(2, new PGvector(new float[]{0.2f, 0.3f, 0.4f}));
                ps.executeUpdate();
                ok("建表成功，插入 3 条向量（vector(3)）");
            }

            // —— 5. HNSW 索引 ——
            System.out.println("\n[5/6] 创建 HNSW 向量索引 ...");
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE INDEX idx_test_hnsw ON test_vector_conn " +
                        "USING hnsw (embedding vector_cosine_ops) " +
                        "WITH (m = 16, ef_construction = 64);");
                ok("HNSW 索引创建成功（m=16, ef_construction=64）");
            }

            // —— 6. 相似度查询 ——
            System.out.println("\n[6/6] 向量相似度查询（余弦距离 <=>）...");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT content, embedding <=> ? AS distance " +
                    "FROM test_vector_conn ORDER BY embedding <=> ? LIMIT 2;")) {
                PGvector q = new PGvector(new float[]{1.0f, 1.2f, 0.85f});
                ps.setObject(1, q);
                ps.setObject(2, q);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder("Top-2 近邻：");
                    while (rs.next()) {
                        sb.append("\n   - ").append(rs.getString("content"))
                          .append("  (distance=").append(rs.getString("distance")).append(")");
                    }
                    ok(sb.toString());
                }
            }

            // 清理测试表
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS test_vector_conn;");
            }

        } catch (SQLException e) {
            fail("SQL 错误：" + e.getMessage());
            // 常见错误针对性提示
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("password") || msg.contains("authentication") || msg.contains("role") || msg.contains("user")) {
                hint("→ 认证失败：PostgreSQL 默认无 root 用户。若未手动创建，请用 postgres 超管账户，"
                   + "或先执行：docker exec -it postgres_db psql -U postgres -c \"CREATE ROLE root WITH LOGIN SUPERUSER PASSWORD 'root';\"");
            }
            if (msg.contains("connection refused") || msg.contains("timed out") || msg.contains("connect")) {
                hint("→ 网络不通：检查 IP " + HOST + " 是否可达、端口 " + PORT
                   + " 是否开放、防火墙/虚拟机网络。可在宿主机 ping " + HOST + " 或 telnet " + HOST + " " + PORT);
            }
            if (msg.contains("database") && msg.contains("does not exist")) {
                hint("→ 数据库不存在：默认连 postgres 库，或用 PG_DB 环境变量指定已存在的库。");
            }
        }
    }

    // —— 输出辅助 ——
    private static void printHeader() {
        System.out.println("========================================");
        System.out.println("  PostgreSQL + pgvector 连接测试");
        System.out.println("========================================");
        System.out.println("目标: " + HOST + ":" + PORT + "/" + DB);
        System.out.println("用户: " + USER);
    }

    private static void ok(String msg) {
        System.out.println("  [PASS] " + msg);
    }

    private static void fail(String msg) {
        System.out.println("  [FAIL] " + msg);
    }

    private static void hint(String msg) {
        System.out.println("  [HINT] " + msg);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
