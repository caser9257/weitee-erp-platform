-- Keep the supply-chain experience account able to submit and cancel other stock-out approvals.
-- This migration is idempotent and only adds missing menu permissions/grants.

SET NAMES utf8mb4;

SET @erp_stock_out_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
          component = 'erp/stock/out/index'
          OR component_name IN ('ErpStockOut', 'FormalScmStockOut')
      )
    ORDER BY id
    LIMIT 1
);
SET @erp_stock_out_permission_next_id := (SELECT COALESCE(MAX(id), 0) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @erp_stock_out_permission_next_id, 'Stock-out submit', 'erp:stock-out:submit', 3, 7,
       @erp_stock_out_page_id, '', '', '', '', 0, b'1', b'1', b'0', 'tester', NOW(), 'tester', NOW(), b'0'
WHERE @erp_stock_out_page_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:stock-out:submit'
  );

SET @erp_stock_out_permission_next_id := @erp_stock_out_permission_next_id + 1;
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @erp_stock_out_permission_next_id, 'Stock-out cancel approval', 'erp:stock-out:cancel-approval', 3, 8,
       @erp_stock_out_page_id, '', '', '', '', 0, b'1', b'1', b'0', 'tester', NOW(), 'tester', NOW(), b'0'
WHERE @erp_stock_out_page_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:stock-out:cancel-approval'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT role.id, menu.id, 'tester', NOW(), 'tester', NOW(), b'0'
FROM system_role role
JOIN system_menu menu
  ON menu.deleted = b'0'
 AND menu.permission IN ('erp:stock-out:submit', 'erp:stock-out:cancel-approval')
WHERE role.deleted = b'0'
  AND role.code = 'supply_chain_manager'
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
        AND existing.deleted = b'0'
  );
