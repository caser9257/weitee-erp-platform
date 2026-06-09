USE `ruoyi-vue-pro`;

SELECT DATABASE() AS db_name;

SELECT
  SUM(table_name IN (
    'erp_purchase_source_batch',
    'erp_stock_batch',
    'erp_purchase_in_stock_execute_item_batch',
    'erp_production_issue_batch'
  )) AS target_table_count
FROM information_schema.tables
WHERE table_schema = DATABASE();

SELECT
  COUNT(*) AS source_batch_count
FROM erp_purchase_source_batch
WHERE id IN (900401, 900402) AND deleted = b'0';

SELECT
  COUNT(*) AS stock_batch_count
FROM erp_stock_batch
WHERE id IN (900101, 900102) AND deleted = b'0';

SELECT
  COUNT(*) AS issue_batch_count
FROM erp_production_issue_batch
WHERE id IN (900011, 900012) AND deleted = b'0';

SELECT
  sb.id,
  sb.batch_no,
  sb.purchase_source_batch_id,
  psb.batch_no AS purchase_source_batch_no,
  sb.inbound_time
FROM erp_stock_batch sb
LEFT JOIN erp_purchase_source_batch psb ON psb.id = sb.purchase_source_batch_id
WHERE sb.id IN (900101, 900102)
ORDER BY sb.inbound_time, sb.id;

SELECT
  pib.id,
  pib.issue_item_id,
  pib.stock_batch_id,
  pib.batch_no,
  pib.issue_qty,
  pib.inbound_time
FROM erp_production_issue_batch pib
WHERE pib.issue_item_id = 900001
ORDER BY pib.inbound_time, pib.id;
