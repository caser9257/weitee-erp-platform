-- 资产折旧和摊销模板的分录方向必须与 ErpFinanceVoucherEntryDirectionEnum 保持一致。
UPDATE erp_finance_voucher_template_item item
JOIN erp_finance_voucher_template template ON template.id = item.template_id
SET item.entry_direction = CASE item.entry_no
    WHEN 1 THEN 10
    WHEN 2 THEN 20
    ELSE item.entry_direction
END
WHERE template.deleted = b'0'
  AND item.deleted = b'0'
  AND template.biz_type IN (70, 71, 72)
  AND item.entry_direction IN (1, 2);
