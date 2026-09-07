/*
 Target: ERP 采购入库 IQC 权限拆分、角色与测试账号
 Schema: ruoyi-vue-pro
 Date: 2026-04-13

 说明：
 1. 补齐采购入库 IQC 独立按钮权限
 2. 创建 IQC 质检员角色
 3. 创建 IQC 测试账号 iqc_user / 123456
 4. 为超级管理员、供应链经理、IQC质检员授予对应权限
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @admin_role_id = 1;
SET @purchase_root_menu_id = 2563;
SET @purchase_menu_id = 2602;

SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

SET @iqc_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_menu_id, 920561), '采购入库 IQC', '', 2, 7, @purchase_menu_id, 'in-quality', 'ep:finished',
       'erp/purchase/in-quality/index', 'ErpPurchaseInQuality',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
)
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

SET @iqc_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

SET @iqc_query_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:query' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_query_menu_id, 920562), '质检单查询', 'erp:purchase-in-quality:query', 3, 1, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_create_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:create' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_create_menu_id, 920563), '质检单创建', 'erp:purchase-in-quality:create', 3, 2, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_first_check_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:first-check' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_first_check_menu_id, 920564), '质检单初检', 'erp:purchase-in-quality:first-check', 3, 3, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_start_recheck_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:start-recheck' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_start_recheck_menu_id, 920565), '发起复检', 'erp:purchase-in-quality:start-recheck', 3, 4, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_recheck_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:recheck' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_recheck_menu_id, 920566), '质检单复检', 'erp:purchase-in-quality:recheck', 3, 5, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'erp_iqc_inspector'
  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_role_id, 920701), 'IQC质检员', 'erp_iqc_inspector', 65, 1, '', 0, 2,
       '采购入库 IQC 质检专用角色', '1', NOW(), '1', NOW(), b'0'
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
`deleted` = VALUES(`deleted`);

SET @iqc_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'erp_iqc_inspector'
  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

SET @iqc_dept_id := (
  SELECT `id` FROM `system_dept`
  WHERE `name` = 'IQC质检组'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_dept`
(`id`, `name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_dept_id, 920710), 'IQC质检组', 100, 65, 1, '13800003001', 'iqc@test.local', 0,
       '1', NOW(), '1', NOW(), b'0'
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
`deleted` = VALUES(`deleted`);

SET @iqc_dept_id := (
  SELECT `id` FROM `system_dept`
  WHERE `name` = 'IQC质检组'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

SET @iqc_user_id := (
  SELECT `id` FROM `system_users`
  WHERE `username` = 'iqc_user'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_users`
(`id`, `username`, `password`, `nickname`, `remark`, `dept_id`, `post_ids`, `email`, `mobile`, `sex`,
 `avatar`, `status`, `login_ip`, `login_date`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`)
SELECT COALESCE(@iqc_user_id, 920720), 'iqc_user', @password_hash, 'IQC质检员', '采购入库 IQC 测试账号',
       @iqc_dept_id, '[]', 'iqc_user@test.local', '13800003002', 1,
       '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0'
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

SET @iqc_user_id := (
  SELECT `id` FROM `system_users`
  WHERE `username` = 'iqc_user'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_user_role`
(`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @iqc_user_id, @iqc_role_id, '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_user_id IS NOT NULL
  AND @iqc_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_user_role`
    WHERE `user_id` = @iqc_user_id
      AND `role_id` = @iqc_role_id

      AND `deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @admin_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_create_menu_id
  UNION ALL SELECT @iqc_first_check_menu_id
  UNION ALL SELECT @iqc_start_recheck_menu_id
  UNION ALL SELECT @iqc_recheck_menu_id
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @admin_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @iqc_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_create_menu_id
  UNION ALL SELECT @iqc_first_check_menu_id
  UNION ALL SELECT @iqc_start_recheck_menu_id
  UNION ALL SELECT @iqc_recheck_menu_id
) target
WHERE @iqc_role_id IS NOT NULL
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @iqc_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

SET @supply_chain_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @supply_chain_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
) target
WHERE @supply_chain_role_id IS NOT NULL
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @supply_chain_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
