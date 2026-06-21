-- ============================================================
-- 小米商城智能导购 Agent · 初始化数据 (init)
-- 配套: schema.sql (先建表), doc/数据库设计.md
-- 内容: 用户 / 类目 / 示例商品 / 知识库 / 意图树 / 术语映射
-- ============================================================

-- ============================================================
-- 1. 用户（示例）
-- ============================================================
INSERT INTO t_user (user_code, username, password, role, avatar) VALUES
('U1001', 'admin', 'admin', 'admin', NULL),
('U1002', 'demo_user', 'demo', 'user', NULL);

-- ============================================================
-- 2. 商品类目（10 大一级品类 + 部分二级）
-- ============================================================
INSERT INTO t_category (category_code, name, level, parent_id, sort_order) VALUES
-- 一级
('C_PHONE',      '手机',     1, NULL, 1),
('C_TABLET',     '平板',     1, NULL, 2),
('C_TV',         '电视',     1, NULL, 3),
('C_NOTEBOOK',   '笔记本',   1, NULL, 4),
('C_WEARABLE',   '穿戴',     1, NULL, 5),
('C_HOME_APPL',  '家电',     1, NULL, 6),
('C_SMARTHOME',  '智能家居', 1, NULL, 7),
('C_AUDIO',      '音频',     1, NULL, 8),
('C_ACCESSORY',  '配件',     1, NULL, 9),
('C_LIFESTYLE',  '生活周边', 1, NULL, 10);

-- 二级（手机/平板示例，level=2，parent_id 指向一级 id）
-- 注意: parent_id 需与一级插入后的实际 id 对应; 此处用子查询兜底
INSERT INTO t_category (category_code, name, level, parent_id, sort_order)
SELECT 'C_PHONE_FLAGSHIP', '旗舰手机', 2, id, 1 FROM t_category WHERE category_code='C_PHONE';
INSERT INTO t_category (category_code, name, level, parent_id, sort_order)
SELECT 'C_PHONE_REDMI', 'Redmi', 2, id, 2 FROM t_category WHERE category_code='C_PHONE';
INSERT INTO t_category (category_code, name, level, parent_id, sort_order)
SELECT 'C_TABLET_FLAGSHIP', '旗舰平板', 2, id, 1 FROM t_category WHERE category_code='C_TABLET';

-- ============================================================
-- 3. 示例商品 SPU / SKU（手机/平板示例，对应 500SKU 业务的子集）
-- ============================================================
INSERT INTO t_product_spu (spu_code, name, brand, category_id, subtitle, description, status) VALUES
('MI-14',
 '小米14',
 'Xiaomi',
 (SELECT id FROM t_category WHERE category_code='C_PHONE_FLAGSHIP'),
 '骁龙8 Gen3 徕卡光学镜头',
 '小米14 旗舰手机，第三代骁龙8，徕卡专业光学镜头，高刷新率屏幕。',
 1),
('MI-14-PRO',
 '小米14 Pro',
 'Xiaomi',
 (SELECT id FROM t_category WHERE category_code='C_PHONE_FLAGSHIP'),
 '全等深微曲屏 徕卡可变光圈',
 '小米14 Pro，全等深微曲屏，徕卡可变光圈镜头，旗舰影像。',
 1),
('REDMI-K70',
 'Redmi K70',
 'Redmi',
 (SELECT id FROM t_category WHERE category_code='C_PHONE_REDMI'),
 '第二代骁龙8 2K屏',
 'Redmi K70，第二代骁龙8，2K直屏，性价比旗舰。',
 1),
('MI-PAD-6',
 '小米平板6',
 'Xiaomi',
 (SELECT id FROM t_category WHERE category_code='C_TABLET_FLAGSHIP'),
 '骁龙870 11英寸 2.8K屏',
 '小米平板6，骁龙870，11英寸2.8K高刷屏，影音娱乐。',
 1);

-- SKU（规格单元，加购粒度）
INSERT INTO t_product_sku (sku_code, spu_id, spec_info, spec_json, price, stock, status) VALUES
('MI-14-16-512-BLACK',
 (SELECT id FROM t_product_spu WHERE spu_code='MI-14'),
 '16GB+512GB 黑色',
 '{"ram":"16GB","storage":"512GB","color":"黑色"}',
 4599.00, 100, 1),
('MI-14-16-256-BLACK',
 (SELECT id FROM t_product_spu WHERE spu_code='MI-14'),
 '16GB+256GB 黑色',
 '{"ram":"16GB","storage":"256GB","color":"黑色"}',
 4299.00, 0, 1),  -- 模拟缺库存
('MI-14-PRO-16-512-WHITE',
 (SELECT id FROM t_product_spu WHERE spu_code='MI-14-PRO'),
 '16GB+512GB 白色',
 '{"ram":"16GB","storage":"512GB","color":"白色"}',
 5299.00, 50, 1),
('REDMI-K70-16-512-BLACK',
 (SELECT id FROM t_product_spu WHERE spu_code='REDMI-K70'),
 '16GB+512GB 墨羽',
 '{"ram":"16GB","storage":"512GB","color":"墨羽"}',
 2699.00, 80, 1),
('MI-PAD-6-8-256-GRAY',
 (SELECT id FROM t_product_spu WHERE spu_code='MI-PAD-6'),
 '8GB+256GB 灰色',
 '{"ram":"8GB","storage":"256GB","color":"灰色"}',
 1999.00, 60, 1);

-- ============================================================
-- 4. 知识库（示例：手机咨询知识库）
-- ============================================================
INSERT INTO t_knowledge_base (name, embedding_model, collection_name, description) VALUES
('手机平板咨询知识库',
 'text-embedding-v3',
 'kb_phone_tablet',
 '覆盖手机/平板商品咨询、规格比对、促销政策、售后服务');

-- 示例文档（demo 阶段 file_url 指向本地资料，状态 pending 待 Tika 解析）
INSERT INTO t_knowledge_document (kb_id, spu_id, doc_name, file_url, file_type, status, source_type) VALUES
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 (SELECT id FROM t_product_spu WHERE spu_code='MI-14'),
 '小米14 产品规格.pdf', 'classpath:/data/docs/mi14_spec.pdf', 'pdf', 'pending', 'file'),
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 (SELECT id FROM t_product_spu WHERE spu_code='MI-14-PRO'),
 '小米14Pro 产品规格.pdf', 'classpath:/data/docs/mi14pro_spec.pdf', 'pdf', 'pending', 'file');

-- ============================================================
-- 5. 意图树（3 一级 + 部分二级，对齐架构 §3.1 / 知识库/购物/系统）
-- ============================================================
-- 一级意图
INSERT INTO t_intent_node (kb_id, intent_code, name, level, parent_code, kind, description, sort_order) VALUES
(NULL, 'KNOWLEDGE', '知识库问答', 1, NULL, 0, '商品咨询、规格比对、促销/售后政策查询', 1),
(NULL, 'SHOPPING',  '购物操作',   1, NULL, 1, '加购、下单、查询订单/物流、改购物车', 2),
(NULL, 'SYSTEM',    '系统指令',   1, NULL, 2, '清除记忆、查看历史对话等系统操作', 3);

-- 二级 · 知识库类（KNOWLEDGE 下）
INSERT INTO t_intent_node (kb_id, intent_code, name, level, parent_code, kind, description, examples, top_k, sort_order) VALUES
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 'KNOWLEDGE.PRODUCT_CONSULT', '商品咨询', 2, 'KNOWLEDGE', 0,
 '商品基本信息、卖点、适用场景咨询',
 '小米14的影像规格怎么样||这款手机续航如何',
 5, 1),
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 'KNOWLEDGE.SPEC_COMPARE', '规格比对', 2, 'KNOWLEDGE', 0,
 '多款商品规格参数对比',
 '小米14和小米14 Pro有什么区别||对比一下这两款手机',
 5, 2),
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 'KNOWLEDGE.PROMOTION', '促销政策', 2, 'KNOWLEDGE', 0,
 '优惠、满减、以旧换新等促销政策',
 '现在有什么优惠||支持以旧换新吗',
 3, 3),
((SELECT id FROM t_knowledge_base WHERE collection_name='kb_phone_tablet'),
 'KNOWLEDGE.AFTER_SALE', '售后服务', 2, 'KNOWLEDGE', 0,
 '保修、退换货、维修等售后政策',
 '保修期多久||七天无理由退货吗',
 3, 4);

-- 二级 · 购物类（SHOPPING 下，带 shopping_action）
INSERT INTO t_intent_node (kb_id, intent_code, name, level, parent_code, kind, description, examples, shopping_action, sort_order) VALUES
(NULL, 'SHOPPING.ADD_TO_CART', '加入购物车', 2, 'SHOPPING', 1,
 '将商品加入购物车',
 '帮我加购一台小米14 16+512||把这款加到购物车',
 'ADD_TO_CART', 1),
(NULL, 'SHOPPING.PLACE_ORDER', '提交订单', 2, 'SHOPPING', 1,
 '提交订单/结算下单',
 '帮我下单||直接结算吧',
 'PLACE_ORDER', 2),
(NULL, 'SHOPPING.QUERY_LOGISTICS', '查物流', 2, 'SHOPPING', 1,
 '查询订单物流状态',
 '我的订单到哪了||物流单号多少',
 'QUERY_LOGISTICS', 3),
(NULL, 'SHOPPING.QUERY_STOCK', '查库存', 2, 'SHOPPING', 1,
 '查询商品库存',
 '这个还有货吗||库存多少',
 'QUERY_STOCK', 4),
(NULL, 'SHOPPING.QUERY_PROMOTION', '查优惠', 2, 'SHOPPING', 1,
 '查询可享优惠',
 '我能享受什么优惠||有哪些满减',
 'QUERY_PROMOTION', 5);

-- 二级 · 系统类（SYSTEM 下）
INSERT INTO t_intent_node (kb_id, intent_code, name, level, parent_code, kind, description, examples, sort_order) VALUES
(NULL, 'SYSTEM.CLEAR_MEMORY', '清除记忆', 2, 'SYSTEM', 2,
 '清除用户的长期记忆（隐私可控）',
 '清空我的记忆||忘掉我之前的偏好',
 1),
(NULL, 'SYSTEM.VIEW_HISTORY', '查看历史', 2, 'SYSTEM', 2,
 '查看历史对话',
 '我之前问过什么||看看历史聊天',
 2);

-- ============================================================
-- 6. 术语归一化映射（查询重写口语→规范，喂给 Knowledge 重写）
-- ============================================================
INSERT INTO t_query_term_mapping (domain, source_term, target_term, match_type, priority) VALUES
('phone', '打游戏爽',       '高性能 GPU 高刷新率',  2, 100),
('phone', '续航久',         '大电池容量 低功耗',     2, 100),
('phone', '拍照好',         '高像素 徕卡影像 大底传感器', 2, 100),
('phone', '它',             '指代消解-结合会话快照', 1, 90),
('phone', '这款',           '指代消解-结合会话快照', 1, 90),
('tablet','追剧用',         '大屏 高分辨率 双扬声器', 2, 100);

-- ============================================================
-- 初始化完成
-- 说明:
-- 1. t_knowledge_chunk / t_knowledge_vector 需运行时由 Knowledge 子节点
--    用 Tika 解析文档→切片→embedding 后写入, 此处不预置(向量需调用模型)。
-- 2. t_message / t_user_longterm_memory / t_cart_item / t_order / t_agent_trace
--    为运行时产生, 此处不预置。
-- ============================================================
