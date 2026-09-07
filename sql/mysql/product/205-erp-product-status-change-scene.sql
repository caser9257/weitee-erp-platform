/*
  物料启停审批场景注册（erp.product.status.change）+ 废除旧两段式场景清理
  背景：
    启停收敛为一段式审批（提交理由 → 审批 → 通过即切换 status，目标状态存暂存）；
    废除同样收敛为一段式（erp.product.obsolete.request 保留，通过即销号）。
    原"废除阶段二确认"场景 erp.product.obsolete.confirm 已废弃，做禁用清理。
  内容：
    A. 场景 erp.product.status.change 注册 + 方案/版本/规则绑定（幂等，参照 203 范式）
    B. 禁用已废弃场景 erp.product.obsolete.confirm
  幂等：全部按自然键判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化。
*/
SET NAMES utf8mb4;

-- ===================== A. erp.product.status.change 场景注册 =====================
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.status.change', '物料启停审批', 'erp_product', 'product', 'status', NULL, 1, 1, '启停一段式审批：提交理由，审批通过即切换启停状态（目标状态存暂存，审批期间主表不变）', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.status.change' AND `deleted`=b'0');
UPDATE `bpm_approval_scene` SET `status`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scene_code`='erp.product.status.change';
SET @s := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.status.change' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.status.change.scheme.v1', '物料启停审批方案', 'erp_product', 'product', @s, '物料启停默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.status.change.scheme.v1' AND `deleted`=b'0');
SET @scheme := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.status.change.scheme.v1' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @scheme, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @scheme IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@scheme AND `deleted`=b'0');
UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scheme_id`=@scheme AND `deleted`=b'0';
SET @ver := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@scheme AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @ver, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @ver IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@ver AND `rule_name`='默认规则' AND `deleted`=b'0');
UPDATE `bpm_approval_scheme` SET `active_version_id`=@ver, `latest_version_id`=@ver, `updater`='1', `update_time`=NOW() WHERE `id`=@scheme;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@scheme, `updater`='1', `update_time`=NOW() WHERE `id`=@s;

-- ===================== B. 废弃场景清理（废除阶段二确认） =====================
UPDATE `bpm_approval_scene` SET `status`=0, `remark`=CONCAT(IFNULL(`remark`,''),'（已废弃：废除收敛为一段式，2026-09-03）'), `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.product.obsolete.confirm';

-- ===================== 校验 =====================
SELECT `scene_code`, `name`, `status`, `active_scheme_id`
FROM `bpm_approval_scene`
WHERE `scene_code` IN ('erp.product.status.change','erp.product.obsolete.request','erp.product.obsolete.confirm');
