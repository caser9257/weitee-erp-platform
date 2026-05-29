-- Stock batch trace permission diagnostics.
-- Scope: read-only checks for the current tenant, current login user, menu, role and tenant package.

SELECT
    t.id AS tenant_id,
    t.name AS tenant_name,
    t.package_id AS tenant_package_id,
    tp.name AS tenant_package_name,
    tp.status AS tenant_package_status,
    CHAR_LENGTH(CAST(tp.menu_ids AS CHAR)) AS tenant_package_menu_ids_length,
    LOCATE('990009', CAST(tp.menu_ids AS CHAR)) > 0 AS package_contains_stock_batch_permission
FROM system_tenant t
LEFT JOIN system_tenant_package tp ON tp.id = t.package_id
WHERE t.id = 1;

SELECT
    id,
    name,
    permission,
    type,
    status,
    parent_id,
    component,
    deleted,
    creator,
    create_time,
    updater,
    update_time
FROM system_menu
WHERE id IN (2590, 990009)
   OR permission = 'erp:stock-batch:query'
   OR component = 'erp/stock/stock/index'
ORDER BY id;

SELECT
    ur.user_id,
    ur.role_id,
    r.name AS role_name,
    r.code AS role_code,
    r.status AS role_status,
    r.type AS role_type,
    r.tenant_id AS role_tenant_id,
    ur.deleted AS user_role_deleted
FROM system_user_role ur
JOIN system_role r ON r.id = ur.role_id
WHERE ur.user_id = 145
ORDER BY ur.role_id;

SELECT
    r.id AS role_id,
    r.name AS role_name,
    r.code AS role_code,
    r.status AS role_status,
    r.tenant_id AS role_tenant_id,
    rm.menu_id,
    rm.tenant_id AS role_menu_tenant_id,
    rm.deleted AS role_menu_deleted
FROM system_role r
LEFT JOIN system_role_menu rm ON rm.role_id = r.id
    AND rm.menu_id = 990009
WHERE r.code = 'super_admin'
   OR rm.menu_id = 990009
ORDER BY r.id;
