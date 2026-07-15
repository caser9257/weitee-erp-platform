-- 销售订单 BPM 场景、方案、版本和默认规则
-- 幂等：恢复历史软删除配置，兼容已有可用方案。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

-- 历史数据库中的销售订单 BPM 定义曾按租户 1 部署。Flowable 的实际唯一键不含租户，
-- 先统一到无租户值，避免初始化器按无租户查询为空后从版本 1 重复部署。
UPDATE ACT_RE_DEPLOYMENT d
SET d.TENANT_ID_ = ''
WHERE d.TENANT_ID_ = '1'
  AND EXISTS (
      SELECT 1 FROM ACT_RE_PROCDEF p
      WHERE p.DEPLOYMENT_ID_ = d.ID_ AND p.KEY_ = 'erp_sale_order'
  );

UPDATE ACT_RE_PROCDEF
SET TENANT_ID_ = ''
WHERE KEY_ = 'erp_sale_order' AND TENANT_ID_ = '1';

UPDATE bpm_approval_scene
SET name = '销售订单审批', module_code = 'erp_sale', biz_type = 'sale_order', action_code = 'submit',
    status = 1, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE scene_code = 'erp.sale.order.submit';

INSERT INTO bpm_approval_scene
    (scene_code, name, module_code, biz_type, action_code, active_scheme_id,
     owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.sale.order.submit', '销售订单审批', 'erp_sale', 'sale_order', 'submit', NULL,
       0, 1, '销售模块-销售订单 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM bpm_approval_scene WHERE scene_code = 'erp.sale.order.submit'
);

SET @sale_order_scene_id = (
    SELECT id FROM bpm_approval_scene WHERE scene_code = 'erp.sale.order.submit' LIMIT 1
);

UPDATE bpm_approval_scheme
SET name = '销售订单审批方案', module_code = 'erp_sale', biz_type = 'sale_order',
    scene_id = @sale_order_scene_id, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE code = 'erp.sale.order.submit.scheme.v2';

INSERT INTO bpm_approval_scheme
    (code, name, module_code, biz_type, scene_id, remark, active_version_id,
     latest_version_id, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.sale.order.submit.scheme.v2', '销售订单审批方案', 'erp_sale', 'sale_order',
       @sale_order_scene_id, '销售订单默认审批方案', NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_order_scene_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_scheme WHERE code = 'erp.sale.order.submit.scheme.v2'
  );

SET @sale_order_scheme_id = (
    SELECT id FROM bpm_approval_scheme WHERE code = 'erp.sale.order.submit.scheme.v2' LIMIT 1
);

UPDATE bpm_approval_scheme_version
SET status = 30, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE scheme_id = @sale_order_scheme_id AND version_no = 1;

INSERT INTO bpm_approval_scheme_version
    (scheme_id, version_no, status, source_version_id, source_type, design_json,
     notify_json, change_summary, published_by, published_time, creator,
     create_time, updater, update_time, deleted)
SELECT @sale_order_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL,
       '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_order_scheme_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_scheme_version
      WHERE scheme_id = @sale_order_scheme_id AND version_no = 1
  );

SET @sale_order_version_id = (
    SELECT id FROM bpm_approval_scheme_version
    WHERE scheme_id = @sale_order_scheme_id AND version_no = 1 LIMIT 1
);

UPDATE bpm_approval_rule
SET rule_type = 'DEFAULT', priority = 1, is_default = 1, condition_json = NULL,
    process_json = 'erp_sale_order', enabled = 1, deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @sale_order_version_id AND rule_name = '默认规则';

INSERT INTO bpm_approval_rule
    (scheme_version_id, rule_name, rule_type, priority, is_default, condition_json,
     process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @sale_order_version_id, '默认规则', 'DEFAULT', 1, 1, NULL,
       'erp_sale_order', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_order_version_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM bpm_approval_rule
      WHERE scheme_version_id = @sale_order_version_id AND rule_name = '默认规则'
  );

SET @sale_order_default_rule_id = (
    SELECT id FROM bpm_approval_rule
    WHERE scheme_version_id = @sale_order_version_id AND rule_name = '默认规则' LIMIT 1
);

UPDATE bpm_approval_rule
SET is_default = b'0', enabled = 0, updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @sale_order_version_id
  AND rule_type = 'DEFAULT'
  AND id <> @sale_order_default_rule_id;

UPDATE bpm_approval_scheme
SET scene_id = @sale_order_scene_id,
    active_version_id = @sale_order_version_id,
    latest_version_id = @sale_order_version_id,
    updater = 'admin', update_time = NOW()
WHERE id = @sale_order_scheme_id;

UPDATE bpm_approval_scene
SET active_scheme_id = @sale_order_scheme_id,
    updater = 'admin', update_time = NOW()
WHERE id = @sale_order_scene_id;

SET FOREIGN_KEY_CHECKS = 1;
