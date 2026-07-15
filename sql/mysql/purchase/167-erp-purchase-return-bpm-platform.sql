-- 采购退货 BPM 场景、方案、版本和默认规则
-- 幂等：兼容已有旧方案，将候选人配置交给 erp_purchase_return_approval BPMN。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

INSERT INTO bpm_approval_scene
    (scene_code, name, module_code, biz_type, action_code, active_scheme_id,
     owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.purchase.return.submit', '采购退货审批', 'erp_purchase', 'purchase_return', 'submit', NULL,
       0, 1, '供应链模块-采购退货 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM bpm_approval_scene
    WHERE scene_code = 'erp.purchase.return.submit' AND deleted = b'0'
);

SET @purchase_return_scene_id = (
    SELECT id FROM bpm_approval_scene
    WHERE scene_code = 'erp.purchase.return.submit' AND deleted = b'0'
    LIMIT 1
);

INSERT INTO bpm_approval_scheme
    (code, name, module_code, biz_type, scene_id, remark, active_version_id,
     latest_version_id, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.purchase.return.scheme.v1', '采购退货审批方案', 'erp_purchase', 'purchase_return',
       @purchase_return_scene_id, '采购退货默认审批方案', NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
WHERE @purchase_return_scene_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_scheme
      WHERE code = 'erp.purchase.return.scheme.v1' AND deleted = b'0'
  );

SET @purchase_return_scheme_id = (
    SELECT id FROM bpm_approval_scheme
    WHERE code = 'erp.purchase.return.scheme.v1' AND deleted = b'0'
    LIMIT 1
);

INSERT INTO bpm_approval_scheme_version
    (scheme_id, version_no, status, source_version_id, source_type, design_json,
     notify_json, change_summary, published_by, published_time, creator,
     create_time, updater, update_time, deleted)
SELECT @purchase_return_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL,
       '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @purchase_return_scheme_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_scheme_version
      WHERE scheme_id = @purchase_return_scheme_id AND deleted = b'0'
  );

SET @purchase_return_version_id = (
    SELECT id FROM bpm_approval_scheme_version
    WHERE scheme_id = @purchase_return_scheme_id AND deleted = b'0'
    ORDER BY id LIMIT 1
);

UPDATE bpm_approval_scheme_version
SET status = 30, design_json = '{}', updater = 'admin', update_time = NOW()
WHERE id = @purchase_return_version_id;

INSERT INTO bpm_approval_rule
    (scheme_version_id, rule_name, rule_type, priority, is_default, condition_json,
     process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @purchase_return_version_id, '默认规则', 'DEFAULT', 1, 1, NULL,
       'erp_purchase_return_approval', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @purchase_return_version_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_rule
      WHERE scheme_version_id = @purchase_return_version_id
        AND rule_name = '默认规则' AND deleted = b'0'
  );

UPDATE bpm_approval_rule
SET rule_type = 'DEFAULT', priority = 1, is_default = 1, condition_json = NULL,
    process_json = 'erp_purchase_return_approval', enabled = 1, deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @purchase_return_version_id
  AND rule_name = '默认规则';

UPDATE bpm_approval_scheme
SET active_version_id = @purchase_return_version_id,
    latest_version_id = @purchase_return_version_id,
    updater = 'admin', update_time = NOW()
WHERE id = @purchase_return_scheme_id;

UPDATE bpm_approval_scene
SET active_scheme_id = @purchase_return_scheme_id,
    updater = 'admin', update_time = NOW()
WHERE id = @purchase_return_scene_id;

SET FOREIGN_KEY_CHECKS = 1;
