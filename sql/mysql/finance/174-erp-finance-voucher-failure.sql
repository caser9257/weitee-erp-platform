-- ============================================================
-- 凭证生成失败记录表（P3 失败重试最小闭环）
-- 日期：2026-09-07
-- 说明：业务审核通过但凭证生成失败时落库，支持查询、重试、人工确认；
--       不接入正式收付款制证（P5 范围）
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `erp_finance_voucher_failure` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型（ErpBizTypeEnum）',
    `biz_id` BIGINT NOT NULL COMMENT '业务单据编号',
    `error_message` VARCHAR(1000) NULL COMMENT '最近一次失败原因',
    `error_stack` VARCHAR(2000) NULL COMMENT '最近一次异常堆栈（截断）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `last_retry_time` DATETIME NULL COMMENT '最近一次重试时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待重试 1=重试成功 2=人工确认关闭',
    `confirm_reason` VARCHAR(500) NULL COMMENT '人工确认关闭原因',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_finance_voucher_failure_biz` (`biz_type`, `biz_id`, `deleted`),
    KEY `idx_finance_voucher_failure_status` (`status`, `deleted`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 凭证生成失败记录';

-- -----------------------------------------------------------
-- 权限按钮（挂在既有“财务凭证”页面菜单下，P9 再补独立页面菜单）
-- 幂等：按 permission 判重
-- -----------------------------------------------------------

SET @voucher_menu_id = (SELECT `id` FROM `system_menu`
    WHERE `component` = 'erp/finance/voucher/index' AND `deleted` = b'0' LIMIT 1);

INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
    `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '凭证失败查询', 'erp:finance-voucher-failure:query', 3, 10, @voucher_menu_id, '', '', '',
    0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM DUAL
WHERE @voucher_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-failure:query');

INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
    `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '凭证失败重试', 'erp:finance-voucher-failure:retry', 3, 11, @voucher_menu_id, '', '', '',
    0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM DUAL
WHERE @voucher_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-failure:retry');

INSERT INTO `system_menu` (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
    `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '凭证失败人工确认', 'erp:finance-voucher-failure:confirm', 3, 12, @voucher_menu_id, '', '', '',
    0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0'
FROM DUAL
WHERE @voucher_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-failure:confirm');
