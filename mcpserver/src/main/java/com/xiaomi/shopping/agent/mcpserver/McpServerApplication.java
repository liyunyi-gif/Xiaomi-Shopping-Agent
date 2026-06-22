package com.xiaomi.shopping.agent.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Server 独立启动入口。
 * <p>
 * 作为独立进程运行，封装外部业务服务（加购/下单/物流/库存/优惠）为标准 MCP 工具，
 * 供 Shopping 子节点经 MCP 协议远程调用（非 Java 包依赖，对齐 P5）。
 *
 * @author liyunyi
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
