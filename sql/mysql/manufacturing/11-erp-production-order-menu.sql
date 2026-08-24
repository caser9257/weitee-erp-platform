-- ERP 生产工单菜单与权限点接入
-- 归属：制造执行管理（一级菜单，path = /mes）下新增"生产工单"二级菜单
-- 说明：/manufacturing 旧目录已被信息架构吸收（前端 projectDrivenFlat 丢弃该根），
--       生产执行域菜单统一挂在 /mes（制造执行管理）下
-- 幂等依据：parent_id + path 判重；权限点以 permission 判重
-- 说明：后端 /erp/production-order 仅提供 create/update/release/finish/get/page/summary，无 delete 接口，
--       因此只挂 query/create/update 三个按钮权限点。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 父菜单定位：优先按一级菜单路径 /mes 查找，找不到时兜底制造执行管理固定 ID 930170
SET @mf_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mf_root_id := IFNULL(@mf_root_id, 930170);

-- 1. 生产工单菜单（二级，挂在制造管理下）
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3207, '生产工单', '', 2, 35, @mf_root_id, 'production-order', 'ep:document',
       'erp/manufacturing/production-order/index', 'ErpProductionOrder',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mf_root_id AND `path` = 'production-order' AND `deleted` = b'0'
);

-- 2. 按钮权限点
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3271, '生产工单查询', 'erp:production-order:query', 3, 1, 3207, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 3207 AND `permission` = 'erp:production-order:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3272, '生产工单创建', 'erp:production-order:create', 3, 2, 3207, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 3207 AND `permission` = 'erp:production-order:create' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3273, '生产工单更新', 'erp:production-order:update', 3, 3, 3207, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 3207 AND `permission` = 'erp:production-order:update' AND `deleted` = b'0'
);

-- 3. 管理员角色授权（role_id = 1）
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 3207 AS menu_id
  UNION ALL SELECT 3271
  UNION ALL SELECT 3272
  UNION ALL SELECT 3273
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

-- 4. 供应链经理角色授权（补充 24-erp-mrp-supply-chain-role.sql 中因菜单缺失而落空的授权）
SET @scm_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scm_role_id, 3207, '1', NOW(), '1', NOW(), b'0'
WHERE @scm_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @scm_role_id AND rm.`menu_id` = 3207 AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scm_role_id, 3271, '1', NOW(), '1', NOW(), b'0'
WHERE @scm_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @scm_role_id AND rm.`menu_id` = 3271 AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scm_role_id, 3272, '1', NOW(), '1', NOW(), b'0'
WHERE @scm_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @scm_role_id AND rm.`menu_id` = 3272 AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scm_role_id, 3273, '1', NOW(), '1', NOW(), b'0'
WHERE @scm_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @scm_role_id AND rm.`menu_id` = 3273 AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;

-- 5. 验证
SELECT id, name, permission, type, sort, parent_id, path, component, component_name
FROM system_menu
WHERE id IN (3207, 3271, 3272, 3273) AND deleted = 0;
