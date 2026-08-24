-- MES 排程模块第二阶段：任务优先级
-- 说明：priority 越小越优先，默认 0；自动排程排序 key = priority → 交期 → stepNo

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @priority_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mes_work_task' AND COLUMN_NAME = 'priority'
);
SET @priority_sql := IF(@priority_exists = 0,
  'ALTER TABLE `mes_work_task` ADD COLUMN `priority` int NOT NULL DEFAULT 0 COMMENT ''优先级（越小越优先）'' AFTER `plan_qty`',
  'SELECT 1');
PREPARE stmt FROM @priority_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

SELECT column_name, column_type, column_comment FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mes_work_task' AND COLUMN_NAME = 'priority';
