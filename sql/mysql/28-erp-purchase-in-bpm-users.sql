/*
 Target: ERP purchase-in BPM permissions for existing purchase roles
 Schema: ruoyi-vue-pro
 Date: 2026-04-09

 Depends on:
 - 26-erp-purchase-order-bpm-users.sql
 - base purchase-in menus (2673 ~ 2679)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;

SET @menu_submit_id = 920521;
SET @menu_cancel_id = 920522;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@menu_submit_id, '采购入库提交审批', 'erp:purchase-in:submit', 3, 7, 2673, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(@menu_cancel_id, '采购入库撤回审批', 'erp:purchase-in:cancel-approval', 3, 8, 2673, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
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

SET @role_apply_id = (
  SELECT id FROM `system_role`
  WHERE `tenant_id` = @tenant_id AND `code` = 'erp_purchase_order_applicant'
  LIMIT 1
);
SET @role_leader_id = (
  SELECT id FROM `system_role`
  WHERE `tenant_id` = @tenant_id AND `code` = 'erp_purchase_order_leader_approver'
  LIMIT 1
);
SET @role_manager_id = (
  SELECT id FROM `system_role`
  WHERE `tenant_id` = @tenant_id AND `code` = 'erp_purchase_order_manager_approver'
  LIMIT 1
);

DELETE FROM `system_role_menu`
WHERE (`tenant_id` = @tenant_id AND `id` BETWEEN 920523 AND 920540);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 920523, @role_apply_id, 2673, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920524, @role_apply_id, 2674, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920525, @role_apply_id, 2675, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920526, @role_apply_id, 2676, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920527, @role_apply_id, 2677, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920528, @role_apply_id, 2678, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920529, @role_apply_id, @menu_submit_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920530, @role_apply_id, @menu_cancel_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_apply_id IS NOT NULL
UNION ALL
SELECT 920531, @role_leader_id, 2673, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_leader_id IS NOT NULL
UNION ALL
SELECT 920532, @role_leader_id, 2674, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_leader_id IS NOT NULL
UNION ALL
SELECT 920533, @role_manager_id, 2673, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_manager_id IS NOT NULL
UNION ALL
SELECT 920534, @role_manager_id, 2674, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_manager_id IS NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
