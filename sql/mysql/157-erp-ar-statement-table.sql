-- ============================================================
-- 应收台账模块建表脚本
-- 日期：2026-06-23
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- -----------------------------------------------------------
-- 应收台账主表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `erp_ar_statement`;
CREATE TABLE `erp_ar_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `statement_no` VARCHAR(64) NOT NULL COMMENT '台账编号',
  `biz_type` TINYINT NOT NULL COMMENT '业务类型：21=销售出库 22=销售退货',
  `biz_id` BIGINT NOT NULL COMMENT '业务单据ID',
  `biz_no` VARCHAR(64) DEFAULT NULL COMMENT '业务单据号',
  `source_order_id` BIGINT DEFAULT NULL COMMENT '来源销售订单ID',
  `source_order_no` VARCHAR(64) DEFAULT NULL COMMENT '来源销售订单号',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `account_id` BIGINT DEFAULT NULL COMMENT '结算账户ID',
  `amount` DECIMAL(19,4) NOT NULL DEFAULT 0 COMMENT '应收金额',
  `received_amount` DECIMAL(19,4) NOT NULL DEFAULT 0 COMMENT '已收金额',
  `remain_amount` DECIMAL(19,4) NOT NULL DEFAULT 0 COMMENT '剩余金额',
  `currency_code` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `biz_date` DATE NOT NULL COMMENT '业务日期',
  `due_date` DATE DEFAULT NULL COMMENT '到期日期',
  `invoice_status` TINYINT DEFAULT 0 COMMENT '开票状态：0=未开票 1=部分开票 2=已开票',
  `invoice_no` VARCHAR(64) DEFAULT NULL COMMENT '发票号',
  `invoice_amount` DECIMAL(19,4) DEFAULT 0 COMMENT '已开票金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待收 1=部分收 2=已结清 3=已关闭',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_statement_no` (`statement_no`),
  KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_source_order_id` (`source_order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_biz_date` (`biz_date`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应收台账';

-- -----------------------------------------------------------
-- 应收台账明细表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `erp_ar_statement_item`;
CREATE TABLE `erp_ar_statement_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `statement_id` BIGINT NOT NULL COMMENT '台账ID',
  `item_type` TINYINT NOT NULL COMMENT '明细类型：1=应收 2=收款分配 3=收款退回',
  `ref_type` TINYINT DEFAULT NULL COMMENT '关联类型：21=销售出库 22=销售退货 31=收款单',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联单据ID',
  `ref_no` VARCHAR(64) DEFAULT NULL COMMENT '关联单据号',
  `amount` DECIMAL(19,4) NOT NULL DEFAULT 0 COMMENT '金额',
  `after_received_amount` DECIMAL(19,4) DEFAULT NULL COMMENT '操作后已收金额',
  `after_remain_amount` DECIMAL(19,4) DEFAULT NULL COMMENT '操作后剩余金额',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_statement_id` (`statement_id`),
  KEY `idx_ref_type_ref_id` (`ref_type`, `ref_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应收台账明细';

-- -----------------------------------------------------------
-- 应收台账状态枚举说明
-- -----------------------------------------------------------
-- status: 0=待收 1=部分收 2=已结清 3=已关闭
-- biz_type: 21=销售出库 22=销售退货
-- item_type: 1=应收 2=收款分配 3=收款退回
-- ref_type: 21=销售出库 22=销售退货 31=收款单
-- invoice_status: 0=未开票 1=部分开票 2=已开票

SET FOREIGN_KEY_CHECKS = 1;
