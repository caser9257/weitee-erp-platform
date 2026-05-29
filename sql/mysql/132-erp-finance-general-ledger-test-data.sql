-- =====================================================
-- ERP finance general ledger test data
-- 1. create ledger and accounting periods
-- 2. seed subjects, voucher templates, posted vouchers, entries and balances
-- 3. cover subject balance page and general ledger detail page
-- =====================================================

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

SET @ledger_id := 132001;
SET @period_prev_id := 132010;
SET @period_id := 132011;
SET @period_prev_sort := 202605;
SET @period_sort := 202606;

INSERT INTO erp_finance_ledger
(id, no, name, status, sort, default_status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(@ledger_id, 'LEDGER-GL-TEST-202606', '总账测试账簿', 0, 132, b'0', '总账页面测试数据', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(@period_prev_id, @ledger_id, '2026-05', 2026, 5, @period_prev_sort, '2026-05-01', '2026-05-31', 20, '2026-05-31 23:59:59', @finance_user_id, '总账测试前期', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(@period_id, @ledger_id, '2026-06', 2026, 6, @period_sort, '2026-06-01', '2026-06-30', 10, NULL, NULL, '总账测试期间', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(132101, @ledger_id, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132102, @ledger_id, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132103, @ledger_id, NULL, '1122', '应收账款', 10, 10, b'1', 0, 30, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132104, @ledger_id, NULL, '1403', '原材料', 10, 10, b'1', 0, 40, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132105, @ledger_id, NULL, '2202', '应付账款', 20, 20, b'1', 0, 50, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132106, @ledger_id, NULL, '4001', '实收资本', 30, 20, b'1', 0, 60, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132107, @ledger_id, NULL, '4103', '本年利润', 30, 20, b'1', 0, 70, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132108, @ledger_id, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 80, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132109, @ledger_id, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 90, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132110, @ledger_id, NULL, '6601', '销售费用', 60, 10, b'1', 0, 100, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132111, @ledger_id, NULL, '6602', '管理费用', 60, 10, b'1', 0, 110, '总账测试科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_finance_voucher_template
(id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(132301, @ledger_id, 11, '总账测试模板-采购入库', 0, b'0', '采购入库总账测试', '总账测试模板', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132302, @ledger_id, 21, '总账测试模板-销售出库', 0, b'0', '销售出库总账测试', '总账测试模板', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132303, @ledger_id, 40, '总账测试模板-费用报销', 0, b'0', '费用报销总账测试', '总账测试模板', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_finance_voucher
(id, voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status,
 total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time,
 reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id, reverse_remark, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(132401, 'GL-202606-001', @ledger_id, @period_id, 132301, 11, 132501, 'PI-GL-202606-001', '2026-06-05 09:30:00', 30,
 3200.000000, 3200.000000, @finance_user_id, '2026-06-05 10:00:00', @finance_user_id, '2026-06-05 10:10:00',
 NULL, NULL, NULL, NULL, NULL, '总账测试-采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132402, 'GL-202606-002', @ledger_id, @period_id, 132302, 21, 132502, 'SO-GL-202606-002', '2026-06-12 14:20:00', 30,
 13800.000000, 13800.000000, @finance_user_id, '2026-06-12 15:00:00', @finance_user_id, '2026-06-12 15:10:00',
 NULL, NULL, NULL, NULL, NULL, '总账测试-销售出库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132403, 'GL-202606-003', @ledger_id, @period_id, 132303, 40, 132503, 'EXP-GL-202606-003', '2026-06-18 11:00:00', 30,
 1200.000000, 1200.000000, @finance_user_id, '2026-06-18 11:30:00', @finance_user_id, '2026-06-18 11:40:00',
 NULL, NULL, NULL, NULL, NULL, '总账测试-费用报销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(132501, 132401, 1, '采购入库确认原材料', '1403', '原材料', 3200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132502, 132401, 2, '采购入库确认应付', '2202', '应付账款', 0.000000, 3200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132503, 132402, 1, '销售出库确认应收', '1122', '应收账款', 8600.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132504, 132402, 2, '销售出库确认收入', '6001', '主营业务收入', 0.000000, 8600.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132505, 132402, 3, '销售出库结转成本', '6401', '主营业务成本', 5200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132506, 132402, 4, '销售出库减少原材料', '1403', '原材料', 0.000000, 5200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132507, 132403, 1, '费用报销银行支付', '6601', '销售费用', 1200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132508, 132403, 2, '费用报销银行支付', '1002', '银行存款', 0.000000, 1200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

INSERT INTO erp_finance_subject_balance
(id, ledger_id, period_id, period_sort, subject_code, subject_name, opening_debit_amount, opening_credit_amount,
 current_debit_amount, current_credit_amount, ending_debit_amount, ending_credit_amount, creator, create_time,
 updater, update_time, deleted, tenant_id)
VALUES
(132201, @ledger_id, @period_prev_id, @period_prev_sort, '1001', '库存现金', 0.000000, 0.000000, 5000.000000, 0.000000, 5000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132202, @ledger_id, @period_prev_id, @period_prev_sort, '1002', '银行存款', 0.000000, 0.000000, 60000.000000, 0.000000, 60000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132203, @ledger_id, @period_prev_id, @period_prev_sort, '1122', '应收账款', 0.000000, 0.000000, 15000.000000, 0.000000, 15000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132204, @ledger_id, @period_prev_id, @period_prev_sort, '1403', '原材料', 0.000000, 0.000000, 30000.000000, 0.000000, 30000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132205, @ledger_id, @period_prev_id, @period_prev_sort, '2202', '应付账款', 0.000000, 0.000000, 0.000000, 10000.000000, 0.000000, 10000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132206, @ledger_id, @period_prev_id, @period_prev_sort, '4001', '实收资本', 0.000000, 0.000000, 0.000000, 80000.000000, 0.000000, 80000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132207, @ledger_id, @period_prev_id, @period_prev_sort, '4103', '本年利润', 0.000000, 0.000000, 0.000000, 2000.000000, 0.000000, 2000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132208, @ledger_id, @period_prev_id, @period_prev_sort, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132209, @ledger_id, @period_prev_id, @period_prev_sort, '6401', '主营业务成本', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132210, @ledger_id, @period_prev_id, @period_prev_sort, '6601', '销售费用', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132211, @ledger_id, @period_prev_id, @period_prev_sort, '6602', '管理费用', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132221, @ledger_id, @period_id, @period_sort, '1001', '库存现金', 5000.000000, 0.000000, 0.000000, 0.000000, 5000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132222, @ledger_id, @period_id, @period_sort, '1002', '银行存款', 60000.000000, 0.000000, 0.000000, 1200.000000, 58800.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132223, @ledger_id, @period_id, @period_sort, '1122', '应收账款', 15000.000000, 0.000000, 8600.000000, 0.000000, 23600.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132224, @ledger_id, @period_id, @period_sort, '1403', '原材料', 30000.000000, 0.000000, 3200.000000, 5200.000000, 28000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132225, @ledger_id, @period_id, @period_sort, '2202', '应付账款', 0.000000, 10000.000000, 0.000000, 3200.000000, 0.000000, 13200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132226, @ledger_id, @period_id, @period_sort, '4001', '实收资本', 0.000000, 80000.000000, 0.000000, 0.000000, 0.000000, 80000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132227, @ledger_id, @period_id, @period_sort, '4103', '本年利润', 0.000000, 2000.000000, 0.000000, 0.000000, 0.000000, 2000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132228, @ledger_id, @period_id, @period_sort, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 8600.000000, 0.000000, 8600.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132229, @ledger_id, @period_id, @period_sort, '6401', '主营业务成本', 0.000000, 0.000000, 5200.000000, 0.000000, 5200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132230, @ledger_id, @period_id, @period_sort, '6601', '销售费用', 0.000000, 0.000000, 1200.000000, 0.000000, 1200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(132231, @ledger_id, @period_id, @period_sort, '6602', '管理费用', 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

SELECT '总账测试数据已准备' AS message,
       @tenant_id AS tenant_id,
       @ledger_id AS ledger_id,
       @period_prev_id AS period_prev_id,
       @period_id AS period_id,
       @period_prev_sort AS period_prev_sort,
       @period_sort AS period_sort;

SET FOREIGN_KEY_CHECKS = 1;
