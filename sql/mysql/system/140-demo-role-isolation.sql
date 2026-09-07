/*
  演示账号多角色隔离 SQL
  Schema: ruoyi-vue-pro
  Date: 2026-06-30

  内容：
  1. 修复供应链经理(910004)角色，补齐 3 个 BPM 模块权限
  2. 修复销售经理用户(910203)分配销售审批经理角色
  3. 新建 3 个角色：销售总监(95001)、仓库主管(95002)、仓库操作员(95003)
  4. 新建 4 个用户 + 角色分配
  5. 为所有新角色分配菜单权限（严格隔离）

  密码：所有新用户密码为 123456
  密码哈希：$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 常量
-- ============================================================
SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';
SET @erp_root = 2563;

-- ============================================================
-- Part 1: 修复供应链经理(910004)角色 —— 补齐采购退货/其它入库/其它出库菜单+按钮
-- ============================================================
INSERT IGNORE INTO system_role_menu (role_id, menu_id) VALUES
  -- 采购退货 (parent=2680)
  (910004, 2680),
  (910004, 2681), (910004, 2682), (910004, 2683), (910004, 2684), (910004, 2685), (910004, 2686),
  -- 其它入库 (parent=2596)
  (910004, 2596),
  (910004, 2597), (910004, 2598), (910004, 2599), (910004, 2600), (910004, 2601), (910004, 2609),
  -- 其它出库 (parent=2610)
  (910004, 2610),
  (910004, 2611), (910004, 2612), (910004, 2613), (910004, 2614), (910004, 2615), (910004, 2616),
  -- 产品库存按钮 (parent=2590) 补齐批次库存
  (910004, 930202), (910004, 930203), (910004, 930204), (910004, 930205),
  -- 库存调拨按钮 (parent=2624)
  (910004, 2626), (910004, 2627), (910004, 2628), (910004, 2629), (910004, 2630),
  -- 库存盘点按钮 (parent=2631)
  (910004, 2633), (910004, 2634), (910004, 2635), (910004, 2636), (910004, 2637);

-- ============================================================
-- Part 2: 修复销售经理用户(910203)分配角色
-- ============================================================
INSERT IGNORE INTO system_user_role (user_id, role_id) VALUES
  (910203, 910003);

-- ============================================================
-- Part 3: 新建 3 个角色
-- ============================================================
INSERT INTO system_role (id, name, code, data_scope, sort, type, status, deleted, create_time, update_time) VALUES
  (95001, '销售总监',   'erp_sales_director',       1, 0, 1, 0, b'0', NOW(), NOW()),
  (95002, '仓库主管',   'erp_warehouse_supervisor',  1, 0, 1, 0, b'0', NOW(), NOW()),
  (95003, '仓库操作员', 'erp_warehouse_operator',    1, 0, 1, 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================
-- Part 4: 新建 4 个用户 + 分配角色
-- ============================================================

-- 4.1 销售总监
INSERT INTO system_users (id, username, password, nickname, status, deleted, create_time, update_time)
VALUES (980001, 'sales_director', @password_hash, '销售总监', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '销售总监';
INSERT IGNORE INTO system_user_role (user_id, role_id) VALUES (980001, 95001);

-- 4.2 仓库主管
INSERT INTO system_users (id, username, password, nickname, status, deleted, create_time, update_time)
VALUES (980002, 'warehouse_mgr', @password_hash, '仓库主管', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '仓库主管';
INSERT IGNORE INTO system_user_role (user_id, role_id) VALUES (980002, 95002);

-- 4.3 仓库操作员
INSERT INTO system_users (id, username, password, nickname, status, deleted, create_time, update_time)
VALUES (980003, 'warehouse_op', @password_hash, '仓库操作员', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '仓库操作员';
INSERT IGNORE INTO system_user_role (user_id, role_id) VALUES (980003, 95003);

-- 4.4 销售经理（修复已有用户）
INSERT INTO system_users (id, username, password, nickname, status, deleted, create_time, update_time)
VALUES (910203, 'sales_gm', @password_hash, '销售审批总经理', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '销售审批总经理';
INSERT IGNORE INTO system_user_role (user_id, role_id) VALUES (910203, 910003);

-- ============================================================
-- Part 5: 角色菜单权限分配
-- ============================================================

-- ============================================================
-- 5.1 销售总监 (95001) —— 完整销售模块（不含财务）
-- ============================================================
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, @erp_root),
  (95001, 2617);

-- 客户管理
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 931313),
  (95001, 93131301), (95001, 93131302), (95001, 93131303),
  (95001, 93131304), (95001, 93131305), (95001, 93131306);

-- 销售订单台账
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 2638),
  (95001, 2639), (95001, 2640), (95001, 2641), (95001, 2642), (95001, 2643), (95001, 2644);

-- 发货通知与签收
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 2652),
  (95001, 2653), (95001, 2654), (95001, 2655), (95001, 2656), (95001, 2657), (95001, 2658);

-- 销售退货
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 2659),
  (95001, 2660), (95001, 2661), (95001, 2662), (95001, 2663), (95001, 2664), (95001, 2665);

-- 发货放行审核
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 931308),
  (95001, 93130801), (95001, 93130802), (95001, 93130803),
  (95001, 93130804), (95001, 93130805), (95001, 93130806);

-- 市场执行台账
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 931309),
  (95001, 93130901), (95001, 93130902);

-- 市场预警与统计
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 931310),
  (95001, 93131001), (95001, 93131002), (95001, 93131003);

-- 销项发票管理（只读）
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95001, 932020),
  (95001, 93202001), (95001, 93202003);

-- ============================================================
-- 5.2 仓库主管 (95002) —— 供应链仓库全量（含审批）
-- ============================================================
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, @erp_root),
  (95002, 2602);

-- 产品库存
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2590),
  (95002, 2591), (95002, 2592),
  (95002, 930202), (95002, 930203), (95002, 930204), (95002, 930205);

-- 库存明细
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2593),
  (95002, 2594), (95002, 2595);

-- 其它入库 (含审批 2609)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2596),
  (95002, 2597), (95002, 2598), (95002, 2599), (95002, 2600), (95002, 2601), (95002, 2609);

-- 其它出库 (含审批 2616)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2610),
  (95002, 2611), (95002, 2612), (95002, 2613), (95002, 2614), (95002, 2615), (95002, 2616);

-- 采购退货 (含审批 2686)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2680),
  (95002, 2681), (95002, 2682), (95002, 2683), (95002, 2684), (95002, 2685), (95002, 2686);

-- 库存调拨
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2624),
  (95002, 2625), (95002, 2626), (95002, 2627), (95002, 2628), (95002, 2629), (95002, 2630);

-- 库存盘点
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2631),
  (95002, 2632), (95002, 2633), (95002, 2634), (95002, 2635), (95002, 2636), (95002, 2637);

-- 仓库信息
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 2584),
  (95002, 2585), (95002, 2586), (95002, 2587), (95002, 2588), (95002, 2589);

-- 库存分析
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 932310),
  (95002, 932311);

-- 仓库分类管理
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95002, 932312),
  (95002, 932313);

-- ============================================================
-- 5.3 仓库操作员 (95003) —— 只能建单/编辑/查询/导出，无审批权
-- ============================================================
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, @erp_root),
  (95003, 2602);

-- 产品库存 (只读)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2590),
  (95003, 2591), (95003, 2592);

-- 库存明细 (只读)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2593),
  (95003, 2594), (95003, 2595);

-- 其它入库 (无审批按钮 2609)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2596),
  (95003, 2597), (95003, 2598), (95003, 2599), (95003, 2600), (95003, 2601);

-- 其它出库 (无审批按钮 2616)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2610),
  (95003, 2611), (95003, 2612), (95003, 2613), (95003, 2614), (95003, 2615);

-- 采购退货 (无审批按钮 2686)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2680),
  (95003, 2681), (95003, 2682), (95003, 2683), (95003, 2684), (95003, 2685);

-- 仓库信息 (只读)
INSERT INTO system_role_menu (role_id, menu_id) VALUES
  (95003, 2584),
  (95003, 2585);

SET FOREIGN_KEY_CHECKS = 1;
