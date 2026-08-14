-- =====================================================
-- ERP finance asset phase1
-- 1. fixed asset master
-- 2. fixed asset candidate
-- 3. fixed asset depreciation
-- 4. explicit asset flags
-- =====================================================

SET @product_asset_flag_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_product'
    AND COLUMN_NAME = 'asset_flag'
);
SET @product_asset_flag_sql := IF(
  @product_asset_flag_exists > 0,
  'SELECT ''erp_product.asset_flag already exists''',
  'ALTER TABLE `erp_product` ADD COLUMN `asset_flag` BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''是否固定资产候选'' AFTER `min_price`'
);
PREPARE product_asset_flag_stmt FROM @product_asset_flag_sql;
EXECUTE product_asset_flag_stmt;
DEALLOCATE PREPARE product_asset_flag_stmt;

SET @expense_item_asset_flag_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_expense_item'
    AND COLUMN_NAME = 'asset_candidate_flag'
);
SET @expense_item_asset_flag_sql := IF(
  @expense_item_asset_flag_exists > 0,
  'SELECT ''erp_finance_expense_item.asset_candidate_flag already exists''',
  'ALTER TABLE `erp_finance_expense_item` ADD COLUMN `asset_candidate_flag` BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''是否转固定资产候选'' AFTER `remark`'
);
PREPARE expense_item_asset_flag_stmt FROM @expense_item_asset_flag_sql;
EXECUTE expense_item_asset_flag_stmt;
DEALLOCATE PREPARE expense_item_asset_flag_stmt;

CREATE TABLE IF NOT EXISTS `erp_finance_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `no` VARCHAR(64) NOT NULL COMMENT '资产编号',
  `name` VARCHAR(128) NOT NULL COMMENT '资产名称',
  `category_name` VARCHAR(64) NOT NULL COMMENT '资产分类',
  `candidate_id` BIGINT NULL COMMENT '来源候选记录编号',
  `source_type` INT NOT NULL DEFAULT 0 COMMENT '来源类型',
  `source_biz_id` BIGINT NULL COMMENT '来源业务编号',
  `source_biz_no` VARCHAR(64) NULL COMMENT '来源业务单号',
  `source_item_id` BIGINT NULL COMMENT '来源明细编号',
  `dept_id` BIGINT NULL COMMENT '部门编号',
  `responsible_user_id` BIGINT NULL COMMENT '责任人编号',
  `purchase_date` DATE NULL COMMENT '购置日期',
  `start_use_date` DATE NULL COMMENT '启用日期',
  `original_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '原值',
  `salvage_rate` DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT '残值率',
  `salvage_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '残值金额',
  `depreciation_method` VARCHAR(32) NOT NULL COMMENT '折旧方式',
  `depreciation_period_months` INT NOT NULL DEFAULT 0 COMMENT '折旧月数',
  `depreciation_start_period` VARCHAR(7) NOT NULL COMMENT '折旧起始期间',
  `depreciated_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '累计折旧',
  `current_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '净值',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
  `last_depreciation_period` VARCHAR(7) NULL COMMENT '最近计提期间',
  `remark` VARCHAR(255) NULL COMMENT '备注',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_no` (`no`, `deleted`),
  KEY `idx_finance_asset_status` (`status`, `deleted`),
  KEY `idx_finance_asset_candidate_id` (`candidate_id`, `deleted`),
  KEY `idx_finance_asset_source_item` (`source_type`, `source_biz_id`, `source_item_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 固定资产台账';

SET @finance_asset_candidate_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset'
    AND COLUMN_NAME = 'candidate_id'
);
SET @finance_asset_candidate_id_sql := IF(
  @finance_asset_candidate_id_exists > 0,
  'SELECT ''erp_finance_asset.candidate_id already exists''',
  'ALTER TABLE `erp_finance_asset` ADD COLUMN `candidate_id` BIGINT NULL COMMENT ''来源候选记录编号'' AFTER `category_name`'
);
PREPARE finance_asset_candidate_id_stmt FROM @finance_asset_candidate_id_sql;
EXECUTE finance_asset_candidate_id_stmt;
DEALLOCATE PREPARE finance_asset_candidate_id_stmt;

SET @finance_asset_source_item_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset'
    AND COLUMN_NAME = 'source_item_id'
);
SET @finance_asset_source_item_id_sql := IF(
  @finance_asset_source_item_id_exists > 0,
  'SELECT ''erp_finance_asset.source_item_id already exists''',
  'ALTER TABLE `erp_finance_asset` ADD COLUMN `source_item_id` BIGINT NULL COMMENT ''来源明细编号'' AFTER `source_biz_no`'
);
PREPARE finance_asset_source_item_id_stmt FROM @finance_asset_source_item_id_sql;
EXECUTE finance_asset_source_item_id_stmt;
DEALLOCATE PREPARE finance_asset_source_item_id_stmt;

SET @finance_asset_candidate_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset'
    AND INDEX_NAME = 'idx_finance_asset_candidate_id'
);
SET @finance_asset_candidate_idx_sql := IF(
  @finance_asset_candidate_idx_exists > 0,
  'SELECT ''idx_finance_asset_candidate_id already exists''',
  'ALTER TABLE `erp_finance_asset` ADD INDEX `idx_finance_asset_candidate_id` (`candidate_id`, `deleted`)'
);
PREPARE finance_asset_candidate_idx_stmt FROM @finance_asset_candidate_idx_sql;
EXECUTE finance_asset_candidate_idx_stmt;
DEALLOCATE PREPARE finance_asset_candidate_idx_stmt;

SET @finance_asset_source_item_idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset'
    AND INDEX_NAME = 'idx_finance_asset_source_item'
);
SET @finance_asset_source_item_idx_sql := IF(
  @finance_asset_source_item_idx_exists > 0,
  'SELECT ''idx_finance_asset_source_item already exists''',
  'ALTER TABLE `erp_finance_asset` ADD INDEX `idx_finance_asset_source_item` (`source_type`, `source_biz_id`, `source_item_id`, `deleted`)'
);
PREPARE finance_asset_source_item_idx_stmt FROM @finance_asset_source_item_idx_sql;
EXECUTE finance_asset_source_item_idx_stmt;
DEALLOCATE PREPARE finance_asset_source_item_idx_stmt;

CREATE TABLE IF NOT EXISTS `erp_finance_asset_candidate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `source_type` INT NOT NULL DEFAULT 0 COMMENT '来源类型',
  `source_biz_id` BIGINT NOT NULL COMMENT '来源业务编号',
  `source_biz_no` VARCHAR(64) NOT NULL COMMENT '来源业务单号',
  `source_item_id` BIGINT NULL COMMENT '来源明细编号',
  `product_id` BIGINT NULL COMMENT '产品编号',
  `asset_name` VARCHAR(128) NOT NULL COMMENT '候选资产名称',
  `category_name` VARCHAR(64) NULL COMMENT '候选资产分类',
  `amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '候选金额',
  `purchase_date` DATE NULL COMMENT '购置日期',
  `dept_id` BIGINT NULL COMMENT '部门编号',
  `responsible_user_id` BIGINT NULL COMMENT '责任人编号',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` VARCHAR(255) NULL COMMENT '备注',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_candidate_source` (`source_type`, `source_biz_id`, `source_item_id`, `deleted`),
  KEY `idx_finance_asset_candidate_status` (`status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 固定资产候选';

CREATE TABLE IF NOT EXISTS `erp_finance_asset_depreciation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
  `asset_id` BIGINT NOT NULL COMMENT '资产编号',
  `asset_no` VARCHAR(64) NOT NULL COMMENT '资产单号',
  `period` VARCHAR(7) NOT NULL COMMENT '期间',
  `depreciation_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '本期折旧',
  `before_depreciated_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '前累计折旧',
  `after_depreciated_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '后累计折旧',
  `before_current_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '前净值',
  `after_current_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '后净值',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` VARCHAR(255) NULL COMMENT '备注',
  `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_depreciation_period` (`asset_id`, `period`, `deleted`),
  KEY `idx_finance_asset_depreciation_period` (`period`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 固定资产折旧';

SET @finance_asset_cost_center_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset'
    AND COLUMN_NAME = 'cost_center_id'
);
SET @finance_asset_cost_center_id_sql := IF(
  @finance_asset_cost_center_id_exists > 0,
  'SELECT ''erp_finance_asset.cost_center_id already exists''',
  'ALTER TABLE `erp_finance_asset` ADD COLUMN `cost_center_id` BIGINT NULL COMMENT ''成本中心ID（部门ID）'' AFTER `sub_category`'
);
PREPARE finance_asset_cost_center_id_stmt FROM @finance_asset_cost_center_id_sql;
EXECUTE finance_asset_cost_center_id_stmt;
DEALLOCATE PREPARE finance_asset_cost_center_id_stmt;

SET @finance_asset_depreciation_voucher_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_asset_depreciation'
    AND COLUMN_NAME = 'voucher_id'
);
SET @finance_asset_depreciation_voucher_id_sql := IF(
  @finance_asset_depreciation_voucher_id_exists > 0,
  'SELECT ''erp_finance_asset_depreciation.voucher_id already exists''',
  'ALTER TABLE `erp_finance_asset_depreciation` ADD COLUMN `voucher_id` BIGINT NULL COMMENT ''生成的凭证ID'' AFTER `status`'
);
PREPARE finance_asset_depreciation_voucher_id_stmt FROM @finance_asset_depreciation_voucher_id_sql;
EXECUTE finance_asset_depreciation_voucher_id_stmt;
DEALLOCATE PREPARE finance_asset_depreciation_voucher_id_stmt;

-- 固定资产折旧凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (203, 1, 70, '固定资产折旧凭证模板', 0, b'1', '固定资产折旧', '固定资产折旧自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2031, 203, 1, 10, '6602', '管理费用', 10, NULL, '固定资产折旧', '1', NOW(), '1', NOW(), b'0'),
(2032, 203, 2, 20, '1602', '累计折旧', 10, NULL, '固定资产折旧', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `entry_direction` = VALUES(`entry_direction`),
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

-- 无形资产摊销凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (204, 1, 71, '无形资产摊销凭证模板', 0, b'1', '无形资产摊销', '无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2041, 204, 1, 10, '6602', '管理费用', 10, NULL, '无形资产摊销', '1', NOW(), '1', NOW(), b'0'),
(2042, 204, 2, 20, '1702', '累计摊销', 10, NULL, '无形资产摊销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `entry_direction` = VALUES(`entry_direction`),
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

-- 研发无形资产摊销凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (205, 1, 72, '研发无形资产摊销凭证模板', 0, b'1', '研发无形资产摊销', '研发无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(2051, 205, 1, 10, '5301', '研发支出', 10, NULL, '研发无形资产摊销', '1', NOW(), '1', NOW(), b'0'),
(2052, 205, 2, 20, '1702', '累计摊销', 10, NULL, '研发无形资产摊销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `entry_direction` = VALUES(`entry_direction`),
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1602, 1, NULL, '1602', '累计折旧', 1, 2, b'1', 0, 0, '固定资产折旧备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1702, 1, NULL, '1702', '累计摊销', 1, 2, b'1', 0, 0, '无形资产摊销备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5301, 1, NULL, '5301', '研发支出', 1, 1, b'1', 0, 0, '研发无形资产摊销成本科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 固定资产菜单和按钮权限收口
SET @asset_role_id := (
  SELECT id
  FROM system_role
  WHERE code = 'erp_finance_manager'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

SET @asset_finance_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      path = '/finance'
      OR component_name IN ('FormalFinanceRoot', 'ProjectFinanceRoot')
      OR name = '财务管理'
    )
  ORDER BY id
  LIMIT 1
);

SET @assets_menu_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/assets/index'
      OR component_name IN ('FormalFinanceAssets', 'ProjectFinanceAssets')
      OR path = 'assets'
    )
  ORDER BY id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = COALESCE(@asset_finance_root_id, parent_id),
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @assets_menu_id
  AND deleted = b'0';

SET @asset_query_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset:query'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产查询', 'erp:finance-asset:query', 3, 1, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_query_menu_id IS NULL;

SET @asset_create_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset:create'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产创建', 'erp:finance-asset:create', 3, 2, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_create_menu_id IS NULL;

SET @asset_update_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset:update'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产更新', 'erp:finance-asset:update', 3, 3, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_update_menu_id IS NULL;

SET @asset_delete_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset:delete'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产删除', 'erp:finance-asset:delete', 3, 4, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_delete_menu_id IS NULL;

SET @asset_status_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset:update-status'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产状态更新', 'erp:finance-asset:update-status', 3, 5, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_status_menu_id IS NULL;

SET @candidate_query_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset-candidate:query'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产候选查询', 'erp:finance-asset-candidate:query', 3, 6, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @candidate_query_menu_id IS NULL;

SET @candidate_confirm_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset-candidate:confirm'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产候选确认', 'erp:finance-asset-candidate:confirm', 3, 7, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @candidate_confirm_menu_id IS NULL;

SET @depreciation_query_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset-depreciation:query'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产折旧查询', 'erp:finance-asset-depreciation:query', 3, 8, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @depreciation_query_menu_id IS NULL;

SET @depreciation_generate_menu_id := (
  SELECT id FROM system_menu
  WHERE deleted = b'0'
    AND permission = 'erp:finance-asset-depreciation:generate'
  ORDER BY id
  LIMIT 1
);
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产折旧生成', 'erp:finance-asset-depreciation:generate', 3, 9, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @depreciation_generate_menu_id IS NULL;

UPDATE system_menu
SET parent_id = @assets_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:finance-asset:query',
    'erp:finance-asset:create',
    'erp:finance-asset:update',
    'erp:finance-asset:delete',
    'erp:finance-asset:update-status',
    'erp:finance-asset-candidate:query',
    'erp:finance-asset-candidate:confirm',
    'erp:finance-asset-depreciation:query',
    'erp:finance-asset-depreciation:generate'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT @asset_role_id, m.id, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE @asset_role_id IS NOT NULL
  AND m.deleted = b'0'
  AND (
    m.id = @assets_menu_id
    OR m.permission IN (
      'erp:finance-asset:query',
      'erp:finance-asset:create',
      'erp:finance-asset:update',
      'erp:finance-asset:delete',
      'erp:finance-asset:update-status',
      'erp:finance-asset-candidate:query',
      'erp:finance-asset-candidate:confirm',
      'erp:finance-asset-depreciation:query',
      'erp:finance-asset-depreciation:generate'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @asset_role_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
  );
