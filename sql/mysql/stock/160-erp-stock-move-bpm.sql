-- 库存调拨 BPM：流程实例字段、审批场景和操作权限。
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @column_exists = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_stock_move' AND COLUMN_NAME = 'process_instance_id'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE erp_stock_move ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT ''BPM 流程实例 ID'' AFTER status',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @scene_exists = (SELECT COUNT(*) FROM bpm_approval_scene WHERE scene_code = 'erp.stock.move.submit' AND deleted = b'0');
INSERT INTO bpm_approval_scene (scene_code, name, module_code, biz_type, action_code, owner_user_id, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.move.submit', '库存调拨审批', 'erp_stock', 'stock_move', 'submit', 0, 1, '库存调拨 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @scene_exists = 0;

SET @move_scene_id = (SELECT id FROM bpm_approval_scene WHERE scene_code = 'erp.stock.move.submit' AND deleted = b'0' LIMIT 1);
SET @move_scheme_id = (SELECT id FROM bpm_approval_scheme WHERE code = 'erp.stock.move.scheme.v1' AND deleted = b'0' LIMIT 1);
INSERT INTO bpm_approval_scheme (code, name, module_code, biz_type, scene_id, remark, owner_user_id, creator, create_time, updater, update_time, deleted)
SELECT 'erp.stock.move.scheme.v1', '库存调拨审批方案', 'erp_stock', 'stock_move', @move_scene_id, '库存调拨 BPM 审批', 0, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @move_scene_id IS NOT NULL AND @move_scheme_id IS NULL;
SET @move_scheme_id = (SELECT id FROM bpm_approval_scheme WHERE code = 'erp.stock.move.scheme.v1' AND deleted = b'0' LIMIT 1);
SET @move_version_id = (SELECT id FROM bpm_approval_scheme_version WHERE scheme_id = @move_scheme_id AND deleted = b'0' ORDER BY id ASC LIMIT 1);
INSERT INTO bpm_approval_scheme_version (scheme_id, version_no, status, source_type, design_json, change_summary, published_by, published_time, creator, create_time, updater, update_time, deleted)
SELECT @move_scheme_id, 1, 30, 'CREATE', '{}', '初始版本', 'admin', NOW(), 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @move_scheme_id IS NOT NULL AND @move_version_id IS NULL;
SET @move_version_id = (SELECT id FROM bpm_approval_scheme_version WHERE scheme_id = @move_scheme_id AND deleted = b'0' ORDER BY id ASC LIMIT 1);
INSERT INTO bpm_approval_rule (scheme_version_id, rule_name, rule_type, priority, is_default, process_json, enabled, creator, create_time, updater, update_time, deleted)
SELECT @move_version_id, '默认规则', 'DEFAULT', 1, 1, 'erp_stock_out_approval', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @move_version_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM bpm_approval_rule WHERE scheme_version_id = @move_version_id AND rule_name = '默认规则' AND deleted = b'0');
UPDATE bpm_approval_scheme
SET active_version_id = @move_version_id, latest_version_id = @move_version_id, updater = 'admin', update_time = NOW()
WHERE id = @move_scheme_id;
UPDATE bpm_approval_scene
SET active_scheme_id = @move_scheme_id, updater = 'admin', update_time = NOW()
WHERE id = @move_scene_id;

SET @move_menu_id = (SELECT id FROM system_menu WHERE permission = 'erp:stock-move:query' AND deleted = b'0' LIMIT 1);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '提交审批', 'erp:stock-move:submit', 3, 40, @move_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @move_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-move:submit' AND deleted = b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT '撤回审批', 'erp:stock-move:cancel-approval', 3, 41, @move_menu_id, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @move_menu_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-move:cancel-approval' AND deleted = b'0');
