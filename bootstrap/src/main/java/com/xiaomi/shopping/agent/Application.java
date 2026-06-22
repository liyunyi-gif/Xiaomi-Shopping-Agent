package com.xiaomi.shopping.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小米商城智能导购 Agent · 启动入口。
 * <p>
 * 聚合三节点模块（orchestrator / knowledge / shopping），扫描各模块的 Spring 组件。
 * <p>
 * 注意：启动类位于 {@code com.xiaomi.shopping.agent} 包下，默认扫描其下所有子包，
 * 覆盖三节点的包路径（orchestrator / knowledge / shopping 模块的根包）。
 *
 * @author liyunyi
 */
@SpringBootApplication(scanBasePackages = "com.xiaomi.shopping.agent")
@MapperScan("com.xiaomi.shopping.agent.**.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
