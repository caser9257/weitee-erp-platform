/*
  产品两段式变更/废除审批场景注册（change.request / change.confirm / obsolete.request / obsolete.confirm）
  背景：
    产品变更与废除统一升级为两段式审批：
    - 阶段一（xxx.request）：申请 → 审批 → 解锁编辑权限；
    - 阶段二（xxx.confirm）：执行完成/废除确认 → 审批 → 生效/废除留痕。
  实现：
    四个场景复用既有 BPMN 流程定义 erp_product_approval（与新建审核同一审批人体系），
    仅注册 场景→方案→方案版本→默认规则 绑定关系（范式参照 186-erp-product-update-approval.sql）。
    若后续业务要求“阶段一申请人与阶段二确认人不同”，需另建 BPMN 流程定义并将 bpm_approval_rule.process_json 指向新流程。
  幂等：全部按自然键判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化。
*/
SET NAMES utf8mb4;

-- ===================== 公共小工具：为一个场景码补齐 scene/scheme/version/rule 绑定 =====================
-- 通过占位替换实现（每个场景块独立，避免 MySQL 无数组的复杂度）。

-- ---------- 1. erp.product.change.request（变更阶段一） ----------
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.change.request', '物料变更申请（两段式阶段一）', 'erp_product', 'product', 'change', NULL, 1, 1, '生效物料发起变更：申请审批通过后解锁编辑权限（两段式阶段一）', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.change.request' AND `deleted`=b'0');
UPDATE `bpm_approval_scene` SET `status`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scene_code`='erp.product.change.request';
SET @s1 := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.change.request' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.change.request.scheme.v1', '物料变更申请方案', 'erp_product', 'product', @s1, '物料变更申请默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.change.request.scheme.v1' AND `deleted`=b'0');
SET @s1_scheme := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.change.request.scheme.v1' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s1_scheme, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @s1_scheme IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s1_scheme AND `deleted`=b'0');
UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scheme_id`=@s1_scheme AND `deleted`=b'0';
SET @s1_ver := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s1_scheme AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s1_ver, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s1_ver IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@s1_ver AND `rule_name`='默认规则' AND `deleted`=b'0');
UPDATE `bpm_approval_scheme` SET `active_version_id`=@s1_ver, `latest_version_id`=@s1_ver, `updater`='1', `update_time`=NOW() WHERE `id`=@s1_scheme;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@s1_scheme, `updater`='1', `update_time`=NOW() WHERE `id`=@s1;

-- ---------- 2. erp.product.change.confirm（变更阶段二） ----------
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.change.confirm', '物料变更完成确认（两段式阶段二）', 'erp_product', 'product', 'change', NULL, 1, 1, '变更完成提交审批：负责人确认变更正确后生效落库（两段式阶段二）', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.change.confirm' AND `deleted`=b'0');
UPDATE `bpm_approval_scene` SET `status`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scene_code`='erp.product.change.confirm';
SET @s2 := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.change.confirm' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.change.confirm.scheme.v1', '物料变更完成确认方案', 'erp_product', 'product', @s2, '物料变更完成确认默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.change.confirm.scheme.v1' AND `deleted`=b'0');
SET @s2_scheme := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.change.confirm.scheme.v1' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s2_scheme, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @s2_scheme IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s2_scheme AND `deleted`=b'0');
UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scheme_id`=@s2_scheme AND `deleted`=b'0';
SET @s2_ver := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s2_scheme AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s2_ver, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s2_ver IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@s2_ver AND `rule_name`='默认规则' AND `deleted`=b'0');
UPDATE `bpm_approval_scheme` SET `active_version_id`=@s2_ver, `latest_version_id`=@s2_ver, `updater`='1', `update_time`=NOW() WHERE `id`=@s2_scheme;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@s2_scheme, `updater`='1', `update_time`=NOW() WHERE `id`=@s2;

-- ---------- 3. erp.product.obsolete.request（废除阶段一） ----------
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.obsolete.request', '物料废除申请（两段式阶段一）', 'erp_product', 'product', 'obsolete', NULL, 1, 1, '废除（销号）申请：审批通过后解锁废除编辑，编码释放可复用', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.obsolete.request' AND `deleted`=b'0');
UPDATE `bpm_approval_scene` SET `status`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scene_code`='erp.product.obsolete.request';
SET @s3 := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.obsolete.request' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.obsolete.request.scheme.v1', '物料废除申请方案', 'erp_product', 'product', @s3, '物料废除申请默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s3 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.obsolete.request.scheme.v1' AND `deleted`=b'0');
SET @s3_scheme := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.obsolete.request.scheme.v1' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s3_scheme, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @s3_scheme IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s3_scheme AND `deleted`=b'0');
UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scheme_id`=@s3_scheme AND `deleted`=b'0';
SET @s3_ver := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s3_scheme AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s3_ver, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s3_ver IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@s3_ver AND `rule_name`='默认规则' AND `deleted`=b'0');
UPDATE `bpm_approval_scheme` SET `active_version_id`=@s3_ver, `latest_version_id`=@s3_ver, `updater`='1', `update_time`=NOW() WHERE `id`=@s3_scheme;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@s3_scheme, `updater`='1', `update_time`=NOW() WHERE `id`=@s3;

-- ---------- 4. erp.product.obsolete.confirm（废除阶段二） ----------
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.obsolete.confirm', '物料废除确认（两段式阶段二）', 'erp_product', 'product', 'obsolete', NULL, 1, 1, '废除执行确认：负责人审批通过后废除留痕生效，编码释放', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.obsolete.confirm' AND `deleted`=b'0');
UPDATE `bpm_approval_scene` SET `status`=1, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scene_code`='erp.product.obsolete.confirm';
SET @s4 := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.obsolete.confirm' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.obsolete.confirm.scheme.v1', '物料废除确认方案', 'erp_product', 'product', @s4, '物料废除确认默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s4 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.obsolete.confirm.scheme.v1' AND `deleted`=b'0');
SET @s4_scheme := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.obsolete.confirm.scheme.v1' AND `deleted`=b'0' LIMIT 1);
INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s4_scheme, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @s4_scheme IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s4_scheme AND `deleted`=b'0');
UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW() WHERE `scheme_id`=@s4_scheme AND `deleted`=b'0';
SET @s4_ver := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@s4_scheme AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @s4_ver, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @s4_ver IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@s4_ver AND `rule_name`='默认规则' AND `deleted`=b'0');
UPDATE `bpm_approval_scheme` SET `active_version_id`=@s4_ver, `latest_version_id`=@s4_ver, `updater`='1', `update_time`=NOW() WHERE `id`=@s4_scheme;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@s4_scheme, `updater`='1', `update_time`=NOW() WHERE `id`=@s4;

-- ===================== 校验 =====================
SELECT `scene_code`, `name`, `status`, `active_scheme_id`
FROM `bpm_approval_scene`
WHERE `scene_code` IN ('erp.product.change.request','erp.product.change.confirm',
                       'erp.product.obsolete.request','erp.product.obsolete.confirm');
