SELECT id, no, supplier_id, account_id, order_id, order_no, total_price, payment_price, in_time, create_time, update_time, remark, tenant_id, deleted
FROM erp_purchase_in
WHERE id = 29;

SELECT id, name, tenant_id, deleted
FROM erp_supplier
WHERE id = 2;

SELECT id, name, tenant_id, deleted
FROM erp_account
WHERE id = 2;
