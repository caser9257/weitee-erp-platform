-- 应收台账账簿归属与审计角色过滤。
-- NULL 表示历史数据无法根据现有双套账配置确定归属；受限权限必须拒绝 NULL。

DROP PROCEDURE IF EXISTS `erp_add_ar_statement_ledger_id`;

DELIMITER //
CREATE PROCEDURE `erp_add_ar_statement_ledger_id`()
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_ar_statement'
    ) AND NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'erp_ar_statement'
          AND COLUMN_NAME = 'ledger_id'
    ) THEN
        ALTER TABLE `erp_ar_statement`
            ADD COLUMN `ledger_id` BIGINT NULL COMMENT 'owning finance ledger id' AFTER `customer_id`;
    END IF;

    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_ar_statement'
    ) AND NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'erp_ar_statement'
          AND INDEX_NAME = 'idx_ar_statement_ledger_status'
    ) THEN
        ALTER TABLE `erp_ar_statement`
            ADD KEY `idx_ar_statement_ledger_status` (`ledger_id`, `status`, `deleted`);
    END IF;
END//
DELIMITER ;

CALL `erp_add_ar_statement_ledger_id`();
DROP PROCEDURE `erp_add_ar_statement_ledger_id`;

UPDATE `erp_ar_statement` statement
SET statement.`ledger_id` = (
    SELECT config.`external_ledger_id`
    FROM `erp_finance_dual_ledger_config` config
    WHERE config.`biz_type` = statement.`biz_type`
      AND config.`status` = 0
      AND config.`deleted` = b'0'
    ORDER BY config.`id`
    LIMIT 1
)
WHERE statement.`ledger_id` IS NULL
  AND EXISTS (
    SELECT 1
    FROM `erp_finance_dual_ledger_config` config
    WHERE config.`biz_type` = statement.`biz_type`
      AND config.`status` = 0
      AND config.`deleted` = b'0'
  );
