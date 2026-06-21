-- ============================================================
-- 小米商城智能导购 Agent · 建表脚本 (schema)
-- Database: PostgreSQL 14+ + pgvector
-- 说明: 本项目库表, 借鉴主流 RAG Agent 系统库表设计裁剪扩展
-- 配套文档: 数据库设计.md (同目录)
-- 初始化数据见: init.sql
-- ============================================================

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================
-- 模块 1: 用户与会话域
-- ============================================================

CREATE TABLE t_user (
    id          BIGSERIAL    PRIMARY KEY,
    user_code   VARCHAR(64)  NOT NULL,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128),
    role        VARCHAR(32)  NOT NULL DEFAULT 'user',
    avatar      VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_code UNIQUE (user_code),
    CONSTRAINT uk_user_username UNIQUE (username)
);
COMMENT ON TABLE t_user IS '系统用户表';
COMMENT ON COLUMN t_user.user_code IS '用户编码（业务唯一）';
COMMENT ON COLUMN t_user.role IS '角色：admin/user';

CREATE TABLE t_conversation (
    id              BIGSERIAL   PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    user_id         BIGINT      NOT NULL,
    title           VARCHAR(128),
    last_time       TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_conv_id_user UNIQUE (conversation_id, user_id)
);
CREATE INDEX idx_conv_user_time ON t_conversation (user_id, last_time);
COMMENT ON TABLE t_conversation IS '会话列表（①短期记忆会话维度）';

-- ============================================================
-- 模块 2: 三层记忆域
-- ============================================================

-- ③ 对话存档（逐字历史）
CREATE TABLE t_message (
    id              BIGSERIAL   PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    user_id         BIGINT      NOT NULL,
    role            VARCHAR(16) NOT NULL,
    content         TEXT        NOT NULL,
    intent          VARCHAR(32),
    quality_verdict VARCHAR(16),
    retry_count     SMALLINT    DEFAULT 0,
    thinking_content TEXT,
    tokens_used     INTEGER,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT    NOT NULL DEFAULT 0
);
CREATE INDEX idx_msg_conv_time ON t_message (conversation_id, user_id, created_at);
COMMENT ON TABLE t_message IS '对话存档表（③逐字历史，实时落盘）';
COMMENT ON COLUMN t_message.role IS 'user/assistant/system';
COMMENT ON COLUMN t_message.intent IS '命中的意图（仅 assistant）';
COMMENT ON COLUMN t_message.quality_verdict IS '检索质量判定 SUFFICIENT/INCOMPLETE/INSUFFICIENT/FAILURE';

-- ③ 会话摘要（分离存储）
CREATE TABLE t_conversation_summary (
    id              BIGSERIAL   PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL,
    user_id         BIGINT      NOT NULL,
    last_message_id BIGINT,
    content         TEXT        NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT    NOT NULL DEFAULT 0
);
CREATE INDEX idx_sum_conv ON t_conversation_summary (conversation_id, user_id);
COMMENT ON TABLE t_conversation_summary IS '会话摘要表（与逐字存档分离）';

-- ② 长期记忆（提炼后结构化状态）
CREATE TABLE t_user_longterm_memory (
    id                  BIGSERIAL   PRIMARY KEY,
    user_id             BIGINT      NOT NULL,
    mem_type            VARCHAR(32) NOT NULL,
    content             TEXT        NOT NULL,
    weight              FLOAT       NOT NULL DEFAULT 1.0,
    source_conversation VARCHAR(64),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT    NOT NULL DEFAULT 0
);
CREATE INDEX idx_ltm_user ON t_user_longterm_memory (user_id);
COMMENT ON TABLE t_user_longterm_memory IS '长期记忆表（②提炼画像/偏好/决策/已澄清槽位）';
COMMENT ON COLUMN t_user_longterm_memory.mem_type IS 'profile/preference/decision/slot';
COMMENT ON COLUMN t_user_longterm_memory.weight IS '权重（低权重淘汰，防记忆膨胀）';

-- ============================================================
-- 模块 3: 商品业务域
-- ============================================================

CREATE TABLE t_category (
    id            BIGSERIAL   PRIMARY KEY,
    category_code VARCHAR(64) NOT NULL,
    name          VARCHAR(64) NOT NULL,
    level         SMALLINT    NOT NULL,
    parent_id     BIGINT,
    sort_order    INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       SMALLINT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_category_code UNIQUE (category_code)
);
COMMENT ON TABLE t_category IS '商品类目表（10大品类 / 二级）';
COMMENT ON COLUMN t_category.level IS '1 一级 / 2 二级';

CREATE TABLE t_product_spu (
    id          BIGSERIAL    PRIMARY KEY,
    spu_code    VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    brand       VARCHAR(64),
    category_id BIGINT,
    subtitle    VARCHAR(255),
    description TEXT,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_spu_code UNIQUE (spu_code)
);
CREATE INDEX idx_spu_category ON t_product_spu (category_id);
COMMENT ON TABLE t_product_spu IS '商品 SPU 表（标准产品单元）';
COMMENT ON COLUMN t_product_spu.status IS '1 在售 / 0 下架';

CREATE TABLE t_product_sku (
    id         BIGSERIAL    PRIMARY KEY,
    sku_code   VARCHAR(64)  NOT NULL,
    spu_id     BIGINT       NOT NULL,
    spec_info  VARCHAR(128) NOT NULL,
    spec_json  JSONB,
    price      DECIMAL(10,2) NOT NULL,
    stock      INTEGER      NOT NULL DEFAULT 0,
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_sku_code UNIQUE (sku_code)
);
CREATE INDEX idx_sku_spu ON t_product_sku (spu_id);
COMMENT ON TABLE t_product_sku IS '商品 SKU 表（规格单元，加购粒度）';
COMMENT ON COLUMN t_product_sku.spec_json IS '规格明细（内存/存储/颜色等）';
COMMENT ON COLUMN t_product_sku.stock IS '库存（Shopping 加购缺库存判定用）';

-- ============================================================
-- 模块 4: 知识库域（双路召回源）
-- ============================================================

CREATE TABLE t_knowledge_base (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(128) NOT NULL,
    embedding_model VARCHAR(64)  NOT NULL,
    collection_name VARCHAR(64)  NOT NULL,
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_kb_collection UNIQUE (collection_name)
);
COMMENT ON TABLE t_knowledge_base IS '知识库表';

CREATE TABLE t_knowledge_document (
    id             BIGSERIAL    PRIMARY KEY,
    kb_id          BIGINT       NOT NULL,
    spu_id         BIGINT,
    doc_name       VARCHAR(256) NOT NULL,
    file_url       VARCHAR(1024) NOT NULL,
    file_type      VARCHAR(16)  NOT NULL,
    file_size      BIGINT,
    chunk_strategy VARCHAR(32),
    chunk_config   JSONB,
    chunk_count    INTEGER      NOT NULL DEFAULT 0,
    status         VARCHAR(16)  NOT NULL DEFAULT 'pending',
    source_type    VARCHAR(16),
    source_location VARCHAR(1024),
    content_hash   VARCHAR(64),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        SMALLINT     NOT NULL DEFAULT 0
);
CREATE INDEX idx_doc_kb ON t_knowledge_document (kb_id);
COMMENT ON TABLE t_knowledge_document IS '知识文档表（Tika 解析源）';
COMMENT ON COLUMN t_knowledge_document.status IS 'pending/running/success/failed';

-- 切片表：关键词路 + 命中判定源（含 title/spec_text 字段加权列 + tsv 全文检索）
CREATE TABLE t_knowledge_chunk (
    id          BIGSERIAL   PRIMARY KEY,
    kb_id       BIGINT      NOT NULL,
    doc_id      BIGINT      NOT NULL,
    spu_id      BIGINT,
    chunk_index INTEGER     NOT NULL,
    content     TEXT        NOT NULL,
    title       VARCHAR(255),
    spec_text   VARCHAR(255),
    content_hash VARCHAR(64),
    char_count  INTEGER,
    token_count INTEGER,
    tsv         TSVECTOR,
    enabled     SMALLINT    NOT NULL DEFAULT 1,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT    NOT NULL DEFAULT 0
);
CREATE INDEX idx_chunk_doc ON t_knowledge_chunk (doc_id);
CREATE INDEX idx_chunk_tsv ON t_knowledge_chunk USING GIN (tsv);
-- 触发器：自动维护全文检索向量（关键词路用）
CREATE TRIGGER trg_chunk_tsv BEFORE INSERT OR UPDATE
    ON t_knowledge_chunk FOR EACH ROW
    EXECUTE FUNCTION tsvector_update_trigger(tsv, 'pg_catalog.simple', content, title);
COMMENT ON TABLE t_knowledge_chunk IS '知识切片表（关键词路 + 命中判定源）';
COMMENT ON COLUMN t_knowledge_chunk.title IS '标题（rerank 字段加权）';
COMMENT ON COLUMN t_knowledge_chunk.spec_text IS '规格摘要（rerank 字段加权）';
COMMENT ON COLUMN t_knowledge_chunk.tsv IS '全文检索向量（触发器维护）';

-- 向量存储表：语义路 HNSW
CREATE TABLE t_knowledge_vector (
    id        BIGSERIAL    PRIMARY KEY,
    chunk_id  BIGINT       NOT NULL,
    kb_id     BIGINT       NOT NULL,
    content   TEXT,
    metadata  JSONB,
    embedding VECTOR(1024) NOT NULL
);
-- ★ 核心：HNSW 向量索引（小数据量 500SKU 最优）
CREATE INDEX idx_vec_embedding ON t_knowledge_vector
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
CREATE INDEX idx_vec_metadata ON t_knowledge_vector USING GIN (metadata);
CREATE INDEX idx_vec_kb ON t_knowledge_vector (kb_id);
COMMENT ON TABLE t_knowledge_vector IS '向量存储表（语义路 HNSW 召回）';
COMMENT ON COLUMN t_knowledge_vector.embedding IS '向量（通义 text-embedding-v3, 1024维, 待实测确认）';

-- ============================================================
-- 模块 5: 意图与查询域
-- ============================================================

-- 意图树（3 一级 / N 二级）
CREATE TABLE t_intent_node (
    id              BIGSERIAL   PRIMARY KEY,
    kb_id           BIGINT,
    intent_code     VARCHAR(64) NOT NULL,
    name            VARCHAR(64) NOT NULL,
    level           SMALLINT    NOT NULL,
    parent_code     VARCHAR(64),
    kind            SMALLINT    NOT NULL DEFAULT 0,
    description     VARCHAR(512),
    examples        TEXT,
    top_k           INTEGER,
    shopping_action VARCHAR(32),
    prompt_snippet  TEXT,
    sort_order      INTEGER     NOT NULL DEFAULT 0,
    enabled         SMALLINT    NOT NULL DEFAULT 1,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_intent_code UNIQUE (intent_code)
);
COMMENT ON TABLE t_intent_node IS '意图树表（3一级 / N二级）';
COMMENT ON COLUMN t_intent_node.level IS '1 一级 / 2 二级';
COMMENT ON COLUMN t_intent_node.kind IS '0 KNOWLEDGE / 1 SHOPPING / 2 SYSTEM';
COMMENT ON COLUMN t_intent_node.shopping_action IS '购物动作 ADD_TO_CART/PLACE_ORDER/QUERY_LOGISTICS/QUERY_STOCK/QUERY_PROMOTION';

-- 术语归一化映射（查询重写辅助）
CREATE TABLE t_query_term_mapping (
    id          BIGSERIAL   PRIMARY KEY,
    domain      VARCHAR(64),
    source_term VARCHAR(128) NOT NULL,
    target_term VARCHAR(128) NOT NULL,
    match_type  SMALLINT    NOT NULL DEFAULT 1,
    priority    INTEGER     NOT NULL DEFAULT 100,
    enabled     SMALLINT    NOT NULL DEFAULT 1,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     SMALLINT    NOT NULL DEFAULT 0
);
CREATE INDEX idx_term_source ON t_query_term_mapping (source_term);
CREATE INDEX idx_term_domain ON t_query_term_mapping (domain);
COMMENT ON TABLE t_query_term_mapping IS '术语归一化映射表（查询重写口语→规范）';
COMMENT ON COLUMN t_query_term_mapping.match_type IS '1 精确 / 2 模糊';

-- ============================================================
-- 模块 6: 购物业务域（Shopping MCP 操作主数据）
-- ============================================================

CREATE TABLE t_cart_item (
    id         BIGSERIAL   PRIMARY KEY,
    cart_id    VARCHAR(64) NOT NULL,
    user_id    BIGINT      NOT NULL,
    sku_id     BIGINT      NOT NULL,
    quantity   INTEGER     NOT NULL DEFAULT 1,
    selected   SMALLINT    NOT NULL DEFAULT 1,
    added_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    SMALLINT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_cart_sku UNIQUE (cart_id, sku_id)
);
CREATE INDEX idx_cart_user ON t_cart_item (user_id);
COMMENT ON TABLE t_cart_item IS '购物车项表（Shopping add_to_cart 操作）';

CREATE TABLE t_order (
    id           BIGSERIAL    PRIMARY KEY,
    order_no     VARCHAR(64)  NOT NULL,
    user_id      BIGINT       NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    address      VARCHAR(255),
    pay_method   VARCHAR(32),
    status       VARCHAR(16)  NOT NULL DEFAULT 'pending',
    logistics_no VARCHAR(64),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);
CREATE INDEX idx_order_user ON t_order (user_id);
COMMENT ON TABLE t_order IS '订单表（Shopping place_order 操作）';
COMMENT ON COLUMN t_order.status IS 'pending/paid/shipped/done/cancelled';

-- ============================================================
-- 模块 7: 链路追踪域（质量判断/重检/工具调用可观测）
-- ============================================================

CREATE TABLE t_agent_trace (
    id              BIGSERIAL   PRIMARY KEY,
    trace_id        VARCHAR(64) NOT NULL,
    conversation_id VARCHAR(64),
    user_id         BIGINT,
    node_type       VARCHAR(32) NOT NULL,
    node_name       VARCHAR(128),
    status          VARCHAR(16) NOT NULL,
    intent          VARCHAR(32),
    quality_verdict VARCHAR(16),
    retry_count     SMALLINT,
    tool_name       VARCHAR(64),
    input_summary   TEXT,
    output_summary  TEXT,
    duration_ms     BIGINT,
    error_message   TEXT,
    extra_data      JSONB,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT    NOT NULL DEFAULT 0,
    CONSTRAINT uk_trace_id UNIQUE (trace_id)
);
CREATE INDEX idx_trace_conv ON t_agent_trace (conversation_id);
CREATE INDEX idx_trace_user ON t_agent_trace (user_id);
COMMENT ON TABLE t_agent_trace IS 'Agent 执行链路表（意图/质量判断/重检/工具追踪）';
COMMENT ON COLUMN t_agent_trace.node_type IS 'intent/judge/retrieve_rerank/tool/shopping';
COMMENT ON COLUMN t_agent_trace.extra_data IS '扩展（三信号快照等）';
