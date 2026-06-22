# 测试用例 · 契约 DTO 与架构约束

> 模块：common 契约 + 架构原则约束校验
> 前缀：`CONTRACT` / `ARCH`
> 覆盖验收点：架构.md §8（主↔子节点信息契约）+ §2（七原则）

---

## 验收目标

1. 跨节点 DTO 字段完整、序列化正确、契约统一（只在 common 定义一次）。
2. Knowledge 返回不含主观自评；Shopping 返回 NEED_CLARIFY 必带 missingSlots。
3. 会话快照字段齐全（userId/intent/cart/history）。
4. 七原则可作为代码层约束被校验（入口唯一、开口唯一、记忆单点、能力正交）。

---

## CONTRACT-001 主→Knowledge 注入字段完整（正向）

- 模块：契约·KnowledgeRequest
- 用例类型：正向
- 优先级：高
- 前置条件：构造一次 Knowledge 调用
- 测试步骤：
  1. 构造 KnowledgeRequest
  2. 序列化为 JSON
- 预期结果：
  - 含 question / intent / snapshot / retryAttempt
  - JSON 序列化/反序列化无损
  - 字段语义对齐架构.md §8.1（意图+问题+快照）
- 需求来源：架构.md §8.1

---

## CONTRACT-002 Knowledge 返回不含主观质量自评（约束）★ P6

- 模块：契约·KnowledgeResponse
- 用例类型：正向
- 优先级：高
- 前置条件：任意 KnowledgeResponse
- 测试步骤：
  1. 审查 KnowledgeResponse 字段定义
- 预期结果：
  - 仅含 results / topScore / hitEntities / recallCount（客观信号）
  - 无 isGood / qualityScore / sufficient 等主观字段
  - RetrievalItem 含 sourceId/content/score/hitType
- 需求来源：架构.md §8.1；原则 P6

---

## CONTRACT-003 主→Shopping 注入槽位已澄清（正向）

- 模块：契约·ShoppingRequest
- 用例类型：正向
- 优先级：中
- 前置条件：构造一次 Shopping 调用
- 测试步骤：
  1. 构造 ShoppingRequest(action, slots, snapshot)
- 预期结果：
  - action 为明确操作意图（非原始 query）
  - slots 含已澄清的商品/数量/地址
  - snapshot 注入
- 需求来源：架构.md §8.2

---

## CONTRACT-004 Shopping 返回 NEED_CLARIFY 必带 missingSlots（约束）★ P4

- 模块：契约·ShoppingResponse
- 用例类型：正向
- 优先级：高
- 前置条件：status=NEED_CLARIFY 的响应
- 测试步骤：
  1. 校验响应字段
- 预期结果：
  - status=NEED_CLARIFY 时，missingSlots 非空
  - status=SUCCESS 时，resultData 含执行结果
  - status=FAILED 时，errorMessage 非空
  - 状态枚举完整：SUCCESS/NEED_CLARIFY/FAILED
- 需求来源：架构.md §8.2；原则 P4

---

## CONTRACT-005 会话快照字段齐全（正向）

- 模块：契约·SessionSnapshot
- 用例类型：正向
- 优先级：中
- 前置条件：构造 SessionSnapshot
- 测试步骤：
  1. 审查字段
- 预期结果：
  - 含 userId / conversationId / currentIntent / recentContext
  - 含 selectedProducts / cartState / browseHistory
  - 字段对齐架构.md §8.3（用户标识/意图/已选商品/购物车/浏览历史）
- 需求来源：架构.md §8.3

---

## ARCH-001 架构约束：意图识别入口唯一（约束）★ P1

- 模块：架构约束
- 用例类型：正向
- 优先级：高
- 前置条件：全模块代码
- 测试步骤：
  1. 全局搜索意图识别逻辑的位置
- 预期结果：
  - 意图识别相关类/方法仅出现在 orchestrator 模块
  - knowledge / shopping 模块无意图识别入口
- 需求来源：原则 P1

---

## ARCH-002 架构约束：记忆单点（约束）★ P3

- 模块：架构约束
- 用例类型：正向
- 优先级：高
- 前置条件：全模块代码
- 测试步骤：
  1. 检查记忆/状态持有类的分布
- 预期结果：
  - 三层记忆相关类仅出现在 orchestrator 模块
  - knowledge / shopping 无记忆存储字段（无状态）
- 需求来源：原则 P3

---

## ARCH-003 架构约束：能力正交，子节点不互通（约束）★ P5

- 模块：架构约束
- 用例类型：正向
- 优先级：高
- 前置条件：Maven 依赖图
- 测试步骤：
  1. 检查三节点模块间的 Maven 依赖
  2. 检查 Java 包引用
- 预期结果：
  - knowledge 与 shopping 之间**无 Maven 依赖**（不互相 import）
  - 三节点仅依赖 common
  - shopping 经 MCP 协议调 mcpserver（非 Java 包依赖）
  - bootstrap 聚合三节点
- 需求来源：原则 P5；技术架构总纲 §6、§8
