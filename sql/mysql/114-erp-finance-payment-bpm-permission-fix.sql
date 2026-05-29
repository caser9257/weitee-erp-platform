/*
 Target: ERP finance payment BPM permission fix
 Schema: ruoyi-vue-pro
 Date: 2026-05-22
 Note: Idempotently adds submit / cancel-approval menu permissions and assigns them to finance roles.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @payment_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/payment/index'
        OR component_name IN ('FormalFinancePayment', 'ErpFinancePayment')
        OR path = 'payment'
      )
    ORDER BY id
    LIMIT 1
);

SET @submit_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-payment:submit'
    ORDER BY id
    LIMIT 1
);
SET @submit_menu_id := IFNULL(@submit_menu_id, (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @submit_menu_id, '付款单提交审批', 'erp:finance-payment:submit', 3, 7, @payment_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @payment_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @cancel_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-payment:cancel-approval'
    ORDER BY id
    LIMIT 1
);
SET @cancel_menu_id := IFNULL(@cancel_menu_id, (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @cancel_menu_id, '付款单撤回审批', 'erp:finance-payment:cancel-approval', 3, 8, @payment_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @payment_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @role_clerk_id := (
    SELECT id
    FROM system_role
    WHERE deleted = b'0'
      AND code = 'erp_finance_clerk'
    ORDER BY id
    LIMIT 1
);

SET @role_mgr_id := (
    SELECT id
    FROM system_role
    WHERE deleted = b'0'
      AND code = 'erp_finance_manager'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_role_menu
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @role_clerk_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_clerk_id IS NOT NULL
  AND m.deleted = b'0'
  AND m.permission IN ('erp:finance-payment:submit', 'erp:finance-payment:cancel-approval')
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_clerk_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

INSERT INTO system_role_menu
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT @role_mgr_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_mgr_id IS NOT NULL
  AND m.deleted = b'0'
  AND m.permission IN ('erp:finance-payment:submit', 'erp:finance-payment:cancel-approval')
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_mgr_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

SET FOREIGN_KEY_CHECKS = 1;
