/*
 Target: ERP finance personnel test users
 Schema: ruoyi-vue-pro
 Date: 2026-05-07

 Test accounts after import:
 - 财务经办 / 123456
 - 财务主管 / 123456
 - 采购协同 / 123456

 Scope:
 - create dedicated finance test roles, dept and users
 - grant finance-related page and button permissions already present in the menu tree
 - idempotent, no business data or API contract changes
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;
SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

SET @finance_root_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `path` = '/finance'
        OR `component_name` IN ('FormalFinanceRoot', 'ProjectFinanceRoot')
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @apar_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/apar/index'
        OR `component_name` IN ('ErpFinanceApar', 'ProjectFinanceApar')
        OR `path` = 'apar'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @ap_estimate_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/ap-estimate/index'
        OR `component_name` IN ('ErpApEstimate', 'FormalFinanceApEstimate', 'ProjectFinanceApEstimate')
        OR `path` = 'ap-estimate'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @ledger_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/ledger/index'
        OR `component_name` = 'ErpFinanceLedger'
        OR `path` = 'ledger'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @period_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/period/index'
        OR `component_name` = 'ErpFinancePeriod'
        OR `path` = 'period'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @subject_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/subject/index'
        OR `component_name` = 'ErpFinanceSubject'
        OR `path` = 'subject'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @report_item_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/report-item/index'
        OR `component_name` = 'ErpFinanceReportItem'
        OR `path` = 'report-item'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @reports_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/reports/index'
        OR `component_name` = 'ErpFinanceReports'
        OR `path` = 'reports'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @voucher_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/voucher/index'
        OR `component_name` = 'ErpFinanceVoucher'
        OR `path` = 'voucher'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @payment_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/payment/index'
        OR `component_name` = 'ErpFinancePayment'
        OR `path` = 'payment'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @receipt_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/receipt/index'
        OR `component_name` = 'ErpFinanceReceipt'
        OR `path` = 'receipt'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @prepayment_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/prepayment/index'
        OR `component_name` IN ('ErpFinancePrepayment', 'FormalFinancePrepayment')
        OR `path` = 'prepayment'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @expense_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/expense/index'
        OR `component_name` = 'FormalFinanceExpense'
        OR `path` = 'expense'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @account_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/account/index'
        OR `component_name` = 'ErpAccount'
        OR `path` = 'account'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @assets_menu_id := COALESCE((
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND (
        `component` = 'erp/finance/assets/index'
        OR `component_name` IN ('FormalFinanceAssets', 'ProjectFinanceAssets')
        OR `path` = 'assets'
      )
    ORDER BY `id`
    LIMIT 1
), NULL);

SET @role_clerk_id = 940001;
SET @role_mgr_id = 940002;
SET @role_po_id = 940003;

SET @dept_id = 940100;
SET @user_clerk_id = 940201;
SET @user_mgr_id = 940202;
SET @user_po_id = 940203;

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@role_clerk_id, '财务经办', 'erp_finance_clerk', 95, 1, '', 0, 2,
 '财务日常单据维护、收票和付款测试角色', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@role_mgr_id, '财务主管', 'erp_finance_manager', 96, 1, '', 0, 2,
 '财务审核、台账分析和报表测试角色', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@role_po_id, '采购协同', 'erp_finance_purchase_collab', 97, 1, '', 0, 2,
 '采购侧联调应付台账与暂估测试角色', '1', NOW(), '1', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`code` = VALUES(`code`),
`sort` = VALUES(`sort`),
`data_scope` = VALUES(`data_scope`),
`data_scope_dept_ids` = VALUES(`data_scope_dept_ids`),
`status` = VALUES(`status`),
`type` = VALUES(`type`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO `system_dept`
(`id`, `name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@dept_id, '财务测试中心', 100, 95, NULL, '13800019001', 'finance-test@test.local', 0,
 '1', NOW(), '1', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`parent_id` = VALUES(`parent_id`),
`sort` = VALUES(`sort`),
`leader_user_id` = VALUES(`leader_user_id`),
`phone` = VALUES(`phone`),
`email` = VALUES(`email`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO `system_users`
(`id`, `username`, `password`, `nickname`, `remark`, `dept_id`, `post_ids`, `email`, `mobile`, `sex`,
 `avatar`, `status`, `login_ip`, `login_date`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(@user_clerk_id, '财务经办', @password_hash, '财务经办', '财务日常单据维护测试账号', @dept_id, '[]',
 'fin_clerk@test.local', '13800019011', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_mgr_id, '财务主管', @password_hash, '财务主管', '财务审核与报表测试账号', @dept_id, '[]',
 'fin_mgr@test.local', '13800019012', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_po_id, '采购协同', @password_hash, '采购协同', '采购侧财务联调测试账号', @dept_id, '[]',
 'po_finance@test.local', '13800019013', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`username` = VALUES(`username`),
`password` = VALUES(`password`),
`nickname` = VALUES(`nickname`),
`remark` = VALUES(`remark`),
`dept_id` = VALUES(`dept_id`),
`post_ids` = VALUES(`post_ids`),
`email` = VALUES(`email`),
`mobile` = VALUES(`mobile`),
`sex` = VALUES(`sex`),
`avatar` = VALUES(`avatar`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

UPDATE `system_dept`
SET `leader_user_id` = @user_mgr_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @dept_id
  AND `deleted` = b'0';

INSERT INTO `system_user_role`
(`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @user_clerk_id, @role_clerk_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_user_role`
  WHERE `user_id` = @user_clerk_id
    AND `role_id` = @role_clerk_id
    AND `tenant_id` = @tenant_id
    AND `deleted` = b'0'
);

INSERT INTO `system_user_role`
(`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @user_mgr_id, @role_mgr_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_user_role`
  WHERE `user_id` = @user_mgr_id
    AND `role_id` = @role_mgr_id
    AND `tenant_id` = @tenant_id
    AND `deleted` = b'0'
);

INSERT INTO `system_user_role`
(`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @user_po_id, @role_po_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_user_role`
  WHERE `user_id` = @user_po_id
    AND `role_id` = @role_po_id
    AND `tenant_id` = @tenant_id
    AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT DISTINCT @role_clerk_id, menu.`id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `system_menu` menu
WHERE `menu`.`deleted` = b'0'
  AND (
    menu.`id` IN (
      COALESCE(@finance_root_id, -1),
      COALESCE(@apar_menu_id, -1),
      COALESCE(@ap_estimate_menu_id, -1),
      COALESCE(@payment_menu_id, -1),
      COALESCE(@receipt_menu_id, -1),
      COALESCE(@prepayment_menu_id, -1),
      COALESCE(@expense_menu_id, -1)
    )
    OR menu.`permission` IN (
       'erp:ap-statement:query',
       'erp:ap-statement:update',
       'erp:ap-statement:export',
       'erp:purchase-in:query',
       'erp:purchase-return:query',
       'erp:ap-estimate:query',
      'erp:ap-estimate:scan',
      'erp:ap-estimate:update',
      'erp:ap-estimate:confirm',
      'erp:ap-estimate:reverse',
      'erp:ap-estimate:export',
      'erp:finance-payment:create',
      'erp:finance-payment:update',
      'erp:finance-payment:update-status',
      'erp:finance-payment:delete',
      'erp:finance-payment:query',
      'erp:finance-payment:export',
      'erp:finance-receipt:create',
      'erp:finance-receipt:update',
      'erp:finance-receipt:update-status',
      'erp:finance-receipt:delete',
      'erp:finance-receipt:query',
      'erp:finance-receipt:export',
      'erp:finance-prepayment:create',
      'erp:finance-prepayment:update',
      'erp:finance-prepayment:update-status',
      'erp:finance-prepayment:delete',
      'erp:finance-prepayment:query',
      'erp:finance-expense:create',
      'erp:finance-expense:update',
      'erp:finance-expense:update-status',
      'erp:finance-expense:delete',
      'erp:finance-expense:query',
      'erp:finance-expense:export'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = @role_clerk_id
      AND rm.`menu_id` = menu.`id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT DISTINCT @role_mgr_id, menu.`id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `system_menu` menu
WHERE `menu`.`deleted` = b'0'
  AND (
    menu.`id` IN (
      COALESCE(@finance_root_id, -1),
      COALESCE(@apar_menu_id, -1),
      COALESCE(@ap_estimate_menu_id, -1),
      COALESCE(@ledger_menu_id, -1),
      COALESCE(@period_menu_id, -1),
      COALESCE(@subject_menu_id, -1),
      COALESCE(@report_item_menu_id, -1),
      COALESCE(@reports_menu_id, -1),
      COALESCE(@voucher_menu_id, -1),
      COALESCE(@payment_menu_id, -1),
      COALESCE(@receipt_menu_id, -1),
      COALESCE(@prepayment_menu_id, -1),
      COALESCE(@expense_menu_id, -1),
      COALESCE(@account_menu_id, -1),
      COALESCE(@assets_menu_id, -1)
    )
    OR menu.`permission` IN (
       'erp:ap-statement:query',
       'erp:ap-statement:update',
       'erp:ap-statement:export',
       'erp:purchase-in:query',
       'erp:purchase-return:query',
       'erp:ap-estimate:query',
      'erp:ap-estimate:export',
      'erp:finance-ledger:query',
      'erp:finance-period:query',
      'erp:finance-subject:query',
      'erp:finance-report-item:query',
      'erp:finance-report:query',
      'erp:finance-voucher:query',
      'erp:finance-payment:query',
      'erp:finance-payment:export',
      'erp:finance-payment:update-status',
      'erp:finance-receipt:query',
      'erp:finance-receipt:export',
      'erp:finance-receipt:update-status',
      'erp:finance-prepayment:query',
      'erp:finance-prepayment:create',
      'erp:finance-prepayment:update',
      'erp:finance-prepayment:update-status',
      'erp:finance-prepayment:delete',
      'erp:finance-expense:query',
      'erp:finance-expense:export',
      'erp:finance-expense:update-status'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = @role_mgr_id
      AND rm.`menu_id` = menu.`id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT DISTINCT @role_po_id, menu.`id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `system_menu` menu
WHERE `menu`.`deleted` = b'0'
  AND (
    menu.`id` IN (
      COALESCE(@finance_root_id, -1),
      COALESCE(@apar_menu_id, -1),
      COALESCE(@ap_estimate_menu_id, -1),
      COALESCE(@payment_menu_id, -1),
      COALESCE(@receipt_menu_id, -1),
      COALESCE(@prepayment_menu_id, -1)
    )
    OR menu.`permission` IN (
      'erp:ap-statement:query',
      'erp:ap-statement:update',
      'erp:ap-statement:export',
      'erp:ap-estimate:query',
      'erp:ap-estimate:scan',
      'erp:ap-estimate:confirm',
      'erp:ap-estimate:reverse',
      'erp:ap-estimate:export',
      'erp:finance-payment:query',
      'erp:finance-receipt:query',
      'erp:finance-prepayment:query'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = @role_po_id
      AND rm.`menu_id` = menu.`id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
