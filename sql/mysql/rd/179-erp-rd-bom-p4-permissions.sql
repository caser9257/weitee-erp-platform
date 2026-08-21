/*
  P4 收尾：研发BOM与物料审核权限/场景种子
  幂等：WHERE NOT EXISTS
*/
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- 研发BOM 菜单ID
SET @rd_bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/rd/rd-bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

-- 产品 菜单ID（兼容多种 component 写法）
SET @product_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` IN ('erp/product/product/index', 'erp/product/index')
    AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @product_menu_id := COALESCE(@product_menu_id,
  (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:query' AND `deleted` = b'0' LIMIT 1)
);

-- 研发BOM：提交审批
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '研发BOM提交审批', 'erp:rd-bom:submit',
       3, 6, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:rd-bom:submit' AND `deleted`=b'0');

-- 研发BOM：撤回审批
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '研发BOM撤回审批', 'erp:rd-bom:cancel',
       3, 7, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:rd-bom:cancel' AND `deleted`=b'0');

-- 物料：提交审核
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '产品提交审核', 'erp:product:submit',
       3, 6, @product_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:product:submit' AND `deleted`=b'0');

-- 物料：撤回审核
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '产品撤回审核', 'erp:product:cancel',
       3, 7, @product_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:product:cancel' AND `deleted`=b'0');

-- 赋给超级管理员（role_id=1）
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:rd-bom:submit','erp:rd-bom:cancel','erp:product:submit','erp:product:cancel')
  AND m.`deleted`=b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0'
  );

-- 审批场景种子（幂等）
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `status`, `description`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.rd.bom.submit', '研发BOM审批', 'erp_rd', 'rd_bom', 'submit', 0, '研发BOM：硬件工程师提交，器件工程师与供应链主管审批', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.rd.bom.submit' AND `deleted`=b'0');

INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `status`, `description`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.create', '物料新建审核', 'erp_product', 'product', 'submit', 0, '新物料建档审核', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.create' AND `deleted`=b'0');

SET FOREIGN_KEY_CHECKS = 1;
