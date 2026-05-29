/*
 * ERP 财务报表最小闭环测试数据
 *
 * 用途：
 * - 解决财务报表页面选择账簿/期间后无数据显示的问题排查
 * - 为试算平衡表、资产负债表、利润表、现金流量表、科目余额提供可查询样本
 *
 * 使用方式：
 * - 先确保 86、89、90 号财务基础表结构脚本已执行
 * - 执行本脚本后，在前端“财务报表”页面选择：
 *   账簿：财务报表测试账簿
 *   期间：2026-05
 *
 * 说明：
 * - 本脚本幂等，可重复执行
 * - 只写固定测试编号 129000 段数据，不删除现有业务数据
 * - 报表接口依赖 erp_finance_subject_balance 与 erp_finance_report_item_subject 映射，
 *   仅有业务单据或凭证不一定能直接在财务报表页显示
 */

SET NAMES utf8mb4;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @ledger_id := 129001;
SET @period_id := 129011;
SET @period_sort := 202605;

INSERT INTO erp_finance_ledger
(id, no, name, status, sort, default_status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(@ledger_id, 'LEDGER-RPT-TEST-202605', '财务报表测试账簿', 0, 129, b'0', '财务报表页面测试数据', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
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
(id, ledger_id, period_code, period_year, period_month, period_sort, start_date, end_date, status, close_time, close_user_id, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(@period_id, @ledger_id, '2026-05', 2026, 5, @period_sort, '2026-05-01', '2026-05-31', 10, NULL, NULL, '财务报表测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    ledger_id = VALUES(ledger_id),
    period_code = VALUES(period_code),
    period_year = VALUES(period_year),
    period_month = VALUES(period_month),
    period_sort = VALUES(period_sort),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    status = VALUES(status),
    remark = VALUES(remark),
    updater = VALUES(updater),
    update_time = NOW(),
    deleted = VALUES(deleted),
    tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_subject
(id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(129101, @ledger_id, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129102, @ledger_id, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129103, @ledger_id, NULL, '1122', '应收账款', 10, 10, b'1', 0, 30, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129104, @ledger_id, NULL, '1403', '原材料', 10, 10, b'1', 0, 40, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129105, @ledger_id, NULL, '2202', '应付账款', 20, 20, b'1', 0, 50, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129106, @ledger_id, NULL, '4001', '实收资本', 30, 20, b'1', 0, 60, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129107, @ledger_id, NULL, '4103', '本年利润', 30, 20, b'1', 0, 70, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129108, @ledger_id, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 80, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129109, @ledger_id, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 90, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129110, @ledger_id, NULL, '6601', '销售费用', 60, 10, b'1', 0, 100, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129111, @ledger_id, NULL, '6602', '管理费用', 60, 10, b'1', 0, 110, '财务报表测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_finance_subject_balance
(id, ledger_id, period_id, period_sort, subject_code, subject_name, opening_debit_amount, opening_credit_amount, current_debit_amount, current_credit_amount, ending_debit_amount, ending_credit_amount, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(129201, @ledger_id, @period_id, @period_sort, '1001', '库存现金', 5000.000000, 0.000000, 12000.000000, 9000.000000, 8000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129202, @ledger_id, @period_id, @period_sort, '1002', '银行存款', 60000.000000, 0.000000, 78000.000000, 38000.000000, 100000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129203, @ledger_id, @period_id, @period_sort, '1122', '应收账款', 25000.000000, 0.000000, 42000.000000, 30000.000000, 37000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129204, @ledger_id, @period_id, @period_sort, '1403', '原材料', 20000.000000, 0.000000, 18000.000000, 12000.000000, 26000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129205, @ledger_id, @period_id, @period_sort, '2202', '应付账款', 0.000000, 28000.000000, 16000.000000, 23000.000000, 0.000000, 35000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129206, @ledger_id, @period_id, @period_sort, '4001', '实收资本', 0.000000, 80000.000000, 0.000000, 8000.000000, 0.000000, 88000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129207, @ledger_id, @period_id, @period_sort, '4103', '本年利润', 0.000000, 2000.000000, 8000.000000, 30000.000000, 0.000000, 24000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129208, @ledger_id, @period_id, @period_sort, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 88000.000000, 0.000000, 64000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129209, @ledger_id, @period_id, @period_sort, '6401', '主营业务成本', 0.000000, 0.000000, 48000.000000, 0.000000, 48000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129210, @ledger_id, @period_id, @period_sort, '6601', '销售费用', 0.000000, 0.000000, 12000.000000, 0.000000, 12000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129211, @ledger_id, @period_id, @period_sort, '6602', '管理费用', 0.000000, 0.000000, 4000.000000, 0.000000, 4000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_finance_report_item
(id, ledger_id, report_type, item_category, item_code, item_name, status, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(129301, @ledger_id, 10, 10, 'BS-CASH', '货币资金', 0, 10, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129302, @ledger_id, 10, 10, 'BS-AR', '应收账款', 0, 20, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129303, @ledger_id, 10, 10, 'BS-INVENTORY', '存货', 0, 30, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129304, @ledger_id, 10, 20, 'BS-AP', '应付账款', 0, 40, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129305, @ledger_id, 10, 30, 'BS-CAPITAL', '实收资本', 0, 50, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129306, @ledger_id, 10, 30, 'BS-PROFIT', '未分配利润', 0, 60, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129401, @ledger_id, 20, 40, 'IS-REVENUE', '营业收入', 0, 10, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129402, @ledger_id, 20, 50, 'IS-COST', '营业成本', 0, 20, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129403, @ledger_id, 20, 50, 'IS-SELLING-EXPENSE', '销售费用', 0, 30, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129404, @ledger_id, 20, 50, 'IS-MANAGE-EXPENSE', '管理费用', 0, 40, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129501, @ledger_id, 30, 60, 'CF-CASH-IN', '经营活动现金流入', 0, 10, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129502, @ledger_id, 30, 70, 'CF-CASH-OUT', '经营活动现金流出', 0, 20, '财务报表测试项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
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
(129601, 129301, '1001', 90, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129602, 129301, '1002', 90, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129603, 129302, '1122', 90, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129604, 129303, '1403', 90, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129605, 129304, '2202', 100, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129606, 129305, '4001', 100, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129607, 129306, '4103', 100, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129608, 129401, '6001', 80, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129609, 129402, '6401', 70, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129610, 129403, '6601', 70, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129611, 129404, '6602', 70, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129612, 129501, '1001', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129613, 129501, '1002', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129614, 129502, '1001', 40, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(129615, 129502, '1002', 40, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    subject_code = VALUES(subject_code),
    amount_rule = VALUES(amount_rule),
    amount_sign = VALUES(amount_sign),
    updater = VALUES(updater),
    update_time = NOW(),
    deleted = VALUES(deleted),
    tenant_id = VALUES(tenant_id);

SELECT '财务报表测试数据已准备' AS message,
       @tenant_id AS tenant_id,
       @ledger_id AS ledger_id,
       @period_id AS period_id,
       @period_sort AS period_sort;
