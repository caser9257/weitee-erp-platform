/*
 Target: ERP sale-order BPM test users
 Schema: ruoyi-vue-pro
 Date: 2026-04-07

 Test accounts after import:
 - so_apply / 123456    -> 发起销售订单审批
 - so_leader / 123456   -> 一级审批人（测试组负责人）
 - so_gm / 123456       -> 二级审批人（测试中心负责人）
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;
SET @role_id = 910001;

SET @dept_center_id = 910100;
SET @dept_group_id = 910101;

SET @user_apply_id = 910201;
SET @user_leader_id = 910202;
SET @user_gm_id = 910203;

SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@role_id, '销售审批测试角色', 'erp_sale_order_bpm_tester', 91, 1, '', 0, 2,
 '仅用于销售订单 BPM 联调测试', '1', NOW(), '1', NOW(), b'0', @tenant_id)
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
(@dept_center_id, '销售审批测试中心', 100, 91, @user_gm_id, '13800001000', 'so-center@test.local', 0,
 '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@dept_group_id, '销售审批测试组', @dept_center_id, 92, @user_leader_id, '13800001001', 'so-group@test.local', 0,
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
(@user_apply_id, 'so_apply', @password_hash, '销售下单员', '销售订单 BPM 测试发起人', @dept_group_id, '[]',
 'so_apply@test.local', '13800002001', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_leader_id, 'so_leader', @password_hash, '销售审批组长', '销售订单 BPM 一级审批人', @dept_group_id, '[]',
 'so_leader@test.local', '13800002002', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@user_gm_id, 'so_gm', @password_hash, '销售审批总经理', '销售订单 BPM 二级审批人', @dept_center_id, '[]',
 'so_gm@test.local', '13800002003', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id)
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

INSERT INTO `system_user_role`
(`id`, `user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910301, @user_apply_id, @role_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_user_role`
  WHERE `user_id` = @user_apply_id AND `role_id` = @role_id AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_user_role`
(`id`, `user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910302, @user_leader_id, @role_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_user_role`
  WHERE `user_id` = @user_leader_id AND `role_id` = @role_id AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_user_role`
(`id`, `user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910303, @user_gm_id, @role_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_user_role`
  WHERE `user_id` = @user_gm_id AND `role_id` = @role_id AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910401, @role_id, 1185, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1185 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910402, @role_id, 1200, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1200 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910403, @role_id, 1201, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1201 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910404, @role_id, 1202, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1202 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910405, @role_id, 1207, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1207 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910406, @role_id, 1208, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1208 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910407, @role_id, 1221, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1221 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910408, @role_id, 1222, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 1222 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910409, @role_id, 2563, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2563 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910410, @role_id, 2617, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2617 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910411, @role_id, 2638, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2638 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910412, @role_id, 2639, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2639 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910413, @role_id, 2640, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2640 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910414, @role_id, 2641, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2641 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`id`, `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 910415, @role_id, 2644, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = @role_id AND `menu_id` = 2644 AND `tenant_id` = @tenant_id AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
