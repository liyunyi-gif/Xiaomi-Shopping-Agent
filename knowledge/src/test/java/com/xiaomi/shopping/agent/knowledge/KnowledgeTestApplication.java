package com.xiaomi.shopping.agent.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * knowledge 模块测试专用启动类。
 * <p>
 * 正式启动在 bootstrap；此处仅为单测引导最小 Spring 上下文，扫描 knowledge 包组件 +
 * common 配置（MybatisPlusConfig）。不含 VectorStore/ChatClient（语义路/重写退化为规则）。
 *
 * @author liyunyi
 */
@SpringBootApplication(
        scanBasePackages = {"com.xiaomi.shopping.agent.knowledge", "com.xiaomi.shopping.agent.common"},
        excludeName = "org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration"
)
@MapperScan("com.xiaomi.shopping.agent.knowledge.**.mapper")
class KnowledgeTestApplication {
}
