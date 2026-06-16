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
ALTER TABLE `erp_stock_check`
    ADD COLUMN `year_end_flag` BIT(1) DEFAULT b'0' COMMENT '是否年末盘点' AFTER `blind_count`,
    ADD COLUMN `voucher_id` BIGINT NULL COMMENT '生成的凭证ID' AFTER `year_end_flag`;

-- ============================================================
-- 3. 盘点单明细表扩展字段
-- ============================================================
-- 注意：现有字段 stockCount 已经是账面数量，actualCount 是实际数量
-- 我们需要增加差异金额字段
ALTER TABLE `erp_stock_check_item`
    ADD COLUMN `diff_amount` DECIMAL(20,2) NULL COMMENT '差异金额' AFTER `recount_diff`;

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
-- 将现有盘点单的 year_end_flag 设为 0（非年末盘点）
UPDATE `erp_stock_check` SET `year_end_flag` = b'0' WHERE `year_end_flag` IS NULL;

-- ============================================================
-- 6. 凭证模板配置（如果需要）
-- ============================================================
-- 盘点凭证模板（盘盈/盘亏统一进管理费用）
-- biz_type = 92 表示盘点凭证
INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (202, 1, 92, '盘点凭证模板', 0, b'1', '库存盘点', '盘点盘盈/盘亏自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
