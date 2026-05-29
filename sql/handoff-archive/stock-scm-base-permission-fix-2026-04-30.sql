-- SCM stock base permission fix.
-- Scope:
--   - move product stock page base button permissions from disabled legacy /stock tree to enabled /scm/stock
--   - grant the moved permissions to the local super_admin role
-- This script is idempotent and does not change API contracts or business data.

SET @scm_stock_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = (
        SELECT id
        FROM system_menu
        WHERE path = '/scm'
          AND deleted = b'0'
        ORDER BY id
        LIMIT 1
    )
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
  AND permission IN ('erp:stock:query', 'erp:stock:export')
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
  AND menu.permission IN ('erp:stock:query', 'erp:stock:export')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu role_menu
      WHERE role_menu.role_id = role.id
        AND role_menu.menu_id = menu.id
        AND role_menu.tenant_id = role.tenant_id
        AND role_menu.deleted = b'0'
  );

SELECT
    @scm_stock_menu_id AS scm_stock_menu_id;

SELECT
    id,
    name,
    permission,
    type,
    status,
    parent_id,
    deleted
FROM system_menu
WHERE permission IN ('erp:stock:query', 'erp:stock:export')
ORDER BY id;
