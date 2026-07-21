-- 年末库存盘点闭环 - 场景 A 实施
-- 包含：快照表、盘点单扩展字段、状态枚举扩展

-- ============================================================
-- 1. 新建盘点快照表
-- ============================================================
CREATE TABLE IF NOT EXISTS `erp_stock_check_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `check_id` BIGINT NOT NULL COMMENT '盘点单ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `book_qty` DECIMAL(20,6) NULL COMMENT '账面数量',
    `book_amount` DECIMAL(20,2) NULL COMMENT '账面金额',
    `average_cost` DECIMAL(20,6) NULL COMMENT '快照时加权平均单价',
    `snapshot_time` DATETIME NOT NULL COMMENT '快照时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_check_id` (`check_id`),
    INDEX `idx_product_warehouse` (`product_id`, `warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点快照表';

-- ============================================================
-- 2. 盘点单主表扩展字段
-- ============================================================
SET @stock_check_year_end_flag_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_check'
      AND COLUMN_NAME = 'year_end_flag'
);
SET @stock_check_year_end_flag_sql := IF(
    @stock_check_year_end_flag_exists > 0,
    'SELECT 1',
    'ALTER TABLE `erp_stock_check` ADD COLUMN `year_end_flag` BIT(1) DEFAULT b''0'' COMMENT ''是否年末盘点'''
);
PREPARE stock_check_year_end_flag_stmt FROM @stock_check_year_end_flag_sql;
EXECUTE stock_check_year_end_flag_stmt;
DEALLOCATE PREPARE stock_check_year_end_flag_stmt;

SET @stock_check_voucher_id_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_check'
      AND COLUMN_NAME = 'voucher_id'
);
SET @stock_check_voucher_id_sql := IF(
    @stock_check_voucher_id_exists > 0,
    'SELECT 1',
    'ALTER TABLE `erp_stock_check` ADD COLUMN `voucher_id` BIGINT NULL COMMENT ''生成的凭证ID'''
);
PREPARE stock_check_voucher_id_stmt FROM @stock_check_voucher_id_sql;
EXECUTE stock_check_voucher_id_stmt;
DEALLOCATE PREPARE stock_check_voucher_id_stmt;

-- ============================================================
-- 3. 盘点单明细表扩展字段
-- ============================================================
-- 注意：现有字段 stockCount 已经是账面数量，actualCount 是实际数量
-- 我们需要增加差异金额字段
SET @stock_check_diff_amount_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_check_item'
      AND COLUMN_NAME = 'diff_amount'
);
SET @stock_check_diff_amount_sql := IF(
    @stock_check_diff_amount_exists > 0,
    'SELECT 1',
    'ALTER TABLE `erp_stock_check_item` ADD COLUMN `diff_amount` DECIMAL(20,2) NULL COMMENT ''差异金额'''
);
PREPARE stock_check_diff_amount_stmt FROM @stock_check_diff_amount_sql;
EXECUTE stock_check_diff_amount_stmt;
DEALLOCATE PREPARE stock_check_diff_amount_stmt;

-- ============================================================
-- 4. 状态枚举扩展说明
-- ============================================================
-- 现有状态：PROCESS(0) = 草稿/进行中, APPROVE(10) = 已审核
-- 新状态设计：
--   DRAFT(0) = 草稿
--   COUNTING(10) = 盘点中
--   REVIEWING(20) = 审核中
--   APPROVED(30) = 已审核
--   CLOSED(40) = 已关闭
--
-- 注意：需要在 ErpAuditStatus 枚举中扩展这些状态

-- ============================================================
-- 5. 初始化历史数据
-- ============================================================
-- 5.1 将现有盘点单的 year_end_flag 设为 0（非年末盘点）
UPDATE `erp_stock_check` SET `year_end_flag` = b'0' WHERE `year_end_flag` IS NULL;

-- 5.2 状态码迁移：旧 ErpAuditStatus → 新 ErpStockCheckStatusEnum
-- 旧 PROCESS(10) = 未审核 → 新 DRAFT(0) = 草稿
-- 旧 APPROVE(20) = 已审核 → 新 CLOSED(40) = 已关闭
UPDATE `erp_stock_check` SET `status` = 0 WHERE `status` = 10;
UPDATE `erp_stock_check` SET `status` = 40 WHERE `status` = 20;

-- 盘亏成本以盘点单作为来源，不关联生产工单；历史表结构的生产工单列需要允许为空。
SET @production_cost_order_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_production_cost_entry'
      AND COLUMN_NAME = 'production_order_id'
);
SET @production_cost_order_nullable := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_production_cost_entry'
      AND COLUMN_NAME = 'production_order_id'
      AND IS_NULLABLE = 'YES'
);
SET @production_cost_order_sql := IF(
    @production_cost_order_column_exists = 0 OR @production_cost_order_nullable > 0,
    'SELECT 1',
    'ALTER TABLE `erp_production_cost_entry` MODIFY COLUMN `production_order_id` BIGINT NULL COMMENT ''production order id'''
);
PREPARE production_cost_order_stmt FROM @production_cost_order_sql;
EXECUTE production_cost_order_stmt;
DEALLOCATE PREPARE production_cost_order_stmt;

-- ============================================================
-- 6. 凭证模板配置
-- ============================================================
-- 盘点凭证模板（盘盈/盘亏统一进管理费用）
-- biz_type = 60 对应 ErpBizTypeEnum.STOCK_CHECK
SET @stock_check_ledger_id := (
    SELECT id
    FROM `erp_finance_ledger`
    WHERE `deleted` = b'0'
      AND `status` = 0
      AND `default_status` = b'1'
    ORDER BY id
    LIMIT 1
);
SET @stock_check_ledger_id := COALESCE(
    @stock_check_ledger_id,
    (SELECT id
     FROM `erp_finance_ledger`
     WHERE `deleted` = b'0'
       AND `status` = 0
     ORDER BY id
     LIMIT 1),
    1
);

SET @stock_check_template_id := (
    SELECT id
    FROM `erp_finance_voucher_template`
    WHERE `ledger_id` = @stock_check_ledger_id
      AND `biz_type` = 60
    ORDER BY `deleted`, id
    LIMIT 1
);
SET @stock_check_template_id := COALESCE(
    @stock_check_template_id,
    (SELECT COALESCE(MAX(id), 0) + 1 FROM `erp_finance_voucher_template`)
);

INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (@stock_check_template_id, @stock_check_ledger_id, 60, '盘点凭证模板', 0, b'1', '库存盘点', '盘点盘盈/盘亏自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `ledger_id` = VALUES(`ledger_id`),
  `biz_type` = VALUES(`biz_type`),
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `auto_generate` = VALUES(`auto_generate`),
  `default_summary` = VALUES(`default_summary`),
  `remark` = VALUES(`remark`),
  `deleted` = b'0';

UPDATE `erp_finance_voucher_template`
SET `ledger_id` = @stock_check_ledger_id
WHERE `id` = @stock_check_template_id;

SET @stock_check_management_subject_id := (
    SELECT id
    FROM `erp_finance_subject`
    WHERE `ledger_id` = @stock_check_ledger_id
      AND `subject_code` = '6602'
    ORDER BY `deleted`, id
    LIMIT 1
);
SET @stock_check_management_subject_id := COALESCE(
    @stock_check_management_subject_id,
    (SELECT COALESCE(MAX(id), 0) + 1 FROM `erp_finance_subject`)
);

INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (@stock_check_management_subject_id, @stock_check_ledger_id, NULL, '6602', '管理费用', 1, 1, b'1', 0, 0, '库存盘点差异费用科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `ledger_id` = VALUES(`ledger_id`),
  `subject_name` = VALUES(`subject_name`),
  `subject_type` = VALUES(`subject_type`),
  `balance_direction` = VALUES(`balance_direction`),
  `leaf` = VALUES(`leaf`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `deleted` = b'0';

UPDATE `erp_finance_subject`
SET `ledger_id` = @stock_check_ledger_id
WHERE `id` = @stock_check_management_subject_id;

SET @stock_check_debit_item_id := (
    SELECT id
    FROM `erp_finance_voucher_template_item`
    WHERE `template_id` = @stock_check_template_id
      AND `entry_no` = 1
    ORDER BY `deleted`, id
    LIMIT 1
);
SET @stock_check_debit_item_id := COALESCE(
    @stock_check_debit_item_id,
    (SELECT COALESCE(MAX(id), 0) + 1 FROM `erp_finance_voucher_template_item`)
);
SET @stock_check_credit_item_id := (
    SELECT id
    FROM `erp_finance_voucher_template_item`
    WHERE `template_id` = @stock_check_template_id
      AND `entry_no` = 2
    ORDER BY `deleted`, id
    LIMIT 1
);
SET @stock_check_credit_item_id := COALESCE(
    @stock_check_credit_item_id,
    GREATEST(
        (SELECT COALESCE(MAX(id), 0) + 1 FROM `erp_finance_voucher_template_item`),
        @stock_check_debit_item_id + 1
    )
);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
( @stock_check_debit_item_id, @stock_check_template_id, 1, 10, '6602', '管理费用', 10, NULL, '库存盘点', '1', NOW(), '1', NOW(), b'0'),
( @stock_check_credit_item_id, @stock_check_template_id, 2, 20, '1901', '待处理财产损溢', 10, NULL, '库存盘点', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `template_id` = VALUES(`template_id`),
  `entry_no` = VALUES(`entry_no`),
  `entry_direction` = VALUES(`entry_direction`),
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`),
  `amount_source` = VALUES(`amount_source`),
  `amount_source_value` = VALUES(`amount_source_value`),
  `summary` = VALUES(`summary`);
