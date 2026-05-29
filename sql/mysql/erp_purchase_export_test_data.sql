/*
 * 采购订单导出与预付款联动测试数据
 * 目标：
 * 1. 验证采购订单 Excel 导出按“商品明细行”展开
 * 2. 验证同一采购单下，每条商品明细都重复显示来源销售订单信息
 * 3. 验证多供应商、多采购单、多商品、混合来源、手工采购无来源场景
 * 4. 验证预付款核销后，“可核销应付池”仅保留 remain_amount > 0 的记录
 *
 * 使用说明：
 * 1. 建议先在测试库执行
 * 2. 本脚本基于当前仓库正式表结构编写，已补 tenant_id、plan_id、下划线列名
 * 3. 可重复执行，固定测试 ID 会先清理
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @finance_user_id := COALESCE((
    SELECT id
    FROM system_users
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

-- =========================
-- 一、基础清理
-- =========================
DELETE FROM erp_finance_prepayment_allocate WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_finance_prepayment WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_ap_statement WHERE id BETWEEN 980001 AND 980199 AND tenant_id = @tenant_id;
DELETE FROM erp_purchase_suggest WHERE id BETWEEN 980001 AND 980199;
DELETE FROM erp_mrp_plan WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_purchase_order_items WHERE id BETWEEN 980001 AND 980199 AND tenant_id = @tenant_id;
DELETE FROM erp_purchase_order WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_sale_order_items WHERE id BETWEEN 980001 AND 980199 AND tenant_id = @tenant_id;
DELETE FROM erp_sale_order WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_product WHERE id BETWEEN 980001 AND 980199 AND tenant_id = @tenant_id;
DELETE FROM erp_supplier WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_customer WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_account WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_product_unit WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;
DELETE FROM erp_product_category WHERE id BETWEEN 980001 AND 980099 AND tenant_id = @tenant_id;

-- =========================
-- 二、基础资料
-- =========================
INSERT INTO erp_product_category
(id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 0, '采购导出测试分类', 'PO-EXPORT-TEST', 1, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 980001, '五金件', 'PO-EXPORT-HW', 2, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 980001, '机加工件', 'PO-EXPORT-MACH', 3, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 980001, '包材辅料', 'PO-EXPORT-PKG', 4, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 980001, '电子辅料', 'PO-EXPORT-ELC', 5, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
code = VALUES(code),
sort = VALUES(sort),
status = VALUES(status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_product_unit
(id, name, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, '件', 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
status = VALUES(status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_account
(id, name, no, remark, status, sort, default_status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, '中国银行-采购测试户', 'ACC-TEST-001', '采购导出测试账户', 0, 1, b'1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
no = VALUES(no),
remark = VALUES(remark),
status = VALUES(status),
sort = VALUES(sort),
default_status = VALUES(default_status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_customer
(id, name, contact, mobile, telephone, email, fax, remark, status, sort, tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, '华北终端客户A', '李四', '13800000001', '', '', '', '测试客户A', 0, 1, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, '华南终端客户B', '王五', '13800000002', '', '', '', '测试客户B', 0, 2, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, '华东终端客户C', '赵六', '13800000003', '', '', '', '测试客户C', 0, 3, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
contact = VALUES(contact),
mobile = VALUES(mobile),
telephone = VALUES(telephone),
email = VALUES(email),
fax = VALUES(fax),
remark = VALUES(remark),
status = VALUES(status),
sort = VALUES(sort),
tax_no = VALUES(tax_no),
tax_percent = VALUES(tax_percent),
bank_name = VALUES(bank_name),
bank_account = VALUES(bank_account),
bank_address = VALUES(bank_address),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_supplier
(id, name, contact, mobile, telephone, email, fax, remark, status, sort, tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, '华东紧固件供应商', '陈工', '13900000001', '', '', '', '测试供应商1', 0, 1, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, '苏州机加工供应商', '周工', '13900000002', '', '', '', '测试供应商2', 0, 2, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, '宁波包材供应商', '吴工', '13900000003', '', '', '', '测试供应商3', 0, 3, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, '深圳电子辅料供应商', '郑工', '13900000004', '', '', '', '测试供应商4', 0, 4, '', 0.130000, '', '', '', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
contact = VALUES(contact),
mobile = VALUES(mobile),
telephone = VALUES(telephone),
email = VALUES(email),
fax = VALUES(fax),
remark = VALUES(remark),
status = VALUES(status),
sort = VALUES(sort),
tax_no = VALUES(tax_no),
tax_percent = VALUES(tax_percent),
bank_name = VALUES(bank_name),
bank_account = VALUES(bank_account),
bank_address = VALUES(bank_address),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_product
(id, name, material_code, bar_code, category_id, unit_id, status, standard, remark, expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, '六角螺丝 M6x20', 'M001', 'BC-M001', 980002, 980001, 0, 'M6x20', '', 0, b'0', b'0', 0.010000, 0.120000, 0.200000, 0.100000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, '平垫 6mm', 'M002', 'BC-M002', 980002, 980001, 0, '6mm', '', 0, b'0', b'0', 0.002000, 0.030000, 0.060000, 0.020000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, '弹垫 6mm', 'M003', 'BC-M003', 980002, 980001, 0, '6mm', '', 0, b'0', b'0', 0.002000, 0.040000, 0.070000, 0.030000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, '六角螺母 M6', 'M004', 'BC-M004', 980002, 980001, 0, 'M6', '', 0, b'0', b'0', 0.003000, 0.050000, 0.080000, 0.040000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, '膨胀螺栓 M8', 'M005', 'BC-M005', 980002, 980001, 0, 'M8', '', 0, b'0', b'0', 0.030000, 0.680000, 0.950000, 0.550000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, '支架 A 型', 'J001', 'BC-J001', 980003, 980001, 0, 'A型', '', 0, b'0', b'0', 0.300000, 8.500000, 12.000000, 7.500000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980007, '连接板 B 型', 'J002', 'BC-J002', 980003, 980001, 0, 'B型', '', 0, b'0', b'0', 0.220000, 5.200000, 8.000000, 4.500000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980008, '加强肋 C 型', 'J003', 'BC-J003', 980003, 980001, 0, 'C型', '', 0, b'0', b'0', 0.180000, 3.800000, 6.200000, 3.200000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980009, '定位块 D 型', 'J004', 'BC-J004', 980003, 980001, 0, 'D型', '', 0, b'0', b'0', 0.090000, 2.600000, 4.000000, 2.100000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980010, '限位片 E 型', 'J005', 'BC-J005', 980003, 980001, 0, 'E型', '', 0, b'0', b'0', 0.050000, 1.900000, 3.200000, 1.500000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980011, '彩盒 1 号', 'B001', 'BC-B001', 980004, 980001, 0, '1号', '', 0, b'0', b'0', 0.080000, 1.200000, 1.800000, 1.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980012, '内托 1 号', 'B002', 'BC-B002', 980004, 980001, 0, '1号', '', 0, b'0', b'0', 0.030000, 0.450000, 0.800000, 0.350000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980013, '外箱 5 层', 'B003', 'BC-B003', 980004, 980001, 0, '5层', '', 0, b'0', b'0', 0.600000, 6.800000, 9.000000, 5.800000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980014, '端子线 20cm', 'E001', 'BC-E001', 980005, 980001, 0, '20cm', '', 0, b'0', b'0', 0.020000, 0.560000, 0.900000, 0.450000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980015, '热缩管 Φ3', 'E002', 'BC-E002', 980005, 980001, 0, 'Φ3', '', 0, b'0', b'0', 0.001000, 0.080000, 0.120000, 0.050000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980016, '扎带 100mm', 'E003', 'BC-E003', 980005, 980001, 0, '100mm', '', 0, b'0', b'0', 0.001000, 0.030000, 0.060000, 0.020000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980017, '绝缘垫片', 'E004', 'BC-E004', 980005, 980001, 0, '通用型', '', 0, b'0', b'0', 0.002000, 0.110000, 0.180000, 0.080000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
material_code = VALUES(material_code),
bar_code = VALUES(bar_code),
category_id = VALUES(category_id),
unit_id = VALUES(unit_id),
status = VALUES(status),
standard = VALUES(standard),
remark = VALUES(remark),
expiry_day = VALUES(expiry_day),
batch_control_flag = VALUES(batch_control_flag),
inspection_required_flag = VALUES(inspection_required_flag),
weight = VALUES(weight),
purchase_price = VALUES(purchase_price),
sale_price = VALUES(sale_price),
min_price = VALUES(min_price),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

-- =========================
-- 三、销售订单
-- =========================
INSERT INTO erp_sale_order
(id, no, status, process_instance_id, customer_id, project_id, business_type, source_project_id, settlement_type, source_product_id, account_id, sale_user_id, order_time, delivery_date, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, last_reject_reason, last_reject_time, last_reject_user_id, file_url, remark, out_count, return_count, delivery_ready_status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 'SO20260501001', 20, NULL, 980001, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-01 09:00:00', '2026-05-20', 3000.000000, 570.000000, 504.420000, 65.580000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单1', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 'SO20260501002', 20, NULL, 980001, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-01 10:00:00', '2026-05-21', 1600.000000, 157.520000, 139.400000, 18.120000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单2', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 'SO20260501003', 20, NULL, 980001, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-01 11:00:00', '2026-05-22', 200.000000, 153.680000, 136.000000, 17.680000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单3', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 'SO20260502001', 20, NULL, 980002, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-02 09:00:00', '2026-05-23', 400.000000, 3095.600000, 2740.000000, 355.600000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单4', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 'SO20260502002', 20, NULL, 980002, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-02 10:00:00', '2026-05-24', 120.000000, 515.280000, 456.000000, 59.280000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单5', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, 'SO20260503001', 20, NULL, 980003, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-03 09:00:00', '2026-05-25', 1000.000000, 932.250000, 825.000000, 107.250000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单6', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980007, 'SO20260503002', 20, NULL, 980003, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-03 10:00:00', '2026-05-26', 100.000000, 768.400000, 680.000000, 88.400000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单7', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980008, 'SO20260504001', 20, NULL, 980003, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-04 09:00:00', '2026-05-27', 3500.000000, 763.880000, 676.000000, 87.880000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单8', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980009, 'SO20260504002', 20, NULL, 980003, NULL, '标准销售', NULL, '月结', NULL, 980001, @finance_user_id, '2026-05-04 10:00:00', '2026-05-28', 600.000000, 74.580000, 66.000000, 8.580000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, '', '销售单9', 0.000000, 0.000000, 'READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
process_instance_id = VALUES(process_instance_id),
customer_id = VALUES(customer_id),
project_id = VALUES(project_id),
business_type = VALUES(business_type),
source_project_id = VALUES(source_project_id),
settlement_type = VALUES(settlement_type),
source_product_id = VALUES(source_product_id),
account_id = VALUES(account_id),
sale_user_id = VALUES(sale_user_id),
order_time = VALUES(order_time),
delivery_date = VALUES(delivery_date),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
deposit_price = VALUES(deposit_price),
last_reject_reason = VALUES(last_reject_reason),
last_reject_time = VALUES(last_reject_time),
last_reject_user_id = VALUES(last_reject_user_id),
file_url = VALUES(file_url),
remark = VALUES(remark),
out_count = VALUES(out_count),
return_count = VALUES(return_count),
delivery_ready_status = VALUES(delivery_ready_status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_items
(id, order_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark, out_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 980001, 980001, 980001, 0.120000, 1000.000000, 120.000000, 13.000000, 15.600000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 980001, 980002, 980001, 0.030000, 1000.000000, 30.000000, 13.000000, 3.900000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 980001, 980003, 980001, 0.040000, 1000.000000, 40.000000, 13.000000, 5.200000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 980002, 980001, 980001, 0.120000, 800.000000, 96.000000, 13.000000, 12.480000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 980002, 980004, 980001, 0.050000, 800.000000, 40.000000, 13.000000, 5.200000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, 980003, 980005, 980001, 0.680000, 200.000000, 136.000000, 13.000000, 17.680000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980007, 980004, 980006, 980001, 8.500000, 200.000000, 1700.000000, 13.000000, 221.000000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980008, 980004, 980007, 980001, 5.200000, 200.000000, 1040.000000, 13.000000, 135.200000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980009, 980005, 980008, 980001, 3.800000, 120.000000, 456.000000, 13.000000, 59.280000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980010, 980006, 980011, 980001, 1.200000, 500.000000, 600.000000, 13.000000, 78.000000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980011, 980006, 980012, 980001, 0.450000, 500.000000, 225.000000, 13.000000, 29.250000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980012, 980007, 980013, 980001, 6.800000, 100.000000, 680.000000, 13.000000, 88.400000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980013, 980008, 980014, 980001, 0.560000, 1000.000000, 560.000000, 13.000000, 72.800000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980014, 980008, 980015, 980001, 0.080000, 1000.000000, 80.000000, 13.000000, 10.400000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980015, 980008, 980016, 980001, 0.030000, 1500.000000, 45.000000, 13.000000, 5.850000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980016, 980009, 980017, 980001, 0.110000, 600.000000, 66.000000, 13.000000, 8.580000, '', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
product_id = VALUES(product_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
remark = VALUES(remark),
out_count = VALUES(out_count),
return_count = VALUES(return_count),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

-- =========================
-- 四、采购订单与明细
-- =========================
INSERT INTO erp_purchase_order
(id, no, status, process_instance_id, supplier_id, account_id, order_time, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 'PO20260515001', 20, NULL, 980001, 980001, '2026-05-15 10:00:00', 3000.000000, 190.970000, 169.000000, 21.970000, 0.000000, 0.000000, 0.000000, '', '同一来源多商品', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 'PO20260515002', 20, NULL, 980001, 980001, '2026-05-15 11:00:00', 1800.000000, 307.360000, 272.000000, 35.360000, 0.000000, 0.000000, 0.000000, '', '同单混合多个来源销售单', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 'PO20260516001', 20, NULL, 980002, 980001, '2026-05-16 09:30:00', 520.000000, 3610.880000, 3195.470000, 415.410000, 0.000000, 0.000000, 0.000000, '', '结构件委外采购', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 'PO20260516002', 20, NULL, 980002, 980001, '2026-05-16 14:00:00', 600.000000, 1525.500000, 1350.000000, 175.500000, 0.000000, 0.000000, 0.000000, '', '手工采购无来源', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 'PO20260517001', 20, NULL, 980003, 980001, '2026-05-17 10:15:00', 1100.000000, 1694.650000, 1499.690000, 194.960000, 0.000000, 0.000000, 0.000000, '', '包材齐套采购', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, 'PO20260517002', 20, NULL, 980004, 980001, '2026-05-17 15:30:00', 4100.000000, 849.760000, 752.000000, 97.760000, 0.000000, 0.000000, 0.000000, '', '三行同源一行异源', NULL, NULL, NULL, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
process_instance_id = VALUES(process_instance_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
order_time = VALUES(order_time),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
deposit_price = VALUES(deposit_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
last_reject_reason = VALUES(last_reject_reason),
last_reject_time = VALUES(last_reject_time),
last_reject_user_id = VALUES(last_reject_user_id),
in_count = VALUES(in_count),
return_count = VALUES(return_count),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order_items
(id, order_id, product_id, project_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version, count, total_price, tax_percent, tax_price, remark, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 980001, 980001, NULL, 980001, 0.120000, 0.000000, NULL, NULL, 1000.000000, 120.000000, 13.000000, 15.600000, '来源 SO20260501001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 980001, 980002, NULL, 980001, 0.030000, 0.000000, NULL, NULL, 1000.000000, 30.000000, 13.000000, 3.900000, '来源 SO20260501001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 980001, 980003, NULL, 980001, 0.040000, 0.000000, NULL, NULL, 1000.000000, 40.000000, 13.000000, 5.200000, '来源 SO20260501001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 980002, 980001, NULL, 980001, 0.120000, 0.000000, NULL, NULL, 800.000000, 96.000000, 13.000000, 12.480000, '来源 SO20260501002', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 980002, 980004, NULL, 980001, 0.050000, 0.000000, NULL, NULL, 800.000000, 40.000000, 13.000000, 5.200000, '来源 SO20260501002', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, 980002, 980005, NULL, 980001, 0.680000, 0.000000, NULL, NULL, 200.000000, 136.000000, 13.000000, 17.680000, '来源 SO20260501003', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980007, 980003, 980006, NULL, 980001, 8.500000, 0.000000, NULL, NULL, 200.000000, 1700.000000, 13.000000, 221.000000, '来源 SO20260502001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980008, 980003, 980007, NULL, 980001, 5.200000, 0.000000, NULL, NULL, 200.000000, 1040.000000, 13.000000, 135.200000, '来源 SO20260502001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980009, 980003, 980008, NULL, 980001, 3.800000, 0.000000, NULL, NULL, 120.000000, 456.000000, 13.000000, 59.280000, '来源 SO20260502002', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980010, 980004, 980009, NULL, 980001, 2.600000, 0.000000, NULL, NULL, 300.000000, 780.000000, 13.000000, 101.400000, '手工采购无来源', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980011, 980004, 980010, NULL, 980001, 1.900000, 0.000000, NULL, NULL, 300.000000, 570.000000, 13.000000, 74.100000, '手工采购无来源', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980012, 980005, 980011, NULL, 980001, 1.200000, 0.000000, NULL, NULL, 500.000000, 600.000000, 13.000000, 78.000000, '来源 SO20260503001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980013, 980005, 980012, NULL, 980001, 0.450000, 0.000000, NULL, NULL, 500.000000, 225.000000, 13.000000, 29.250000, '来源 SO20260503001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980014, 980005, 980013, NULL, 980001, 6.800000, 0.000000, NULL, NULL, 100.000000, 680.000000, 13.000000, 88.400000, '来源 SO20260503002', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980015, 980006, 980014, NULL, 980001, 0.560000, 0.000000, NULL, NULL, 1000.000000, 560.000000, 13.000000, 72.800000, '来源 SO20260504001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980016, 980006, 980015, NULL, 980001, 0.080000, 0.000000, NULL, NULL, 1000.000000, 80.000000, 13.000000, 10.400000, '来源 SO20260504001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980017, 980006, 980016, NULL, 980001, 0.030000, 0.000000, NULL, NULL, 1500.000000, 45.000000, 13.000000, 5.850000, '来源 SO20260504001', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980018, 980006, 980017, NULL, 980001, 0.110000, 0.000000, NULL, NULL, 600.000000, 66.000000, 13.000000, 8.580000, '来源 SO20260504002', 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
product_id = VALUES(product_id),
project_id = VALUES(project_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
engineering_fee = VALUES(engineering_fee),
pricing_bom_id = VALUES(pricing_bom_id),
pricing_bom_version = VALUES(pricing_bom_version),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
remark = VALUES(remark),
in_count = VALUES(in_count),
return_count = VALUES(return_count),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

-- =========================
-- 五、MRP 计划与采购建议映射
-- 作用：决定采购单的来源销售单号聚合结果
-- =========================
INSERT INTO erp_mrp_plan
(id, plan_no, plan_name, plan_start_date, plan_end_date, status, run_time, operator_id, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 'MRP-PO-EXPORT-001', '采购导出测试计划', '2026-05-01', '2026-05-31', 20, NOW(), @finance_user_id, '采购导出测试计划', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
plan_no = VALUES(plan_no),
plan_name = VALUES(plan_name),
plan_start_date = VALUES(plan_start_date),
plan_end_date = VALUES(plan_end_date),
status = VALUES(status),
run_time = VALUES(run_time),
operator_id = VALUES(operator_id),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_suggest
(id, plan_id, material_id, project_id, source_type, business_type, suggest_qty, suggest_arrival_date, gross_demand_qty, available_stock_qty, incoming_qty, wip_qty, reserved_stock_qty, safety_stock_qty, net_demand_qty, source_order_id, source_item_id, status, convert_purchase_order_id, remark, creator, create_time, updater, update_time, deleted)
VALUES
(980001, 980001, 980001, NULL, NULL, NULL, 1000.000000, '2026-05-20', 1000.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1000.000000, 980001, 980001, 20, 980001, 'PO20260515001 <- SO20260501001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980002, 980001, 980002, NULL, NULL, NULL, 1000.000000, '2026-05-20', 1000.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1000.000000, 980001, 980002, 20, 980001, 'PO20260515001 <- SO20260501001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980003, 980001, 980003, NULL, NULL, NULL, 1000.000000, '2026-05-20', 1000.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1000.000000, 980001, 980003, 20, 980001, 'PO20260515001 <- SO20260501001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980004, 980001, 980001, NULL, NULL, NULL, 800.000000, '2026-05-21', 800.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 800.000000, 980002, 980004, 20, 980002, 'PO20260515002 <- SO20260501002', 'tester', NOW(), 'tester', NOW(), b'0'),
(980005, 980001, 980004, NULL, NULL, NULL, 800.000000, '2026-05-21', 800.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 800.000000, 980002, 980005, 20, 980002, 'PO20260515002 <- SO20260501002', 'tester', NOW(), 'tester', NOW(), b'0'),
(980006, 980001, 980005, NULL, NULL, NULL, 200.000000, '2026-05-22', 200.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 200.000000, 980003, 980006, 20, 980002, 'PO20260515002 <- SO20260501003', 'tester', NOW(), 'tester', NOW(), b'0'),
(980007, 980001, 980006, NULL, NULL, NULL, 200.000000, '2026-05-23', 200.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 200.000000, 980004, 980007, 20, 980003, 'PO20260516001 <- SO20260502001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980008, 980001, 980007, NULL, NULL, NULL, 200.000000, '2026-05-23', 200.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 200.000000, 980004, 980008, 20, 980003, 'PO20260516001 <- SO20260502001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980009, 980001, 980008, NULL, NULL, NULL, 120.000000, '2026-05-24', 120.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 120.000000, 980005, 980009, 20, 980003, 'PO20260516001 <- SO20260502002', 'tester', NOW(), 'tester', NOW(), b'0'),
(980010, 980001, 980011, NULL, NULL, NULL, 500.000000, '2026-05-25', 500.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 500.000000, 980006, 980010, 20, 980005, 'PO20260517001 <- SO20260503001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980011, 980001, 980012, NULL, NULL, NULL, 500.000000, '2026-05-25', 500.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 500.000000, 980006, 980011, 20, 980005, 'PO20260517001 <- SO20260503001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980012, 980001, 980013, NULL, NULL, NULL, 100.000000, '2026-05-26', 100.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 100.000000, 980007, 980012, 20, 980005, 'PO20260517001 <- SO20260503002', 'tester', NOW(), 'tester', NOW(), b'0'),
(980013, 980001, 980014, NULL, NULL, NULL, 1000.000000, '2026-05-27', 1000.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1000.000000, 980008, 980013, 20, 980006, 'PO20260517002 <- SO20260504001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980014, 980001, 980015, NULL, NULL, NULL, 1000.000000, '2026-05-27', 1000.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1000.000000, 980008, 980014, 20, 980006, 'PO20260517002 <- SO20260504001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980015, 980001, 980016, NULL, NULL, NULL, 1500.000000, '2026-05-27', 1500.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 1500.000000, 980008, 980015, 20, 980006, 'PO20260517002 <- SO20260504001', 'tester', NOW(), 'tester', NOW(), b'0'),
(980016, 980001, 980017, NULL, NULL, NULL, 600.000000, '2026-05-28', 600.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 600.000000, 980009, 980016, 20, 980006, 'PO20260517002 <- SO20260504002', 'tester', NOW(), 'tester', NOW(), b'0');

-- =========================
-- 六、应付台账
-- 用于验证预付款联动筛选 remain_amount > 0
-- =========================
INSERT INTO erp_ap_statement
(id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount, paid_amount, remain_amount, currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 'AP-11-PO20260515001', 11, 980001, 'PO20260515001', 980001, 'PO20260515001', 980001, 980001, 190.970000, 190.970000, 0.000000, 'CNY', '2026-05-15 10:00:00', '2026-05-31 00:00:00', 0, NULL, NULL, 30, '预付后已结清', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 'AP-11-PO20260515002', 11, 980002, 'PO20260515002', 980002, 'PO20260515002', 980001, 980001, 307.360000, 120.000000, 187.360000, 'CNY', '2026-05-15 11:00:00', '2026-05-31 00:00:00', 0, NULL, NULL, 20, '部分付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 'AP-11-PO20260516001', 11, 980003, 'PO20260516001', 980003, 'PO20260516001', 980002, 980001, 3610.880000, 1000.000000, 2610.880000, 'CNY', '2026-05-16 09:30:00', '2026-06-01 00:00:00', 0, NULL, NULL, 20, '部分付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980004, 'AP-11-PO20260516002', 11, 980004, 'PO20260516002', 980004, 'PO20260516002', 980002, 980001, 1525.500000, 0.000000, 1525.500000, 'CNY', '2026-05-16 14:00:00', '2026-06-01 00:00:00', 0, NULL, NULL, 10, '手工采购未付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980005, 'AP-11-PO20260517001', 11, 980005, 'PO20260517001', 980005, 'PO20260517001', 980003, 980001, 1694.650000, 1694.650000, 0.000000, 'CNY', '2026-05-17 10:15:00', '2026-06-02 00:00:00', 0, NULL, NULL, 30, '已结清', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980006, 'AP-11-PO20260517002', 11, 980006, 'PO20260517002', 980006, 'PO20260517002', 980004, 980001, 849.760000, 300.000000, 549.760000, 'CNY', '2026-05-17 15:30:00', '2026-06-02 00:00:00', 0, NULL, NULL, 20, '部分付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
statement_no = VALUES(statement_no),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
source_order_id = VALUES(source_order_id),
source_order_no = VALUES(source_order_no),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
amount = VALUES(amount),
paid_amount = VALUES(paid_amount),
remain_amount = VALUES(remain_amount),
currency_code = VALUES(currency_code),
biz_date = VALUES(biz_date),
due_date = VALUES(due_date),
invoice_status = VALUES(invoice_status),
invoice_no = VALUES(invoice_no),
invoice_amount = VALUES(invoice_amount),
status = VALUES(status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

-- =========================
-- 七、预付款与核销
-- =========================
INSERT INTO erp_finance_prepayment
(id, no, status, prepayment_time, finance_user_id, supplier_id, account_id, prepayment_price, allocated_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 'YFK202605150001', 20, '2026-05-15 12:00:00', @finance_user_id, 980001, 980001, 190.970000, 190.970000, 0.000000, '核销 PO20260515001', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 'YFK202605170001', 20, '2026-05-17 16:00:00', @finance_user_id, 980003, 980001, 1694.650000, 1694.650000, 0.000000, '核销 PO20260517001', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 'YFK202605170002', 20, '2026-05-17 17:00:00', @finance_user_id, 980004, 980001, 300.000000, 300.000000, 0.000000, '部分核销 PO20260517002', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
prepayment_time = VALUES(prepayment_time),
finance_user_id = VALUES(finance_user_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
prepayment_price = VALUES(prepayment_price),
allocated_price = VALUES(allocated_price),
remain_price = VALUES(remain_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment_allocate
(id, prepayment_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(980001, 980001, 980001, 190.970000, 980001, 11, 980001, 'PO20260515001', 20, '预付核销完成', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980002, 980002, 980005, 1694.650000, 980003, 11, 980005, 'PO20260517001', 20, '预付核销完成', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(980003, 980003, 980006, 300.000000, 980004, 11, 980006, 'PO20260517002', 20, '预付部分核销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
prepayment_id = VALUES(prepayment_id),
ap_statement_id = VALUES(ap_statement_id),
allocate_amount = VALUES(allocate_amount),
supplier_id = VALUES(supplier_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
status = VALUES(status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

-- =========================
-- 八、建议验证 SQL
-- =========================
-- 1. 导出来源验证
-- SELECT
--     po.no AS purchase_no,
--     poi.id AS item_id,
--     p.name AS product_name,
--     GROUP_CONCAT(DISTINCT so.no ORDER BY so.no SEPARATOR ',') AS source_sale_nos
-- FROM erp_purchase_order po
-- JOIN erp_purchase_order_items poi
--   ON poi.order_id = po.id
--  AND poi.deleted = b'0'
--  AND poi.tenant_id = @tenant_id
-- LEFT JOIN erp_product p
--   ON p.id = poi.product_id
--  AND p.deleted = b'0'
--  AND p.tenant_id = @tenant_id
-- LEFT JOIN erp_purchase_suggest ps
--   ON ps.convert_purchase_order_id = po.id
--  AND ps.deleted = b'0'
-- LEFT JOIN erp_sale_order so
--   ON so.id = ps.source_order_id
--  AND so.deleted = b'0'
--  AND so.tenant_id = @tenant_id
-- WHERE po.id IN (980001, 980002, 980003, 980004, 980005, 980006)
--   AND po.deleted = b'0'
--   AND po.tenant_id = @tenant_id
-- GROUP BY po.no, poi.id, p.name
-- ORDER BY po.no, poi.id;
--
-- 2. 可核销应付池验证：这里只应返回 remain_amount > 0 的记录
-- SELECT statement_no, supplier_id, amount, paid_amount, remain_amount, status
-- FROM erp_ap_statement
-- WHERE deleted = b'0'
--   AND tenant_id = @tenant_id
--   AND status != 40
--   AND remain_amount > 0
-- ORDER BY id;

SET FOREIGN_KEY_CHECKS = 1;
