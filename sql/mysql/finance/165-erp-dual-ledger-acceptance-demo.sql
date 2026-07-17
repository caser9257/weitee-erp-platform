-- P0 验收用双账演示样本：同一费用业务在两本账产生不同金额。

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `weitee-erp`;
START TRANSACTION;

SET @biz_type := 40;
SET @biz_id := 20260717001;
SET @biz_no := 'DUAL-DEMO-20260717-001';
SET @external_voucher_no := 'VOU-EXT-DUAL-DEMO-20260717-001';
SET @internal_voucher_no := 'VOU-INT-DUAL-DEMO-20260717-001';

INSERT INTO erp_finance_voucher (
    voucher_no, ledger_id, dept_id, period_id, template_id, biz_type, biz_id, biz_no,
    voucher_time, status, total_debit_amount, total_credit_amount,
    approve_user_id, approve_time, post_user_id, post_time,
    reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id,
    reverse_remark, remark, creator, create_time, updater, update_time, deleted
)
SELECT @external_voucher_no, 99603, NULL, 132012, 126023, @biz_type, @biz_id, @biz_no,
       '2026-07-17 10:00:00', 50, 1800.00, 1800.00,
       NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, 'ACCEPTANCE_DUAL_DEMO_EXTERNAL', 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher WHERE voucher_no = @external_voucher_no AND deleted = b'0'
);

INSERT INTO erp_finance_voucher (
    voucher_no, ledger_id, dept_id, period_id, template_id, biz_type, biz_id, biz_no,
    voucher_time, status, total_debit_amount, total_credit_amount,
    approve_user_id, approve_time, post_user_id, post_time,
    reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id,
    reverse_remark, remark, creator, create_time, updater, update_time, deleted
)
SELECT @internal_voucher_no, 99604, NULL, 132013, 126024, @biz_type, @biz_id, @biz_no,
       '2026-07-17 10:00:00', 50, 1200.00, 1200.00,
       NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, 'ACCEPTANCE_DUAL_DEMO_INTERNAL', 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher WHERE voucher_no = @internal_voucher_no AND deleted = b'0'
);

SET @external_voucher_id := (
    SELECT id FROM erp_finance_voucher WHERE voucher_no = @external_voucher_no AND deleted = b'0' LIMIT 1
);
SET @internal_voucher_id := (
    SELECT id FROM erp_finance_voucher WHERE voucher_no = @internal_voucher_no AND deleted = b'0' LIMIT 1
);

UPDATE erp_finance_voucher
SET total_debit_amount = 1800.00,
    total_credit_amount = 1800.00,
    updater = 'acceptance',
    update_time = NOW()
WHERE id = @external_voucher_id;

UPDATE erp_finance_voucher
SET total_debit_amount = 1200.00,
    total_credit_amount = 1200.00,
    updater = 'acceptance',
    update_time = NOW()
WHERE id = @internal_voucher_id;

INSERT INTO erp_finance_voucher_entry (
    voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
    creator, create_time, updater, update_time, deleted
)
SELECT @external_voucher_id, 1, 'ACCEPTANCE_DUAL_DEMO', '6601',
       COALESCE((SELECT subject_name FROM erp_finance_subject
                 WHERE ledger_id = 99603 AND subject_code = '6601' AND deleted = b'0' LIMIT 1), '6601'),
       1800.00, 0.00, 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher_entry WHERE voucher_id = @external_voucher_id AND entry_no = 1 AND deleted = b'0'
);

INSERT INTO erp_finance_voucher_entry (
    voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
    creator, create_time, updater, update_time, deleted
)
SELECT @external_voucher_id, 2, 'ACCEPTANCE_DUAL_DEMO', '1002',
       COALESCE((SELECT subject_name FROM erp_finance_subject
                 WHERE ledger_id = 99603 AND subject_code = '1002' AND deleted = b'0' LIMIT 1), '1002'),
       0.00, 1800.00, 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher_entry WHERE voucher_id = @external_voucher_id AND entry_no = 2 AND deleted = b'0'
);

INSERT INTO erp_finance_voucher_entry (
    voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
    creator, create_time, updater, update_time, deleted
)
SELECT @internal_voucher_id, 1, 'ACCEPTANCE_DUAL_DEMO', '6601',
       COALESCE((SELECT subject_name FROM erp_finance_subject
                 WHERE ledger_id = 99604 AND subject_code = '6601' AND deleted = b'0' LIMIT 1), '6601'),
       1200.00, 0.00, 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher_entry WHERE voucher_id = @internal_voucher_id AND entry_no = 1 AND deleted = b'0'
);

INSERT INTO erp_finance_voucher_entry (
    voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
    creator, create_time, updater, update_time, deleted
)
SELECT @internal_voucher_id, 2, 'ACCEPTANCE_DUAL_DEMO', '1002',
       COALESCE((SELECT subject_name FROM erp_finance_subject
                 WHERE ledger_id = 99604 AND subject_code = '1002' AND deleted = b'0' LIMIT 1), '1002'),
       0.00, 1200.00, 'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher_entry WHERE voucher_id = @internal_voucher_id AND entry_no = 2 AND deleted = b'0'
);

UPDATE erp_finance_voucher_entry
SET debit_amount = CASE entry_no WHEN 1 THEN 1800.00 ELSE 0.00 END,
    credit_amount = CASE entry_no WHEN 1 THEN 0.00 ELSE 1800.00 END,
    updater = 'acceptance',
    update_time = NOW()
WHERE voucher_id = @external_voucher_id
  AND entry_no IN (1, 2)
  AND deleted = b'0';

UPDATE erp_finance_voucher_entry
SET debit_amount = CASE entry_no WHEN 1 THEN 1200.00 ELSE 0.00 END,
    credit_amount = CASE entry_no WHEN 1 THEN 0.00 ELSE 1200.00 END,
    updater = 'acceptance',
    update_time = NOW()
WHERE voucher_id = @internal_voucher_id
  AND entry_no IN (1, 2)
  AND deleted = b'0';

INSERT INTO erp_finance_dual_write_log (
    source_voucher_id, target_voucher_id, source_ledger_id, target_ledger_id,
    biz_type, biz_id, status, error_message, retry_count, remark,
    creator, create_time, updater, update_time, deleted
)
SELECT @external_voucher_id, @internal_voucher_id, 99603, 99604,
       @biz_type, @biz_id, 1, NULL, 0, 'ACCEPTANCE_DUAL_DEMO',
       'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_dual_write_log
    WHERE source_voucher_id = @external_voucher_id
      AND target_voucher_id = @internal_voucher_id
      AND deleted = b'0'
);

INSERT INTO erp_finance_dual_ledger_amount_diff_log (
    source_voucher_id, target_voucher_id, diff_item_type, calculation_type,
    internal_amount, external_amount, diff_amount, ratio, fixed_amount,
    biz_type, biz_id, remark, creator, create_time, updater, update_time, deleted
)
SELECT @internal_voucher_id, @external_voucher_id, 50, 2,
       1200.00, 1800.00, -600.00, NULL, -600.00,
       @biz_type, @biz_id, 'ACCEPTANCE_DUAL_DEMO',
       'acceptance', NOW(), 'acceptance', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM erp_finance_dual_ledger_amount_diff_log
    WHERE source_voucher_id = @internal_voucher_id
      AND target_voucher_id = @external_voucher_id
      AND biz_type = @biz_type
      AND biz_id = @biz_id
      AND deleted = b'0'
);

UPDATE erp_finance_dual_ledger_amount_diff_log
SET internal_amount = 1200.00,
    external_amount = 1800.00,
    diff_amount = -600.00,
    fixed_amount = -600.00,
    updater = 'acceptance',
    update_time = NOW()
WHERE source_voucher_id = @internal_voucher_id
  AND target_voucher_id = @external_voucher_id
  AND biz_type = @biz_type
  AND biz_id = @biz_id
  AND deleted = b'0';

COMMIT;

SELECT voucher_no, ledger_id, biz_type, biz_id, biz_no, total_debit_amount, total_credit_amount
FROM erp_finance_voucher
WHERE voucher_no IN (@external_voucher_no, @internal_voucher_no)
  AND deleted = b'0'
ORDER BY ledger_id;

SELECT source_voucher_id, target_voucher_id, internal_amount, external_amount, diff_amount
FROM erp_finance_dual_ledger_amount_diff_log
WHERE biz_type = @biz_type AND biz_id = @biz_id AND deleted = b'0';
