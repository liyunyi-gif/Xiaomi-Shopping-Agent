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

## OCRFIX-010 无结构化数据的澄清响应清理旧状态

- 模块：前端 chatStore / responseParser
- 用例标题：澄清回复没有 data 时不保留旧购物状态
- 前置条件：store 已有购物车或订单状态；接口返回 `needClarify=true` 且省略 `data`
- 测试步骤：
  1. 发送会触发澄清的请求。
  2. 检查购物状态卡片。
- 预期结果：显示当前澄清状态，不继续显示旧购物车/订单状态。
- 对应需求/设计来源：`doc/前端UIUX需求文档.md` §3.3；`doc/接口文档.md` §5.8
- 优先级：中
- 用例类型：异常

## OCRFIX-011 Trace span 标识在单个 trace 内唯一

- 模块：数据库 trace schema
- 用例标题：span_id 非空且按 trace_id 组合唯一
- 前置条件：执行 `doc/database/schema.sql`
- 测试步骤：
  1. 插入同一 trace 的不同 span。
  2. 尝试插入同一 trace、同一 span_id 的重复记录。
- 预期结果：不同 span 插入成功；重复 `(trace_id, span_id)` 被数据库约束拒绝；span_id 不允许为空。
- 对应需求/设计来源：`doc/database/数据库设计.md` trace 设计
- 优先级：中
- 用例类型：异常/边界

## OCRFIX-012 状态面板首次加载自动刷新

- 模块：前端 BackendStatusPanel
- 用例标题：面板挂载时自动请求 health/ready
- 前置条件：页面首次加载，后端状态尚未缓存
- 测试步骤：
  1. 挂载状态面板。
  2. 观察 API 调用和 loading 状态。
- 预期结果：自动调用 `refresh`；完成后展示状态；用户无需先点击刷新。
- 对应需求/设计来源：`test/11-前端后端状态面板与降级错误状态.md` FESTATUS-001
- 优先级：高
- 用例类型：正向

## OCRFIX-013 状态标签文案统一

- 模块：前端 BackendStatusPanel
- 用例标题：状态芯片使用一致的中文文案
- 前置条件：Ready 与 Health 状态已加载
- 测试步骤：检查两个状态芯片的可访问名称。
- 预期结果：分别显示“就绪状态”和“健康状态”，不出现中英文无空格混排。
- 对应需求/设计来源：`test/11-前端后端状态面板与降级错误状态.md` FESTATUS-010
- 优先级：低
- 用例类型：正向

## OCRFIX-014 结构化字段仅展示原始类型或回退值

- 模块：前端 responseParser
- 用例标题：对象/数组字段不会渲染为 `[object Object]`
- 前置条件：后端返回 malformed 的对象或数组字段
- 测试步骤：调用 `parseShoppingState` 并检查卡片 fields。
- 预期结果：非字符串、数字、布尔值回退到默认文案；不展示误导性的对象字符串。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §12.4
- 优先级：低
- 用例类型：异常/边界

## OCRFIX-015 Tailwind 非标准透明度使用任意值语法

- 模块：前端 Button 样式
- 用例标题：按钮透明度 class 可被 Tailwind 正确生成
- 前置条件：构建前端生产包
- 测试步骤：
  1. 检查 secondary/danger 按钮的背景和 hover class。
  2. 检查构建产物是否包含对应透明背景规则。
- 预期结果：非标准透明度使用 `bg-*/[0.xx]` 任意值语法；构建后按钮背景和 hover 状态生效。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §7.2；前端视觉 token 规范
- 优先级：中
- 用例类型：正向

## OCRFIX-016 1024–1279px 辅助面板保持可达

- 模块：前端响应式布局
- 用例标题：平板/小桌面仍可访问购物和后端状态
- 前置条件：视口宽度为 1024–1279px
- 测试步骤：
  1. 打开页面并检查辅助面板入口。
  2. 打开购物和后端状态面板。
- 预期结果：三栏桌面侧栏可以收敛为底部 Sheet 或等价入口；购物车和状态面板均可访问；不出现功能空窗。
- 对应需求/设计来源：`doc/前端UIUX需求文档.md` §5.2/§5.3；FEUX-002/FEUX-013
- 优先级：高
- 用例类型：边界

## OCRFIX-017 Trace span_id 缺省时自动生成

- 模块：数据库 trace schema
- 用例标题：应用未显式传入 span_id 时仍可插入链路记录
- 前置条件：执行 schema 后，仅填写 trace_id 和其他必填字段插入 t_agent_trace
- 测试步骤：执行插入并查询生成的 span_id。
- 预期结果：插入成功；数据库自动生成非空 span_id；同一 trace 的不同记录仍保持组合唯一。
- 对应需求/设计来源：`doc/database/数据库设计.md` trace 设计；`doc/database/schema.sql`
- 优先级：高
- 用例类型：边界

## OCRFIX-018 非成功响应不渲染成功业务卡片

- 模块：前端 responseParser
- 用例标题：NEED_CLARIFY/FAILED 响应中的残留 payload 不得被当作成功
- 前置条件：响应 data 同时包含非 SUCCESS status 和 cart/order/stock payload
- 测试步骤：调用 `parseShoppingState`。
- 预期结果：仅返回澄清/失败状态或 undefined；不返回 added/orderCreated/logistics/stock 成功卡片。
- 对应需求/设计来源：`doc/接口文档.md` §5.8；`doc/前端技术架构文档.md` §12.4
- 优先级：中
- 用例类型：异常

## OCRFIX-019 优惠查询动作完整路由

- 模块：Orchestrator Shopping action
- 用例标题：促销咨询路由到 query_promotion
- 前置条件：`secondaryIntent=促销咨询`
- 测试步骤：调用 Orchestrator 并检查传给 ShoppingGateway 的 action。
- 预期结果：action 为 `query_promotion`；调用一次 ShoppingGateway；不返回 unsupported action。
- 对应需求/设计来源：`doc/购物Agent-技术架构.md` §5；`doc/接口文档.md` §9.3/§10.6
- 优先级：高
- 用例类型：正向

## OCRFIX-020 槽位别名跳过空白首选值

- 模块：ShoppingSlotUtils
- 用例标题：skuCode 为空时回退到 skuId
- 前置条件：slots 同时包含空白 `skuCode` 和有效 `skuId`
- 测试步骤：调用 `stringSlot(slots, "skuCode", "skuId")`。
- 预期结果：返回有效 `skuId`；不因首个键存在但为空而提前返回 null。
- 对应需求/设计来源：`doc/购物Agent-技术架构.md` §5；CONTRACT-003
- 优先级：中
- 用例类型：边界

## OCRFIX-021 数值字段拒绝对象和数组

- 模块：前端 responseParser
- 用例标题：库存/数量字段类型异常时使用回退值
- 前置条件：stock 或 quantity 字段为对象/数组
- 测试步骤：调用 `parseShoppingState` 并检查 fields。
- 预期结果：不渲染 `[object Object]` 或数组拼接文本；使用定义的回退值。
- 对应需求/设计来源：`doc/前端技术架构文档.md` §12.4
- 优先级：低
- 用例类型：异常/边界
