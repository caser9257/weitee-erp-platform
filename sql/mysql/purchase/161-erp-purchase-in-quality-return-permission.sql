-- IQC 不合格退货沿用后端 create-return 的 update 权限。
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @iqc_menu_id = (
  SELECT id FROM system_menu
  WHERE component_name = 'ErpPurchaseInQuality' AND deleted = b'0'
  ORDER BY id ASC LIMIT 1
);
SET @iqc_menu_id = COALESCE(@iqc_menu_id, (
  SELECT parent_id FROM system_menu
  WHERE permission = 'erp:purchase-in-quality:query' AND deleted = b'0'
  ORDER BY id ASC LIMIT 1
));
SET @update_menu_id = (
  SELECT id FROM system_menu
  WHERE permission = 'erp:purchase-in-quality:update' AND deleted = b'0'
  ORDER BY id ASC LIMIT 1
);
INSERT INTO system_menu
(id, name, permission, type, sort, parent_id, path, icon, component, component_name,
 status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(@update_menu_id, 920570), '质检不合格退货', 'erp:purchase-in-quality:update', 3, 8, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE name = VALUES(name), permission = VALUES(permission), parent_id = VALUES(parent_id),
                        deleted = b'0', updater = '1', update_time = NOW();

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 1, COALESCE(@update_menu_id, 920570), '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = COALESCE(@update_menu_id, 920570) AND deleted = b'0'
);
