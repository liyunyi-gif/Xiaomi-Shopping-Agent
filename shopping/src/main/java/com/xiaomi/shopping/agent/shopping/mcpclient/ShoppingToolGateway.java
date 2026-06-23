package com.xiaomi.shopping.agent.shopping.mcpclient;

import java.util.Map;

/**
 * Shopping 子节点调用外部购物工具的统一网关。
 * <p>
 * 生产环境可经 MCP Client 调用 mcpserver；测试/演示环境可使用 mock 实现。
 *
 * @author liyunyi
 */
public interface ShoppingToolGateway {

    /**
     * 调用指定工具。
     *
     * @param toolName MCP 工具名
     * @param args     工具参数
     * @return 结构化工具结果
     */
    ToolResult invoke(String toolName, Map<String, Object> args);
}
