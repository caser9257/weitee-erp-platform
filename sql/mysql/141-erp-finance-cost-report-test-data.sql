/*
 * 产品成本分析报表测试数据
 *
 * 覆盖页面：
 * - 产品成本分析报表：/erp/finance/cost-report
 * - 产品成本汇总：/erp/finance/cost-product
 *
 * 设计说明：
 * - 可重复执行，固定 ID，使用 ON DUPLICATE KEY UPDATE 覆盖同一批样本
 * - 不删除现有业务数据
 * - 包含 2026-01 到 2026-06 共 6 个月的数据，用于测试趋势分析
 * - 包含 3 个产品、8 个工单，用于测试产品对比
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

-- ============================================================
-- 1. 产品分类和单位（如不存在则创建）
-- ============================================================

-- 产品分类
INSERT INTO erp_product_category (id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted)
VALUES
(99001, 0, '测试成品', 'TEST-COST-001', 1, 0, '1', NOW(), '1', NOW(), b'0'),
(99002, 99001, '电机类', 'TEST-COST-001-01', 2, 0, '1', NOW(), '1', NOW(), b'0'),
(99003, 99001, '电子类', 'TEST-COST-001-02', 3, 0, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), updater = '1', update_time = NOW();

-- 产品单位
INSERT INTO erp_product_unit (id, name, status, creator, create_time, updater, update_time, deleted)
VALUES
(99001, '个', 0, '1', NOW(), '1', NOW(), b'0'),
(99002, '块', 0, '1', NOW(), '1', NOW(), b'0'),
(99003, '台', 0, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), updater = '1', update_time = NOW();

-- ============================================================
-- 2. 产品数据（如不存在则创建）
-- ============================================================

-- 产品A：精密电机
SET @product_a_id := COALESCE((
    SELECT id FROM erp_product WHERE id = 99201 AND deleted = b'0' LIMIT 1
), (
    SELECT id FROM erp_product WHERE name LIKE '%电机%' AND deleted = b'0' ORDER BY id LIMIT 1
), 99201);

INSERT INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark,
    expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, asset_flag,
    creator, create_time, updater, update_time, deleted)
VALUES (@product_a_id, '精密电机 A', 'P-99201', 'BC-99201', 99002, 99001, 0, 'V2.0', '测试产品',
    365, b'0', b'0', 12.5, 320.00, 520.00, 300.00, b'0',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), standard = VALUES(standard), updater = '1', update_time = NOW();

-- 产品B：控制板组件
SET @product_b_id := COALESCE((
    SELECT id FROM erp_product WHERE id = 99202 AND deleted = b'0' LIMIT 1
), (
    SELECT id FROM erp_product WHERE name LIKE '%控制板%' AND deleted = b'0' ORDER BY id LIMIT 1
), 99202);

INSERT INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark,
    expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, asset_flag,
    creator, create_time, updater, update_time, deleted)
VALUES (@product_b_id, '控制板组件 B', 'P-99202', 'BC-99202', 99003, 99002, 0, 'V1.5', '测试产品',
    540, b'0', b'0', 0.85, 58.00, 88.00, 55.00, b'0',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), standard = VALUES(standard), updater = '1', update_time = NOW();

-- 产品C：传感器模块
SET @product_c_id := COALESCE((
    SELECT id FROM erp_product WHERE id = 99203 AND deleted = b'0' LIMIT 1
), (
    SELECT id FROM erp_product WHERE name LIKE '%传感器%' AND deleted = b'0' ORDER BY id LIMIT 1
), 99203);

INSERT INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark,
    expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, asset_flag,
    creator, create_time, updater, update_time, deleted)
VALUES (@product_c_id, '传感器模块 C', 'P-99203', 'BC-99203', 99003, 99001, 0, 'V3.0', '测试产品',
    720, b'0', b'0', 0.5, 45.00, 75.00, 42.00, b'0',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), standard = VALUES(standard), updater = '1', update_time = NOW();

-- ============================================================
-- 2. 生产工单数据（覆盖 2026-01 到 2026-06）
-- ============================================================

-- 工单 1：产品 A - 2026-01
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99301, 'WO-20260101', @product_a_id, NULL, 1000, 1000, NULL,
    '2026-01-05 08:00:00', '2026-01-25 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 2：产品 A - 2026-02
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99302, 'WO-20260201', @product_a_id, NULL, 1200, 1200, NULL,
    '2026-02-03 08:00:00', '2026-02-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 3：产品 B - 2026-01
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99303, 'WO-20260102', @product_b_id, NULL, 800, 800, NULL,
    '2026-01-10 08:00:00', '2026-01-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 4：产品 B - 2026-03
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99304, 'WO-20260301', @product_b_id, NULL, 900, 900, NULL,
    '2026-03-05 08:00:00', '2026-03-30 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 5：产品 C - 2026-02
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99305, 'WO-20260202', @product_c_id, NULL, 600, 600, NULL,
    '2026-02-08 08:00:00', '2026-02-25 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 6：产品 C - 2026-04
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99306, 'WO-20260401', @product_c_id, NULL, 700, 700, NULL,
    '2026-04-02 08:00:00', '2026-04-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 7：产品 A - 2026-05
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99307, 'WO-20260501', @product_a_id, NULL, 1500, 1500, NULL,
    '2026-05-04 08:00:00', '2026-05-30 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- 工单 8：产品 B - 2026-06
INSERT INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES (99308, 'WO-20260601', @product_b_id, NULL, 1000, 1000, NULL,
    '2026-06-02 08:00:00', '2026-06-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单',
    '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), status = VALUES(status), updater = '1', update_time = NOW();

-- ============================================================
-- 3. 生产成本归集数据（2026-01 到 2026-06）
-- ============================================================

-- 2026-01 - 产品 A（工单 99301）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99401, 99301, 10, 10, '2026-01', 45000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99402, 99301, 20, 10, '2026-01', 18000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99403, 99301, 30, 20, '2026-01', 6000.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99404, 99301, 40, 20, '2026-01', 3500.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99405, 99301, 50, 10, '2026-01', 2500.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-01 - 产品 B（工单 99303）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99406, 99303, 10, 10, '2026-01', 32000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99407, 99303, 20, 10, '2026-01', 12000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99408, 99303, 30, 20, '2026-01', 4500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99409, 99303, 40, 20, '2026-01', 2800.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99410, 99303, 50, 10, '2026-01', 1700.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-02 - 产品 A（工单 99302）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99411, 99302, 10, 10, '2026-02', 48000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99412, 99302, 20, 10, '2026-02', 19500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99413, 99302, 30, 20, '2026-02', 6500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99414, 99302, 40, 20, '2026-02', 3800.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99415, 99302, 50, 10, '2026-02', 2700.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-02 - 产品 C（工单 99305）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99416, 99305, 10, 10, '2026-02', 28000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99417, 99305, 20, 10, '2026-02', 10500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99418, 99305, 30, 20, '2026-02', 3800.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99419, 99305, 40, 20, '2026-02', 2200.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99420, 99305, 50, 10, '2026-02', 1500.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-03 - 产品 B（工单 99304）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99421, 99304, 10, 10, '2026-03', 35000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99422, 99304, 20, 10, '2026-03', 13500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99423, 99304, 30, 20, '2026-03', 5000.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99424, 99304, 40, 20, '2026-03', 3100.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99425, 99304, 50, 10, '2026-03', 1900.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-04 - 产品 C（工单 99306）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99426, 99306, 10, 10, '2026-04', 31000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99427, 99306, 20, 10, '2026-04', 11800.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99428, 99306, 30, 20, '2026-04', 4200.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99429, 99306, 40, 20, '2026-04', 2500.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99430, 99306, 50, 10, '2026-04', 1600.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-05 - 产品 A（工单 99307）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99431, 99307, 10, 10, '2026-05', 52000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99432, 99307, 20, 10, '2026-05', 21000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99433, 99307, 30, 20, '2026-05', 7200.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99434, 99307, 40, 20, '2026-05', 4200.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99435, 99307, 50, 10, '2026-05', 3000.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- 2026-06 - 产品 B（工单 99308）
INSERT INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES
(99436, 99308, 10, 10, '2026-06', 38000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99437, 99308, 20, 10, '2026-06', 15000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99438, 99308, 30, 20, '2026-06', 5500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99439, 99308, 40, 20, '2026-06', 3300.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99440, 99308, 50, 10, '2026-06', 2100.00, '其他', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE amount = VALUES(amount), updater = '1', update_time = NOW();

-- ============================================================
-- 4. 工时数据（2026-01 到 2026-06）
-- ============================================================

INSERT INTO erp_production_man_hour (id, production_order_id, accounting_month, work_date, man_hour, remark, creator, create_time, updater, update_time, deleted)
VALUES
-- 2026-01
(99501, 99301, '2026-01', '2026-01-15', 120.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
(99502, 99303, '2026-01', '2026-01-20', 85.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
-- 2026-02
(99503, 99302, '2026-02', '2026-02-10', 135.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
(99504, 99305, '2026-02', '2026-02-18', 75.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
-- 2026-03
(99505, 99304, '2026-03', '2026-03-12', 95.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
-- 2026-04
(99506, 99306, '2026-04', '2026-04-08', 82.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
-- 2026-05
(99507, 99307, '2026-05', '2026-05-15', 145.00, '测试工时', '1', NOW(), '1', NOW(), b'0'),
-- 2026-06
(99508, 99308, '2026-06', '2026-06-10', 105.00, '测试工时', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE man_hour = VALUES(man_hour), updater = '1', update_time = NOW();

-- ============================================================
-- 5. 验证输出
-- ============================================================

SELECT '=== 产品数据 ===' AS info;
SELECT id, name, material_code, standard FROM erp_product WHERE id IN (99201, 99202, 99203) AND deleted = b'0';

SELECT '=== 工单数据 ===' AS info;
SELECT id, order_no, product_id, status FROM erp_production_order WHERE id BETWEEN 99301 AND 99308 AND deleted = b'0';

SELECT '=== 成本归集数据 ===' AS info;
SELECT accounting_month, cost_type, COUNT(*) AS cnt, SUM(amount) AS total_amount
FROM erp_production_cost_entry
WHERE id BETWEEN 99401 AND 99440 AND deleted = b'0'
GROUP BY accounting_month, cost_type
ORDER BY accounting_month, cost_type;

SELECT '=== 工时数据 ===' AS info;
SELECT accounting_month, COUNT(*) AS cnt, SUM(man_hour) AS total_hours
FROM erp_production_man_hour
WHERE id BETWEEN 99501 AND 99508 AND deleted = b'0'
GROUP BY accounting_month
ORDER BY accounting_month;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
