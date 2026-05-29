SELECT id, no, status, supplier_id, account_id, total_price, discount_price, payment_price, tenant_id
FROM erp_finance_payment
WHERE id = 11;

SELECT id, payment_id, payment_item_id, ap_statement_id, allocate_amount, status, tenant_id
FROM erp_finance_payment_allocate
WHERE payment_id = 11
  AND deleted = b'0';

SELECT id, no, payment_price, tenant_id
FROM erp_purchase_in
WHERE id = 29;
