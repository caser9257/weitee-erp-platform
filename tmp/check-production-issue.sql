USE `ruoyi-vue-pro`;
SELECT COUNT(*) AS production_order_exists
FROM erp_production_order
WHERE id = 1 AND deleted = b'0';
SELECT id, order_no, status
FROM erp_production_order
ORDER BY id DESC
LIMIT 5;
SELECT id, issue_no, production_order_id, status, remark
FROM erp_production_issue
WHERE id = 900000 AND deleted = b'0';
