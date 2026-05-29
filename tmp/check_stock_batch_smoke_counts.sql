SELECT 'erp_stock_batch' AS table_name, COUNT(*) AS row_count FROM erp_stock_batch WHERE deleted = 0;
SELECT 'erp_stock_batch_record' AS table_name, COUNT(*) AS row_count FROM erp_stock_batch_record WHERE deleted = 0;
SELECT 'erp_stock_batch_allocation' AS table_name, COUNT(*) AS row_count FROM erp_stock_batch_allocation WHERE deleted = 0;
SELECT 'erp_stock_batch_reservation' AS table_name, COUNT(*) AS row_count FROM erp_stock_batch_reservation WHERE deleted = 0;
