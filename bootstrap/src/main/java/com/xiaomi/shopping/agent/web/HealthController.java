package com.xiaomi.shopping.agent.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查 / 根接口（骨架阶段用于验证三节点模块装配是否成功）。
 * <p>
 * 后续将替换为正式的对话入口 / 历史回显 / 记忆清除接口（架构.md 对外能力）。
 *
 * @author liyunyi
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 存活探针。
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "project", "xiaomi-shopping-agent",
                "arch", "orchestrator + knowledge + shopping"
        );
    }
}
