/*
 * ERP 财务报表与项目成本分析测试数据
 *
 * 覆盖页面：
 * - 财务报表中心：/erp/finance/reports
 * - 项目成本分析：/erp/finance/cost
 *
 * 设计说明：
 * - 可重复执行，固定 ID，使用 ON DUPLICATE KEY UPDATE 覆盖同一批样本
 * - 不删除现有业务数据
 * - 若当前库缺少 erp_production_man_hour 表，脚本会先补最小可用表结构
 * - 默认使用 2026-05 期间；项目成本分析页面查询归集月份请选 2026-05
 * - 推荐前置脚本：sql/mysql/99-erp-finance-demo-data.sql
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

SET @finance_user_id := COALESCE((
    SELECT id
    FROM system_users
    WHERE username = '财务主管'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @customer_id := COALESCE((
    SELECT id
    FROM erp_customer
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 99401);

SET @product_motor_id := COALESCE((
    SELECT id
    FROM erp_product
    WHERE id = 99104
      AND deleted = b'0'
    LIMIT 1
), (
    SELECT id
    FROM erp_product
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 99104);

SET @product_board_id := COALESCE((
    SELECT id
    FROM erp_product
    WHERE id = 99102
      AND deleted = b'0'
    LIMIT 1
), @product_motor_id);

SET @product_pack_id := COALESCE((
    SELECT id
    FROM erp_product
    WHERE id = 99105
      AND deleted = b'0'
    LIMIT 1
), @product_motor_id);

SET @ledger_id := COALESCE((
    SELECT id
    FROM erp_finance_ledger
    WHERE id = 99601
      AND deleted = b'0'
    LIMIT 1
), (
    SELECT id
    FROM erp_finance_ledger
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 99601);

SET @period_id := COALESCE((
    SELECT id
    FROM erp_finance_period
    WHERE ledger_id = @ledger_id
      AND period_code = '2026-05'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
), 128011);

INSERT INTO erp_finance_ledger
(id, no, name, status, sort, default_status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(99601, 'LEDGER-FIN-001', '财务主账簿', 0, 1, b'1', '财务报表测试主账簿', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
no = VALUES(no),
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
(id, ledger_id, period_code, period_year, period_month, period_sort, start_date, end_date, status,
 close_time, close_user_id, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128011, @ledger_id, '2026-05', 2026, 5, 202605, '2026-05-01', '2026-05-31', 10,
 NULL, NULL, '财务报表测试期间', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

SET @period_id := COALESCE((
    SELECT id
    FROM erp_finance_period
    WHERE ledger_id = @ledger_id
      AND period_code = '2026-05'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
), 128011);

INSERT INTO erp_finance_subject
(id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status,
 sort, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128101, @ledger_id, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128102, @ledger_id, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128103, @ledger_id, NULL, '1122', '应收账款', 10, 10, b'1', 0, 30, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128104, @ledger_id, NULL, '1403', '原材料', 10, 10, b'1', 0, 40, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128105, @ledger_id, NULL, '1405', '库存商品', 10, 10, b'1', 0, 50, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128106, @ledger_id, NULL, '2202', '应付账款', 20, 20, b'1', 0, 60, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128107, @ledger_id, NULL, '2221', '应交税费', 20, 20, b'1', 0, 70, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128108, @ledger_id, NULL, '4001', '实收资本', 30, 20, b'1', 0, 80, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128109, @ledger_id, NULL, '5001', '生产成本', 50, 10, b'1', 0, 90, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128110, @ledger_id, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 100, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128111, @ledger_id, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 110, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128112, @ledger_id, NULL, '6601', '销售费用', 50, 10, b'1', 0, 120, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128113, @ledger_id, NULL, '6602', '管理费用', 50, 10, b'1', 0, 130, '财务报表测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(128201, @ledger_id, 10, 10, 'BS-CASH', '货币资金', 0, 10, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128202, @ledger_id, 10, 10, 'BS-AR', '应收账款', 0, 20, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128203, @ledger_id, 10, 10, 'BS-INVENTORY', '存货', 0, 30, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128204, @ledger_id, 10, 20, 'BS-AP', '应付账款', 0, 40, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128205, @ledger_id, 10, 20, 'BS-TAX', '应交税费', 0, 50, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128206, @ledger_id, 10, 30, 'BS-CAPITAL', '实收资本', 0, 60, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128207, @ledger_id, 20, 40, 'IS-REV', '营业收入', 0, 10, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128208, @ledger_id, 20, 50, 'IS-COST', '营业成本', 0, 20, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128209, @ledger_id, 20, 50, 'IS-SALE-FEE', '销售费用', 0, 30, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128210, @ledger_id, 20, 50, 'IS-MANAGE-FEE', '管理费用', 0, 40, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128211, @ledger_id, 30, 60, 'CF-SALE-CASH', '销售商品收到的现金', 0, 10, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128212, @ledger_id, 30, 70, 'CF-PURCHASE-CASH', '购买商品支付的现金', 0, 20, '财务报表测试项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
ledger_id = VALUES(ledger_id),
report_type = VALUES(report_type),
item_category = VALUES(item_category),
item_code = VALUES(item_code),
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
(128301, 128201, '1001', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128302, 128201, '1002', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128303, 128202, '1122', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128304, 128203, '1403', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128305, 128203, '1405', 50, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128306, 128204, '2202', 60, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128307, 128205, '2221', 60, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128308, 128206, '4001', 60, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128309, 128207, '6001', 80, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128310, 128208, '6401', 70, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128311, 128209, '6601', 70, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128312, 128210, '6602', 70, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128313, 128211, '6001', 40, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128314, 128212, '2202', 30, 1, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
item_id = VALUES(item_id),
subject_code = VALUES(subject_code),
amount_rule = VALUES(amount_rule),
amount_sign = VALUES(amount_sign),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_subject_balance
(id, ledger_id, period_id, period_sort, subject_code, subject_name,
 opening_debit_amount, opening_credit_amount, current_debit_amount, current_credit_amount,
 ending_debit_amount, ending_credit_amount, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128401, @ledger_id, @period_id, 202605, '1001', '库存现金', 12000.000000, 0.000000, 1800.000000, 1500.000000, 12300.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128402, @ledger_id, @period_id, 202605, '1002', '银行存款', 540000.000000, 0.000000, 286000.000000, 248300.000000, 577700.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128403, @ledger_id, @period_id, 202605, '1122', '应收账款', 86000.000000, 0.000000, 148000.000000, 116000.000000, 118000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128404, @ledger_id, @period_id, 202605, '1403', '原材料', 188000.000000, 0.000000, 76000.000000, 52000.000000, 212000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128405, @ledger_id, @period_id, 202605, '1405', '库存商品', 226000.000000, 0.000000, 93000.000000, 82000.000000, 237000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128406, @ledger_id, @period_id, 202605, '2202', '应付账款', 0.000000, 134000.000000, 92000.000000, 118000.000000, 0.000000, 160000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128407, @ledger_id, @period_id, 202605, '2221', '应交税费', 0.000000, 28000.000000, 18000.000000, 26500.000000, 0.000000, 36500.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128408, @ledger_id, @period_id, 202605, '4001', '实收资本', 0.000000, 850000.000000, 0.000000, 0.000000, 0.000000, 850000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128409, @ledger_id, @period_id, 202605, '5001', '生产成本', 64000.000000, 0.000000, 38800.000000, 31200.000000, 71600.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128410, @ledger_id, @period_id, 202605, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 286000.000000, 0.000000, 286000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128411, @ledger_id, @period_id, 202605, '6401', '主营业务成本', 0.000000, 0.000000, 168000.000000, 0.000000, 168000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128412, @ledger_id, @period_id, 202605, '6601', '销售费用', 0.000000, 0.000000, 18600.000000, 0.000000, 18600.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128413, @ledger_id, @period_id, 202605, '6602', '管理费用', 0.000000, 0.000000, 24200.000000, 0.000000, 24200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_project
(id, no, name, project_type, business_type, source_type, source_project_id, sale_order_id,
 project_manager_id, plan_coordinator_id, material_controller_id, owner_dept_id, current_stage_code,
 risk_level, customer_id, status, pc_status, mc_status, pc_confirm_time, mc_confirm_time,
 pc_remark, mc_remark, delivery_date, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128501, 'PJ-COST-202605-001', '智能电机交付项目', 'DELIVERY', 'SELF_RESEARCH', 'SALE_ORDER', NULL, NULL,
 @finance_user_id, @finance_user_id, @finance_user_id, 103, 'PRODUCTION',
 'MEDIUM', @customer_id, 1, 'DONE', 'DONE', '2026-05-03 10:00:00', '2026-05-03 11:00:00',
 '测试项目成本-计划确认', '测试项目成本-物料确认', '2026-06-20', '项目成本分析测试项目 A', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128502, 'PJ-COST-202605-002', '控制板试制项目', 'PROCESS_VALIDATION', 'CUSTOMER_SUPPLIED', 'SALE_ORDER', NULL, NULL,
 @finance_user_id, @finance_user_id, @finance_user_id, 103, 'TRIAL_PRODUCTION',
 'LOW', @customer_id, 1, 'DONE', 'DONE', '2026-05-04 10:00:00', '2026-05-04 11:00:00',
 '测试项目成本-计划确认', '测试项目成本-物料确认', '2026-06-28', '项目成本分析测试项目 B', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128503, 'PJ-COST-202605-003', '包装优化项目', 'DELIVERY', 'TOLL_MANUFACTURING', 'SALE_ORDER', NULL, NULL,
 @finance_user_id, @finance_user_id, @finance_user_id, 103, 'CLOSING_REVIEW',
 'HIGH', @customer_id, 1, 'DONE', 'PENDING', '2026-05-05 10:00:00', NULL,
 '测试项目成本-计划确认', NULL, '2026-06-10', '项目成本分析测试项目 C', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
no = VALUES(no),
name = VALUES(name),
project_type = VALUES(project_type),
business_type = VALUES(business_type),
source_type = VALUES(source_type),
source_project_id = VALUES(source_project_id),
sale_order_id = VALUES(sale_order_id),
project_manager_id = VALUES(project_manager_id),
plan_coordinator_id = VALUES(plan_coordinator_id),
material_controller_id = VALUES(material_controller_id),
owner_dept_id = VALUES(owner_dept_id),
current_stage_code = VALUES(current_stage_code),
risk_level = VALUES(risk_level),
customer_id = VALUES(customer_id),
status = VALUES(status),
pc_status = VALUES(pc_status),
mc_status = VALUES(mc_status),
pc_confirm_time = VALUES(pc_confirm_time),
mc_confirm_time = VALUES(mc_confirm_time),
pc_remark = VALUES(pc_remark),
mc_remark = VALUES(mc_remark),
delivery_date = VALUES(delivery_date),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_order
(id, order_no, product_id, project_id, plan_qty, finished_qty, warehouse_id, plan_start_time, plan_end_time,
 status, source_type, source_id, source_order_id, source_item_id, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128601, 'MO-COST-202605-001', @product_motor_id, 128501, 120.000000, 110.000000, 99202, '2026-05-06 08:30:00', '2026-05-18 18:00:00',
 3, 'PROJECT', 128501, NULL, NULL, '项目成本分析测试工单 A-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128602, 'MO-COST-202605-002', @product_board_id, 128501, 80.000000, 76.000000, 99202, '2026-05-08 08:30:00', '2026-05-20 18:00:00',
 3, 'PROJECT', 128501, NULL, NULL, '项目成本分析测试工单 A-2', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128603, 'MO-COST-202605-003', @product_board_id, 128502, 60.000000, 58.000000, 99202, '2026-05-10 08:30:00', '2026-05-24 18:00:00',
 3, 'PROJECT', 128502, NULL, NULL, '项目成本分析测试工单 B-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128604, 'MO-COST-202605-004', @product_pack_id, 128503, 400.000000, 360.000000, 99202, '2026-05-12 08:30:00', '2026-05-26 18:00:00',
 2, 'PROJECT', 128503, NULL, NULL, '项目成本分析测试工单 C-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_no = VALUES(order_no),
product_id = VALUES(product_id),
project_id = VALUES(project_id),
plan_qty = VALUES(plan_qty),
finished_qty = VALUES(finished_qty),
warehouse_id = VALUES(warehouse_id),
plan_start_time = VALUES(plan_start_time),
plan_end_time = VALUES(plan_end_time),
status = VALUES(status),
source_type = VALUES(source_type),
source_id = VALUES(source_id),
source_order_id = VALUES(source_order_id),
source_item_id = VALUES(source_item_id),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

CREATE TABLE IF NOT EXISTS `erp_production_man_hour` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `production_order_id` BIGINT NOT NULL COMMENT 'production order id',
  `accounting_month` VARCHAR(7) NOT NULL COMMENT 'accounting month',
  `work_date` DATE NOT NULL COMMENT 'work date',
  `man_hour` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT 'man hour',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `creator` VARCHAR(64) DEFAULT '' COMMENT 'creator',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` VARCHAR(64) DEFAULT '' COMMENT 'updater',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_erp_production_man_hour_order` (`tenant_id`, `production_order_id`, `deleted`),
  KEY `idx_erp_production_man_hour_month` (`tenant_id`, `accounting_month`, `deleted`),
  KEY `idx_erp_production_man_hour_work_date` (`tenant_id`, `work_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production man hour';

CREATE TABLE IF NOT EXISTS `erp_production_issue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `issue_no` VARCHAR(64) NOT NULL COMMENT '领料单号',
  `production_order_id` BIGINT NOT NULL COMMENT '生产工单编号',
  `issue_time` DATETIME NOT NULL COMMENT '领料时间',
  `status` INT NOT NULL DEFAULT 20 COMMENT '状态',
  `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT '领料金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_production_issue_no` (`tenant_id`, `issue_no`, `deleted`),
  KEY `idx_production_issue_order` (`tenant_id`, `production_order_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产领料单';

CREATE TABLE IF NOT EXISTS `erp_production_cost_allocation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `allocation_no` VARCHAR(64) NOT NULL COMMENT 'allocation no',
  `accounting_month` VARCHAR(7) NOT NULL COMMENT 'accounting month',
  `cost_type` INT NOT NULL COMMENT 'cost type',
  `rule_id` BIGINT DEFAULT NULL COMMENT 'rule id',
  `total_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT 'total amount',
  `status` INT NOT NULL DEFAULT 10 COMMENT 'status',
  `executed_time` DATETIME DEFAULT NULL COMMENT 'executed time',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_production_cost_allocation_no` (`tenant_id`, `allocation_no`, `deleted`),
  KEY `idx_production_cost_allocation_month` (`tenant_id`, `accounting_month`, `deleted`),
  KEY `idx_production_cost_allocation_type` (`tenant_id`, `cost_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production cost allocation';

CREATE TABLE IF NOT EXISTS `erp_production_cost_allocation_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `allocation_id` BIGINT NOT NULL COMMENT 'allocation id',
  `production_order_id` BIGINT NOT NULL COMMENT 'production order id',
  `basis_value` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT 'basis value',
  `basis_ratio` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT 'basis ratio',
  `allocated_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0.000000 COMMENT 'allocated amount',
  `generated_cost_entry_id` BIGINT DEFAULT NULL COMMENT 'generated cost entry id',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_production_cost_allocation_result_allocation` (`tenant_id`, `allocation_id`, `deleted`),
  KEY `idx_production_cost_allocation_result_generated_entry` (`tenant_id`, `generated_cost_entry_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production cost allocation result';

INSERT INTO erp_production_issue
(id, issue_no, production_order_id, issue_time, status, issue_amount, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128901, 'MI-COST-202605-001', 128601, '2026-05-09 09:20:00', 20, 18600.000000, '项目成本分析测试-材料领料 A-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128902, 'MI-COST-202605-002', 128602, '2026-05-17 10:15:00', 20, 10480.000000, '项目成本分析测试-材料领料 A-2', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128903, 'MI-COST-202605-003', 128603, '2026-05-19 11:05:00', 20, 13260.000000, '项目成本分析测试-材料领料 B-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128904, 'MI-COST-202605-004', 128604, '2026-05-22 14:40:00', 20, 3980.000000, '项目成本分析测试-材料领料 C-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
issue_no = VALUES(issue_no),
production_order_id = VALUES(production_order_id),
issue_time = VALUES(issue_time),
status = VALUES(status),
issue_amount = VALUES(issue_amount),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_man_hour
(id, production_order_id, accounting_month, work_date, man_hour, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128701, 128601, '2026-05', '2026-05-08', 42.500000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128702, 128601, '2026-05', '2026-05-15', 38.000000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128703, 128602, '2026-05', '2026-05-16', 26.000000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128704, 128603, '2026-05', '2026-05-18', 31.500000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128705, 128604, '2026-05', '2026-05-20', 18.000000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128706, 128604, '2026-05', '2026-05-24', 16.500000, '项目成本分析测试工时', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
production_order_id = VALUES(production_order_id),
accounting_month = VALUES(accounting_month),
work_date = VALUES(work_date),
man_hour = VALUES(man_hour),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_cost_entry
(id, production_order_id, cost_type, source_type, accounting_month, amount, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128801, 128601, 20, 10, '2026-05', 12800.000000, '项目成本分析测试-直接人工', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128802, 128601, 30, 20, '2026-05', 3600.000000, '项目成本分析测试-折旧分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128803, 128601, 40, 20, '2026-05', 2450.000000, '项目成本分析测试-电费分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128804, 128601, 50, 10, '2026-05', 1180.000000, '项目成本分析测试-其他制造费用', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128805, 128602, 20, 10, '2026-05', 7200.000000, '项目成本分析测试-直接人工', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128806, 128602, 30, 20, '2026-05', 2100.000000, '项目成本分析测试-折旧分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128807, 128602, 40, 20, '2026-05', 1380.000000, '项目成本分析测试-电费分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128808, 128602, 50, 10, '2026-05', 680.000000, '项目成本分析测试-其他制造费用', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128809, 128603, 20, 10, '2026-05', 8600.000000, '项目成本分析测试-直接人工', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128810, 128603, 30, 20, '2026-05', 2600.000000, '项目成本分析测试-折旧分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128811, 128603, 40, 20, '2026-05', 1760.000000, '项目成本分析测试-电费分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128812, 128603, 50, 10, '2026-05', 940.000000, '项目成本分析测试-其他制造费用', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128813, 128604, 20, 10, '2026-05', 5200.000000, '项目成本分析测试-直接人工', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128814, 128604, 30, 20, '2026-05', 1350.000000, '项目成本分析测试-折旧分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128815, 128604, 40, 20, '2026-05', 980.000000, '项目成本分析测试-电费分摊', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128816, 128604, 50, 10, '2026-05', 420.000000, '项目成本分析测试-其他制造费用', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
production_order_id = VALUES(production_order_id),
cost_type = VALUES(cost_type),
source_type = VALUES(source_type),
accounting_month = VALUES(accounting_month),
amount = VALUES(amount),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_cost_allocation
(id, allocation_no, accounting_month, cost_type, rule_id, total_amount, status, executed_time, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128851, 'PCA-COST-202605-001', '2026-05', 30, NULL, 5700.000000, 20, '2026-05-26 08:40:00', '项目成本分析测试-折旧分摊单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128852, 'PCA-COST-202605-002', '2026-05', 40, NULL, 4570.000000, 20, '2026-05-26 08:50:00', '项目成本分析测试-电费分摊单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
allocation_no = VALUES(allocation_no),
accounting_month = VALUES(accounting_month),
cost_type = VALUES(cost_type),
rule_id = VALUES(rule_id),
total_amount = VALUES(total_amount),
status = VALUES(status),
executed_time = VALUES(executed_time),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_cost_allocation_result
(id, allocation_id, production_order_id, basis_value, basis_ratio, allocated_amount, generated_cost_entry_id, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(128861, 128851, 128601, 80.500000, 0.500000, 3600.000000, 128802, '项目成本分析测试-折旧分摊结果 A-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128862, 128851, 128602, 26.000000, 0.161491, 2100.000000, 128806, '项目成本分析测试-折旧分摊结果 A-2', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128863, 128852, 128601, 80.500000, 0.500000, 2450.000000, 128803, '项目成本分析测试-电费分摊结果 A-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128864, 128852, 128602, 26.000000, 0.161491, 1380.000000, 128807, '项目成本分析测试-电费分摊结果 A-2', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128865, 128851, 128603, 31.500000, 0.195652, 2600.000000, 128810, '项目成本分析测试-折旧分摊结果 B-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128866, 128852, 128603, 31.500000, 0.195652, 1760.000000, 128811, '项目成本分析测试-电费分摊结果 B-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128867, 128851, 128604, 34.500000, 0.214286, 1350.000000, 128814, '项目成本分析测试-折旧分摊结果 C-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(128868, 128852, 128604, 34.500000, 0.214286, 980.000000, 128815, '项目成本分析测试-电费分摊结果 C-1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
allocation_id = VALUES(allocation_id),
production_order_id = VALUES(production_order_id),
basis_value = VALUES(basis_value),
basis_ratio = VALUES(basis_ratio),
allocated_amount = VALUES(allocated_amount),
generated_cost_entry_id = VALUES(generated_cost_entry_id),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
