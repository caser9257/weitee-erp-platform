-- Stock batch menu parent chain diagnostics.
-- Scope: read-only checks for status filtering in AuthController.getPermissionInfo.

WITH RECURSIVE menu_chain AS (
    SELECT id, name, permission, type, status, parent_id, component, deleted, 0 AS depth
    FROM system_menu
    WHERE id = 990009
    UNION ALL
    SELECT parent.id, parent.name, parent.permission, parent.type, parent.status, parent.parent_id,
           parent.component, parent.deleted, child.depth + 1 AS depth
    FROM system_menu parent
    JOIN menu_chain child ON child.parent_id = parent.id
    WHERE child.parent_id <> 0
)
SELECT id, name, permission, type, status, parent_id, component, deleted, depth
FROM menu_chain
ORDER BY depth DESC;

SELECT COUNT(*) AS total_enabled_menu_count
FROM system_menu
WHERE deleted = b'0'
  AND status = 0;

SELECT COUNT(*) AS stock_batch_permission_visible_by_raw_menu
FROM system_menu
WHERE deleted = b'0'
  AND status = 0
  AND permission = 'erp:stock-batch:query';

SELECT
    id,
    name,
    permission,
    type,
    status,
    parent_id,
    path,
    component,
    sort,
    deleted
FROM system_menu
WHERE parent_id IN (0, 2583, 2590)
   OR id IN (2583, 2590, 990009)
ORDER BY parent_id, sort, id;
