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
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_no` (`tenant_id`, `no`, `deleted`),
  KEY `idx_finance_asset_status` (`tenant_id`, `status`, `deleted`),
  KEY `idx_finance_asset_candidate_id` (`tenant_id`, `candidate_id`, `deleted`),
  KEY `idx_finance_asset_source_item` (`tenant_id`, `source_type`, `source_biz_id`, `source_item_id`, `deleted`)
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
  'ALTER TABLE `erp_finance_asset` ADD INDEX `idx_finance_asset_candidate_id` (`tenant_id`, `candidate_id`, `deleted`)'
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
  'ALTER TABLE `erp_finance_asset` ADD INDEX `idx_finance_asset_source_item` (`tenant_id`, `source_type`, `source_biz_id`, `source_item_id`, `deleted`)'
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
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_candidate_source` (`tenant_id`, `source_type`, `source_biz_id`, `source_item_id`, `deleted`),
  KEY `idx_finance_asset_candidate_status` (`tenant_id`, `status`, `deleted`)
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
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_finance_asset_depreciation_period` (`tenant_id`, `asset_id`, `period`, `deleted`),
  KEY `idx_finance_asset_depreciation_period` (`tenant_id`, `period`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 固定资产折旧';
