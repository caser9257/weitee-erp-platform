USE `ruoyi-vue-pro`;
SELECT id, product_id, warehouse_id, batch_no, inbound_time, total_qty, available_qty, purchase_source_batch_id, purchase_source_batch_no
FROM erp_stock_batch
WHERE product_id IN (99101, 99102, 99103, 3)
ORDER BY product_id, inbound_time, id;
