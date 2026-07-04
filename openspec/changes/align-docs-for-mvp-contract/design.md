## Context

当前 `/doc` 文档体系已经形成较完整的智能导购 Agent 设计，但文档之间存在明显的阶段错位：后端/Agent 文档接近完整 RAG + MCP + 购物系统架构，数据库文档覆盖完整业务域，接口文档仍以 `/api/chat` 单业务入口为主，前端文档则同时承担用户体验、联调面板和购物状态展示。

本设计的目标不是新增应用功能，而是建立一个可执行的文档修订基线：先把首期 MVP 的契约统一，再把增强能力明确放入后续阶段，避免实现时在“demo 快速闭环”和“完整平台能力”之间反复摇摆。

当前约束：

- 遵循项目既有“文档先行，代码最后”的流程。
- 本 change 阶段只创建 OpenSpec 规划工件，不直接修改应用代码。
- 后续 apply 阶段应优先修改需求/架构/测试文档，再修改 SQL 或代码。
- 首期设计应服务于演示与联调闭环，而不是一次性实现完整商城交易系统。

## Goals / Non-Goals

**Goals:**

- 建立首期 MVP 的统一边界：`/api/chat`、`/api/ready`、主 Agent、Knowledge、Shopping MCP Mock、基础前端 Chat UI。
- 统一跨文档契约：`ChatReply` DTO、意图枚举、质量枚举、Shopping action、SKU/订单/购物车标识命名。
- 明确 Agent 职责：主 Agent 负责决策，Knowledge 和 Shopping 作为能力节点返回结构化结果。
- 明确 MCP 职责：Shopping 首期仍通过 MCP，MCP Server 可模拟外部小米商城系统。
- 明确数据库阶段化：区分 P0/P1/P2 表与约束，修正明显会阻塞实现的设计矛盾。
- 明确前端阶段化：首期不强解析 `answer` 字符串，正式购物状态以结构化 `data` 为准。
- 将 Redis 三层记忆、外部 rerank、HNSW、Tika 多格式、复杂交易一致性、复杂动效和高覆盖率 E2E 标记为后续增强。

**Non-Goals:**

- 不在本 change 中实现新的业务代码。
- 不一次性完成完整电商交易系统，如支付、退款、售后、复杂优惠、库存锁定。
- 不把所有数据库表都要求首期落地。
- 不强制首期完成完整三层记忆和自动长期记忆提炼。
- 不强制首期完成外部 rerank、HNSW 参数调优或中文 FTS 最终方案。
- 不要求前端首期实现完整商城卡片体系或复杂视觉动效。

## Decisions

### Decision 1: 首期以“可运行联调闭环”作为唯一 MVP 基线

首期 MVP SHALL 围绕以下链路收敛：

```text
Frontend Chat UI
  -> POST /api/chat
  -> Main Agent
  -> Knowledge 或 Shopping
  -> ChatReply
  -> Frontend 渲染 answer / data / ready 摘要
```

理由：当前文档最大风险是范围过大。先用一个清晰闭环保证实现、测试、演示一致，再逐步增强。

替代方案：继续按完整架构一次性推进。该方案展示性强，但实现风险大，且容易让接口、数据库、前端提前背负复杂度。

### Decision 2: `/api/chat` 统一返回结构化 `ChatReply`

`ChatReply` 作为文档和实现的唯一响应契约，至少包含：

- `answer`: 面向用户的自然语言回答。
- `intent`: `KNOWLEDGE / SHOPPING / SYSTEM`。
- `needClarify`: 是否需要用户澄清。
- `clarifyQuestion`: 需要澄清时的问题。
- `qualityLevel`: `SUFFICIENT / INCOMPLETE / INSUFFICIENT / FAILED`。
- `retryCount`: Knowledge 重检次数。
- `childCalls`: 子节点调用摘要。
- `data`: 结构化业务结果，按类型承载 cart/order/stock/logistics/references 等。
- `traceId`: 联调定位 ID。

理由：前端不能依赖 `answer` 的自然语言或 `{key=value}` 字符串解析购物状态；数据库和 trace 的结构化价值也需要通过接口透出。

替代方案：保持 `String answer` 响应。该方案短期最简单，但会让前端卡片、联调面板、测试用例全部依赖脆弱解析。

### Decision 3: 命名和枚举使用一套跨层规范

统一约定：

- 一级意图：`KNOWLEDGE / SHOPPING / SYSTEM`。
- 质量判定：`SUFFICIENT / INCOMPLETE / INSUFFICIENT / FAILED`。
- Shopping action：`add_to_cart / place_order / query_stock / query_logistics / query_promotion`。
- 对外 SKU：`skuCode`。
- 数据库自增主键：`id` 或文档中明确为内部 ID。
- 订单展示编号：`orderNo`。
- 购物车展示编号：`cartId`。

理由：当前 `TOOL/SHOPPING`、`FAILED/FAILURE`、`skuId/skuCode`、大小写 action 混用会直接导致 DTO、数据库、前端和测试用例分叉。

替代方案：保留各层自己的命名并加映射。该方案兼容性强，但对当前小规模项目没有必要，会增加实现心智负担。

### Decision 4: 主 Agent 负责决策，Knowledge 负责证据

职责边界：

```text
Main Agent:
- 意图识别
- 槽位/实体/约束抽取
- 是否澄清
- 是否调用 Knowledge 或 Shopping
- 是否重检
- 最终质量判定
- 最终回答组织

Knowledge:
- 接收 query + constraints
- 执行召回、rerank、证据聚合
- 返回 evidence、scores、hitSignals、references
- 不拥有最终对话权
```

理由：避免主 Agent 和 Knowledge 双重 query rewrite、双重实体抽取、双重质量判断。

替代方案：Knowledge 内部完全自主完成 query rewrite 和质量判断。该方案可能提高检索模块自治程度，但会削弱“唯一真 Agent”的架构清晰度。

### Decision 5: Shopping 首期仍通过 MCP，但 MCP Server 可以是 demo 模拟外部系统

首期文档应统一为：

```text
Main Agent / Shopping Node
  -> MCP Client
  -> MCP Server
  -> Mock 或 demo 数据源
```

MCP Server 在 demo 阶段可以共享本项目 PostgreSQL 或内置 Mock 数据，但文档必须说明它逻辑上模拟外部小米商城系统。不得同时声明“工具调用必须经 MCP”和“可在 shopping 模块内绕过 MCP”。

理由：MCP 是本项目亮点之一，应保持架构主线；但首期不应强依赖真实外部商城系统。

替代方案：首期完全本地 mock，不走 MCP。该方案最快，但会削弱项目核心亮点，并导致后续切 MCP 时重构边界。

### Decision 6: 数据库按优先级阶段化，并修正会阻塞实现的硬矛盾

文档应区分：

- P0：首期必须支撑 `/api/chat`、基础会话、消息、意图、知识检索、demo 商品/购物工具。
- P1：购物车、订单、订单明细、结构化 Shopping data、trace 联调定位。
- P2：长期记忆、完整 trace span、促销模型、复杂交易一致性、多版本向量管理。

必须优先修正的设计矛盾：

- `t_agent_trace.trace_id` 不应单列唯一。
- FK 文档与 SQL 需统一：要么建物理 FK，要么明确逻辑 FK。
- 订单若作为真实购物能力，应有 `t_order_item`；若只是 mock，应明确限制。
- 购物车若保留 `cartId`，需说明生命周期；否则首期可简化为 user + sku。
- `updated_at` 维护策略需明确。
- 初始化脚本幂等性需明确。
- 中文检索方案需标出首期方案和后续优化。

理由：这些问题会直接影响 SQL 能否支撑文档声称的能力。

### Decision 7: 前端首期只强依赖稳定契约，不强依赖自然语言解析

前端首期渲染原则：

- `answer` 只作为自然语言消息展示。
- 购物状态卡片正式依据 `data`。
- 如后端暂未提供 `data`，前端只能展示弱提示或演示 mock，并明确标注。
- `/api/ready` 用于联调状态面板，`/api/health` 为可选存活探针。
- 三栏布局只在足够宽度启用，1024-1199 使用两栏或抽屉。

理由：避免前端为了展示完整效果而解析不稳定字符串，造成验收和联调不稳定。

## Risks / Trade-offs

- [Risk] 收敛 MVP 可能让部分“架构亮点”看起来被弱化。  
  Mitigation: 在文档中明确这些能力是二期增强，而不是删除；保留演进路线。

- [Risk] 引入结构化 `data` 会要求后端 DTO 和前端渲染同时调整。  
  Mitigation: 先在文档和测试用例中定义字段，再分阶段实现；`answer` 保持兼容。

- [Risk] MCP 首期仍保留会增加联调成本。  
  Mitigation: MCP Server 内部使用 Mock 或 demo 数据，并提供 clear ready 检查和降级文案。

- [Risk] 数据库表优先级调整可能与已有 SQL 不一致。  
  Mitigation: 先修正文档口径，再决定是否通过 migration 修改 SQL；不直接删除已存在设计，只标阶段。

- [Risk] Knowledge 职责收敛后，检索模块自治能力下降。  
  Mitigation: 保留 Knowledge 内部轻量查询规范化能力，但最终重检策略和质量结论由主 Agent 负责。

- [Risk] 前端 mock 商品卡标注“演示”会降低沉浸感。  
  Mitigation: 用视觉弱标签区分来源；当后端提供结构化 data 后再升级为真实推荐卡。

## Migration Plan

1. 修订文档状态与总览，标明本 change 是“首期 MVP 契约收敛”。
2. 修订接口文档，统一 `ChatReply`、错误响应、ready/health 分级、枚举和 ID 命名。
3. 修订 Agent 技术架构文档，收敛主 Agent、Knowledge、Shopping、MCP 的职责边界。
4. 修订数据库设计文档和 SQL 文档口径，标注 P0/P1/P2、修正 trace/订单/购物车/枚举/FK/updated_at/init 等问题。
5. 修订前端 UIUX 和前端技术架构文档，统一布局断点、快捷操作、ready 面板、购物卡片数据来源。
6. 更新测试用例文档，使验收标准基于收敛后的 MVP 契约。
7. 完成文档审核后，再进入代码/SQL 实现变更。

## Open Questions

- 首期 MCP 传输方式最终是否固定为 SSE？如果不是，需明确 stdio 与 SSE 的使用场景。
- 首期是否必须实际返回 `data` 字段，还是先在文档中定义并允许前端弱提示过渡？
- 首期数据库是否要实际新增 `t_order_item`，还是仅在设计中标为 P1？
- 首期中文检索采用 trigram/LIKE、zhparser，还是仅依赖向量召回 + 商品别名表？
- 是否需要把 `/api/ready` 拆成基础设施 ready 和业务数据 ready，还是先在同一接口中增加字段？
