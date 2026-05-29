/*
 * 一期主链路真实验收数据包（可直接落库）
 *
 * 用途：
 * - 销售 -> 采购 -> 财务 主链路浏览器验收
 * - 固定一套真实主数据 + 正常/异常/边界三类单据
 *
 * 说明：
 * - 幂等导入，优先使用固定主键 + UPSERT
 * - 默认按当前库第一个未删除租户落 tenant_id；找不到时回退到 1
 * - 该脚本只负责真实业务数据，不会自动关闭前端 Demo/示例兜底分支
 * - 依赖当前数据库已执行本仓库 ERP 最新结构迁移
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @dept_id := COALESCE((
    SELECT id
    FROM system_dept
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @password_hash := '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

INSERT INTO system_users
(`id`, `username`, `password`, `nickname`, `remark`, `dept_id`, `post_ids`, `email`, `mobile`, `sex`,
 `avatar`, `status`, `login_ip`, `login_date`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(905901, 'phase1_sale_mgr', @password_hash, '刘销售', '一期验收固定数据-销售负责人', @dept_id, '[]',
 'phase1.sale@test.local', '13800015001', 1, '', 0, '', NULL, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(905902, 'phase1_fin_mgr', @password_hash, '陈财务', '一期验收固定数据-财务负责人', @dept_id, '[]',
 'phase1.finance@test.local', '13800015002', 2, '', 0, '', NULL, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(905903, 'phase1_pm', @password_hash, '周项目', '一期验收固定数据-项目经理', @dept_id, '[]',
 'phase1.pm@test.local', '13800015003', 1, '', 0, '', NULL, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(905904, 'phase1_pc', @password_hash, '李计划', '一期验收固定数据-PC 负责人', @dept_id, '[]',
 'phase1.pc@test.local', '13800015004', 1, '', 0, '', NULL, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(905905, 'phase1_mc', @password_hash, '王物控', '一期验收固定数据-MC 负责人', @dept_id, '[]',
 'phase1.mc@test.local', '13800015005', 1, '', 0, '', NULL, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`password` = VALUES(`password`),
`nickname` = VALUES(`nickname`),
`remark` = VALUES(`remark`),
`dept_id` = VALUES(`dept_id`),
`post_ids` = VALUES(`post_ids`),
`email` = VALUES(`email`),
`mobile` = VALUES(`mobile`),
`sex` = VALUES(`sex`),
`avatar` = VALUES(`avatar`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_product_category
(id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(903001, 0, '一期验收物料', 'PHASE1-MATERIAL', 1, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903002, 903001, '电子类', 'PHASE1-ELEC', 2, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903003, 903001, '五金包材类', 'PHASE1-HWPACK', 3, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(903011, '件', 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903012, '套', 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903013, '箱', 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
status = VALUES(status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_warehouse_category
(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(904001, 0, '一期验收仓库分类', 'PHASE1-WH', 1, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(904002, 904001, '原料仓', 'PHASE1-RAW', 10, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(904003, 904001, '成品仓', 'PHASE1-FG', 20, 0, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`code` = VALUES(`code`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_product
(id, name, material_code, bar_code, category_id, unit_id, status, standard, remark, expiry_day,
 batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(903101, '工业控制板A', 'MAT-CB-001', 'BC-CB-001', 903002, 903011, 0, 'A-V1', '正常链路主物料', 540,
 b'1', b'1', 0.850000, 86.000000, 128.000000, 80.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903102, '不锈钢螺栓 M8', 'MAT-BOLT-001', 'BC-BOLT-001', 903003, 903011, 0, 'M8*30', '备选五金物料', 720,
 b'1', b'0', 0.120000, 1.020000, 1.280000, 0.980000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903103, '电子连接器套装', 'MAT-CONN-001', 'BC-CONN-001', 903002, 903012, 0, 'CONN-A', '异常链路物料', 365,
 b'1', b'1', 0.060000, 84.000000, 118.000000, 80.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(903104, '包装箱套装', 'MAT-PACK-001', 'BC-PACK-001', 903003, 903013, 0, 'PACK-A', '边界链路物料', 365,
 b'0', b'0', 0.050000, 1.020000, 0.990000, 0.900000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_customer
(id, name, contact, mobile, telephone, email, fax, remark, status, sort,
 tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(900101, '海川制造有限公司', '李敏', '13911880001', '021-66000001', 'customer.hc@test.local', NULL, '一期验收客户-正常', 0, 1,
 '91110108MA01HC001X', 0.130000, '中国银行上海分行', '622202000000900101', '上海浦东新区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(900102, '东海装备有限公司', '周宁', '13811770002', '0755-66000002', 'customer.dh@test.local', NULL, '一期验收客户-异常', 0, 2,
 '91310115MA01DH002X', 0.130000, '工商银行深圳分行', '622202000000900102', '深圳南山区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(900103, '中原电子科技有限公司', '王悦', '13711870003', '010-66000003', 'customer.zy@test.local', NULL, '一期验收客户-边界', 0, 3,
 '91440106MA01ZY003X', 0.130000, '建设银行北京分行', '622202000000900103', '北京市朝阳区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(id, name, contact, mobile, telephone, email, fax, remark, status, sort,
 tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(901101, '华东精工供应链有限公司', '王凯', '13817770001', '021-88000001', 'supplier.hd@test.local', NULL, '一期验收供应商-正常', 0, 1,
 '91310115MA02SUP001', 0.130000, '招商银行上海分行', '621483000000901101', '上海嘉定区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(901102, '深港电子元件有限公司', '赵强', '13817770002', '0755-88000002', 'supplier.sg@test.local', NULL, '一期验收供应商-异常', 0, 2,
 '91440300MA02SUP002', 0.130000, '建设银行深圳分行', '621483000000901102', '深圳宝安区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(901103, '广州包材贸易有限公司', '陈涛', '13817770003', '020-88000003', 'supplier.gz@test.local', NULL, '一期验收供应商-边界', 0, 3,
 '91440101MA02SUP003', 0.090000, '工商银行广州分行', '621483000000901103', '广州市黄埔区', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_account
(id, name, no, remark, status, sort, default_status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(905101, '招商银行结算户', 'ACC-001', '一期验收付款账户', 0, 1, b'1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(905102, '工商银行收款户', 'ACC-002', '一期验收收款账户', 0, 2, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_warehouse
(id, category_id, name, address, sort, remark, principal, warehouse_price, truckage_price, status, default_status,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(904101, 904002, '原料仓', '深圳南山 A 区', 1, '一期验收原料仓', '仓管-原料', 0.500000, 0.200000, 0, b'1',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(904102, 904003, '成品仓', '深圳南山 B 区', 2, '一期验收成品仓', '仓管-成品', 0.500000, 0.200000, 0, b'0',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
category_id = VALUES(category_id),
name = VALUES(name),
address = VALUES(address),
sort = VALUES(sort),
remark = VALUES(remark),
principal = VALUES(principal),
warehouse_price = VALUES(warehouse_price),
truckage_price = VALUES(truckage_price),
status = VALUES(status),
default_status = VALUES(default_status),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_project
(`id`, `no`, `name`, `project_type`, `business_type`, `source_type`, `source_project_id`, `sale_order_id`,
 `project_manager_id`, `plan_coordinator_id`, `material_controller_id`, `owner_dept_id`, `current_stage_code`,
 `risk_level`, `customer_id`, `status`, `pc_status`, `mc_status`, `pc_confirm_time`, `mc_confirm_time`,
 `pc_remark`, `mc_remark`, `delivery_date`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(902101, 'PJ-20260520-001', '海川二期交付项目', 'DELIVERY', 'SELF_RESEARCH', 'MANUAL', NULL, NULL,
 905903, 905904, 905905, @dept_id, 'INIT', 'NORMAL', 900101, 0, 'DONE', 'DONE', '2026-05-15 09:00:00', '2026-05-16 10:00:00',
 '计划已确认', '物料已确认', '2026-05-31', '正常链路项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(902102, 'PJ-20260520-002', '深港来料加工批次A', 'RESEARCH', 'TOLL_MANUFACTURING', 'MANUAL', NULL, NULL,
 905903, 905904, 905905, @dept_id, 'INIT', 'HIGH', 900102, 0, 'PENDING', 'PENDING', NULL, NULL,
 NULL, NULL, '2026-06-05', '异常链路项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(902103, 'PJ-20260520-003', '中原工艺验证项目', 'PROCESS_VALIDATION', 'CUSTOMER_SUPPLIED', 'MANUAL', NULL, NULL,
 905903, 905904, 905905, @dept_id, 'INIT', 'LOW', 900103, 0, 'DONE', 'PENDING', '2026-05-18 11:00:00', NULL,
 '边界场景计划已确认', NULL, '2026-06-10', '边界链路项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`project_type` = VALUES(`project_type`),
`business_type` = VALUES(`business_type`),
`source_type` = VALUES(`source_type`),
`source_project_id` = VALUES(`source_project_id`),
`sale_order_id` = VALUES(`sale_order_id`),
`project_manager_id` = VALUES(`project_manager_id`),
`plan_coordinator_id` = VALUES(`plan_coordinator_id`),
`material_controller_id` = VALUES(`material_controller_id`),
`owner_dept_id` = VALUES(`owner_dept_id`),
`current_stage_code` = VALUES(`current_stage_code`),
`risk_level` = VALUES(`risk_level`),
`customer_id` = VALUES(`customer_id`),
`status` = VALUES(`status`),
`pc_status` = VALUES(`pc_status`),
`mc_status` = VALUES(`mc_status`),
`pc_confirm_time` = VALUES(`pc_confirm_time`),
`mc_confirm_time` = VALUES(`mc_confirm_time`),
`pc_remark` = VALUES(`pc_remark`),
`mc_remark` = VALUES(`mc_remark`),
`delivery_date` = VALUES(`delivery_date`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_finance_ledger
(id, no, name, status, sort, default_status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(906001, 'LEDGER-FIN-001', '财务主账簿', 0, 1, b'1', '一期验收固定账簿', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
status = VALUES(status),
sort = VALUES(sort),
default_status = VALUES(default_status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_period
(id, ledger_id, period_code, period_year, period_month, period_sort, start_date, end_date, status, close_time, close_user_id, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(906101, 906001, '2026-05', 2026, 5, 202605, '2026-05-01', '2026-05-31', 10, NULL, NULL, '一期验收当前打开期间',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(906102, 906001, '2026-06', 2026, 6, 202606, '2026-06-01', '2026-06-30', 10, NULL, NULL, '一期验收下一打开期间',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
period_code = VALUES(period_code),
period_year = VALUES(period_year),
period_month = VALUES(period_month),
period_sort = VALUES(period_sort),
start_date = VALUES(start_date),
end_date = VALUES(end_date),
status = VALUES(status),
close_time = VALUES(close_time),
close_user_id = VALUES(close_user_id),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_subject
(id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907001, 906001, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907002, 906001, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907003, 906001, NULL, '1122', '应收账款', 20, 10, b'1', 0, 30, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907004, 906001, NULL, '2202', '应付账款', 20, 20, b'1', 0, 40, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907005, 906001, NULL, '1403', '原材料', 10, 10, b'1', 0, 50, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907006, 906001, NULL, '1405', '库存商品', 10, 10, b'1', 0, 60, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907007, 906001, NULL, '5001', '生产成本', 40, 10, b'1', 0, 70, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907008, 906001, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 80, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907009, 906001, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 90, '一期验收科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
parent_id = VALUES(parent_id),
subject_name = VALUES(subject_name),
subject_type = VALUES(subject_type),
balance_direction = VALUES(balance_direction),
leaf = VALUES(leaf),
status = VALUES(status),
sort = VALUES(sort),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_report_item
(id, ledger_id, report_type, item_category, item_code, item_name, status, sort, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907101, 906001, 10, 10, 'BS-CASH', '货币资金', 0, 10, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907102, 906001, 10, 20, 'BS-AR', '应收账款', 0, 20, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907103, 906001, 10, 30, 'BS-AP', '应付账款', 0, 30, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907104, 906001, 10, 40, 'BS-INV', '存货', 0, 40, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907105, 906001, 20, 10, 'IS-REV', '营业收入', 0, 10, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907106, 906001, 20, 20, 'IS-COST', '营业成本', 0, 20, '一期验收报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
report_type = VALUES(report_type),
item_category = VALUES(item_category),
item_name = VALUES(item_name),
status = VALUES(status),
sort = VALUES(sort),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_report_item_subject
(id, item_id, subject_code, amount_rule, amount_sign, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907201, 907101, '1001', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907202, 907101, '1002', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907203, 907102, '1122', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907204, 907103, '2202', 60, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907205, 907104, '1403', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907206, 907104, '1405', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907207, 907105, '6001', 40, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907208, 907106, '6401', 30, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
item_id = VALUES(item_id),
subject_code = VALUES(subject_code),
amount_rule = VALUES(amount_rule),
amount_sign = VALUES(amount_sign),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_template
(id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907301, 906001, 11, '采购入库自动凭证模板', 0, b'1', '采购入库自动生成凭证', '一期验收凭证模板', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
biz_type = VALUES(biz_type),
name = VALUES(name),
status = VALUES(status),
auto_generate = VALUES(auto_generate),
default_summary = VALUES(default_summary),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_template_item
(id, template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907311, 907301, 1, 10, '1403', '原材料', 10, 0.000000, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907312, 907301, 2, 20, '2202', '应付账款', 20, 0.000000, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
template_id = VALUES(template_id),
entry_no = VALUES(entry_no),
entry_direction = VALUES(entry_direction),
subject_code = VALUES(subject_code),
subject_name = VALUES(subject_name),
amount_source = VALUES(amount_source),
amount_source_value = VALUES(amount_source_value),
summary = VALUES(summary),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order
(id, no, status, process_instance_id, customer_id, project_id, business_type, source_project_id, settlement_type, source_product_id,
 account_id, sale_user_id, order_time, delivery_date, total_count, total_price, total_product_price, total_tax_price,
 discount_percent, discount_price, deposit_price, last_reject_reason, last_reject_time, last_reject_user_id, file_url, remark,
 out_count, return_count, delivery_ready_status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910101, 'SO-20260520-001', 20, NULL, 900101, 902101, 'SELF_RESEARCH', NULL, 'PRODUCT_SALE', 903101,
 905102, 905901, '2026-05-20 09:00:00', '2026-05-31', 100.000000, 12800.000000, 12800.000000, 0.000000,
 0.000000, 0.000000, 2000.000000, NULL, NULL, NULL, NULL, '正常场景销售订单', 100.000000, 0.000000, 'READY_TO_SHIP',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910102, 'SO-20260520-002', 30, NULL, 900102, 902102, 'TOLL_MANUFACTURING', NULL, 'PROCESSING_FEE', 903103,
 905102, 905901, '2026-05-20 10:30:00', '2026-06-05', 50.000000, 5900.000000, 5900.000000, 0.000000,
 0.000000, 0.000000, 0.000000, '项目资料未齐，来料清单与加工费口径未确认', '2026-05-20 15:20:00', 905903, NULL, '异常场景销售订单',
 0.000000, 0.000000, 'NOT_READY', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910103, 'SO-20260520-003', 20, NULL, 900103, 902103, 'CUSTOMER_SUPPLIED', NULL, 'PRODUCT_SALE', 903104,
 905102, 905901, '2026-05-20 11:10:00', '2026-06-10', 1.000000, 0.990000, 0.990000, 0.000000,
 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, NULL, '边界场景销售订单', 1.000000, 0.000000, 'PART_READY',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(id, order_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark,
 out_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910201, 910101, 903101, 903011, 128.000000, 100.000000, 12800.000000, 0.000000, 0.000000, '正常场景-工业控制板A',
 100.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910202, 910102, 903103, 903012, 118.000000, 50.000000, 5900.000000, 0.000000, 0.000000, '异常场景-电子连接器套装',
 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910203, 910103, 903104, 903013, 0.990000, 1.000000, 0.990000, 0.000000, 0.000000, '边界场景-包装箱套装',
 1.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_sale_out
(id, no, status, customer_id, account_id, sale_user_id, out_time, order_id, order_no, total_count, total_price, receipt_price,
 total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910301, 'SOOUT-20260520-001', 20, 900101, 905102, 905901, '2026-05-21 09:30:00', 910101, 'SO-20260520-001',
 100.000000, 12800.000000, 12800.000000, 12800.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, '正常场景销售出库',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910303, 'SOOUT-20260520-003', 20, 900103, 905102, 905901, '2026-05-21 11:20:00', 910103, 'SO-20260520-003',
 1.000000, 0.990000, 0.500000, 0.990000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, '边界场景销售出库',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
customer_id = VALUES(customer_id),
account_id = VALUES(account_id),
sale_user_id = VALUES(sale_user_id),
out_time = VALUES(out_time),
order_id = VALUES(order_id),
order_no = VALUES(order_no),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
receipt_price = VALUES(receipt_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
other_price = VALUES(other_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_out_items
(id, out_id, order_item_id, warehouse_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910401, 910301, 910201, 904102, 903101, 903011, 128.000000, 100.000000, 12800.000000, 0.000000, 0.000000, '正常场景销售出库明细',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910403, 910303, 910203, 904102, 903104, 903013, 0.990000, 1.000000, 0.990000, 0.000000, 0.000000, '边界场景销售出库明细',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
out_id = VALUES(out_id),
order_item_id = VALUES(order_item_id),
warehouse_id = VALUES(warehouse_id),
product_id = VALUES(product_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt
(id, no, status, receipt_time, finance_user_id, customer_id, account_id, total_price, discount_price, receipt_price, remark, file_url,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910501, 'RCPT-20260520-001', 20, '2026-05-22 10:00:00', 905902, 900101, 905102, 12800.000000, 0.000000, 12800.000000, '正常场景销售回款', NULL,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910503, 'RCPT-20260520-003', 20, '2026-05-22 15:00:00', 905902, 900103, 905102, 0.990000, 0.000000, 0.500000, '边界场景部分回款', NULL,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
receipt_time = VALUES(receipt_time),
finance_user_id = VALUES(finance_user_id),
customer_id = VALUES(customer_id),
account_id = VALUES(account_id),
total_price = VALUES(total_price),
discount_price = VALUES(discount_price),
receipt_price = VALUES(receipt_price),
remark = VALUES(remark),
file_url = VALUES(file_url),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt_item
(id, receipt_id, biz_type, biz_id, biz_no, total_price, receipted_price, receipt_price, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910601, 910501, 21, 910301, 'SOOUT-20260520-001', 12800.000000, 0.000000, 12800.000000, '正常场景收款核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(910603, 910503, 21, 910303, 'SOOUT-20260520-003', 0.990000, 0.000000, 0.500000, '边界场景部分收款',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
receipt_id = VALUES(receipt_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
total_price = VALUES(total_price),
receipted_price = VALUES(receipted_price),
receipt_price = VALUES(receipt_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order
(id, no, status, process_instance_id, supplier_id, account_id, order_time, total_count, total_price, total_product_price, total_tax_price,
 discount_percent, discount_price, deposit_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id,
 in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911101, 'PO-20260520-001', 20, NULL, 901101, 905101, '2026-05-20 09:40:00', 100.000000, 8600.000000, 8600.000000, 0.000000,
 0.000000, 0.000000, 1000.000000, NULL, '正常场景采购订单', NULL, NULL, NULL,
 100.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911102, 'PO-20260520-002', 20, NULL, 901102, 905101, '2026-05-20 10:50:00', 50.000000, 4200.000000, 4200.000000, 0.000000,
 0.000000, 0.000000, 0.000000, NULL, '异常场景采购订单：质检不通过', NULL, NULL, NULL,
 50.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911103, 'PO-20260520-003', 20, NULL, 901103, 905101, '2026-05-20 11:30:00', 1.000000, 1.020000, 1.020000, 0.000000,
 0.000000, 0.000000, 0.000000, NULL, '边界场景采购订单', NULL, NULL, NULL,
 0.500000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(id, order_id, product_id, project_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version,
 count, total_price, tax_percent, tax_price, remark, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911201, 911101, 903101, 902101, 903011, 86.000000, 0.000000, NULL, NULL,
 100.000000, 8600.000000, 0.000000, 0.000000, '正常场景-工业控制板A', 100.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911202, 911102, 903103, 902102, 903012, 84.000000, 0.000000, NULL, NULL,
 50.000000, 4200.000000, 0.000000, 0.000000, '异常场景-电子连接器套装', 50.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911203, 911103, 903104, 902103, 903013, 1.020000, 0.000000, NULL, NULL,
 1.000000, 1.020000, 0.000000, 0.000000, '边界场景-包装箱套装', 0.500000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_purchase_in
(id, no, status, qa_status, process_instance_id, supplier_id, account_id, in_time, order_id, order_no, total_count, total_price, payment_price,
 total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark, last_reject_reason, last_reject_time,
 last_reject_user_id, qa_time, qa_user_id, qa_remark, qa_pass_count, qa_reject_count, stock_in_count, stock_in_status, stock_in_time, stock_in_user_id,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911301, 'PI-20260520-001', 20, 50, NULL, 901101, 905101, '2026-05-21 10:00:00', 911101, 'PO-20260520-001', 100.000000, 8600.000000, 8600.000000,
 8600.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, '正常场景采购入库', NULL, NULL,
 NULL, '2026-05-21 11:00:00', 905905, '全部合格', 100.000000, 0.000000, 100.000000, 20, '2026-05-21 12:00:00', 905905,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911302, 'PI-20260520-002', 20, 50, NULL, 901102, 905101, '2026-05-21 13:30:00', 911102, 'PO-20260520-002', 50.000000, 4200.000000, 0.000000,
 4200.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, '异常场景采购入库：质检不通过未入库', NULL, NULL,
 NULL, '2026-05-21 14:10:00', 905905, '全部不合格', 0.000000, 50.000000, 0.000000, 30, NULL, NULL,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911303, 'PI-20260520-003', 20, 50, NULL, 901103, 905101, '2026-05-21 15:20:00', 911103, 'PO-20260520-003', 1.000000, 1.020000, 0.510000,
 1.020000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, '边界场景采购入库：部分入库', NULL, NULL,
 NULL, '2026-05-21 15:50:00', 905905, '部分合格', 0.500000, 0.500000, 0.500000, 15, '2026-05-21 16:10:00', 905905,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
qa_status = VALUES(qa_status),
process_instance_id = VALUES(process_instance_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
in_time = VALUES(in_time),
order_id = VALUES(order_id),
order_no = VALUES(order_no),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
payment_price = VALUES(payment_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
other_price = VALUES(other_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
last_reject_reason = VALUES(last_reject_reason),
last_reject_time = VALUES(last_reject_time),
last_reject_user_id = VALUES(last_reject_user_id),
qa_time = VALUES(qa_time),
qa_user_id = VALUES(qa_user_id),
qa_remark = VALUES(qa_remark),
qa_pass_count = VALUES(qa_pass_count),
qa_reject_count = VALUES(qa_reject_count),
stock_in_count = VALUES(stock_in_count),
stock_in_status = VALUES(stock_in_status),
stock_in_time = VALUES(stock_in_time),
stock_in_user_id = VALUES(stock_in_user_id),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_items
(id, in_id, order_item_id, warehouse_id, product_id, purchase_source_batch_id, product_unit_id, product_price, engineering_fee, pricing_bom_id,
 pricing_bom_version, count, total_price, tax_percent, tax_price, qa_pass_count, qa_reject_count, stock_in_count, remark, qa_remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911311, 911301, 911201, 904101, 903101, NULL, 903011, 86.000000, 0.000000, NULL,
 NULL, 100.000000, 8600.000000, 0.000000, 0.000000, 100.000000, 0.000000, 100.000000, '正常场景采购入库明细', '通过',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911312, 911302, 911202, 904101, 903103, NULL, 903012, 84.000000, 0.000000, NULL,
 NULL, 50.000000, 4200.000000, 0.000000, 0.000000, 0.000000, 50.000000, 0.000000, '异常场景采购入库明细', '全部不通过',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911313, 911303, 911203, 904101, 903104, NULL, 903013, 1.020000, 0.000000, NULL,
 NULL, 1.000000, 1.020000, 0.000000, 0.000000, 0.500000, 0.500000, 0.500000, '边界场景采购入库明细', '部分通过',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
in_id = VALUES(in_id),
order_item_id = VALUES(order_item_id),
warehouse_id = VALUES(warehouse_id),
product_id = VALUES(product_id),
purchase_source_batch_id = VALUES(purchase_source_batch_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
engineering_fee = VALUES(engineering_fee),
pricing_bom_id = VALUES(pricing_bom_id),
pricing_bom_version = VALUES(pricing_bom_version),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
qa_pass_count = VALUES(qa_pass_count),
qa_reject_count = VALUES(qa_reject_count),
stock_in_count = VALUES(stock_in_count),
remark = VALUES(remark),
qa_remark = VALUES(qa_remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement
(id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount, paid_amount, remain_amount,
 currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911401, 'AP-20260520-001', 11, 911301, 'PI-20260520-001', 911101, 'PO-20260520-001', 901101, 905101, 8600.000000, 8600.000000, 0.000000,
 'CNY', '2026-05-21 12:00:00', '2026-06-20 12:00:00', 2, 'INV-20260520-001', 8600.000000, 30, '正常场景应付台账',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911402, 'AP-20260520-002', 11, 911302, 'PI-20260520-002', 911102, 'PO-20260520-002', 901102, 905101, 4200.000000, 0.000000, 4200.000000,
 'CNY', '2026-05-21 14:10:00', '2026-06-20 14:10:00', 0, NULL, NULL, 10, '异常场景应付台账',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911403, 'AP-20260520-003', 11, 911303, 'PI-20260520-003', 911103, 'PO-20260520-003', 901103, 905101, 1.020000, 0.510000, 0.510000,
 'CNY', '2026-05-21 16:10:00', '2026-06-20 16:10:00', 1, 'INV-20260520-003', 0.510000, 20, '边界场景应付台账',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_ap_statement_item
(id, statement_id, item_type, ref_type, ref_id, ref_no, amount, after_paid_amount, after_remain_amount, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911501, 911401, 10, 11, 911301, 'PI-20260520-001', 8600.000000, 0.000000, 8600.000000, '采购入库转应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911502, 911401, 50, 11, 911301, 'PI-20260520-001', 8600.000000, 0.000000, 8600.000000, '收票登记：INV-20260520-001',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911503, 911401, 20, 11, 911601, 'PAY-20260520-001', 8600.000000, 0.000000, 8600.000000, 'allocate payment PAY-20260520-001',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911504, 911402, 10, 11, 911302, 'PI-20260520-002', 4200.000000, 0.000000, 4200.000000, '采购入库转应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911505, 911403, 10, 11, 911303, 'PI-20260520-003', 1.020000, 0.000000, 1.020000, '采购入库转应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911506, 911403, 50, 11, 911303, 'PI-20260520-003', 0.510000, 0.000000, 1.020000, '收票登记：INV-20260520-003',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911507, 911403, 20, 11, 911603, 'PAY-20260520-003', 0.510000, 0.000000, 1.020000, 'allocate payment PAY-20260520-003',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
statement_id = VALUES(statement_id),
item_type = VALUES(item_type),
ref_type = VALUES(ref_type),
ref_id = VALUES(ref_id),
ref_no = VALUES(ref_no),
amount = VALUES(amount),
after_paid_amount = VALUES(after_paid_amount),
after_remain_amount = VALUES(after_remain_amount),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment
(id, no, status, payment_time, finance_user_id, supplier_id, account_id, total_price, discount_price, payment_price, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911601, 'PAY-20260520-001', 20, '2026-05-22 09:30:00', 905902, 901101, 905101, 8600.000000, 0.000000, 8600.000000, '正常场景付款单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911602, 'PAY-20260520-002', 10, '2026-05-22 11:00:00', 905902, 901102, 905101, 0.000000, 0.000000, 0.000000, '异常场景草稿付款单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911603, 'PAY-20260520-003', 20, '2026-05-22 16:00:00', 905902, 901103, 905101, 0.510000, 0.000000, 0.510000, '边界场景部分付款单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
payment_time = VALUES(payment_time),
finance_user_id = VALUES(finance_user_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
total_price = VALUES(total_price),
discount_price = VALUES(discount_price),
payment_price = VALUES(payment_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_item
(id, payment_id, ap_statement_id, biz_type, biz_id, biz_no, total_price, paid_price, payment_price, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911701, 911601, 911401, 11, 911301, 'PI-20260520-001', 8600.000000, 0.000000, 8600.000000, '正常场景应付核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911703, 911603, 911403, 11, 911303, 'PI-20260520-003', 1.020000, 0.000000, 0.510000, '边界场景部分核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
payment_id = VALUES(payment_id),
ap_statement_id = VALUES(ap_statement_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
total_price = VALUES(total_price),
paid_price = VALUES(paid_price),
payment_price = VALUES(payment_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_allocate
(id, payment_id, payment_item_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911801, 911601, 911701, 911401, 8600.000000, 901101, 11, 911301, 'PI-20260520-001', 20, '正常场景核销完成',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911803, 911603, 911703, 911403, 0.510000, 901103, 11, 911303, 'PI-20260520-003', 20, '边界场景部分核销完成',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
payment_id = VALUES(payment_id),
payment_item_id = VALUES(payment_item_id),
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

INSERT INTO erp_finance_voucher
(id, voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status,
 total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time,
 reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id, reverse_remark, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911901, 'VOU-20260520-001', 906001, 906101, 907301, 11, 911301, 'PI-20260520-001', '2026-05-22 10:30:00', 30,
 8600.000000, 8600.000000, 905902, '2026-05-22 10:40:00', 905902, '2026-05-22 10:50:00',
 NULL, NULL, NULL, NULL, NULL, '正常场景采购入库凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
voucher_no = VALUES(voucher_no),
ledger_id = VALUES(ledger_id),
period_id = VALUES(period_id),
template_id = VALUES(template_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
voucher_time = VALUES(voucher_time),
status = VALUES(status),
total_debit_amount = VALUES(total_debit_amount),
total_credit_amount = VALUES(total_credit_amount),
approve_user_id = VALUES(approve_user_id),
approve_time = VALUES(approve_time),
post_user_id = VALUES(post_user_id),
post_time = VALUES(post_time),
reverse_user_id = VALUES(reverse_user_id),
reverse_time = VALUES(reverse_time),
reverse_voucher_id = VALUES(reverse_voucher_id),
reverse_from_voucher_id = VALUES(reverse_from_voucher_id),
reverse_remark = VALUES(reverse_remark),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_entry
(id, voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(911911, 911901, 1, '确认采购入库', '1403', '原材料', 8600.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(911912, 911901, 2, '确认采购入库', '2202', '应付账款', 0.000000, 8600.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
voucher_id = VALUES(voucher_id),
entry_no = VALUES(entry_no),
summary = VALUES(summary),
subject_code = VALUES(subject_code),
subject_name = VALUES(subject_name),
debit_amount = VALUES(debit_amount),
credit_amount = VALUES(credit_amount),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_audit_log
(id, order_id, action_type, before_status, after_status, reason, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910701, 910101, 'APPROVE', 10, 20, '正常链路订单审核通过，允许推进交付。', 'tester', '2026-05-20 09:20:00', 'tester', NOW(), b'0', @tenant_id),
(910702, 910102, 'REJECT', 10, 30, '项目资料未齐，来料清单与加工费口径未确认。', 'tester', '2026-05-20 15:20:00', 'tester', NOW(), b'0', @tenant_id),
(910703, 910103, 'APPROVE', 10, 20, '边界场景订单审核通过，允许最小金额联调。', 'tester', '2026-05-20 11:30:00', 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
action_type = VALUES(action_type),
before_status = VALUES(before_status),
after_status = VALUES(after_status),
reason = VALUES(reason),
creator = VALUES(creator),
create_time = VALUES(create_time),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_reject_log
(id, order_id, reason, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(910801, 910102, '项目资料未齐，来料清单与加工费口径未确认。', 'tester', '2026-05-20 15:20:00', 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
reason = VALUES(reason),
creator = VALUES(creator),
create_time = VALUES(create_time),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order_audit_log
(id, order_id, action_type, before_status, after_status, reason, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(912001, 911101, 'APPROVE', 10, 20, '正常链路采购订单审核通过。', 'tester', '2026-05-20 09:50:00', 'tester', NOW(), b'0', @tenant_id),
(912002, 911102, 'APPROVE', 10, 20, '异常链路采购订单审核通过，进入质检异常验证。', 'tester', '2026-05-20 11:10:00', 'tester', NOW(), b'0', @tenant_id),
(912003, 911103, 'APPROVE', 10, 20, '边界链路采购订单审核通过。', 'tester', '2026-05-20 11:45:00', 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
action_type = VALUES(action_type),
before_status = VALUES(before_status),
after_status = VALUES(after_status),
reason = VALUES(reason),
creator = VALUES(creator),
create_time = VALUES(create_time),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

UPDATE erp_project
SET sale_order_id = CASE id
    WHEN 902101 THEN 910101
    WHEN 902102 THEN 910102
    WHEN 902103 THEN 910103
    ELSE sale_order_id
END,
    updater = 'tester',
    update_time = NOW()
WHERE id IN (902101, 902102, 902103)
  AND tenant_id = @tenant_id
  AND deleted = b'0';

INSERT INTO erp_finance_subject_balance
(id, ledger_id, period_id, period_sort, subject_code, subject_name,
 opening_debit_amount, opening_credit_amount, current_debit_amount, current_credit_amount,
 ending_debit_amount, ending_credit_amount,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(907401, 906001, 906101, 202605, '1001', '库存现金', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907402, 906001, 906101, 202605, '1002', '银行存款', 100000.000000, 0.000000, 12800.500000, 8600.510000, 104199.990000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907403, 906001, 906101, 202605, '1122', '应收账款', 0.000000, 0.000000, 12800.990000, 12800.500000, 0.490000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907404, 906001, 906101, 202605, '2202', '应付账款', 0.000000, 0.000000, 8600.510000, 12801.020000, 0.000000, 4200.510000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907405, 906001, 906101, 202605, '1403', '原材料', 0.000000, 0.000000, 8601.020000, 0.000000, 8601.020000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907406, 906001, 906101, 202605, '1405', '库存商品', 0.000000, 0.000000, 12800.990000, 0.000000, 12800.990000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907407, 906001, 906101, 202605, '5001', '生产成本', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907408, 906001, 906101, 202605, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 12800.990000, 0.000000, 12800.990000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(907409, 906001, 906101, 202605, '6401', '主营业务成本', 0.000000, 0.000000, 8601.020000, 0.000000, 8601.020000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
period_id = VALUES(period_id),
period_sort = VALUES(period_sort),
subject_name = VALUES(subject_name),
opening_debit_amount = VALUES(opening_debit_amount),
opening_credit_amount = VALUES(opening_credit_amount),
current_debit_amount = VALUES(current_debit_amount),
current_credit_amount = VALUES(current_credit_amount),
ending_debit_amount = VALUES(ending_debit_amount),
ending_credit_amount = VALUES(ending_credit_amount),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

SELECT
    @tenant_id AS tenant_id,
    (SELECT COUNT(*) FROM erp_customer WHERE id IN (900101, 900102, 900103) AND deleted = b'0' AND tenant_id = @tenant_id) AS customer_count,
    (SELECT COUNT(*) FROM erp_supplier WHERE id IN (901101, 901102, 901103) AND deleted = b'0' AND tenant_id = @tenant_id) AS supplier_count,
    (SELECT COUNT(*) FROM erp_project WHERE id IN (902101, 902102, 902103) AND deleted = b'0' AND tenant_id = @tenant_id) AS project_count,
    (SELECT COUNT(*) FROM erp_sale_order WHERE id IN (910101, 910102, 910103) AND deleted = b'0' AND tenant_id = @tenant_id) AS sale_order_count,
    (SELECT COUNT(*) FROM erp_purchase_in WHERE id IN (911301, 911302, 911303) AND deleted = b'0' AND tenant_id = @tenant_id) AS purchase_in_count,
    (SELECT COUNT(*) FROM erp_ap_statement WHERE id IN (911401, 911402, 911403) AND deleted = b'0' AND tenant_id = @tenant_id) AS ap_statement_count,
    (SELECT COUNT(*) FROM erp_finance_payment WHERE id IN (911601, 911602, 911603) AND deleted = b'0' AND tenant_id = @tenant_id) AS payment_count,
    (SELECT COUNT(*) FROM erp_finance_receipt WHERE id IN (910501, 910503) AND deleted = b'0' AND tenant_id = @tenant_id) AS receipt_count;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
