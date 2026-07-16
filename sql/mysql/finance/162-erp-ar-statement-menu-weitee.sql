-- 当前 weitee-erp 库的应收台账菜单与权限种子。
-- 与旧版 sql/mysql/158-erp-ar-statement-menu.sql 不同，本脚本不依赖 ruoyi-vue-pro 库名，
-- 并按当前库中的 /finance 父菜单动态建立菜单和角色授权。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `weitee-erp`;

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = 0
      AND path = '/finance'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @ar_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/finance/ar-statement/index'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @ar_menu_id := COALESCE(@ar_menu_id, (SELECT COALESCE(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @ar_menu_id, '应收台账', 'erp:ar-statement:query', 2, 25, @finance_root_id,
       'ar-statement', 'ep:money', 'erp/finance/ar-statement/index', 'ErpArStatement',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
  WHERE component = 'erp/finance/ar-statement/index' AND deleted = b'0'
  );

UPDATE system_menu
SET parent_id = @finance_root_id,
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE component = 'erp/finance/ar-statement/index'
  AND deleted = b'0'
  AND @finance_root_id IS NOT NULL;

SET @ar_query_menu_id := (
    SELECT id FROM system_menu
    WHERE parent_id = @ar_menu_id
      AND permission = 'erp:ar-statement:query'
      AND type = 3
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @ar_query_menu_id := COALESCE(@ar_query_menu_id, (SELECT COALESCE(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @ar_query_menu_id, '查询', 'erp:ar-statement:query', 3, 1, @ar_menu_id,
       '', '', '', '', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ar_menu_id IS NOT NULL
  AND @finance_root_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @ar_menu_id
        AND permission = 'erp:ar-statement:query'
        AND type = 3
        AND deleted = b'0'
  );

SET @ar_export_menu_id := (
    SELECT id FROM system_menu
    WHERE parent_id = @ar_menu_id
      AND permission = 'erp:ar-statement:export'
      AND type = 3
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @ar_export_menu_id := COALESCE(@ar_export_menu_id, (SELECT COALESCE(MAX(id), 0) + 1 FROM system_menu));

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @ar_export_menu_id, '导出', 'erp:ar-statement:export', 3, 2, @ar_menu_id,
       '', '', '', '', 0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ar_menu_id IS NOT NULL
  AND @finance_root_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @ar_menu_id
        AND permission = 'erp:ar-statement:export'
        AND type = 3
        AND deleted = b'0'
  );

SET @role_manager_id := (SELECT id FROM system_role WHERE code = 'erp_finance_manager' AND deleted = b'0' ORDER BY id LIMIT 1);
SET @role_clerk_id := (SELECT id FROM system_role WHERE code = 'erp_finance_clerk' AND deleted = b'0' ORDER BY id LIMIT 1);
SET @role_audit_id := (SELECT id FROM system_role WHERE code = 'finance_audit' AND deleted = b'0' ORDER BY id LIMIT 1);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT role_id, menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
    SELECT @role_manager_id AS role_id, @ar_menu_id AS menu_id
    UNION ALL SELECT @role_manager_id, @ar_query_menu_id
    UNION ALL SELECT @role_manager_id, @ar_export_menu_id
    UNION ALL SELECT @role_clerk_id, @ar_menu_id
    UNION ALL SELECT @role_clerk_id, @ar_query_menu_id
    UNION ALL SELECT @role_clerk_id, @ar_export_menu_id
    UNION ALL SELECT @role_audit_id, @ar_menu_id
    UNION ALL SELECT @role_audit_id, @ar_query_menu_id
) role_menu
WHERE @finance_root_id IS NOT NULL
  AND role_id IS NOT NULL
  AND menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu existing
      WHERE existing.role_id = role_menu.role_id
        AND existing.menu_id = role_menu.menu_id
        AND existing.deleted = b'0'
  );

SELECT m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component
FROM system_menu m
WHERE m.component = 'erp/finance/ar-statement/index'
  AND m.deleted = b'0';
