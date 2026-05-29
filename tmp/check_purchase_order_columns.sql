SELECT COLUMN_NAME, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'erp_purchase_order_items'
  AND COLUMN_NAME IN ('engineering_fee', 'pricing_bom_id', 'pricing_bom_version');
