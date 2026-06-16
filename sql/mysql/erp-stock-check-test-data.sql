/*
 * 库存盘点功能完整测试数据（无租户版本）
 * 
 * 覆盖场景：
 * 1. 基础数据：产品分类、产品单位、产品、仓库
 * 2. 库存数据：不同产品在不同仓库的库存（含零库存场景）
 * 3. 盘点单数据：覆盖所有状态（草稿、盘点中、审核中、已审核、已关闭）
 * 4. 盘点项数据：盘盈、盘亏、无差异、盲盘、年末盘点
 * 5. 快照数据：盘点时点的库存快照
 * 6. 库存变动记录：审核通过后的出入库记录
 *
 * 使用说明：
 * 1. 建议在测试库执行
 * 2. 幂等导入，可重复执行
 * 3. 固定测试 ID 范围：970000 - 979999
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- 获取管理员用户ID
SET @admin_user_id := COALESCE((
    SELECT id FROM system_users WHERE username = 'admin' AND deleted = b'0' LIMIT 1
), 1);

-- =========================
-- 一、基础清理
-- =========================
DELETE FROM erp_stock_record WHERE biz_id BETWEEN 970001 AND 970099;
DELETE FROM erp_stock_check_snapshot WHERE check_id BETWEEN 970001 AND 970099;
DELETE FROM erp_stock_check_item WHERE check_id BETWEEN 970001 AND 970099;
DELETE FROM erp_stock_check WHERE id BETWEEN 970001 AND 970099;
DELETE FROM erp_stock WHERE id BETWEEN 970001 AND 970099;
DELETE FROM erp_warehouse WHERE id BETWEEN 970001 AND 970099;
DELETE FROM erp_product WHERE id BETWEEN 970001 AND 970099;
DELETE FROM erp_product_unit WHERE id BETWEEN 970001 AND 970099;
DELETE FROM erp_product_category WHERE id BETWEEN 970001 AND 970099;

-- =========================
-- 二、基础资料
-- =========================

-- 2.1 产品分类
INSERT INTO erp_product_category (id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted) VALUES
(970001, 0, '盘点测试-原材料', 'CHK-RAW', 1, 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970002, 0, '盘点测试-半成品', 'CHK-SEMI', 2, 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970003, 0, '盘点测试-成品', 'CHK-FIN', 3, 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970004, 0, '盘点测试-辅料', 'CHK-AUX', 4, 0, 'admin', NOW(), 'admin', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), code = VALUES(code), sort = VALUES(sort), status = VALUES(status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted);

-- 2.2 产品单位
INSERT INTO erp_product_unit (id, name, status, creator, create_time, updater, update_time, deleted) VALUES
(970001, '个', 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970002, '件', 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970003, '箱', 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970004, '千克', 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970005, '米', 0, 'admin', NOW(), 'admin', NOW(), b'0'),
(970006, '套', 0, 'admin', NOW(), 'admin', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted);

-- 2.3 产品（8个产品覆盖不同场景）
INSERT INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark, purchase_price, sale_price, creator, create_time, updater, update_time, deleted) VALUES
(970001, '钢材 Q235B', 'MAT-CHK-001', 'BC-CHK-001', 970001, 970004, 0, 'Q235B 10mm', '普通碳素结构钢', 4500.00, 5200.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970002, '铝材 6061', 'MAT-CHK-002', 'BC-CHK-002', 970001, 970004, 0, '6061-T6', '铝合金型材', 18500.00, 21000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970003, '铜线 T2', 'MAT-CHK-003', 'BC-CHK-003', 970001, 970005, 0, 'T2 Φ2mm', '紫铜线材', 68.00, 78.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970004, '轴承座组件', 'MAT-CHK-004', 'BC-CHK-004', 970002, 970002, 0, 'UCP205', '带座轴承组件', 85.00, 120.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970005, '齿轮箱体', 'MAT-CHK-005', 'BC-CHK-005', 970002, 970002, 0, 'ZQ250', '减速箱体铸件', 320.00, 450.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970006, '电动机 Y132S', 'MAT-CHK-006', 'BC-CHK-006', 970003, 970006, 0, 'Y132S-4 5.5KW', '三相异步电动机', 850.00, 1200.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970007, '变频器 VFD', 'MAT-CHK-007', 'BC-CHK-007', 970003, 970001, 0, 'VFD-007 5.5KW', '变频调速器', 1200.00, 1680.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970008, '润滑脂 EP2', 'MAT-CHK-008', 'BC-CHK-008', 970004, 970003, 0, 'EP2 15KG/桶', '极压锂基润滑脂', 280.00, 380.00, 'admin', NOW(), 'admin', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), material_code = VALUES(material_code), bar_code = VALUES(bar_code), category_id = VALUES(category_id), unit_id = VALUES(unit_id), status = VALUES(status), standard = VALUES(standard), remark = VALUES(remark), purchase_price = VALUES(purchase_price), sale_price = VALUES(sale_price), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted);

-- 2.4 仓库（4个仓库）
INSERT INTO erp_warehouse (id, name, address, sort, remark, principal, warehouse_price, truckage_price, status, default_status, frozen, creator, create_time, updater, update_time, deleted) VALUES
(970001, '原料仓A区', '深圳工厂1号仓', 1, '存放原材料', '张仓管', 0.50, 0.20, 0, b'1', b'0', 'admin', NOW(), 'admin', NOW(), b'0'),
(970002, '原料仓B区', '深圳工厂2号仓', 2, '存放辅助材料', '李仓管', 0.50, 0.20, 0, b'0', b'0', 'admin', NOW(), 'admin', NOW(), b'0'),
(970003, '成品仓', '深圳工厂3号仓', 3, '存放成品和半成品', '王仓管', 0.80, 0.30, 0, b'0', b'0', 'admin', NOW(), 'admin', NOW(), b'0'),
(970004, '外协仓', '东莞外协仓', 4, '外协加工物料', '赵仓管', 0.60, 0.25, 0, b'0', b'0', 'admin', NOW(), 'admin', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), sort = VALUES(sort), remark = VALUES(remark), principal = VALUES(principal), warehouse_price = VALUES(warehouse_price), truckage_price = VALUES(truckage_price), status = VALUES(status), default_status = VALUES(default_status), frozen = VALUES(frozen), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted);

-- =========================
-- 三、库存数据
-- =========================
INSERT INTO erp_stock (id, product_id, warehouse_id, count, average_cost, total_cost, creator, create_time, updater, update_time, deleted) VALUES
(970001, 970001, 970001, 5000.0000, 4500.00, 22500000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970002, 970002, 970001, 2000.0000, 18500.00, 37000000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970003, 970003, 970001, 500.0000, 68.00, 34000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970004, 970001, 970002, 3000.0000, 4500.00, 13500000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970005, 970004, 970003, 200.0000, 85.00, 17000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970006, 970005, 970003, 50.0000, 320.00, 16000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970007, 970006, 970003, 100.0000, 850.00, 85000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970008, 970007, 970003, 80.0000, 1200.00, 96000.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970009, 970008, 970002, 30.0000, 280.00, 8400.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970010, 970003, 970002, 0.0000, 68.00, 0.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970011, 970004, 970004, 150.0000, 85.00, 12750.00, 'admin', NOW(), 'admin', NOW(), b'0'),
(970012, 970008, 970004, 20.0000, 280.00, 5600.00, 'admin', NOW(), 'admin', NOW(), b'0')
ON DUPLICATE KEY UPDATE count = VALUES(count), average_cost = VALUES(average_cost), total_cost = VALUES(total_cost), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted);

-- =========================
-- 四、库存盘点单
-- =========================

INSERT INTO erp_stock_check (id, no, check_time, total_count, total_price, status, remark, blind_count, year_end_flag, snapshot_time, voucher_id, creator, create_time, updater, update_time, deleted) VALUES
-- 草稿状态
(970001, 'CHK-20260601-001', '2026-06-01 10:00:00', 800.0000, 385000.00, 0, '2026年6月常规盘点-草稿状态', b'0', b'0', NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点中状态
(970002, 'CHK-20260602-001', '2026-06-02 14:00:00', 250.0000, 212500.00, 10, '2026年6月成品仓盘点-盘点中', b'0', b'0', '2026-06-02 14:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 审核中状态
(970003, 'CHK-20260603-001', '2026-06-03 09:00:00', 5030.0000, 22635000.00, 20, '2026年6月原料仓盘点-待审核', b'0', b'0', '2026-06-03 09:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 已关闭状态
(970004, 'CHK-20260501-001', '2026-05-01 10:00:00', 350.0000, 297500.00, 40, '2026年5月成品盘点-已完成', b'0', b'0', '2026-05-01 10:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盲盘模式
(970005, 'CHK-20260604-001', '2026-06-04 08:00:00', 0.0000, 0.00, 0, '盲盘测试-不显示账面数量', b'1', b'0', NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 年末盘点
(970006, 'CHK-20260605-001', '2026-06-05 09:00:00', 0.0000, 0.00, 0, '2026年年末盘点准备', b'0', b'1', NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 多仓库混合盘点
(970007, 'CHK-20260606-001', '2026-06-06 10:00:00', 0.0000, 0.00, 0, '跨仓库盘点测试', b'0', b'0', NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘盈场景
(970008, 'CHK-20260607-001', '2026-06-07 10:00:00', 0.0000, 0.00, 20, '盘盈场景测试-待审核', b'0', b'0', '2026-06-07 10:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘亏场景
(970009, 'CHK-20260608-001', '2026-06-08 10:00:00', 0.0000, 0.00, 20, '盘亏场景测试-待审核', b'0', b'0', '2026-06-08 10:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 无差异场景
(970010, 'CHK-20260609-001', '2026-06-09 10:00:00', 0.0000, 0.00, 20, '无差异场景测试-待审核', b'0', b'0', '2026-06-09 10:30:00', NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0');

-- =========================
-- 五、盘点项数据
-- =========================

INSERT INTO erp_stock_check_item (id, check_id, warehouse_id, product_id, product_unit_id, product_price, stock_count, actual_count, count, total_price, remark, first_count, recount, recount_diff, diff_amount, creator, create_time, updater, update_time, deleted) VALUES
-- 盘点单1（草稿 - 未录入实际数量）
(970001, 970001, 970001, 970001, 970004, 4500.00, 5000.0000, 0.0000, 0.0000, 0.00, '钢材盘点', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970002, 970001, 970001, 970002, 970004, 18500.00, 2000.0000, 0.0000, 0.0000, 0.00, '铝材盘点', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970003, 970001, 970001, 970003, 970005, 68.00, 500.0000, 0.0000, 0.0000, 0.00, '铜线盘点', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单2（盘点中 - 已录入实际数量）
(970004, 970002, 970003, 970006, 970006, 850.00, 100.0000, 98.0000, -2.0000, -1700.00, '电动机盘点-盘亏2台', 98.0000, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970005, 970002, 970003, 970007, 970001, 1200.00, 80.0000, 82.0000, 2.0000, 2400.00, '变频器盘点-盘盈2个', 82.0000, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970006, 970002, 970003, 970004, 970002, 85.00, 200.0000, 200.0000, 0.0000, 0.00, '轴承座盘点-无差异', 200.0000, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单3（审核中）
(970007, 970003, 970001, 970001, 970004, 4500.00, 5000.0000, 4980.0000, -20.0000, -90000.00, '钢材盘亏20kg', 4985.0000, 4980.0000, -5.0000, -90000.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970008, 970003, 970001, 970002, 970004, 18500.00, 2000.0000, 2010.0000, 10.0000, 185000.00, '铝材盘盈10kg', 2005.0000, 2010.0000, 5.0000, 185000.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970009, 970003, 970001, 970003, 970005, 68.00, 500.0000, 500.0000, 0.0000, 0.00, '铜线无差异', 500.0000, 500.0000, 0.0000, 0.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单4（已关闭）
(970010, 970004, 970003, 970006, 970006, 850.00, 120.0000, 118.0000, -2.0000, -1700.00, '5月电动机盘亏', 118.0000, 118.0000, 0.0000, -1700.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970011, 970004, 970003, 970007, 970001, 1200.00, 90.0000, 92.0000, 2.0000, 2400.00, '5月变频器盘盈', 92.0000, 92.0000, 0.0000, 2400.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970012, 970004, 970003, 970004, 970002, 85.00, 180.0000, 180.0000, 0.0000, 0.00, '5月轴承座无差异', 180.0000, 180.0000, 0.0000, 0.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单5（盲盘 - 未录入实际数量）
(970013, 970005, 970002, 970008, 970003, 280.00, 30.0000, 0.0000, 0.0000, 0.00, '盲盘-润滑脂', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970014, 970005, 970001, 970001, 970004, 4500.00, 5000.0000, 0.0000, 0.0000, 0.00, '盲盘-钢材', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单6（年末盘点 - 未录入实际数量）
(970015, 970006, 970001, 970001, 970004, 4500.00, 5000.0000, 0.0000, 0.0000, 0.00, '年末盘点-钢材', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970016, 970006, 970001, 970002, 970004, 18500.00, 2000.0000, 0.0000, 0.0000, 0.00, '年末盘点-铝材', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970017, 970006, 970003, 970006, 970006, 850.00, 100.0000, 0.0000, 0.0000, 0.00, '年末盘点-电动机', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单7（多仓库 - 未录入实际数量）
(970018, 970007, 970001, 970001, 970004, 4500.00, 5000.0000, 0.0000, 0.0000, 0.00, '原料仓A-钢材', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970019, 970007, 970002, 970001, 970004, 4500.00, 3000.0000, 0.0000, 0.0000, 0.00, '原料仓B-钢材', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970020, 970007, 970003, 970006, 970006, 850.00, 100.0000, 0.0000, 0.0000, 0.00, '成品仓-电动机', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970021, 970007, 970004, 970004, 970002, 85.00, 150.0000, 0.0000, 0.0000, 0.00, '外协仓-轴承座', NULL, NULL, NULL, NULL, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单8（盘盈）
(970022, 970008, 970003, 970006, 970006, 850.00, 100.0000, 105.0000, 5.0000, 4250.00, '电动机盘盈5台', 104.0000, 105.0000, 1.0000, 4250.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970023, 970008, 970003, 970004, 970002, 85.00, 200.0000, 210.0000, 10.0000, 850.00, '轴承座盘盈10件', 208.0000, 210.0000, 2.0000, 850.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单9（盘亏）
(970024, 970009, 970003, 970007, 970001, 1200.00, 80.0000, 75.0000, -5.0000, -6000.00, '变频器盘亏5个', 76.0000, 75.0000, -1.0000, -6000.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970025, 970009, 970003, 970005, 970002, 320.00, 50.0000, 48.0000, -2.0000, -640.00, '齿轮箱体盘亏2件', 49.0000, 48.0000, -1.0000, -640.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单10（无差异）
(970026, 970010, 970001, 970003, 970005, 68.00, 500.0000, 500.0000, 0.0000, 0.00, '铜线无差异', 500.0000, 500.0000, 0.0000, 0.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970027, 970010, 970002, 970008, 970003, 280.00, 30.0000, 30.0000, 0.0000, 0.00, '润滑脂无差异', 30.0000, 30.0000, 0.0000, 0.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0');

-- =========================
-- 六、盘点快照数据
-- =========================

INSERT INTO erp_stock_check_snapshot (id, check_id, product_id, warehouse_id, book_qty, book_amount, average_cost, snapshot_time, creator, create_time, updater, update_time, deleted) VALUES
-- 盘点单2的快照
(970001, 970002, 970006, 970003, 100.0000, 85000.00, 850.00, '2026-06-02 14:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970002, 970002, 970007, 970003, 80.0000, 96000.00, 1200.00, '2026-06-02 14:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970003, 970002, 970004, 970003, 200.0000, 17000.00, 85.00, '2026-06-02 14:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单3的快照
(970004, 970003, 970001, 970001, 5000.0000, 22500000.00, 4500.00, '2026-06-03 09:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970005, 970003, 970002, 970001, 2000.0000, 37000000.00, 18500.00, '2026-06-03 09:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970006, 970003, 970003, 970001, 500.0000, 34000.00, 68.00, '2026-06-03 09:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单4的快照
(970007, 970004, 970006, 970003, 120.0000, 102000.00, 850.00, '2026-05-01 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970008, 970004, 970007, 970003, 90.0000, 108000.00, 1200.00, '2026-05-01 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970009, 970004, 970004, 970003, 180.0000, 15300.00, 85.00, '2026-05-01 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单8的快照
(970010, 970008, 970006, 970003, 100.0000, 85000.00, 850.00, '2026-06-07 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970011, 970008, 970004, 970003, 200.0000, 17000.00, 85.00, '2026-06-07 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单9的快照
(970012, 970009, 970007, 970003, 80.0000, 96000.00, 1200.00, '2026-06-08 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970013, 970009, 970005, 970003, 50.0000, 16000.00, 320.00, '2026-06-08 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
-- 盘点单10的快照
(970014, 970010, 970003, 970001, 500.0000, 34000.00, 68.00, '2026-06-09 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970015, 970010, 970008, 970002, 30.0000, 8400.00, 280.00, '2026-06-09 10:30:00', @admin_user_id, NOW(), @admin_user_id, NOW(), b'0');

-- =========================
-- 七、库存变动记录
-- =========================

INSERT INTO erp_stock_record (id, product_id, warehouse_id, count, total_count, biz_type, biz_id, biz_item_id, biz_no, price, amount, creator, create_time, updater, update_time, deleted) VALUES
(970001, 970006, 970003, -2.0000, 118.0000, 30, 970004, 970010, 'CHK-20260501-001', 850.00, -1700.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0'),
(970002, 970007, 970003, 2.0000, 92.0000, 29, 970004, 970011, 'CHK-20260501-001', 1200.00, 2400.00, @admin_user_id, NOW(), @admin_user_id, NOW(), b'0');

-- =========================
-- 八、更新盘点单汇总
-- =========================

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970001 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970001 AND deleted = b'0')
WHERE id = 970001;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970002 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970002 AND deleted = b'0')
WHERE id = 970002;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970003 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970003 AND deleted = b'0')
WHERE id = 970003;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970004 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970004 AND deleted = b'0')
WHERE id = 970004;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970008 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970008 AND deleted = b'0')
WHERE id = 970008;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970009 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970009 AND deleted = b'0')
WHERE id = 970009;

UPDATE erp_stock_check SET 
    total_count = (SELECT COALESCE(SUM(count), 0) FROM erp_stock_check_item WHERE check_id = 970010 AND deleted = b'0'),
    total_price = (SELECT COALESCE(SUM(total_price), 0) FROM erp_stock_check_item WHERE check_id = 970010 AND deleted = b'0')
WHERE id = 970010;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 九、数据验证查询
-- =========================

SELECT '=== 盘点单列表 ===' AS '';
SELECT 
    id AS 'ID',
    no AS '盘点单号',
    CASE status WHEN 0 THEN '草稿' WHEN 10 THEN '盘点中' WHEN 20 THEN '审核中' WHEN 30 THEN '已审核' WHEN 40 THEN '已关闭' END AS '状态',
    total_count AS '合计数量',
    total_price AS '合计金额',
    CASE blind_count WHEN b'1' THEN '是' ELSE '否' END AS '盲盘',
    CASE year_end_flag WHEN b'1' THEN '是' ELSE '否' END AS '年末盘点',
    remark AS '备注'
FROM erp_stock_check WHERE id BETWEEN 970001 AND 970099 AND deleted = b'0' ORDER BY id;

SELECT '=== 盘点项详情 ===' AS '';
SELECT 
    ci.check_id AS '盘点单ID',
    p.name AS '产品',
    w.name AS '仓库',
    ci.stock_count AS '账面数量',
    ci.actual_count AS '实际数量',
    ci.count AS '盈亏',
    ci.remark AS '备注'
FROM erp_stock_check_item ci
LEFT JOIN erp_product p ON ci.product_id = p.id
LEFT JOIN erp_warehouse w ON ci.warehouse_id = w.id
WHERE ci.check_id BETWEEN 970001 AND 970099 AND ci.deleted = b'0' ORDER BY ci.check_id, ci.id;

SELECT '=== 库存数据 ===' AS '';
SELECT 
    p.name AS '产品',
    w.name AS '仓库',
    s.count AS '库存量',
    s.average_cost AS '平均成本'
FROM erp_stock s
LEFT JOIN erp_product p ON s.product_id = p.id
LEFT JOIN erp_warehouse w ON s.warehouse_id = w.id
WHERE s.id BETWEEN 970001 AND 970099 AND s.deleted = b'0' ORDER BY p.id, w.id;
