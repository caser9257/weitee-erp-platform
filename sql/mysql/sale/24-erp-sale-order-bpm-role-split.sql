/*
 Target: ERP sale-order BPM role split
 Schema: ruoyi-vue-pro
 Date: 2026-04-08

 Purpose:
 - split shared test role into 3 roles
 - soapply  -> 销售下单员
 - soleader -> 销售审批组长
 - sogm     -> 销售审批经理
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @role_apply_id = 910001;
SET @role_leader_id = 910002;
SET @role_manager_id = 910003;

SET @user_apply_id = 910201;
SET @user_leader_id = 910202;
SET @user_manager_id = 910203;

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@role_apply_id, '销售下单员', 'erp_sale_order_applicant', 91, 1, '', 0, 2,
 '销售订单发起与提交审批角色', '1', NOW(), '1', NOW(), b'0'),
(@role_leader_id, '销售审批组长', 'erp_sale_order_leader_approver', 92, 1, '', 0, 2,
 '销售订单一级审批角色', '1', NOW(), '1', NOW(), b'0'),
(@role_manager_id, '销售审批经理', 'erp_sale_order_manager_approver', 93, 1, '', 0, 2,
 '销售订单二级审批角色', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`code` = VALUES(`code`),
`sort` = VALUES(`sort`),
`data_scope` = VALUES(`data_scope`),
`status` = VALUES(`status`),
`type` = VALUES(`type`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

DELETE FROM `system_role_menu`
WHERE (`role_id` IN (@role_apply_id, @role_leader_id, @role_manager_id))
   OR `id` BETWEEN 910401 AND 910452;

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(910401, @role_apply_id, 1185, '1', NOW(), '1', NOW(), b'0'),
(910402, @role_apply_id, 1200, '1', NOW(), '1', NOW(), b'0'),
(910403, @role_apply_id, 1201, '1', NOW(), '1', NOW(), b'0'),
(910404, @role_apply_id, 1202, '1', NOW(), '1', NOW(), b'0'),
(910405, @role_apply_id, 2563, '1', NOW(), '1', NOW(), b'0'),
(910406, @role_apply_id, 2617, '1', NOW(), '1', NOW(), b'0'),
(910407, @role_apply_id, 2638, '1', NOW(), '1', NOW(), b'0'),
(910408, @role_apply_id, 2639, '1', NOW(), '1', NOW(), b'0'),
(910409, @role_apply_id, 2640, '1', NOW(), '1', NOW(), b'0'),
(910410, @role_apply_id, 2641, '1', NOW(), '1', NOW(), b'0'),
(910411, @role_apply_id, 2644, '1', NOW(), '1', NOW(), b'0'),
(910421, @role_leader_id, 1185, '1', NOW(), '1', NOW(), b'0'),
(910422, @role_leader_id, 1200, '1', NOW(), '1', NOW(), b'0'),
(910423, @role_leader_id, 1201, '1', NOW(), '1', NOW(), b'0'),
(910424, @role_leader_id, 1202, '1', NOW(), '1', NOW(), b'0'),
(910425, @role_leader_id, 1207, '1', NOW(), '1', NOW(), b'0'),
(910426, @role_leader_id, 1208, '1', NOW(), '1', NOW(), b'0'),
(910427, @role_leader_id, 1221, '1', NOW(), '1', NOW(), b'0'),
(910428, @role_leader_id, 1222, '1', NOW(), '1', NOW(), b'0'),
(910429, @role_leader_id, 2563, '1', NOW(), '1', NOW(), b'0'),
(910430, @role_leader_id, 2617, '1', NOW(), '1', NOW(), b'0'),
(910431, @role_leader_id, 2638, '1', NOW(), '1', NOW(), b'0'),
(910432, @role_leader_id, 2639, '1', NOW(), '1', NOW(), b'0'),
(910441, @role_manager_id, 1185, '1', NOW(), '1', NOW(), b'0'),
(910442, @role_manager_id, 1200, '1', NOW(), '1', NOW(), b'0'),
(910443, @role_manager_id, 1201, '1', NOW(), '1', NOW(), b'0'),
(910444, @role_manager_id, 1202, '1', NOW(), '1', NOW(), b'0'),
(910445, @role_manager_id, 1207, '1', NOW(), '1', NOW(), b'0'),
(910446, @role_manager_id, 1208, '1', NOW(), '1', NOW(), b'0'),
(910447, @role_manager_id, 1221, '1', NOW(), '1', NOW(), b'0'),
(910448, @role_manager_id, 1222, '1', NOW(), '1', NOW(), b'0'),
(910449, @role_manager_id, 2563, '1', NOW(), '1', NOW(), b'0'),
(910450, @role_manager_id, 2617, '1', NOW(), '1', NOW(), b'0'),
(910451, @role_manager_id, 2638, '1', NOW(), '1', NOW(), b'0'),
(910452, @role_manager_id, 2639, '1', NOW(), '1', NOW(), b'0');

DELETE FROM `system_user_role`
WHERE (`user_id` IN (@user_apply_id, @user_leader_id, @user_manager_id))
   OR `id` BETWEEN 910301 AND 910303;

INSERT INTO `system_user_role`
(`id`, `user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(910301, @user_apply_id, @role_apply_id, '1', NOW(), '1', NOW(), b'0'),
(910302, @user_leader_id, @role_leader_id, '1', NOW(), '1', NOW(), b'0'),
(910303, @user_manager_id, @role_manager_id, '1', NOW(), '1', NOW(), b'0');

SET FOREIGN_KEY_CHECKS = 1;
