-- Stock batch trace SCM menu diagnostics.
-- Scope: read-only checks for the enabled /scm menu chain used by the frontend route.

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
WHERE id = 930140
   OR parent_id = 930140
   OR path IN ('/scm', 'stock')
   OR component = 'erp/stock/stock/index'
ORDER BY parent_id, sort, id;

SELECT
    child.id,
    child.name,
    child.permission,
    child.type,
    child.status,
    child.parent_id,
    child.path,
    child.component,
    parent.id AS parent_menu_id,
    parent.name AS parent_menu_name,
    parent.status AS parent_menu_status,
    grand.id AS grand_menu_id,
    grand.name AS grand_menu_name,
    grand.status AS grand_menu_status
FROM system_menu child
LEFT JOIN system_menu parent ON parent.id = child.parent_id
LEFT JOIN system_menu grand ON grand.id = parent.parent_id
WHERE child.component = 'erp/stock/stock/index'
   OR child.path = 'stock'
ORDER BY child.id;
