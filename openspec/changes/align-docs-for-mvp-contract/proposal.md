## Why

当前 `/doc` 文档体系已经覆盖智能导购 Agent、Knowledge、Shopping、MCP、数据库、接口和前端 UIUX，但首期 demo/MVP 与完整商城/RAG 平台能力混写，导致接口契约、枚举命名、MCP 边界、数据库模型和前端展示口径不一致。

本变更用于先收敛“首期可运行闭环”的设计基线：统一 MVP 范围和跨文档契约，降低过度设计对实现的阻塞，并把二期增强能力明确阶段化。

## What Changes

- 统一首期 MVP 范围：以 `/api/chat` 主链路、`/api/ready` 联调状态、主 Agent 编排、简化 Knowledge 检索、Shopping MCP Mock、基础前端 Chat UI 为首期闭环。
- 统一跨文档接口契约：`/api/chat` 以结构化 `ChatReply` 为唯一响应基线，包含 `answer`、`intent`、`needClarify`、`qualityLevel`、`retryCount`、`childCalls`、`data`、`traceId` 等字段。
- 统一命名与枚举：首期一级意图统一为 `KNOWLEDGE / SHOPPING / SYSTEM`；质量失败统一为 `FAILED`；购物动作统一为小写蛇形工具名；对外 SKU 标识统一为 `skuCode`。
- 明确 MCP 首期边界：Shopping 首期仍走 MCP Client/Server，MCP Server 可作为“模拟外部小米商城系统”使用 Mock 或共享 demo 数据，但不允许文档同时声明“必须 MCP”和“可绕过 MCP”。
- 明确 Knowledge 首期边界：主 Agent 负责意图、澄清、质量判定和重检决策；Knowledge 负责检索、召回、rerank 和证据返回，避免双重 query rewrite、双重实体抽取。
- 调整数据库文档口径：区分 P0/P1/P2 表优先级；修正 trace、订单明细、购物车、外键、updated_at、软删除、初始化幂等、中文检索等设计问题的文档基线。
- 调整前端 UIUX 文档口径：首期避免强依赖 `answer` 字符串解析；商品/购物卡片以结构化 `data` 为正式依据，mock 卡片需明确“演示”；三栏布局断点、ready/health 分级、快捷操作行为需统一。
- 阶段化增强能力：Redis 三层记忆、长期记忆自动提炼、外部 rerank、HNSW、Tika 多格式入库、复杂前端动效、高覆盖率 E2E、完整购物交易一致性标为二期或优化项。

## Capabilities

### New Capabilities
- `mvp-contract-alignment`: 定义首期 MVP 的跨文档一致性契约，包括接口 DTO、枚举命名、MCP 边界、Agent/Knowledge/Shopping 职责边界、数据库优先级和前端展示口径。

### Modified Capabilities

## Impact

- Affected documents:
  - `doc/技术架构.md`
  - `doc/主Agent-技术架构.md`
  - `doc/知识库Agent-技术架构.md`
  - `doc/购物Agent-技术架构.md`
  - `doc/接口文档.md`
  - `doc/database/数据库设计.md`
  - `doc/database/schema.sql`
  - `doc/database/init.sql`
  - `doc/database/create-database.sql`
  - `doc/前端UIUX需求文档.md`
  - `doc/前端UIUX设计文档.md`
  - `doc/前端技术架构文档.md`
- Affected implementation areas after approval:
  - API response DTO and controller validation design.
  - Agent orchestration contracts and child node responsibilities.
  - Knowledge retrieval quality contract and rerank fallback contract.
  - Shopping MCP tool contracts and mock/data-source boundary.
  - Database schema documentation and SQL migration scripts.
  - Frontend state rendering, ready panel, shopping card rendering, and responsive layout.
- No production code should be changed before the document baseline is revised and reviewed.
