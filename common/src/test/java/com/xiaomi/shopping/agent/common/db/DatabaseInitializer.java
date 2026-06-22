package com.xiaomi.shopping.agent.common.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 一次性数据库初始化工具（main 方法手动执行，非测试）。
 * <p>
 * 步骤：
 * 1. 连 postgres 库，创建业务库 xiaomi_agent（幂等）
 * 2. 连 xiaomi_agent 库，执行 schema.sql（建 17 表 + 扩展 + 索引）
 * <p>
 * 使用：
 * <pre>
 * mvn -pl common exec:java \
 *   -Dexec.mainClass=com.xiaomi.shopping.agent.common.db.DatabaseInitializer \
 *   -Dexec.classpathScope=test
 * </pre>
 * 或在 IDE 中右键 Run。
 *
 * @author liyunyi
 */
public class DatabaseInitializer {

    private static final String HOST = "192.168.122.128";
    private static final int PORT = 5432;
    private static final String USER = "root";
    private static final String PASS = "root";
    private static final String ADMIN_DB = "postgres";
    private static final String BIZ_DB = "xiaomi_agent";

    private static final String SCHEMA_SQL_PATH = "doc/database/schema.sql";

    public static void main(String[] args) throws Exception {
        createDatabaseIfAbsent();
        executeSchema();
        System.out.println("\n✓ 数据库初始化完成：库 " + BIZ_DB + " + schema.sql 已执行");
    }

    /** 连 postgres 库，幂等创建业务库。 */
    private static void createDatabaseIfAbsent() throws SQLException {
        String url = jdbcUrl(ADMIN_DB);
        try (Connection conn = DriverManager.getConnection(url, USER, PASS);
             Statement st = conn.createStatement()) {
            st.execute("CREATE DATABASE " + BIZ_DB + " OWNER " + USER);
            System.out.println("✓ 创建业务库 " + BIZ_DB);
        } catch (SQLException e) {
            // 42P04 = duplicate_database，库已存在视为成功
            if ("42P04".equals(e.getSQLState())) {
                System.out.println("• 业务库 " + BIZ_DB + " 已存在，跳过创建");
            } else {
                throw e;
            }
        }
    }

    /** 连业务库，执行 schema.sql。 */
    private static void executeSchema() throws SQLException, IOException {
        Path sqlFile = Paths.get(SCHEMA_SQL_PATH);
        if (!Files.exists(sqlFile)) {
            throw new IOException("未找到 schema.sql：" + sqlFile.toAbsolutePath());
        }
        String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);

        // 移除纯注释行（保留语句内字符串内容），再按 ; 分割
        String cleaned = removeCommentLines(sql);
        String[] stmts = cleaned.split(";");

        String url = jdbcUrl(BIZ_DB);
        int success = 0;
        int skipped = 0;
        try (Connection conn = DriverManager.getConnection(url, USER, PASS);
             Statement st = conn.createStatement()) {
            conn.setAutoCommit(true);
            for (String s : stmts) {
                String trimmed = s.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    st.execute(trimmed);
                    success++;
                } catch (SQLException e) {
                    skipped++;
                    System.out.println("  跳过(" + e.getSQLState() + "): " + firstLine(trimmed));
                }
            }
        }
        System.out.println("✓ 执行 schema.sql 完成：成功 " + success + " 条，跳过 " + skipped + " 条");
    }

    /** 移除以 -- 开头的整行注释（不影响字符串内的内容，schema.sql 注释均为行首）。 */
    private static String removeCommentLines(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            String t = line.trim();
            if (t.startsWith("--")) {
                continue;
            }
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static String jdbcUrl(String db) {
        return "jdbc:postgresql://" + HOST + ":" + PORT + "/" + db;
    }

    private static String firstLine(String s) {
        int n = s.indexOf('\n');
        String line = n > 0 ? s.substring(0, n) : s;
        return line.length() > 60 ? line.substring(0, 60) + "..." : line;
    }
}
