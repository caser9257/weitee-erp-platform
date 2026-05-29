/*
 Target: ERP finance payment void permission fix
 Schema: ruoyi-vue-pro
 Date: 2026-05-25
 Note: Idempotently adds the payment void permission and assigns it to finance roles.
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

SET @void_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-payment:void'
    ORDER BY id
    LIMIT 1
);
SET @void_menu_id := IFNULL(@void_menu_id, (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @void_menu_id, '付款单作废', 'erp:finance-payment:void', 3, 9, @payment_menu_id, '', '', '', NULL,
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
  AND m.permission = 'erp:finance-payment:void'
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
  AND m.permission = 'erp:finance-payment:void'
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_mgr_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

SELECT m.id, m.name, m.permission, m.parent_id
FROM system_menu m
WHERE m.deleted = b'0'
  AND m.permission = 'erp:finance-payment:void';

SELECT r.code AS role_code, m.permission
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND m.permission = 'erp:finance-payment:void'
  AND r.code IN ('erp_finance_clerk', 'erp_finance_manager')
ORDER BY r.code;

SET FOREIGN_KEY_CHECKS = 1;
