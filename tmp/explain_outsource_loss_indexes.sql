EXPLAIN SELECT *
FROM erp_outsource_loss_detail
WHERE tenant_id = 1 AND issue_batch_id IN (31, 32, 33) AND deleted = 0;

EXPLAIN SELECT *
FROM erp_outsource_loss_detail
WHERE tenant_id = 1 AND order_id = 1 AND deleted = 0;
