package com.xiaomi.shopping.agent.common.db;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * common 模块测试专用启动类。
 * <p>
 * common 本身无业务启动类（正式启动在 bootstrap），此处仅为连接测试引导最小 Spring 上下文。
 * 扫描 common 包下的配置类（MybatisPlusConfig / MyMetaObjectHandler）。
 *
 * @author liyunyi
 */
@SpringBootApplication(scanBasePackages = "com.xiaomi.shopping.agent.common")
@MapperScan("com.xiaomi.shopping.agent.common.**.mapper")
class CommonTestApplication {
}
