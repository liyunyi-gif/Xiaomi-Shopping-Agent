package com.xiaomi.shopping.agent.common.entity.knowledge;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiaomi.shopping.agent.common.entity.LogicDeleteEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 知识文档表（t_knowledge_document）—— Tika 解析源。
 * <p>
 * status: pending/running/success/failed；chunk_config 为 JSONB。
 *
 * @author liyunyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_knowledge_document", autoResultMap = true)
public class KnowledgeDocument extends LogicDeleteEntity {

    /** 所属知识库ID */
    private Long kbId;

    /** 关联 SPU ID */
    private Long spuId;

    /** 文档名 */
    private String docName;

    /** 文件 URL */
    private String fileUrl;

    /** 文件类型（pdf/docx/html 等） */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 切片策略 */
    private String chunkStrategy;

    /** 切片配置 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> chunkConfig;

    /** 切片数 */
    private Integer chunkCount;

    /** 状态：pending/running/success/failed */
    private String status;

    /** 来源类型 */
    private String sourceType;

    /** 来源位置 */
    private String sourceLocation;

    /** 内容 hash（幂等用） */
    private String contentHash;
}
