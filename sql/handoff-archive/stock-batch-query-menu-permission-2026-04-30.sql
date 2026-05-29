-- Stock batch trace frontend smoke permission patch.
-- Scope:
--   - add system menu permission erp:stock-batch:query when missing
--   - parent menu: component = erp/stock/stock/index
-- This script is idempotent and does not change API contracts.

SET @stock_batch_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/stock/stock/index'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '批次库存查询',
       'erp:stock-batch:query',
       3,
       11,
       @stock_batch_menu_id,
       '',
       '',
       '',
       '',
       0,
       b'1',
       b'1',
       b'1',
       'tester',
       NOW(),
       'tester',
       NOW(),
       b'0'
WHERE @stock_batch_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_menu
      WHERE permission = 'erp:stock-batch:query'
        AND deleted = b'0'
  );

SELECT
    @stock_batch_menu_id AS parent_menu_id,
    COUNT(*) AS stock_batch_query_permission_count
FROM system_menu
WHERE permission = 'erp:stock-batch:query'
  AND deleted = b'0';

SET @stock_batch_query_menu_id := (
    SELECT id
    FROM system_menu
    WHERE permission = 'erp:stock-batch:query'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_role_menu (
    role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT role.id,
       @stock_batch_query_menu_id,
       'tester',
       NOW(),
       'tester',
       NOW(),
       b'0',
       role.tenant_id
FROM system_role role
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND @stock_batch_query_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu role_menu
      WHERE role_menu.role_id = role.id
        AND role_menu.menu_id = @stock_batch_query_menu_id
        AND role_menu.tenant_id = role.tenant_id
        AND role_menu.deleted = b'0'
  );

SELECT
    @stock_batch_query_menu_id AS stock_batch_query_menu_id,
    COUNT(*) AS super_admin_role_menu_count
FROM system_role_menu role_menu
JOIN system_role role ON role.id = role_menu.role_id
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND role_menu.menu_id = @stock_batch_query_menu_id
  AND role_menu.deleted = b'0';
