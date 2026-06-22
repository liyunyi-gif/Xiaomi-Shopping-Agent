# 测试用例 · MCP 外部工具调用路由

> 模块：Shopping 子节点（MCP 能力网关）+ MCP Server
> 前缀：`MCP`
> 覆盖验收点：架构.md §3.3（纯能力网关、工具编排、状态信号）+ §8.2（主→Shopping 契约）+ 原则 P4（只举手不开口）、P5（能力正交）

---

## 验收目标

1. **Shopping 纯能力网关**：无意图识别、无业务判断（推荐/付费引导归 Knowledge，禁止在 Shopping 长出）。
2. 按主 Agent 指令确定性编排工具（加购→下单等）。
3. 状态信号：SUCCESS / NEED_CLARIFY / FAILED + 缺失槽位清单。
4. **P4 举手流**：缺参数/缺库存返回 NEED_CLARIFY + missingSlots，**不直接问用户**。
5. MCP 协议接入：Client（spring-ai-starter-mcp-client）→ Server（@McpTool）经 SSE。
6. 主 Agent 不直接调 MCP，经 Shopping 网关（P5）。

---

## MCP-001 按指令确定性编排加购工具（正向）

- 模块：Shopping·工具编排
- 用例类型：正向
- 优先级：高
- 前置条件：主 Agent 注入 ShoppingRequest(action=add_cart, slots={skuId, qty, ...})
- 测试步骤：
  1. Shopping 接收请求
  2. 经 MCP Client 调用 mcpserver 的 add_cart 工具
- 预期结果：
  - 工具被调用，参数正确透传
  - 返回 ShoppingResponse(status=SUCCESS, resultData={cartId,...})
  - 编排是确定性的（按 action 路由到固定工具，无自主决策）
- 需求来源：架构.md §3.3；技术架构 购物Agent §5

---

## MCP-002 工具编排链：加购→下单（正向）

- 模块：Shopping·工具编排
- 用例类型：正向
- 优先级：中
- 前置条件：主 Agent 指令依次 add_cart → place_order
- 测试步骤：
  1. 执行加购
  2. 复用 cartId 执行下单
- 预期结果：
  - 两步工具按序编排，状态/结果正确传递
  - 每步结果回主 Agent，由主 Agent 决定是否进下一步
- 需求来源：架构.md §3.3；§4.2

---

## MCP-003 缺库存/缺参数 → 返回 NEED_CLARIFY（异常）

- 模块：Shopping·状态信号
- 用例类型：异常
- 优先级：高
- 前置条件：加购时库存为0，或下单缺收货地址
- 测试步骤：
  1. Shopping 调用工具，工具返回缺库存/缺地址
  2. Shopping 组装响应
- 预期结果：
  - ShoppingResponse.status = NEED_CLARIFY
  - missingSlots 含缺失项（如 ["收货地址"]）或缺库存标识
  - **Shopping 不直接向用户提问**（P4）
- 需求来源：架构.md §8.2；原则 P4

---

## MCP-004 举手流：主 Agent 据 missingSlots 开口（正向）★ P2/P4

- 模块：Shopping·举手流
- 用例类型：正向
- 优先级：高
- 前置条件：MCP-003 返回 NEED_CLARIFY + missingSlots=["收货地址"]
- 测试步骤：
  1. 主 Agent 接收 ShoppingResponse
  2. 主 Agent 据缺失清单开口反问
- 预期结果：
  - **由主 Agent 开口**询问"请提供收货地址"（P2 唯一开口权）
  - Shopping 全程不直接面对用户
  - 澄清后主 Agent 重新注入完整槽位再次委派
- 需求来源：架构.md §4.3（举手流）；原则 P4、P2

---

## MCP-005 边界红线：Shopping 不长出推荐/付费引导（约束）★ P5

- 模块：Shopping·能力边界
- 用例类型：正向
- 优先级：高
- 前置条件：审查 Shopping 代码与 prompt
- 测试步骤：
  1. 注入一个"推荐手机"性质的请求到 Shopping
  2. 检查 Shopping 是否产生推荐策略/对比逻辑
- 预期结果：
  - Shopping **拒绝/不处理**推荐型请求（推荐归 Knowledge）
  - Shopping 内无推荐策略、无付费引导逻辑
  - 仅有确定性工具编排（add_cart/order/logistics/inventory/promotion）
  - 能力正交：检索型→Knowledge，外部调用型→Shopping（P5）
- 需求来源：架构.md §3.3（边界红线）；原则 P5
