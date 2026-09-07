-- 138b-supply-chain-bpm-seed-data.sql
-- 供应链模块 BPM 审批场景/方案/规则种子数据
-- 幂等写法：先检查是否存在，再插入

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 审批场景 ==========

-- 采购退货审批场景
INSERT INTO bpm_approval_scene (scene_code, name, module_code, biz_type, action_code, active_scheme_id, owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.purchase.return.submit', '采购退货审批', 'erp_purchase', 'purchase_return', 'submit', NULL, 0, 1, '供应链模块-采购退货 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM bpm_approval_scene WHERE scene_code = 'erp.purchase.return.submit' AND deleted = b'0');

-- 其它入库审批场景
INSERT INTO bpm_approval_scene (scene_code, name, module_code, biz_type, action_code, active_scheme_id, owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.in.submit', '其它入库审批', 'erp_stock', 'stock_in', 'submit', NULL, 0, 1, '供应链模块-其它入库 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM bpm_approval_scene WHERE scene_code = 'erp.stock.in.submit' AND deleted = b'0');

-- 其它出库审批场景
INSERT INTO bpm_approval_scene (scene_code, name, module_code, biz_type, action_code, active_scheme_id, owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.out.submit', '其它出库审批', 'erp_stock', 'stock_out', 'submit', NULL, 0, 1, '供应链模块-其它出库 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM bpm_approval_scene WHERE scene_code = 'erp.stock.out.submit' AND deleted = b'0');

-- ========== 2. 审批方案 ==========

-- 获取场景 ID
SET @pr_scene_id = (SELECT id FROM bpm_approval_scene WHERE scene_code = 'erp.purchase.return.submit' AND deleted = b'0' LIMIT 1);
SET @si_scene_id = (SELECT id FROM bpm_approval_scene WHERE scene_code = 'erp.stock.in.submit' AND deleted = b'0' LIMIT 1);
SET @so_scene_id = (SELECT id FROM bpm_approval_scene WHERE scene_code = 'erp.stock.out.submit' AND deleted = b'0' LIMIT 1);

-- 采购退货方案
INSERT INTO bpm_approval_scheme (code, name, module_code, biz_type, scene_id, remark, active_version_id, latest_version_id, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.purchase.return.scheme.v1', '采购退货审批方案', 'erp_purchase', 'purchase_return', @pr_scene_id, '采购退货默认审批方案', NULL, NULL, 0, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @pr_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme WHERE code = 'erp.purchase.return.scheme.v1' AND deleted = b'0');

-- 其它入库方案
INSERT INTO bpm_approval_scheme (code, name, module_code, biz_type, scene_id, remark, active_version_id, latest_version_id, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.in.scheme.v1', '其它入库审批方案', 'erp_stock', 'stock_in', @si_scene_id, '其它入库默认审批方案', NULL, NULL, 0, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @si_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme WHERE code = 'erp.stock.in.scheme.v1' AND deleted = b'0');

-- 其它出库方案
INSERT INTO bpm_approval_scheme (code, name, module_code, biz_type, scene_id, remark, active_version_id, latest_version_id, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.out.scheme.v1', '其它出库审批方案', 'erp_stock', 'stock_out', @so_scene_id, '其它出库默认审批方案', NULL, NULL, 0, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @so_scene_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme WHERE code = 'erp.stock.out.scheme.v1' AND deleted = b'0');

-- 获取方案 ID
SET @pr_scheme_id = (SELECT id FROM bpm_approval_scheme WHERE code = 'erp.purchase.return.scheme.v1' AND deleted = b'0' LIMIT 1);
SET @si_scheme_id = (SELECT id FROM bpm_approval_scheme WHERE code = 'erp.stock.in.scheme.v1' AND deleted = b'0' LIMIT 1);
SET @so_scheme_id = (SELECT id FROM bpm_approval_scheme WHERE code = 'erp.stock.out.scheme.v1' AND deleted = b'0' LIMIT 1);

-- ========== 3. 审批方案版本 ==========

-- 采购退货方案版本
INSERT INTO bpm_approval_scheme_version (scheme_id, version_no, status, source_version_id, source_type, design_json, notify_json, change_summary, published_by, published_time, creator, create_time, updater, update_time, deleted)
SELECT @pr_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @pr_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme_version WHERE scheme_id = @pr_scheme_id AND deleted = b'0');

-- 其它入库方案版本
INSERT INTO bpm_approval_scheme_version (scheme_id, version_no, status, source_version_id, source_type, design_json, notify_json, change_summary, published_by, published_time, creator, create_time, updater, update_time, deleted)
SELECT @si_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @si_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme_version WHERE scheme_id = @si_scheme_id AND deleted = b'0');

-- 其它出库方案版本
INSERT INTO bpm_approval_scheme_version (scheme_id, version_no, status, source_version_id, source_type, design_json, notify_json, change_summary, published_by, published_time, creator, create_time, updater, update_time, deleted)
SELECT @so_scheme_id, 1, 30, NULL, 'CREATE', '{}', NULL, '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @so_scheme_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_scheme_version WHERE scheme_id = @so_scheme_id AND deleted = b'0');

-- 获取方案版本 ID
SET @pr_version_id = (SELECT id FROM bpm_approval_scheme_version WHERE scheme_id = @pr_scheme_id AND deleted = b'0' LIMIT 1);
SET @si_version_id = (SELECT id FROM bpm_approval_scheme_version WHERE scheme_id = @si_scheme_id AND deleted = b'0' LIMIT 1);
SET @so_version_id = (SELECT id FROM bpm_approval_scheme_version WHERE scheme_id = @so_scheme_id AND deleted = b'0' LIMIT 1);

-- ========== 4. 审批规则 ==========

-- 采购退货规则
INSERT INTO bpm_approval_rule (scheme_version_id, rule_name, rule_type, priority, is_default, condition_json, process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @pr_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_purchase_return_approval', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @pr_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_rule WHERE scheme_version_id = @pr_version_id AND rule_name = '默认规则' AND deleted = b'0');

UPDATE bpm_approval_rule
SET rule_type = 'DEFAULT', priority = 1, is_default = 1, condition_json = NULL,
    process_json = 'erp_purchase_return_approval', enabled = 1, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @pr_version_id AND rule_name = '默认规则';

-- 其它入库规则
INSERT INTO bpm_approval_rule (scheme_version_id, rule_name, rule_type, priority, is_default, condition_json, process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @si_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_stock_in_approval', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @si_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_rule WHERE scheme_version_id = @si_version_id AND rule_name = '默认规则' AND deleted = b'0');

UPDATE bpm_approval_rule
SET rule_type = 'DEFAULT', priority = 1, is_default = 1, condition_json = NULL,
    process_json = 'erp_stock_in_approval', enabled = 1, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @si_version_id AND rule_name = '默认规则';

-- 其它出库规则
INSERT INTO bpm_approval_rule (scheme_version_id, rule_name, rule_type, priority, is_default, condition_json, process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @so_version_id, '默认规则', 'DEFAULT', 1, 1, NULL, 'erp_stock_out_approval', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @so_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_rule WHERE scheme_version_id = @so_version_id AND rule_name = '默认规则' AND deleted = b'0');

UPDATE bpm_approval_rule
SET rule_type = 'DEFAULT', priority = 1, is_default = 1, condition_json = NULL,
    process_json = 'erp_stock_out_approval', enabled = 1, deleted = b'0', updater = 'admin', update_time = NOW()
WHERE scheme_version_id = @so_version_id AND rule_name = '默认规则';

-- ========== 5. 回填场景的 active_scheme_id ==========

UPDATE bpm_approval_scene SET active_scheme_id = @pr_scheme_id, updater = 'admin', update_time = NOW()
WHERE scene_code = 'erp.purchase.return.submit' AND (active_scheme_id IS NULL OR active_scheme_id != @pr_scheme_id);

UPDATE bpm_approval_scene SET active_scheme_id = @si_scheme_id, updater = 'admin', update_time = NOW()
WHERE scene_code = 'erp.stock.in.submit' AND (active_scheme_id IS NULL OR active_scheme_id != @si_scheme_id);

UPDATE bpm_approval_scene SET active_scheme_id = @so_scheme_id, updater = 'admin', update_time = NOW()
WHERE scene_code = 'erp.stock.out.submit' AND (active_scheme_id IS NULL OR active_scheme_id != @so_scheme_id);

-- ========== 6. 回填方案的 active_version_id 和 latest_version_id ==========

UPDATE bpm_approval_scheme SET active_version_id = @pr_version_id, latest_version_id = @pr_version_id, updater = 'admin', update_time = NOW()
WHERE code = 'erp.purchase.return.scheme.v1' AND deleted = b'0'
  AND (active_version_id != @pr_version_id OR latest_version_id != @pr_version_id OR active_version_id IS NULL OR latest_version_id IS NULL);

UPDATE bpm_approval_scheme SET active_version_id = @si_version_id, latest_version_id = @si_version_id, updater = 'admin', update_time = NOW()
WHERE code = 'erp.stock.in.scheme.v1' AND deleted = b'0'
  AND (active_version_id != @si_version_id OR latest_version_id != @si_version_id OR active_version_id IS NULL OR latest_version_id IS NULL);

UPDATE bpm_approval_scheme SET active_version_id = @so_version_id, latest_version_id = @so_version_id, updater = 'admin', update_time = NOW()
WHERE code = 'erp.stock.out.scheme.v1' AND deleted = b'0'
  AND (active_version_id != @so_version_id OR latest_version_id != @so_version_id OR active_version_id IS NULL OR latest_version_id IS NULL);

SET FOREIGN_KEY_CHECKS = 1;
