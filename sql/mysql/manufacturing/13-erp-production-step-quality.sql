-- 阶段4：工序质检与领料按工序归集
-- 1. 新表 erp_production_step_quality：工序质检单
-- 2. erp_production_order_step 增加 qc_flag：下达时从工艺路线工序复制质检要求（快照原则）
-- 3. erp_production_issue_item 增加 production_order_step_id：领料明细按工序归集

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 工序质检单
CREATE TABLE IF NOT EXISTS `erp_production_step_quality` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工序质检单编号',
  `no` varchar(64) NOT NULL COMMENT '质检单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `production_order_step_id` bigint NOT NULL COMMENT '工单工序编号',
  `report_id` bigint DEFAULT NULL COMMENT '来源报工单编号',
  `step_no` int DEFAULT NULL COMMENT '工序序号快照',
  `step_code` varchar(64) DEFAULT NULL COMMENT '工序编码快照',
  `step_name` varchar(128) DEFAULT NULL COMMENT '工序名称快照',
  `report_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '本次报工数量',
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '合格数量',
  `unqualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '不合格数量',
  `status` tinyint NOT NULL DEFAULT 10 COMMENT '状态：10待检 20部分合格 30全部合格 40全部不合格',
  `checker_user_id` bigint DEFAULT NULL COMMENT '质检人',
  `check_time` datetime DEFAULT NULL COMMENT '质检时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_step_quality_no` (`no`),
  KEY `idx_erp_step_quality_order_step` (`production_order_id`, `production_order_step_id`),
  KEY `idx_erp_step_quality_report_id` (`report_id`),
  KEY `idx_erp_step_quality_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工序质检单';

-- 2. 工单工序快照增加质检要求（下达时从工艺路线工序复制）
SET @qc_flag_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order_step' AND COLUMN_NAME = 'qc_flag'
);
SET @qc_flag_sql := IF(@qc_flag_exists = 0,
  'ALTER TABLE `erp_production_order_step` ADD COLUMN `qc_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否要求工序质检'' AFTER `device_id`',
  'SELECT 1');
PREPARE stmt FROM @qc_flag_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 领料明细按工序归集
SET @issue_step_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_issue_item' AND COLUMN_NAME = 'production_order_step_id'
);
SET @issue_step_sql := IF(@issue_step_exists = 0,
  'ALTER TABLE `erp_production_issue_item` ADD COLUMN `production_order_step_id` bigint DEFAULT NULL COMMENT ''工单工序编号'' AFTER `production_material_id`',
  'SELECT 1');
PREPARE stmt FROM @issue_step_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- 4. 工序质检菜单（二级，挂在质量管理下）
-- 说明：/manufacturing 旧目录已被信息架构吸收（前端 projectDrivenFlat 丢弃该根），
--       质检域菜单统一挂在 /qms（质量管理）下
SET @mf_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/qms' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mf_root_id := IFNULL(@mf_root_id, 930150);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3208, '工序质检', '', 2, 45, @mf_root_id, 'step-quality', 'ep:circle-check',
       'erp/manufacturing/step-quality/index', 'ErpStepQuality',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mf_root_id AND `path` = 'step-quality' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3281, '工序质检查询', 'erp:step-quality:query', 3, 1, 3208, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 3208 AND `permission` = 'erp:step-quality:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3282, '工序质检提交', 'erp:step-quality:submit', 3, 2, 3208, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 3208 AND `permission` = 'erp:step-quality:submit' AND `deleted` = b'0'
);

-- 5. 管理员与供应链经理角色授权
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 3208 AS menu_id
  UNION ALL SELECT 3281
  UNION ALL SELECT 3282
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SET @scm_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scm_role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 3208 AS menu_id
  UNION ALL SELECT 3281
) t
WHERE @scm_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @scm_role_id AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
  );

-- 验证
SELECT column_name, column_type, column_comment FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order_step' AND COLUMN_NAME = 'qc_flag';
SELECT column_name, column_type, column_comment FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_issue_item' AND COLUMN_NAME = 'production_order_step_id';
SELECT id, name, permission, type, sort, parent_id, path, component, component_name
FROM system_menu WHERE id IN (3208, 3281, 3282) AND deleted = 0;
