SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME IN ('erp_product', 'erp_product_category', 'erp_product_unit', 'erp_bom', 'erp_bom_item')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
