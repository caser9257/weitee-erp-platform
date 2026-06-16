-- 简化版产品成本测试数据
-- 直接插入，不使用变量

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- 1. 插入产品（如果不存在）
INSERT IGNORE INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark,
    expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, asset_flag,
    creator, create_time, updater, update_time, deleted)
VALUES 
(99201, '精密电机 A', 'P-99201', 'BC-99201', 1, 1, 0, 'V2.0', '测试产品A', 365, b'0', b'0', 12.5, 320.00, 520.00, 300.00, b'0', '1', NOW(), '1', NOW(), b'0'),
(99202, '控制板组件 B', 'P-99202', 'BC-99202', 1, 1, 0, 'V1.5', '测试产品B', 540, b'0', b'0', 0.85, 58.00, 88.00, 55.00, b'0', '1', NOW(), '1', NOW(), b'0'),
(99203, '传感器模块 C', 'P-99203', 'BC-99203', 1, 1, 0, 'V1.0', '测试产品C', 730, b'0', b'0', 0.15, 12.00, 22.00, 10.00, b'0', '1', NOW(), '1', NOW(), b'0');

-- 2. 插入生产工单（如果不存在）
INSERT IGNORE INTO erp_production_order (id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id,
    plan_start_time, plan_end_time, status, source_type, source_id, source_order_id, source_item_id, machine_hour, remark,
    creator, create_time, updater, update_time, deleted)
VALUES 
(99301, 'WO-20260101', 99201, NULL, 1000, 1000, NULL, '2026-01-05 08:00:00', '2026-01-25 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99302, 'WO-20260201', 99201, NULL, 1200, 1200, NULL, '2026-02-03 08:00:00', '2026-02-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99303, 'WO-20260102', 99202, NULL, 2000, 2000, NULL, '2026-01-08 08:00:00', '2026-01-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99304, 'WO-20260301', 99202, NULL, 1800, 1800, NULL, '2026-03-02 08:00:00', '2026-03-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99305, 'WO-20260202', 99203, NULL, 3000, 3000, NULL, '2026-02-05 08:00:00', '2026-02-25 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99306, 'WO-20260401', 99203, NULL, 2500, 2500, NULL, '2026-04-03 08:00:00', '2026-04-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99307, 'WO-20260501', 99201, NULL, 1500, 1500, NULL, '2026-05-04 08:00:00', '2026-05-30 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0'),
(99308, 'WO-20260601', 99202, NULL, 1000, 1000, NULL, '2026-06-02 08:00:00', '2026-06-28 18:00:00', 30, NULL, NULL, NULL, NULL, NULL, '测试工单', '1', NOW(), '1', NOW(), b'0');

-- 3. 插入生产成本归集数据
-- 2026-01 - 产品A（工单99301）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99401, 99301, 10, 10, '2026-01', 45000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99402, 99301, 20, 10, '2026-01', 18000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99403, 99301, 30, 20, '2026-01', 6000.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99404, 99301, 40, 20, '2026-01', 3500.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99405, 99301, 50, 10, '2026-01', 2500.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-01 - 产品B（工单99303）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99406, 99303, 10, 10, '2026-01', 32000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99407, 99303, 20, 10, '2026-01', 12000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99408, 99303, 30, 20, '2026-01', 4500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99409, 99303, 40, 20, '2026-01', 2800.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99410, 99303, 50, 10, '2026-01', 1700.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-02 - 产品A（工单99302）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99411, 99302, 10, 10, '2026-02', 48000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99412, 99302, 20, 10, '2026-02', 19500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99413, 99302, 30, 20, '2026-02', 6500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99414, 99302, 40, 20, '2026-02', 3800.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99415, 99302, 50, 10, '2026-02', 2700.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-02 - 产品C（工单99305）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99416, 99305, 10, 10, '2026-02', 28000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99417, 99305, 20, 10, '2026-02', 10500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99418, 99305, 30, 20, '2026-02', 3800.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99419, 99305, 40, 20, '2026-02', 2200.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99420, 99305, 50, 10, '2026-02', 1500.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-03 - 产品B（工单99304）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99421, 99304, 10, 10, '2026-03', 35000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99422, 99304, 20, 10, '2026-03', 13500.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99423, 99304, 30, 20, '2026-03', 5000.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99424, 99304, 40, 20, '2026-03', 3100.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99425, 99304, 50, 10, '2026-03', 1900.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-04 - 产品C（工单99306）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99426, 99306, 10, 10, '2026-04', 31000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99427, 99306, 20, 10, '2026-04', 11800.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99428, 99306, 30, 20, '2026-04', 4200.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99429, 99306, 40, 20, '2026-04', 2500.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99430, 99306, 50, 10, '2026-04', 1600.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-05 - 产品A（工单99307）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99431, 99307, 10, 10, '2026-05', 52000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99432, 99307, 20, 10, '2026-05', 21000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99433, 99307, 30, 20, '2026-05', 7200.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99434, 99307, 40, 20, '2026-05', 4200.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99435, 99307, 50, 10, '2026-05', 3000.00, '其他', '1', NOW(), '1', NOW(), b'0');

-- 2026-06 - 产品B（工单99308）
INSERT IGNORE INTO erp_production_cost_entry (id, production_order_id, cost_type, source_type, accounting_month, amount, remark, creator, create_time, updater, update_time, deleted)
VALUES 
(99436, 99308, 10, 10, '2026-06', 38000.00, '材料成本', '1', NOW(), '1', NOW(), b'0'),
(99437, 99308, 20, 10, '2026-06', 15000.00, '人工成本', '1', NOW(), '1', NOW(), b'0'),
(99438, 99308, 30, 20, '2026-06', 5500.00, '折旧', '1', NOW(), '1', NOW(), b'0'),
(99439, 99308, 40, 20, '2026-06', 3300.00, '电费', '1', NOW(), '1', NOW(), b'0'),
(99440, 99308, 50, 10, '2026-06', 2100.00, '其他', '1', NOW(), '1', NOW(), b'0');

COMMIT;

-- 验证数据
SELECT '产品数据' as type, COUNT(*) as count FROM erp_product WHERE id >= 99201 AND id <= 99203;
SELECT '工单数据' as type, COUNT(*) as count FROM erp_production_order WHERE id >= 99301 AND id <= 99308;
SELECT '成本归集数据' as type, COUNT(*) as count FROM erp_production_cost_entry WHERE id >= 99401 AND id <= 99440;
