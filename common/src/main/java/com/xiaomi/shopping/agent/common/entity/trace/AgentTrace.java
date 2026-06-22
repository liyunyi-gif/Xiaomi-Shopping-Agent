package com.xiaomi.shopping.agent.common.entity.trace;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Agent 执行链路表（t_agent_trace）—— 意图/质量判断/重检/工具追踪。
 * <p>
 * node_type: intent/judge/retrieve_rerank/tool/shopping。
 * extra_data 记录三信号快照等。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_agent_trace", autoResultMap = true)
public class AgentTrace extends LogicDeleteEntity {

    /** 链路ID（唯一） */
    private String traceId;

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 节点类型：intent/judge/retrieve_rerank/tool/shopping */
    private String nodeType;

    /** 节点名 */
    private String nodeName;

    /** 状态 */
    private String status;

    /** 意图 */
    private String intent;

    /** 质量判定 */
    private String qualityVerdict;

    /** 重检次数 */
    private Integer retryCount;

    /** 工具名 */
    private String toolName;

    /** 输入摘要 */
    private String inputSummary;

    /** 输出摘要 */
    private String outputSummary;

    /** 耗时（ms） */
    private Long durationMs;

    /** 错误信息 */
    private String errorMessage;

    /** 扩展数据 JSONB（三信号快照等） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraData;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;
}
