SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @add_policy_line_component_role = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy_line'
        AND COLUMN_NAME = 'component_role'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_netting_policy_line`
       ADD COLUMN `component_role` varchar(32) DEFAULT NULL AFTER `component_code`'
  )
);
PREPARE stmt FROM @add_policy_line_component_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_mrp_netting_policy_line`
SET `component_role` = CASE `component_code`
  WHEN 'SAFETY_STOCK' THEN 'DEMAND_ADJUST'
  WHEN 'INCOMING_PURCHASE' THEN 'SUPPLY_CONSUME'
  WHEN 'WIP_PRODUCTION' THEN 'SUPPLY_CONSUME'
  WHEN 'ON_HAND_AVAILABLE' THEN 'SUPPLY_CONSUME'
  ELSE `component_role`
END
WHERE (`component_role` IS NULL OR `component_role` = '')
  AND `deleted` = b'0';

SET @add_result_component_role = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result_component'
        AND COLUMN_NAME = 'component_role'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result_component`
       ADD COLUMN `component_role` varchar(32) DEFAULT NULL AFTER `component_name`'
  )
);
PREPARE stmt FROM @add_result_component_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_mrp_result_component`
SET `component_role` = CASE `component_code`
  WHEN 'GROSS_DEMAND' THEN 'DEMAND_BASE'
  WHEN 'SAFETY_STOCK' THEN 'DEMAND_ADJUST'
  WHEN 'INCOMING_PURCHASE' THEN 'SUPPLY_CONSUME'
  WHEN 'WIP_PRODUCTION' THEN 'SUPPLY_CONSUME'
  WHEN 'ON_HAND_AVAILABLE' THEN 'SUPPLY_CONSUME'
  ELSE `component_role`
END
WHERE (`component_role` IS NULL OR `component_role` = '')
  AND `deleted` = b'0';

SET FOREIGN_KEY_CHECKS = 1;
