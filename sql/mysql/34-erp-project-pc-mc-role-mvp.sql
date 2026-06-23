/*
 Target: ERP Project PC/MC + Role Task MVP
 Schema: ruoyi-vue-pro
 Date: 2026-04-13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Alter table for erp_project
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `material_controller_id` bigint NULL COMMENT ''物控负责人'' AFTER `plan_coordinator_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'material_controller_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `pc_status` varchar(32) NOT NULL DEFAULT ''PENDING'' COMMENT ''PC状态'' AFTER `risk_level`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'pc_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `mc_status` varchar(32) NOT NULL DEFAULT ''PENDING'' COMMENT ''MC状态'' AFTER `pc_status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'mc_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `pc_confirm_time` datetime NULL COMMENT ''PC确认时间'' AFTER `mc_status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'pc_confirm_time'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `mc_confirm_time` datetime NULL COMMENT ''MC确认时间'' AFTER `pc_confirm_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'mc_confirm_time'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `pc_remark` varchar(255) NULL COMMENT ''PC备注'' AFTER `mc_confirm_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'pc_remark'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `mc_remark` varchar(255) NULL COMMENT ''MC备注'' AFTER `pc_remark`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'mc_remark'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Table structure for erp_project_role_task
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_project_role_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `project_id` bigint NOT NULL COMMENT '项目编号',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码: PC / MC',
  `task_type` varchar(64) NOT NULL COMMENT '任务类型',
  `task_status` varchar(32) NOT NULL COMMENT '任务状态',
  `assignee_user_id` bigint DEFAULT NULL COMMENT '分配用户',
  `source_type` varchar(32) NOT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源编号',
  `summary` varchar(255) NOT NULL COMMENT '任务摘要',
  `due_time` datetime DEFAULT NULL COMMENT '期望完成时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '处理备注',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_project_role_status` (`project_id`, `role_code`, `task_status`),
  KEY `idx_assignee_status` (`assignee_user_id`, `task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 项目角色任务表';

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `project_id` bigint NOT NULL COMMENT ''项目编号'' AFTER `id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `role_code` varchar(32) NOT NULL COMMENT ''角色编码: PC / MC'' AFTER `project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'role_code'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `task_type` varchar(64) NOT NULL COMMENT ''任务类型'' AFTER `role_code`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'task_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `task_status` varchar(32) NOT NULL COMMENT ''任务状态'' AFTER `task_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'task_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `assignee_user_id` bigint DEFAULT NULL COMMENT ''分配用户'' AFTER `task_status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'assignee_user_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `source_type` varchar(32) NOT NULL COMMENT ''来源类型'' AFTER `assignee_user_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'source_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `source_id` bigint DEFAULT NULL COMMENT ''来源编号'' AFTER `source_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'source_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `summary` varchar(255) NOT NULL COMMENT ''任务摘要'' AFTER `source_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'summary'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `due_time` datetime DEFAULT NULL COMMENT ''期望完成时间'' AFTER `summary`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'due_time'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `finish_time` datetime DEFAULT NULL COMMENT ''完成时间'' AFTER `due_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'finish_time'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT ''处理备注'' AFTER `finish_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND COLUMN_NAME = 'remark'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD INDEX `idx_project_role_status` (`project_id`, `role_code`, `task_status`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND INDEX_NAME = 'idx_project_role_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project_role_task` ADD INDEX `idx_assignee_status` (`assignee_user_id`, `task_status`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project_role_task'
    AND INDEX_NAME = 'idx_assignee_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
