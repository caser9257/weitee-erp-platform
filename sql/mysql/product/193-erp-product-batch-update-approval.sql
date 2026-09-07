/*
  物料批量修改审批种子：batch_id 归组列 + 场景注册 + 审批视图权限点
  背景：
    Cadence 等批量导入此前按物料逐条发起修改审批（N 物料 = N 流程实例 = 2N 待办），
    审批语义被撕裂且审批人负担过重。本迁移支持"一次导入 = 一个批次审批"：
    - erp_product_pending_change 增加 batch_id 列，同一批导入的暂存变更归组；
    - 新场景 erp.product.update.batch 复用内置 BPMN erp_product_batch_approval
      （两级审批，审批人体系与单条修改审批一致），审批通过整批生效、驳回整批作废；
    - 单条修改审批（erp.product.update）保持不变，日常零星维护继续走原路径。
  内容：
    A. erp_product_pending_change 增加 batch_id 列 + 索引
    B. 场景 erp.product.update.batch 注册 + 方案/版本/规则绑定（幂等，参照 186 范式）
    C. 权限按钮「物料批量修改审批视图」erp:product:batch-approval-view + 超管授权
  幂等：全部按自然键判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化。
*/
SET NAMES utf8mb4;

-- ===================== A. batch_id 列 =====================
SET @has_batch_id := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_product_pending_change' AND column_name = 'batch_id');
SET @ddl := IF(@has_batch_id = 0,
    'ALTER TABLE `erp_product_pending_change` ADD COLUMN `batch_id` bigint NULL COMMENT ''批量导入批次编号（同一批导入的暂存变更归组；单条修改为 NULL）'' AFTER `product_id`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'erp_product_pending_change' AND index_name = 'idx_batch_id');
SET @ddl := IF(@has_idx = 0,
    'ALTER TABLE `erp_product_pending_change` ADD INDEX `idx_batch_id` (`batch_id`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== B. 场景注册 =====================
INSERT INTO `bpm_approval_scene` (`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.update.batch', '物料批量修改审核', 'erp_product', 'product', 'update_batch', NULL, 1, 1, '批量导入的物料修改合并为一次审批（batch_id 归组）；通过整批生效，驳回整批作废', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.update.batch' AND `deleted`=b'0');

UPDATE `bpm_approval_scene`
SET `name`='物料批量修改审核', `module_code`='erp_product', `biz_type`='product', `action_code`='update_batch',
    `status`=1, `deleted`=b'0',
    `remark`='批量导入的物料修改合并为一次审批（batch_id 归组）；通过整批生效，驳回整批作废',
    `updater`='1', `update_time`=NOW()
WHERE `scene_code`='erp.product.update.batch';

SET @batch_scene_id := (SELECT `id` FROM `bpm_approval_scene` WHERE `scene_code`='erp.product.update.batch' AND `deleted`=b'0' LIMIT 1);

INSERT INTO `bpm_approval_scheme` (`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`, `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.product.update.batch.scheme.v1', '物料批量修改审核方案', 'erp_product', 'product', @batch_scene_id, '物料批量修改默认审核方案', NULL, NULL, 1, '1', NOW(), '1', NOW(), b'0'
WHERE @batch_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme` WHERE `code`='erp.product.update.batch.scheme.v1' AND `deleted`=b'0');

SET @batch_scheme_id := (SELECT `id` FROM `bpm_approval_scheme` WHERE `code`='erp.product.update.batch.scheme.v1' AND `deleted`=b'0' LIMIT 1);

INSERT INTO `bpm_approval_scheme_version` (`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`, `notify_json`, `change_summary`, `published_by`, `published_time`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @batch_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', '1', NOW(), '1', NOW(), '1', NOW(), b'0'
WHERE @batch_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@batch_scheme_id AND `deleted`=b'0');

UPDATE `bpm_approval_scheme_version` SET `status`=30, `deleted`=b'0', `updater`='1', `update_time`=NOW(),
    `published_by`=COALESCE(`published_by`,'1'), `published_time`=COALESCE(`published_time`,NOW())
WHERE `scheme_id`=@batch_scheme_id AND `deleted`=b'0';

SET @batch_version_id := (SELECT `id` FROM `bpm_approval_scheme_version` WHERE `scheme_id`=@batch_scheme_id AND `deleted`=b'0' ORDER BY `version_no` DESC, `id` DESC LIMIT 1);

-- 规则：绑定内置流程 erp_product_batch_approval（随应用启动自动部署，见 BpmBundledProcessDefinitionInitializer）
INSERT INTO `bpm_approval_rule` (`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`, `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @batch_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_product_batch_approval', 1, '1', NOW(), '1', NOW(), b'0'
WHERE @batch_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `bpm_approval_rule` WHERE `scheme_version_id`=@batch_version_id AND `rule_name`='默认规则' AND `deleted`=b'0');

UPDATE `bpm_approval_scheme` SET `active_version_id`=@batch_version_id, `latest_version_id`=@batch_version_id, `updater`='1', `update_time`=NOW() WHERE `id`=@batch_scheme_id;
UPDATE `bpm_approval_scene` SET `active_scheme_id`=@batch_scheme_id, `updater`='1', `update_time`=NOW() WHERE `id`=@batch_scene_id;

-- ===================== C. 权限点 =====================
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
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '物料批量修改审批视图', 'erp:product:batch-approval-view',
       3, 8, @product_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:product:batch-approval-view' AND `deleted`=b'0');

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:product:batch-approval-view')
  AND NOT EXISTS (SELECT 1 FROM `system_role_menu` rm WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0');
