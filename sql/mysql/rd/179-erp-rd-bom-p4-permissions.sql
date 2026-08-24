/*
  P4 收尾：研发BOM与物料审核权限/场景种子（完整版）
  修复历史缺陷：历史版本误将 `status` 设为 0（禁用），导致提交审批时触发
  APPROVAL_SCENE_DISABLED，进而 afterCommit 标记 FAILED，前端报“提交已受理但流程创建失败”。
  本脚本为幂等全量种子：场景启用 + 方案/版本/规则绑定 + BPMN 流程定义 key 对齐。
  可重复执行；已存在时仅做启用与绑定修复。
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

-- 研发BOM：发起变更（仅限已审核通过，生成新 DRAFT 版本，须重新提交审批）
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '研发BOM发起变更', 'erp:rd-bom:change',
       3, 8, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:rd-bom:change' AND `deleted`=b'0');

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
WHERE m.`permission` IN ('erp:rd-bom:submit','erp:rd-bom:cancel','erp:rd-bom:change','erp:product:submit','erp:product:cancel')
  AND m.`deleted`=b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0'
  );

-- ===================== 审批场景种子（启用态 + 自愈） =====================
-- 历史缺陷：首次种子误写入 status=0（禁用），导致无法提交；此处保证启用
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.rd.bom.submit', '研发BOM审批', 'erp_rd', 'rd_bom', 'submit', NULL, 1, 1, '研发BOM：硬件工程师提交，器件工程师与供应链主管审批', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.rd.bom.submit' AND `deleted`=b'0');

INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.create', '物料新建审核', 'erp_product', 'product', 'submit', NULL, 1, 1, '新物料建档审核', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.create' AND `deleted`=b'0');

-- 自愈：若已存在但为禁用或被逻辑删除，强制启用并复活
UPDATE `bpm_approval_scene`
SET `name`='研发BOM审批', `module_code`='erp_rd', `biz_type`='rd_bom', `action_code`='submit',
    `status`=1, `deleted`=b'0', `owner_user_id`=COALESCE(NULLIF(`owner_user_id`,0),1),
    `remark`='研发BOM：硬件工程师提交，器件工程师与供应链主管审批',
    `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.rd.bom.submit';

UPDATE `bpm_approval_scene`
SET `name`='物料新建审核', `module_code`='erp_product', `biz_type`='product', `action_code`='submit',
    `status`=1, `deleted`=b'0', `owner_user_id`=COALESCE(NULLIF(`owner_user_id`,0),1),
    `remark`='新物料建档审核',
    `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.product.create';

-- 若曾被逻辑删除，复活
UPDATE `bpm_approval_scene` SET `deleted`=b'0', `status`=1, `updater`='1', `update_time`=NOW()
WHERE `scene_code` IN ('erp.rd.bom.submit','erp.product.create') AND `deleted`=b'1';

SET @rd_scene_id := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.rd.bom.submit' AND `deleted`=b'0' LIMIT 1);
SET @product_scene_id := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.create' AND `deleted`=b'0' LIMIT 1);

-- ===================== 审批方案 =====================
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.rd.bom.scheme.v1', '研发BOM审批方案', 'erp_rd', 'rd_bom', @rd_scene_id, '研发BOM默认审批方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @rd_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.rd.bom.scheme.v1' AND `deleted`=b'0');

INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.scheme.v1', '物料新建审核方案', 'erp_product', 'product', @product_scene_id, '物料新建默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @product_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.scheme.v1' AND `deleted`=b'0');

-- 自愈：复活被删方案并纠正 scene_id
UPDATE `bpm_approval_scheme` SET `deleted`=b'0', `scene_id`=@rd_scene_id, `module_code`='erp_rd', `biz_type`='rd_bom', `name`='研发BOM审批方案', `updater`='1', `update_time`=NOW()
WHERE `code`='erp.rd.bom.scheme.v1' AND (`deleted`=b'1' OR `scene_id` IS NULL OR `scene_id`!=@rd_scene_id);

UPDATE `bpm_approval_scheme` SET `deleted`=b'0', `scene_id`=@product_scene_id, `module_code`='erp_product', `biz_type`='product', `name`='物料新建审核方案', `updater`='1', `update_time`=NOW()
WHERE `code`='erp.product.scheme.v1' AND (`deleted`=b'1' OR `scene_id` IS NULL OR `scene_id`!=@product_scene_id);

SET @rd_scheme_id := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.rd.bom.scheme.v1' AND `deleted`=b'0' LIMIT 1);
SET @product_scheme_id := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.scheme.v1' AND `deleted`=b'0' LIMIT 1);

-- ===================== 审批方案版本（ACTIVE=30） =====================
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @rd_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @rd_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@rd_scheme_id AND `deleted`=b'0');

INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @product_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @product_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@product_scheme_id AND `deleted`=b'0');

UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW(), `published_by`=COALESCE(`published_by`,'1'), `published_time`=COALESCE(`published_time`,NOW())
WHERE `scheme_id` IN (@rd_scheme_id, @product_scheme_id) AND `deleted`=b'0';

SET @rd_version_id := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@rd_scheme_id AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
SET @product_version_id := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@product_scheme_id AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);

-- ===================== 审批规则（默认规则 → BPMN process key） =====================
-- 研发BOM 规则：process_json 必须与 BPMN process id 一致（erp_rd_bom_approval）
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @rd_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_rd_bom_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @rd_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@rd_version_id AND `rule_name`='默认规则' AND `deleted`=b'0');

INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @product_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @product_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@product_version_id AND `rule_name`='默认规则' AND `deleted`=b'0');

UPDATE `bpm_approval_rule`
SET `rule_type`='DEFAULT', `priority`=1, `is_default`=1, `condition_json`=NULL, `process_json`='erp_rd_bom_approval', `enabled`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW()
WHERE `scheme_version_id`=@rd_version_id AND `rule_name`='默认规则';

UPDATE `bpm_approval_rule`
SET `rule_type`='DEFAULT', `priority`=1, `is_default`=1, `condition_json`=NULL, `process_json`='erp_product_approval', `enabled`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW()
WHERE `scheme_version_id`=@product_version_id AND `rule_name`='默认规则';

-- ===================== 回填生效绑定 =====================
UPDATE `bpm_approval_scheme` SET `active_version_id`=@rd_version_id, `latest_version_id`=@rd_version_id, `updater`='1', `update_time`=NOW() WHERE `id`=@rd_scheme_id;
UPDATE `bpm_approval_scheme` SET `active_version_id`=@product_version_id, `latest_version_id`=@product_version_id, `updater`='1', `update_time`=NOW() WHERE `id`=@product_scheme_id;

UPDATE `bpm_approval_scene` SET `active_scheme_id`=@rd_scheme_id, `updater`='1', `update_time`=NOW() WHERE `id`=@rd_scene_id;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@product_scheme_id, `updater`='1', `update_time`=NOW() WHERE `id`=@product_scene_id;

SET FOREIGN_KEY_CHECKS = 1;
