-- 库存盘点自动凭证模板：盘亏记入盘点损失，贷记库存商品。
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO erp_finance_voucher_template
(ledger_id, biz_type, research_template, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted)
SELECT ledger.id, 60, b'0', '库存盘点自动凭证', 0, b'1', '库存盘点损益结转', '库存盘亏自动生成凭证', 'admin', NOW(), 'admin', NOW(), b'0'
FROM erp_finance_ledger ledger
WHERE ledger.status = 0
  AND ledger.default_status = b'1'
  AND ledger.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM erp_finance_voucher_template template
    WHERE template.ledger_id = ledger.id AND template.biz_type = 60 AND template.deleted = b'0'
  );
SET @template_id = (
  SELECT template.id FROM erp_finance_voucher_template template
  INNER JOIN erp_finance_ledger ledger ON ledger.id = template.ledger_id
  WHERE ledger.status = 0
    AND ledger.default_status = b'1'
    AND ledger.deleted = b'0'
    AND template.biz_type = 60
    AND template.deleted = b'0'
  ORDER BY template.id ASC LIMIT 1
);
INSERT INTO erp_finance_voucher_template_item
(template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary, creator, create_time, updater, update_time, deleted)
SELECT @template_id, 1, 10, '5001', '营业外支出', 10, NULL, '库存盘点盘亏', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @template_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM erp_finance_voucher_template_item WHERE template_id = @template_id AND entry_no = 1 AND deleted = b'0');
INSERT INTO erp_finance_voucher_template_item
(template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary, creator, create_time, updater, update_time, deleted)
SELECT @template_id, 2, 20, '1405', '库存商品', 10, NULL, '库存盘点盘亏', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @template_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM erp_finance_voucher_template_item WHERE template_id = @template_id AND entry_no = 2 AND deleted = b'0');
