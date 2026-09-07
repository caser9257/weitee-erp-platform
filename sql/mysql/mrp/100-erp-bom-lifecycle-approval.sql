/*
  制造 BOM 生命周期审批种子：停用申请场景 + 重新发布权限点
  背景（对齐《产品列表与BOM列表设计结论》10.3/10.4）：
    制造 BOM 是研发 BOM 审批发布的快照，不再提供结构维护入口：
    - /erp/bom/create、/update、/delete 直改接口已下线；
    - 停用/废止必须走 BPM 申请流（场景 erp.mrp.bom.disable）；
    - 新增受控「重新发布」管理动作（erp:bom:republish），从研发 BOM 刷新 MBOM 快照。
  内容：
    A. 场景 erp.mrp.bom.disable 注册 + 方案/版本/规则绑定
       注意：process_json 指向新流程 erp_bom_disable_approval，
       需在 BPM 模型管理部署同名流程后申请流才可端到端运行；未部署前提交会失败并可重试。
    B. 权限按钮「制造BOM重新发布」erp:bom:republish + 超管授权
  幂等：全部按自然键判重，可重复执行。
*/
SET NAMES utf8mb4;

-- ===================== A. 场景注册 =====================
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.mrp.bom.disable', '制造BOM停用审核', 'erp_bom', 'bom', 'disable', NULL, 1, 1, '制造 BOM 停用/废止必须经审批；通过后由系统落 DISABLE', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.mrp.bom.disable' AND `deleted`=b'0');

UPDATE `bpm_approval_scene`
SET `name`='制造BOM停用审核', `module_code`='erp_bom', `biz_type`='bom', `action_code`='disable',
    `status`=1, `deleted`=b'0',
    `remark`='制造 BOM 停用/废止必须经审批；通过后由系统落 DISABLE',
    `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.mrp.bom.disable';

SET @disable_scene_id := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.mrp.bom.disable' AND `deleted`=b'0' LIMIT 1);

INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.mrp.bom.disable.scheme.v1', '制造BOM停用审核方案', 'erp_bom', 'bom', @disable_scene_id, '制造 BOM 停用默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @disable_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.mrp.bom.disable.scheme.v1' AND `deleted`=b'0');

SET @disable_scheme_id := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.mrp.bom.disable.scheme.v1' AND `deleted`=b'0' LIMIT 1);

INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @disable_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @disable_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@disable_scheme_id AND `deleted`=b'0');

UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW(),
    `published_by`=COALESCE(`published_by`,'1'), `published_time`=COALESCE(`published_time`,NOW())
WHERE `scheme_id`=@disable_scheme_id AND `deleted`=b'0';

SET @disable_version_id := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@disable_scheme_id AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);

-- 规则：绑定 BPMN 流程 erp_bom_disable_approval（需在模型管理部署同名流程后生效）
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @disable_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_bom_disable_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @disable_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@disable_version_id AND `rule_name`='默认规则' AND `deleted`=b'0');

UPDATE `bpm_approval_rule`
SET `rule_type`='DEFAULT', `priority`=1, `is_default`=1, `condition_json`=NULL,
    `process_json`='erp_bom_disable_approval', `enabled`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW()
WHERE `scheme_version_id`=@disable_version_id AND `rule_name`='默认规则';

UPDATE `bpm_approval_scheme` SET `active_version_id`=@disable_version_id, `latest_version_id`=@disable_version_id, `updater`='1', `update_time`=NOW() WHERE `id`=@disable_scheme_id;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@disable_scheme_id, `updater`='1', `update_time`=NOW() WHERE `id`=@disable_scene_id;

-- ===================== B. 权限点 =====================
SET @bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '制造BOM重新发布', 'erp:bom:republish',
       3, 9, @bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:bom:republish' AND `deleted`=b'0');

-- 超管授权（其他角色按需在界面分配）
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:bom:republish')
  AND NOT EXISTS (SELECT 1 FROM `system_role_menu` rm WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0');
