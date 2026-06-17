-- =====================================================
-- 修复无形资产摊销凭证模板 ledger_id 不匹配问题
-- 问题：凭证模板 203/204/205 使用了 ledger_id=1，但实际默认账簿是其他 ID
-- 解决：动态获取默认账簿 ID，并为该账簿创建凭证模板和科目
-- =====================================================

-- 获取默认账簿 ID
SET @default_ledger_id := (
    SELECT id
    FROM erp_finance_ledger
    WHERE default_status = b'1'
      AND status = 0
      AND deleted = b'0'
    LIMIT 1
);

-- 如果没有默认账簿，使用 ID=1 作为兜底
SET @default_ledger_id := IFNULL(@default_ledger_id, 1);

-- 输出当前使用的账簿 ID（调试用）
SELECT @default_ledger_id AS '当前默认账簿ID';

-- =====================================================
-- 1. 为默认账簿创建科目（如果不存在）
-- =====================================================

-- 累计折旧科目
INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(160201, @default_ledger_id, NULL, '1602', '累计折旧', 1, 2, b'1', 0, 0, '固定资产折旧备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 累计摊销科目
INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(170201, @default_ledger_id, NULL, '1702', '累计摊销', 1, 2, b'1', 0, 0, '无形资产摊销备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 研发支出科目
INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(530101, @default_ledger_id, NULL, '5301', '研发支出', 1, 1, b'1', 0, 0, '研发无形资产摊销成本科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 管理费用科目（如果不存在）
INSERT INTO `erp_finance_subject`
(`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(660201, @default_ledger_id, NULL, '6602', '管理费用', 4, 1, b'1', 0, 0, '无形资产摊销费用科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- =====================================================
-- 2. 为默认账簿创建凭证模板（如果不存在）
-- =====================================================

-- 固定资产折旧凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(20301, @default_ledger_id, 70, '固定资产折旧凭证模板', 0, b'1', '固定资产折旧', '固定资产折旧自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(203101, 20301, 1, 1, '6602', '管理费用', 10, NULL, '固定资产折旧', '1', NOW(), '1', NOW(), b'0'),
(203201, 20301, 2, 2, '1602', '累计折旧', 10, NULL, '固定资产折旧', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

-- 无形资产摊销凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(20401, @default_ledger_id, 71, '无形资产摊销凭证模板', 0, b'1', '无形资产摊销', '无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(204101, 20401, 1, 1, '6602', '管理费用', 10, NULL, '无形资产摊销', '1', NOW(), '1', NOW(), b'0'),
(204201, 20401, 2, 2, '1702', '累计摊销', 10, NULL, '无形资产摊销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

-- 研发无形资产摊销凭证模板
INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(20501, @default_ledger_id, 72, '研发无形资产摊销凭证模板', 0, b'1', '研发无形资产摊销', '研发无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`, `amount_source`, `amount_source_value`, `summary`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(205101, 20501, 1, 1, '5301', '研发支出', 10, NULL, '研发无形资产摊销', '1', NOW(), '1', NOW(), b'0'),
(205201, 20501, 2, 2, '1702', '累计摊销', 10, NULL, '研发无形资产摊销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `subject_code` = VALUES(`subject_code`),
  `subject_name` = VALUES(`subject_name`);

-- =====================================================
-- 3. 验证插入结果
-- =====================================================

SELECT '凭证模板创建结果' AS '验证项',
    COUNT(*) AS '数量'
FROM erp_finance_voucher_template
WHERE biz_type IN (70, 71, 72)
  AND ledger_id = @default_ledger_id
  AND deleted = b'0';

SELECT '科目创建结果' AS '验证项',
    COUNT(*) AS '数量'
FROM erp_finance_subject
WHERE subject_code IN ('1602', '1702', '5301', '6602')
  AND ledger_id = @default_ledger_id
  AND deleted = b'0';
