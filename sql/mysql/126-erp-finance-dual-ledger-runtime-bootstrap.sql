-- =====================================================
-- ERP finance dual ledger runtime bootstrap
-- 1. 补齐双写运行表
-- 2. 补齐双写配置与账簿映射
-- 3. 为现有双账套测试样本补可重算日志
-- 4. 为费用报销样本补兼容型差异规则
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT tenant_id FROM erp_finance_ledger WHERE id = 99603 LIMIT 1
), 0);

-- =====================================================
-- 1. 双写运行表
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_dual_write_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `enable_status` INT NOT NULL DEFAULT 0 COMMENT '启用状态（1-启用，0-禁用）',
    `write_mode` INT NOT NULL DEFAULT 1 COMMENT '双写模式（1-同步，2-异步）',
    `exception_strategy` INT NOT NULL DEFAULT 1 COMMENT '异常处理策略（1-记录日志，2-抛出异常，3-自动重试）',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_dual_write_config_ledger` (`tenant_id`, `ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务双写配置';

CREATE TABLE IF NOT EXISTS `erp_finance_dual_write_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `source_voucher_id` BIGINT NOT NULL COMMENT '源凭证编号',
    `target_voucher_id` BIGINT NULL COMMENT '目标凭证编号',
    `source_ledger_id` BIGINT NOT NULL COMMENT '源账簿编号',
    `target_ledger_id` BIGINT NOT NULL COMMENT '目标账簿编号',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务单据编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '双写状态',
    `error_message` TEXT NULL COMMENT '错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_dual_write_log_source` (`tenant_id`, `source_voucher_id`, `deleted`),
    KEY `idx_dual_write_log_biz` (`tenant_id`, `biz_type`, `biz_id`, `deleted`),
    KEY `idx_dual_write_log_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双写日志';

-- =====================================================
-- 2. 双写基础配置
-- =====================================================

INSERT INTO `erp_finance_dual_write_config`
(`id`, `ledger_id`, `enable_status`, `write_mode`, `exception_strategy`, `max_retry_count`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126001, 99603, 1, 1, 1, 3, '双账套运行补齐-对外账启用双写', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `enable_status` = VALUES(`enable_status`),
    `write_mode` = VALUES(`write_mode`),
    `exception_strategy` = VALUES(`exception_strategy`),
    `max_retry_count` = VALUES(`max_retry_count`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

INSERT INTO `erp_finance_ledger_mapping`
(`id`, `external_ledger_id`, `internal_ledger_id`, `mapping_type`, `mapping_rule`, `status`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126002, 99603, 99604, 'DUAL_LEDGER', '{"mode":"external_to_internal"}', 0,
     '双账套运行补齐-对外账映射内部账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `internal_ledger_id` = VALUES(`internal_ledger_id`),
    `mapping_type` = VALUES(`mapping_type`),
    `mapping_rule` = VALUES(`mapping_rule`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

-- =====================================================
-- 3. 差异规则：先补费用报销样本可重算规则
-- 说明：
-- - 当前费用报销测试样本使用科目 6601/1002
-- - 为兼容历史实现，这里直接写入 legacy 科目编码 6601
-- - 新代码会优先按成本项语义匹配；遇到非标准成本项枚举时回退到旧科目编码匹配
-- =====================================================

INSERT INTO `erp_finance_dual_ledger_diff_config`
(`id`, `biz_type`, `diff_item_type`, `external_source_type`, `external_source_value`,
 `internal_source_type`, `internal_source_value`, `calculation_type`, `ratio`, `fixed_amount`,
 `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126003, 40, 50, 10, 6601, 10, 6601, 2, NULL, 40.00,
     0, '双账套运行补齐-费用报销样本使用 legacy 科目编码 6601 固定差额 40.00', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `external_source_type` = VALUES(`external_source_type`),
    `external_source_value` = VALUES(`external_source_value`),
    `internal_source_type` = VALUES(`internal_source_type`),
    `internal_source_value` = VALUES(`internal_source_value`),
    `calculation_type` = VALUES(`calculation_type`),
    `ratio` = VALUES(`ratio`),
    `fixed_amount` = VALUES(`fixed_amount`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

-- =====================================================
-- 4. 补两套账簿自动凭证模板，使 recompute 可真正删旧重生
-- 说明：
-- - 必须按账簿维度补模板，避免 /recompute 删除旧凭证后无法重新生成
-- - 模板分录统一使用“业务金额”，确保借贷自动平衡
-- =====================================================

INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`,
 `research_category`, `research_template`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126021, 99603, 11, '对外账-采购入库自动凭证模板', 0, b'1', '采购入库自动生成凭证', '双账套运行补齐-对外账采购入库模板',
     NULL, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126022, 99604, 11, '内部账-采购入库自动凭证模板', 0, b'1', '采购入库自动生成凭证', '双账套运行补齐-内部账采购入库模板',
     NULL, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126023, 99603, 40, '对外账-费用报销自动凭证模板', 0, b'1', '费用报销自动生成凭证', '双账套运行补齐-对外账费用报销模板',
     NULL, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126024, 99604, 40, '内部账-费用报销自动凭证模板', 0, b'1', '费用报销自动生成凭证', '双账套运行补齐-内部账费用报销模板',
     NULL, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `ledger_id` = VALUES(`ledger_id`),
    `biz_type` = VALUES(`biz_type`),
    `name` = VALUES(`name`),
    `status` = VALUES(`status`),
    `auto_generate` = VALUES(`auto_generate`),
    `default_summary` = VALUES(`default_summary`),
    `remark` = VALUES(`remark`),
    `research_category` = VALUES(`research_category`),
    `research_template` = VALUES(`research_template`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`,
 `amount_source`, `amount_source_value`, `summary`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126031, 126021, 1, 10, '1405', '库存商品', 10, NULL, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126032, 126021, 2, 20, '2202', '应付账款', 10, NULL, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126033, 126022, 1, 10, '1405', '库存商品', 10, NULL, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126034, 126022, 2, 20, '2202', '应付账款', 10, NULL, '确认采购入库', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126035, 126023, 1, 10, '6601', '销售费用', 10, NULL, '费用报销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126036, 126023, 2, 20, '1002', '银行存款', 10, NULL, '费用报销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126037, 126024, 1, 10, '6601', '销售费用', 10, NULL, '费用报销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126038, 126024, 2, 20, '1002', '银行存款', 10, NULL, '费用报销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `template_id` = VALUES(`template_id`),
    `entry_no` = VALUES(`entry_no`),
    `entry_direction` = VALUES(`entry_direction`),
    `subject_code` = VALUES(`subject_code`),
    `subject_name` = VALUES(`subject_name`),
    `amount_source` = VALUES(`amount_source`),
    `amount_source_value` = VALUES(`amount_source_value`),
    `summary` = VALUES(`summary`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

-- =====================================================
-- 5. 为现有测试凭证补双写日志，使 recompute-by-biz 可直接运行
-- 状态：1=成功
-- =====================================================

INSERT INTO `erp_finance_dual_write_log`
(`id`, `source_voucher_id`, `target_voucher_id`, `source_ledger_id`, `target_ledger_id`,
 `biz_type`, `biz_id`, `status`, `error_message`, `retry_count`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (126011, 124001, 124002, 99603, 99604, 11, 981201, 1, NULL, 0,
     '双账套运行补齐-采购入库样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (126012, 124003, 124004, 99603, 99604, 40, 107006, 1, NULL, 0,
     '双账套运行补齐-费用报销样本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `source_voucher_id` = VALUES(`source_voucher_id`),
    `target_voucher_id` = VALUES(`target_voucher_id`),
    `source_ledger_id` = VALUES(`source_ledger_id`),
    `target_ledger_id` = VALUES(`target_ledger_id`),
    `biz_type` = VALUES(`biz_type`),
    `biz_id` = VALUES(`biz_id`),
    `status` = VALUES(`status`),
    `error_message` = VALUES(`error_message`),
    `retry_count` = VALUES(`retry_count`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`);

SELECT 'dual_write_config' AS section, COUNT(*) AS total
FROM `erp_finance_dual_write_config`
WHERE `deleted` = b'0'
UNION ALL
SELECT 'voucher_template', COUNT(*)
FROM `erp_finance_voucher_template`
WHERE `deleted` = b'0' AND `ledger_id` IN (99603, 99604) AND `biz_type` IN (11, 40)
UNION ALL
SELECT 'voucher_template_item', COUNT(*)
FROM `erp_finance_voucher_template_item`
WHERE `deleted` = b'0' AND `template_id` IN (126021, 126022, 126023, 126024)
UNION ALL
SELECT 'dual_write_log', COUNT(*)
FROM `erp_finance_dual_write_log`
WHERE `deleted` = b'0'
UNION ALL
SELECT 'dual_ledger_diff_config', COUNT(*)
FROM `erp_finance_dual_ledger_diff_config`
WHERE `deleted` = b'0' AND `biz_type` = 40
UNION ALL
SELECT 'ledger_mapping', COUNT(*)
FROM `erp_finance_ledger_mapping`
WHERE `deleted` = b'0' AND `external_ledger_id` = 99603;

SET FOREIGN_KEY_CHECKS = 1;
