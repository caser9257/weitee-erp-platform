/*
 Target: ERP purchase-order BPM test users and role split
 Schema: ruoyi-vue-pro
 Date: 2026-04-08

 Test accounts after import:
 - poapply / 123456  -> 采购下单员
 - poleader / 123456 -> 采购审批组长
 - pogm / 123456     -> 采购审批经理
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;

SET @role_apply_id = 920001;
SET @role_leader_id = 920002;
SET @role_manager_id = 920003;

SET @dept_center_id = 920100;
SET @dept_group_id = 920101;

SET @user_apply_id = 920201;
SET @user_leader_id = 920202;
SET @user_manager_id = 920203;
SET @menu_submit_id = 920511;
SET @menu_cancel_id = 920512;

SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@menu_submit_id, '采购订单提交审批', 'erp:purchase-order:submit', 3, 6, 2666, '', '', '', NULL,
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(@menu_cancel_id, '采购订单撤回审批', 'erp:purchase-order:cancel-approval', 3, 7, 2666, '', '', '', NULL,
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

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@role_apply_id, '采购下单员', 'erp_purchase_order_applicant', 92, 1, '', 0, 2,
 '采购订单发起与提交审批角色', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@role_leader_id, '采购审批组长', 'erp_purchase_order_leader_approver', 93, 1, '', 0, 2,
 '采购订单一级审批角色', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@role_manager_id, '采购审批经理', 'erp_purchase_order_manager_approver', 94, 1, '', 0, 2,
 '采购订单二级审批角色', '1', NOW(), '1', NOW(), b'0', @tenant_id)
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
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO `system_dept`
(`id`, `name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@dept_center_id, '采购审批测试中心', 100, 92, @user_manager_id, '13800011000', 'po-center@test.local', 0,
 '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@dept_group_id, '采购审批测试组', @dept_center_id, 93, @user_leader_id, '13800011001', 'po-group@test.local', 0,
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
(@user_apply_id, 'poapply', @password_hash, '采购下单员', '采购订单 BPM 发起人', @dept_group_id, '[]',
 'poapply@test.local', '13800012001', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_leader_id, 'poleader', @password_hash, '采购审批组长', '采购订单 BPM 一级审批人', @dept_group_id, '[]',
 'poleader@test.local', '13800012002', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_manager_id, 'pogm', @password_hash, '采购审批经理', '采购订单 BPM 二级审批人', @dept_center_id, '[]',
 'pogm@test.local', '13800012003', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id)
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

DELETE FROM `system_role_menu`
WHERE (`role_id` IN (@role_apply_id, @role_leader_id, @role_manager_id)
    AND `tenant_id` = @tenant_id)
   OR `id` BETWEEN 920401 AND 920460;

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920401, @role_apply_id, 1185, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920402, @role_apply_id, 1200, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920403, @role_apply_id, 1201, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920404, @role_apply_id, 1202, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920405, @role_apply_id, 2563, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920406, @role_apply_id, 2602, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920407, @role_apply_id, 2666, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920408, @role_apply_id, 2667, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920409, @role_apply_id, 2668, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920410, @role_apply_id, 2669, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920411, @role_apply_id, 2670, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920412, @role_apply_id, 2671, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920413, @role_apply_id, @menu_submit_id, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920414, @role_apply_id, @menu_cancel_id, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920421, @role_leader_id, 1185, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920422, @role_leader_id, 1200, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920423, @role_leader_id, 1202, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920424, @role_leader_id, 1207, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920425, @role_leader_id, 1208, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920426, @role_leader_id, 1221, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920427, @role_leader_id, 1222, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920428, @role_leader_id, 2563, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920429, @role_leader_id, 2602, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920430, @role_leader_id, 2666, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920431, @role_leader_id, 2667, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920441, @role_manager_id, 1185, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920442, @role_manager_id, 1200, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920443, @role_manager_id, 1202, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920444, @role_manager_id, 1207, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920445, @role_manager_id, 1208, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920446, @role_manager_id, 1221, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920447, @role_manager_id, 1222, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920448, @role_manager_id, 2563, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920449, @role_manager_id, 2602, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920450, @role_manager_id, 2666, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920451, @role_manager_id, 2667, '1', NOW(), '1', NOW(), b'0', @tenant_id);

DELETE FROM `system_user_role`
WHERE (`user_id` IN (@user_apply_id, @user_leader_id, @user_manager_id)
    AND `tenant_id` = @tenant_id)
   OR `id` BETWEEN 920301 AND 920303;

INSERT INTO `system_user_role`
(`id`, `user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920301, @user_apply_id, @role_apply_id, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920302, @user_leader_id, @role_leader_id, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(920303, @user_manager_id, @role_manager_id, '1', NOW(), '1', NOW(), b'0', @tenant_id);

SET FOREIGN_KEY_CHECKS = 1;
