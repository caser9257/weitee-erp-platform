INSERT INTO erp_ap_statement (
    statement_no,
    biz_type,
    biz_id,
    biz_no,
    source_order_id,
    source_order_no,
    supplier_id,
    account_id,
    amount,
    paid_amount,
    remain_amount,
    currency_code,
    biz_date,
    due_date,
    invoice_status,
    status,
    remark,
    tenant_id
)
SELECT
    CONCAT('AP-11-', pi.no),
    11,
    pi.id,
    pi.no,
    pi.order_id,
    pi.order_no,
    pi.supplier_id,
    pi.account_id,
    pi.total_price,
    0,
    pi.total_price,
    'CNY',
    COALESCE(pi.in_time, pi.create_time, pi.update_time, NOW()),
    COALESCE(pi.in_time, pi.create_time, pi.update_time, NOW()),
    0,
    10,
    pi.remark,
    pi.tenant_id
FROM erp_purchase_in pi
WHERE pi.id = 29
  AND NOT EXISTS (
      SELECT 1
      FROM erp_ap_statement s
      WHERE s.biz_type = 11
        AND s.biz_id = pi.id
        AND s.deleted = b'0'
  );

INSERT INTO erp_ap_statement_item (
    statement_id,
    item_type,
    ref_type,
    ref_id,
    ref_no,
    amount,
    after_paid_amount,
    after_remain_amount,
    remark,
    tenant_id
)
SELECT
    s.id,
    10,
    s.biz_type,
    s.biz_id,
    s.biz_no,
    s.amount,
    s.paid_amount,
    s.remain_amount,
    'create statement',
    s.tenant_id
FROM erp_ap_statement s
WHERE s.biz_type = 11
  AND s.biz_id = 29
  AND s.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM erp_ap_statement_item i
      WHERE i.statement_id = s.id
        AND i.item_type = 10
        AND i.deleted = b'0'
  );

SELECT id, statement_no, biz_type, biz_id, supplier_id, account_id, amount, paid_amount, remain_amount, invoice_status, status, tenant_id
FROM erp_ap_statement
WHERE biz_type = 11
  AND biz_id = 29
  AND deleted = b'0';

SELECT id, statement_id, item_type, ref_type, ref_id, amount, after_paid_amount, after_remain_amount, tenant_id
FROM erp_ap_statement_item
WHERE statement_id IN (
    SELECT id
    FROM erp_ap_statement
    WHERE biz_type = 11
      AND biz_id = 29
      AND deleted = b'0'
)
  AND deleted = b'0';
