/*
 * 产品主档与研发管理菜单一致性修复。
 *
 * 目的：
 * 1. 补齐当前 ErpProductDO 分页查询依赖的产品主表字段，修复历史库漏跑迁移导致的 SQL 500。
 * 2. 确保产品分页组装依赖的 Cadence 扩展表存在。
 * 3. 确保超级管理员拥有正式研发管理菜单及其现有子菜单/按钮权限。
 *
 * 说明：全部语句可重复执行，不删除或覆盖已有业务数据。
 */
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 产品主档分页字段 ========== 
SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'material_code'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `material_code` varchar(64) DEFAULT NULL COMMENT ''物料编码'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'packaging'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `packaging` varchar(255) DEFAULT NULL COMMENT ''产品封装'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'quality_grade'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `quality_grade` varchar(255) DEFAULT NULL COMMENT ''质量等级'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'brand_manufacturer'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `brand_manufacturer` varchar(255) DEFAULT NULL COMMENT ''品牌/制造商'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'alternative_model'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `alternative_model` varchar(255) DEFAULT NULL COMMENT ''替代型号'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'expiry_day'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `expiry_day` int DEFAULT NULL COMMENT ''保质期天数'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'batch_control_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `batch_control_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否批次管理'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'inspection_required_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `inspection_required_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否来料检验'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'weight'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `weight` decimal(24,6) DEFAULT NULL COMMENT ''重量'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'purchase_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `purchase_price` decimal(24,6) DEFAULT NULL COMMENT ''采购价格'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'sale_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `sale_price` decimal(24,6) DEFAULT NULL COMMENT ''销售价格'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'min_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `min_price` decimal(24,6) DEFAULT NULL COMMENT ''最低价格'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'mrp_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `mrp_enable` bit(1) NOT NULL DEFAULT b''1'' COMMENT ''是否参与MRP'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'asset_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `asset_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否固定资产候选'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @audit_status_added := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'audit_status'), 0, 1
);
SET @sql := IF(
  @audit_status_added = 1,
  'ALTER TABLE `erp_product` ADD COLUMN `audit_status` int NOT NULL DEFAULT 0 COMMENT ''审核状态''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'process_instance_id'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT ''BPM流程实例编号'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'is_pcb_component'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `is_pcb_component` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否PCB元器件'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND INDEX_NAME = 'idx_audit_status'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD INDEX `idx_audit_status` (`audit_status`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND INDEX_NAME = 'idx_process_instance_id'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD INDEX `idx_process_instance_id` (`process_instance_id`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 只对新加的审核字段做存量初始化，避免覆盖已有审核流状态。
SET @sql := IF(
  @audit_status_added = 1,
  'UPDATE `erp_product` SET `audit_status` = CASE WHEN `status` = 1 THEN 20 ELSE 0 END',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== 2. 产品分页组装依赖的 Cadence 扩展表 ========== 
CREATE TABLE IF NOT EXISTS `erp_product_cadence` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `schematic_part` varchar(255) DEFAULT NULL COMMENT '原理图库符号',
  `pcb_footprint` varchar(255) DEFAULT NULL COMMENT 'PCB封装',
  `cadence_description` varchar(1000) DEFAULT NULL COMMENT '关键参数描述',
  `manufacturer_part_number` varchar(255) DEFAULT NULL COMMENT '厂家型号',
  `dimension` varchar(255) DEFAULT NULL COMMENT '三维尺寸',
  `datasheet` varchar(1000) DEFAULT NULL COMMENT '数据手册地址或编号',
  `lifecycle` varchar(64) DEFAULT NULL COMMENT '生命周期',
  `preferred_part` bit(1) DEFAULT NULL COMMENT '是否优选',
  `operating_temperature` varchar(255) DEFAULT NULL COMMENT '工作温度',
  `mounting_type` varchar(64) DEFAULT NULL COMMENT '安装类型',
  `dnp` bit(1) DEFAULT NULL COMMENT '空置标志',
  `imported_or_replacement` varchar(64) DEFAULT NULL COMMENT '进口/替代物料',
  `second_description` varchar(1000) DEFAULT NULL COMMENT '参数描述2',
  `third_description` varchar(1000) DEFAULT NULL COMMENT '参数描述3',
  `fourth_description` varchar(1000) DEFAULT NULL COMMENT '参数描述4',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品Cadence扩展配置';

-- ========== 3. 正式研发管理菜单与超级管理员授权 ========== 
SET @rd_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/rd' AND `type` = 1 AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

-- 优先复活历史上被逻辑删除的正式研发根菜单，避免同一路径产生重复根节点。
SET @rd_deleted_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE @rd_root_id IS NULL
    AND `parent_id` = 0 AND `path` = '/rd' AND `type` = 1 AND `deleted` = b'1'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '研发管理', `permission` = '', `type` = 1, `sort` = 330, `parent_id` = 0,
    `path` = '/rd', `icon` = 'ep:cpu', `component` = '', `component_name` = 'FormalRdRoot',
    `status` = 0, `visible` = b'1', `keep_alive` = b'1', `always_show` = b'1',
    `deleted` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `id` = @rd_deleted_root_id;

SET @rd_root_id := COALESCE(@rd_root_id, @rd_deleted_root_id);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发管理', '', 1, 330, 0,
       '/rd', 'ep:cpu', '', 'FormalRdRoot', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_root_id IS NULL;

SET @rd_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/rd' AND `type` = 1 AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

-- 统一修复已存在根菜单的可见性和启用状态，确保授权后前端菜单树可展示。
UPDATE `system_menu`
SET `name` = '研发管理', `permission` = '', `type` = 1, `sort` = 330, `parent_id` = 0,
    `path` = '/rd', `icon` = 'ep:cpu', `component` = '', `component_name` = 'FormalRdRoot',
    `status` = 0, `visible` = b'1', `keep_alive` = b'1', `always_show` = b'1',
    `deleted` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `id` = @rd_root_id;

-- 恢复 190 号脚本可能已写入但被逻辑删除的 Cadence 菜单记录，并固定其父节点。
UPDATE `system_menu`
SET `parent_id` = @rd_root_id, `status` = 0, `visible` = b'1', `deleted` = b'0',
    `updater` = '1', `update_time` = NOW()
WHERE `permission` = 'erp:product:rd-cadence:query';

SET @super_admin_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'super_admin' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

-- 先复活历史软删除授权，再补齐根菜单、两级子菜单和按钮权限。
UPDATE `system_role_menu` rm
JOIN `system_menu` m ON m.`id` = rm.`menu_id`
SET rm.`deleted` = b'0', rm.`updater` = '1', rm.`update_time` = NOW()
WHERE rm.`role_id` = @super_admin_role_id
  AND m.`deleted` = b'0'
  AND (m.`id` = @rd_root_id
       OR m.`parent_id` = @rd_root_id
       OR m.`parent_id` IN (SELECT c.`id` FROM `system_menu` c WHERE c.`parent_id` = @rd_root_id AND c.`deleted` = b'0'));

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @super_admin_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE @super_admin_role_id IS NOT NULL
  AND m.`deleted` = b'0'
  AND (m.`id` = @rd_root_id
       OR m.`parent_id` = @rd_root_id
       OR m.`parent_id` IN (SELECT c.`id` FROM `system_menu` c WHERE c.`parent_id` = @rd_root_id AND c.`deleted` = b'0'))
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @super_admin_role_id AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
