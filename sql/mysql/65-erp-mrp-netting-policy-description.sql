SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @add_description_column = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy'
        AND COLUMN_NAME = 'description'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_netting_policy`
       ADD COLUMN `description` varchar(255) DEFAULT NULL AFTER `default_flag`'
  )
);
PREPARE stmt FROM @add_description_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sync_description_from_remark = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy'
        AND COLUMN_NAME = 'description'
    ) AND EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy'
        AND COLUMN_NAME = 'remark'
    ),
    'UPDATE `erp_mrp_netting_policy`
        SET `description` = `remark`
      WHERE (`description` IS NULL OR `description` = '''')
        AND `remark` IS NOT NULL
        AND `remark` <> ''''',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sync_description_from_remark;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
