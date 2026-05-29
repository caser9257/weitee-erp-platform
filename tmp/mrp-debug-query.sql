SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME IN ('erp_finance_ledger', 'erp_finance_period')
ORDER BY TABLE_NAME;

SELECT INDEX_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'erp_finance_ledger'
  AND INDEX_NAME = 'uk_finance_ledger_no';

SELECT INDEX_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ruoyi-vue-pro'
  AND TABLE_NAME = 'erp_finance_period'
  AND INDEX_NAME = 'uk_finance_period_ledger_sort';
