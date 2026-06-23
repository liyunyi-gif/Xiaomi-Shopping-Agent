package com.xiaomi.shopping.agent.mcpserver.config;

import com.xiaomi.shopping.agent.mcpserver.tools.CartTools;
import com.xiaomi.shopping.agent.mcpserver.tools.LogisticsTools;
import com.xiaomi.shopping.agent.mcpserver.tools.OrderTools;
import com.xiaomi.shopping.agent.mcpserver.tools.PromotionTools;
import com.xiaomi.shopping.agent.mcpserver.tools.StockTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具注册配置。
 *
 * @author liyunyi
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider shoppingTools(CartTools cartTools,
                                              OrderTools orderTools,
                                              LogisticsTools logisticsTools,
                                              StockTools stockTools,
                                              PromotionTools promotionTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(cartTools, orderTools, logisticsTools, stockTools, promotionTools)
                .build();
    }
}
