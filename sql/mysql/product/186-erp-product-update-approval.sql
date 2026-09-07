/*
  物料修改审批种子：场景注册 + 审批视图权限点
  背景：
    物料所有编辑统一走「修改审批」（暂存表模式）：
    - 未生效物料（草稿/驳回/失败）编辑直接落库；
    - 已生效（APPROVE）物料编辑写入 erp_product_pending_change 暂存表并提交流程，
      审批通过才落主表；materialCode/standard 被 BOM 引用时禁改。
    - 场景流程复用已部署的 erp_product_approval BPMN（与新建审核同一审批人体系），无需额外部署。
  内容：
    A. 场景 erp.product.update 注册 + 方案/版本/规则绑定（幂等，参照 rd/179 范式）
    B. 权限按钮「物料修改审批视图」erp:product:approval-view + 超管授权
  幂等：全部按自然键判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化。
*/
SET NAMES utf8mb4;

-- ===================== A. 场景注册 =====================
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.update', '物料修改审核', 'erp_product', 'product', 'update', NULL, 1, 1, '已生效物料的编辑走修改审批（暂存表模式）；关键字段被 BOM 引用时禁改', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.update' AND `deleted`=b'0');

-- 自愈：复活误删/禁用
UPDATE `bpm_approval_scene`
SET `name`='物料修改审核', `module_code`='erp_product', `biz_type`='product', `action_code`='update',
    `status`=1, `deleted`=b'0',
    `remark`='已生效物料的编辑走修改审批（暂存表模式）；关键字段被 BOM 引用时禁改',
    `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.product.update';

SET @update_scene_id := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.update' AND `deleted`=b'0' LIMIT 1);

-- 方案
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.update.scheme.v1', '物料修改审核方案', 'erp_product', 'product', @update_scene_id, '物料修改默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @update_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.update.scheme.v1' AND `deleted`=b'0');

SET @update_scheme_id := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.update.scheme.v1' AND `deleted`=b'0' LIMIT 1);

-- 方案版本（ACTIVE=30）
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @update_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @update_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@update_scheme_id AND `deleted`=b'0');

UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW(),
    `published_by`=COALESCE(`published_by`,'1'), `published_time`=COALESCE(`published_time`,NOW())
WHERE `scheme_id`=@update_scheme_id AND `deleted`=b'0';

SET @update_version_id := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@update_scheme_id AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);

-- 规则：默认规则绑定既有 BPMN 流程 erp_product_approval（已随新建审核部署，无需新增流程定义）
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @update_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @update_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@update_version_id AND `rule_name`='默认规则' AND `deleted`=b'0');

-- 回填生效绑定
UPDATE `bpm_approval_scheme` SET `active_version_id`=@update_version_id, `latest_version_id`=@update_version_id, `updater`='1', `update_time`=NOW() WHERE `id`=@update_scheme_id;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@update_scheme_id, `updater`='1', `update_time`=NOW() WHERE `id`=@update_scene_id;

-- ===================== B. 权限点 =====================
SET @product_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` IN ('erp/product/product/index', 'erp/product/index') AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @product_menu_id := COALESCE(@product_menu_id,
  (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:query' AND `deleted` = b'0' LIMIT 1)
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '物料修改审批视图', 'erp:product:approval-view',
       3, 7, @product_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:product:approval-view' AND `deleted`=b'0');

-- 超管授权（其他角色按需在界面分配）
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:product:approval-view')
  AND NOT EXISTS (SELECT 1 FROM `system_role_menu` rm WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0');
