-- 双账套结果页最小测试数据
-- 目标：
-- 1. 为双账套结果页补充可直接查询的外账/内账凭证
-- 2. 覆盖采购入库、费用报销两个已启用双账套业务类型
-- 3. 产生“一致”和“不一致”两类结果，便于页面验证

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT tenant_id FROM erp_finance_ledger WHERE id = 99603 LIMIT 1
), 1);

SET @finance_user_id := COALESCE((
    SELECT id FROM system_users WHERE nickname = '财务主管' AND deleted = b'0' ORDER BY id LIMIT 1
), 1);

SET @period_ext_202605 := COALESCE((
    SELECT id FROM erp_finance_period
    WHERE ledger_id = 99603 AND period_code = '2026-05' AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99625);

SET @period_int_202605 := COALESCE((
    SELECT id FROM erp_finance_period
    WHERE ledger_id = 99604 AND period_code = '2026-05' AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99635);

SET @tpl_purchase_in_ext := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE ledger_id = 99603 AND biz_type = 11 AND deleted = b'0'
    ORDER BY id LIMIT 1
), (
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 11 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99821);

SET @tpl_purchase_in_int := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE ledger_id = 99604 AND biz_type = 11 AND deleted = b'0'
    ORDER BY id LIMIT 1
), @tpl_purchase_in_ext);

SET @tpl_expense_ext := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE ledger_id = 99603 AND biz_type = 40 AND deleted = b'0'
    ORDER BY id LIMIT 1
), (
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 40 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99822);

SET @tpl_expense_int := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE ledger_id = 99604 AND biz_type = 40 AND deleted = b'0'
    ORDER BY id LIMIT 1
), @tpl_expense_ext);

INSERT INTO erp_finance_voucher
(id, voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status,
 total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time,
 reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id, reverse_remark, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(124001, 'DL-EXT-PI-202605-001', 99603, @period_ext_202605, @tpl_purchase_in_ext, 11, 981201, 'PI-FINENH-20260525-001', '2026-05-25 16:00:00', 20,
 3200.00, 3200.00, @finance_user_id, '2026-05-25 16:10:00', @finance_user_id, '2026-05-25 16:20:00',
 NULL, NULL, NULL, NULL, NULL, '双账套测试-采购入库-外账一致样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124002, 'DL-INT-PI-202605-001', 99604, @period_int_202605, @tpl_purchase_in_int, 11, 981201, 'PI-FINENH-20260525-001', '2026-05-25 16:00:00', 20,
 3200.00, 3200.00, @finance_user_id, '2026-05-25 16:10:00', @finance_user_id, '2026-05-25 16:20:00',
 NULL, NULL, NULL, NULL, NULL, '双账套测试-采购入库-内账一致样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124003, 'DL-EXT-EXP-202605-001', 99603, @period_ext_202605, @tpl_expense_ext, 40, 107006, 'EXP-202605-002', '2026-05-26 16:00:00', 20,
 760.00, 760.00, @finance_user_id, '2026-05-26 16:10:00', @finance_user_id, '2026-05-26 16:20:00',
 NULL, NULL, NULL, NULL, NULL, '双账套测试-费用报销-外账差异样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124004, 'DL-INT-EXP-202605-001', 99604, @period_int_202605, @tpl_expense_int, 40, 107006, 'EXP-202605-002', '2026-05-26 16:00:00', 20,
 800.00, 800.00, @finance_user_id, '2026-05-26 16:10:00', @finance_user_id, '2026-05-26 16:20:00',
 NULL, NULL, NULL, NULL, NULL, '双账套测试-费用报销-内账差异样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
(124101, 124001, 1, '双账套测试采购入库外账', '1405', '库存商品', 3200.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124102, 124001, 2, '双账套测试采购入库外账', '2202', '应付账款', 0.00, 3200.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124103, 124002, 1, '双账套测试采购入库内账', '1405', '库存商品', 3200.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124104, 124002, 2, '双账套测试采购入库内账', '2202', '应付账款', 0.00, 3200.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124105, 124003, 1, '双账套测试费用报销外账', '6601', '销售费用', 760.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124106, 124003, 2, '双账套测试费用报销外账', '1002', '银行存款', 0.00, 760.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124107, 124004, 1, '双账套测试费用报销内账', '6601', '销售费用', 800.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(124108, 124004, 2, '双账套测试费用报销内账', '1002', '银行存款', 0.00, 800.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

SELECT id, voucher_no, ledger_id, biz_type, biz_id, biz_no, total_debit_amount, total_credit_amount
FROM erp_finance_voucher
WHERE deleted = b'0'
  AND id IN (124001, 124002, 124003, 124004)
ORDER BY id;

SET FOREIGN_KEY_CHECKS = 1;
