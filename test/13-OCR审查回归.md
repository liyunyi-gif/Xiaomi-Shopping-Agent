# OCR 审查回归测试用例

> 模块前缀：OCRFIX  
> 来源：OpenCodeReview `main..HEAD` 会话 `5d050bc1-9fca-4b09-ab17-ec5407d7e0a0`  
> 目标：修复 OCR 发现的问题，并防止同类回归。

## OCRFIX-001 数据库索引名称唯一

- 模块：数据库 schema
- 用例标题：`t_agent_trace` 不重复创建 `idx_trace_id`
- 前置条件：使用空 PostgreSQL 数据库执行 `doc/database/schema.sql`
- 测试步骤：
  1. 执行 schema 脚本。
  2. 查询 `pg_indexes` 中 `t_agent_trace` 的索引。
- 预期结果：脚本执行成功；`idx_trace_id` 仅出现一次；同一 `trace_id` 允许对应多个节点记录。
- 对应需求/设计来源：`doc/database/数据库设计.md` trace 索引设计
- 优先级：高
- 用例类型：异常/边界

## OCRFIX-002 未知购物动作安全拒绝

- 模块：Orchestrator Shopping 路由
- 用例标题：未知二级意图不得默认映射为加购
- 前置条件：构造 `primaryIntent=SHOPPING` 且 `secondaryIntent` 不在白名单中的请求
- 测试步骤：
  1. 调用 Orchestrator。
  2. 检查回复和 ShoppingGateway 调用次数。
- 预期结果：返回 `UNSUPPORTED_SHOPPING_ACTION` 澄清/失败结果；不调用 ShoppingGateway；不产生 `add_to_cart` 副作用。
- 对应需求/设计来源：`doc/购物Agent-技术架构.md` ShoppingAction；`doc/接口文档.md` Shopping action 白名单
- 优先级：高
- 用例类型：异常

## OCRFIX-003 空状态响应不抛异常

- 模块：Orchestrator 结构化回复
- 用例标题：ShoppingResponse 的可空 status 不导致 NPE
- 前置条件：ShoppingGateway 返回 `status=null`，其余结果数据可用
- 测试步骤：
  1. 执行 Shopping 请求。
  2. 检查 answer、data、traceId。
- 预期结果：返回可读降级提示；结构化 data 保留空状态；不抛出 NullPointerException。
- 对应需求/设计来源：`doc/接口文档.md` ChatReply；`test/08-契约DTO与架构约束.md` CONTRACT-009
- 优先级：中
- 用例类型：边界

## OCRFIX-004 库存结果使用 stock 类型

- 模块：前端 responseParser
- 用例标题：库存查询和空购物状态可区分
- 前置条件：`ChatReply.data.stock` 包含 SKU 和库存数量
- 测试步骤：
  1. 调用 `parseShoppingState`。
  2. 检查返回的 type 和 fields。
- 预期结果：`type=stock`；返回 SKU 与库存数量；不返回 `type=empty`。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §12.4；`doc/接口文档.md` §10.5
- 优先级：中
- 用例类型：正向

## OCRFIX-005 输入框占位文案分支可维护

- 模块：前端 InputComposer
- 用例标题：loading、澄清和普通输入使用显式分支
- 前置条件：组件可分别处于 loading、needClarify 和普通状态
- 测试步骤：
  1. 静态检查占位文案选择逻辑。
  2. 分别渲染三种状态。
- 预期结果：不使用嵌套三元表达式；三种状态显示对应占位文案。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §12.3
- 优先级：低
- 用例类型：正向

## OCRFIX-006 移动端面板标题映射可维护

- 模块：前端 MobileBottomSheet
- 用例标题：面板标题通过映射表渲染
- 前置条件：面板状态分别为 prompts、cart、status
- 测试步骤：
  1. 静态检查标题渲染逻辑。
  2. 分别打开三个面板。
- 预期结果：不使用嵌套三元表达式；三个面板分别显示推荐商品、购物状态、后端状态。
- 对应需求/设计来源：`doc/前端UIUX设计文档.md` §5.3；`test/10-前端商品卡片购物状态与快捷操作.md`
- 优先级：低
- 用例类型：正向

## OCRFIX-007 滚动条仅作用于应用滚动区

- 模块：前端样式与无障碍
- 用例标题：滚动条样式不污染全局嵌套容器
- 前置条件：页面存在消息列表、侧栏、状态详情、移动端 Sheet 等滚动区域
- 测试步骤：
  1. 检查 CSS 选择器范围。
  2. 启用 `forced-colors: active`。
- 预期结果：滚动条样式仅作用于显式应用滚动容器；强制颜色模式恢复系统可辨识颜色。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §7.3、§11.3；FEUX 无障碍规范
- 优先级：低
- 用例类型：边界

## OCRFIX-008 页面背景层叠顺序稳定

- 模块：前端全局样式
- 用例标题：背景装饰不因负 z-index 消失或遮挡正文
- 前置条件：加载包含 body 网格背景的页面
- 测试步骤：
  1. 检查根层叠上下文。
  2. 在主流浏览器打开页面并滚动/切换面板。
- 预期结果：正文始终位于背景装饰之上；网格背景可见且不影响点击；不同浏览器表现一致。
- 对应需求/设计来源：`doc/前端UIUX设计文档.md` §3.1、§5.1
- 优先级：低
- 用例类型：边界

## OCRFIX-009 Shopping 结构化 payload 无冗余分支

- 模块：Orchestrator shoppingData
- 用例标题：payload 直接使用结果数据
- 前置条件：工具结果分别来自 add_to_cart、place_order、query_stock、query_logistics
- 测试步骤：
  1. 检查各工具结果的结构化 data。
  2. 静态检查 payload 组装逻辑。
- 预期结果：各工具均保留完整 resultData；不保留所有分支都返回同一对象的无效 switch；工具 key 映射仍正确。
- 对应需求/设计来源：`doc/接口文档.md` §5、§10；`doc/购物Agent-技术架构.md` §5
- 优先级：低
- 用例类型：正向/边界
