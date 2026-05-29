-- =====================================================
-- ERP 财务双账套生产成本样本
-- 目标：
-- 1. 补一条“同一业务源单 -> 内外两套财务记录”的生产成本样本
-- 2. 内部账保留真实成本，对外账按差异规则上浮人工/折旧/制造费用
-- 3. 用于验证双账套结果页、凭证明细和账簿权限隔离
--
-- 推荐前置脚本：
-- - sql/mysql/99-erp-finance-demo-data.sql
-- - sql/mysql/126-erp-finance-dual-ledger-runtime-bootstrap.sql
-- - 若要验证外部审计角色，再执行：
--   - sql/mysql/119-erp-finance-audit-role-seed.sql
--   - sql/mysql/120-erp-finance-audit-role-permissions.sql
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT tenant_id
    FROM erp_finance_ledger
    WHERE id IN (99603, 99604)
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
), (
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @external_ledger_id := 99603;
SET @internal_ledger_id := 99604;
SET @biz_type := 30;
SET @biz_id := 130001;
SET @biz_no := 'OSF-20260526-TEST-001';
SET @finance_user_id := COALESCE((
    SELECT id
    FROM system_users
    WHERE nickname = '财务主管'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @external_period_id := COALESCE((
    SELECT id
    FROM erp_finance_period
    WHERE ledger_id = @external_ledger_id
      AND period_code = '2026-05'
      AND deleted = b'0'
    LIMIT 1
), 127011);

SET @internal_period_id := COALESCE((
    SELECT id
    FROM erp_finance_period
    WHERE ledger_id = @internal_ledger_id
      AND period_code = '2026-05'
      AND deleted = b'0'
    LIMIT 1
), 127012);

INSERT INTO `erp_finance_period`
(`id`, `ledger_id`, `period_code`, `period_year`, `period_month`, `period_sort`,
 `start_date`, `end_date`, `status`, `close_time`, `close_user_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127011, @external_ledger_id, '2026-05', 2026, 5, 202605,
     '2026-05-01', '2026-05-31', 10, NULL, NULL, '双账套生产成本样本-对外账期间',
     'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127012, @internal_ledger_id, '2026-05', 2026, 5, 202605,
     '2026-05-01', '2026-05-31', 10, NULL, NULL, '双账套生产成本样本-内部账期间',
     'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `ledger_id` = VALUES(`ledger_id`),
    `period_code` = VALUES(`period_code`),
    `period_year` = VALUES(`period_year`),
    `period_month` = VALUES(`period_month`),
    `period_sort` = VALUES(`period_sort`),
    `start_date` = VALUES(`start_date`),
    `end_date` = VALUES(`end_date`),
    `status` = VALUES(`status`),
    `close_time` = VALUES(`close_time`),
    `close_user_id` = VALUES(`close_user_id`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_voucher_template`
(`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`,
 `research_category`, `research_template`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127051, @external_ledger_id, @biz_type, '对外账-委外加工费自动凭证模板', 0, b'1',
     '委外加工费自动生成对外账凭证', '双账套生产成本样本模板-对外账',
     NULL, b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127052, @internal_ledger_id, @biz_type, '内部账-委外加工费自动凭证模板', 0, b'1',
     '委外加工费自动生成内部账凭证', '双账套生产成本样本模板-内部账',
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
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_voucher_template_item`
(`id`, `template_id`, `entry_no`, `entry_direction`, `subject_code`, `subject_name`,
 `amount_source`, `amount_source_value`, `summary`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127061, 127051, 1, 10, '5001', '生产成本', 10, NULL, '委外加工生产成本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127062, 127051, 2, 20, '2202', '应付账款', 10, NULL, '委外加工生产成本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127063, 127052, 1, 10, '5001', '生产成本', 10, NULL, '委外加工生产成本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127064, 127052, 2, 20, '2202', '应付账款', 10, NULL, '委外加工生产成本', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_dual_ledger_config`
(`id`, `biz_type`, `external_ledger_id`, `internal_ledger_id`, `status`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127301, @biz_type, @external_ledger_id, @internal_ledger_id, 0,
     '双账套生产成本样本-委外加工费业务启用内外双账',
     'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `external_ledger_id` = VALUES(`external_ledger_id`),
    `internal_ledger_id` = VALUES(`internal_ledger_id`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_dual_ledger_diff_config`
(`id`, `biz_type`, `diff_item_type`, `external_source_type`, `external_source_value`,
 `internal_source_type`, `internal_source_value`, `calculation_type`, `ratio`, `fixed_amount`,
 `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127311, @biz_type, 20, 10, 20, 10, 20, 1, 1.1500, NULL,
     0, '双账套生产成本样本-人工成本按 115% 形成对外账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127312, @biz_type, 30, 20, NULL, 20, NULL, 2, NULL, -300.00,
     0, '双账套生产成本样本-折旧按固定上浮 300 形成对外账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127313, @biz_type, 60, 10, 60, 10, 60, 1, 1.1500, NULL,
     0, '双账套生产成本样本-制造费用按 115% 形成对外账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_voucher`
(`id`, `voucher_no`, `ledger_id`, `period_id`, `template_id`, `biz_type`, `biz_id`, `biz_no`,
 `voucher_time`, `status`, `total_debit_amount`, `total_credit_amount`,
 `approve_user_id`, `approve_time`, `post_user_id`, `post_time`,
 `reverse_user_id`, `reverse_time`, `reverse_voucher_id`, `reverse_from_voucher_id`, `reverse_remark`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127101, 'VOU-EXT-20260526-001', @external_ledger_id, @external_period_id, 127051, @biz_type, @biz_id, @biz_no,
     '2026-05-26 16:30:00', 20, 11300.00, 11300.00,
     @finance_user_id, '2026-05-26 17:00:00', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '双账套生产成本样本-对外账凭证', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127102, 'VOU-INT-20260526-001', @internal_ledger_id, @internal_period_id, 127052, @biz_type, @biz_id, @biz_no,
     '2026-05-26 16:30:00', 20, 9800.00, 9800.00,
     @finance_user_id, '2026-05-26 17:00:00', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '双账套生产成本样本-内部账凭证', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `voucher_no` = VALUES(`voucher_no`),
    `ledger_id` = VALUES(`ledger_id`),
    `period_id` = VALUES(`period_id`),
    `template_id` = VALUES(`template_id`),
    `biz_type` = VALUES(`biz_type`),
    `biz_id` = VALUES(`biz_id`),
    `biz_no` = VALUES(`biz_no`),
    `voucher_time` = VALUES(`voucher_time`),
    `status` = VALUES(`status`),
    `total_debit_amount` = VALUES(`total_debit_amount`),
    `total_credit_amount` = VALUES(`total_credit_amount`),
    `approve_user_id` = VALUES(`approve_user_id`),
    `approve_time` = VALUES(`approve_time`),
    `post_user_id` = VALUES(`post_user_id`),
    `post_time` = VALUES(`post_time`),
    `reverse_user_id` = VALUES(`reverse_user_id`),
    `reverse_time` = VALUES(`reverse_time`),
    `reverse_voucher_id` = VALUES(`reverse_voucher_id`),
    `reverse_from_voucher_id` = VALUES(`reverse_from_voucher_id`),
    `reverse_remark` = VALUES(`reverse_remark`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_voucher_entry`
(`id`, `voucher_id`, `entry_no`, `summary`, `subject_code`, `subject_name`,
 `debit_amount`, `credit_amount`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127201, 127101, 1, '委外加工人工成本', '5001', '生产成本', 5980.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127202, 127101, 2, '委外加工折旧分摊', '5001', '生产成本', 2100.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127203, 127101, 3, '委外加工制造费用', '5001', '生产成本', 3220.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127204, 127101, 4, '委外加工生产成本结转', '2202', '应付账款', 0.00, 11300.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127205, 127102, 1, '委外加工人工成本', '5001', '生产成本', 5200.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127206, 127102, 2, '委外加工折旧分摊', '5001', '生产成本', 1800.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127207, 127102, 3, '委外加工制造费用', '5001', '生产成本', 2800.00, 0.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127208, 127102, 4, '委外加工生产成本结转', '2202', '应付账款', 0.00, 9800.00, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `voucher_id` = VALUES(`voucher_id`),
    `entry_no` = VALUES(`entry_no`),
    `summary` = VALUES(`summary`),
    `subject_code` = VALUES(`subject_code`),
    `subject_name` = VALUES(`subject_name`),
    `debit_amount` = VALUES(`debit_amount`),
    `credit_amount` = VALUES(`credit_amount`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_finance_dual_ledger_amount_diff_log`
(`id`, `source_voucher_id`, `target_voucher_id`, `diff_item_type`, `calculation_type`,
 `internal_amount`, `external_amount`, `diff_amount`, `ratio`, `fixed_amount`,
 `biz_type`, `biz_id`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (127501, 127102, 127101, 20, 1, 5200.00, 5980.00, -780.00, 1.1500, NULL,
     @biz_type, @biz_id, '双账套生产成本样本-人工成本差异', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127502, 127102, 127101, 30, 2, 1800.00, 2100.00, -300.00, NULL, -300.00,
     @biz_type, @biz_id, '双账套生产成本样本-折旧差异', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (127503, 127102, 127101, 60, 1, 2800.00, 3220.00, -420.00, 1.1500, NULL,
     @biz_type, @biz_id, '双账套生产成本样本-制造费用差异', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    `source_voucher_id` = VALUES(`source_voucher_id`),
    `target_voucher_id` = VALUES(`target_voucher_id`),
    `diff_item_type` = VALUES(`diff_item_type`),
    `calculation_type` = VALUES(`calculation_type`),
    `internal_amount` = VALUES(`internal_amount`),
    `external_amount` = VALUES(`external_amount`),
    `diff_amount` = VALUES(`diff_amount`),
    `ratio` = VALUES(`ratio`),
    `fixed_amount` = VALUES(`fixed_amount`),
    `biz_type` = VALUES(`biz_type`),
    `biz_id` = VALUES(`biz_id`),
    `remark` = VALUES(`remark`),
    `updater` = VALUES(`updater`),
    `update_time` = NOW(),
    `deleted` = VALUES(`deleted`),
    `tenant_id` = VALUES(`tenant_id`);

SET @audit_role_id := (
    SELECT id
    FROM system_role
    WHERE code = 'finance_audit'
      AND deleted = b'0'
    LIMIT 1
);

SET @finance_manager_role_id := (
    SELECT id
    FROM system_role
    WHERE code = 'erp_finance_manager'
      AND deleted = b'0'
    LIMIT 1
);

INSERT INTO `erp_finance_ledger_role`
(`ledger_id`, `role_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @external_ledger_id, @audit_role_id, 0, '双账套生产成本样本-外部审计仅看对外账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
WHERE @audit_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `erp_finance_ledger_role`
      WHERE `ledger_id` = @external_ledger_id
        AND `role_id` = @audit_role_id
        AND `deleted` = b'0'
        AND `tenant_id` = @tenant_id
  );

INSERT INTO `erp_finance_ledger_role`
(`ledger_id`, `role_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @external_ledger_id, @finance_manager_role_id, 0, '双账套生产成本样本-财务主管可看对外账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
WHERE @finance_manager_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `erp_finance_ledger_role`
      WHERE `ledger_id` = @external_ledger_id
        AND `role_id` = @finance_manager_role_id
        AND `deleted` = b'0'
        AND `tenant_id` = @tenant_id
  );

INSERT INTO `erp_finance_ledger_role`
(`ledger_id`, `role_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @internal_ledger_id, @finance_manager_role_id, 0, '双账套生产成本样本-财务主管可看内部账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
WHERE @finance_manager_role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM `erp_finance_ledger_role`
      WHERE `ledger_id` = @internal_ledger_id
        AND `role_id` = @finance_manager_role_id
        AND `deleted` = b'0'
        AND `tenant_id` = @tenant_id
  );

SELECT
    v.ledger_id,
    l.name AS ledger_name,
    v.voucher_no,
    v.biz_type,
    v.biz_id,
    v.biz_no,
    v.total_debit_amount,
    v.total_credit_amount,
    v.status
FROM `erp_finance_voucher` v
LEFT JOIN `erp_finance_ledger` l ON l.id = v.ledger_id
WHERE v.biz_type = @biz_type
  AND v.biz_id = @biz_id
  AND v.deleted = b'0'
ORDER BY v.ledger_id, v.id;

SELECT
    diff_item_type,
    calculation_type,
    internal_amount,
    external_amount,
    diff_amount,
    ratio,
    fixed_amount
FROM `erp_finance_dual_ledger_amount_diff_log`
WHERE biz_type = @biz_type
  AND biz_id = @biz_id
  AND deleted = b'0'
ORDER BY id;

SET FOREIGN_KEY_CHECKS = 1;
