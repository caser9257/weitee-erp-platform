-- Stock batch trace SCM permission fix.
-- Scope:
--   - ensure an enabled backend menu node exists for /scm/stock
--   - move stock-batch button permissions from the disabled legacy /stock tree to /scm/stock
--   - grant the SCM stock menu and stock-batch buttons to the local super_admin role
-- This script is idempotent and does not change API contracts or business data.

SET @scm_menu_id := (
    SELECT id
    FROM system_menu
    WHERE path = '/scm'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @scm_stock_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @scm_menu_id
      AND path = 'stock'
      AND component = 'erp/stock/stock/index'
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
       '产品库存',
       '',
       2,
       62,
       @scm_menu_id,
       'stock',
       'ep:coffee',
       'erp/stock/stock/index',
       'ProjectScmStock',
       0,
       b'1',
       b'0',
       b'0',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE @scm_menu_id IS NOT NULL
  AND @scm_stock_menu_id IS NULL;

SET @scm_stock_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @scm_menu_id
      AND path = 'stock'
      AND component = 'erp/stock/stock/index'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

UPDATE system_menu
SET parent_id = @scm_stock_menu_id,
    updater = '1',
    update_time = NOW()
WHERE @scm_stock_menu_id IS NOT NULL
  AND permission LIKE 'erp:stock-batch:%'
  AND deleted = b'0'
  AND parent_id <> @scm_stock_menu_id;

INSERT INTO system_role_menu (
    role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT role.id,
       menu.id,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0',
       role.tenant_id
FROM system_role role
JOIN system_menu menu ON menu.deleted = b'0'
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND (
      menu.id = @scm_stock_menu_id
      OR menu.permission LIKE 'erp:stock-batch:%'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu role_menu
      WHERE role_menu.role_id = role.id
        AND role_menu.menu_id = menu.id
        AND role_menu.tenant_id = role.tenant_id
        AND role_menu.deleted = b'0'
  );

SELECT
    @scm_menu_id AS scm_menu_id,
    @scm_stock_menu_id AS scm_stock_menu_id;

SELECT
    id,
    name,
    permission,
    type,
    status,
    parent_id,
    path,
    component,
    deleted
FROM system_menu
WHERE id = @scm_stock_menu_id
   OR permission LIKE 'erp:stock-batch:%'
ORDER BY type, id;

SELECT
    COUNT(*) AS super_admin_scm_stock_and_batch_permission_count
FROM system_role_menu role_menu
JOIN system_role role ON role.id = role_menu.role_id
JOIN system_menu menu ON menu.id = role_menu.menu_id
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND role_menu.deleted = b'0'
  AND (
      menu.id = @scm_stock_menu_id
      OR menu.permission LIKE 'erp:stock-batch:%'
  );
