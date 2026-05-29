SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;
SET @role_id := (
    SELECT id
    FROM system_role
    WHERE code = 'erp_finance_manager'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @assets_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/assets/index'
            OR component_name IN ('FormalFinanceAssets', 'ProjectFinanceAssets')
            OR path = 'assets'
        )
    ORDER BY id
    LIMIT 1
);

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
            OR component_name IN ('FormalFinanceRoot')
            OR name = '财务管理'
        )
    ORDER BY id
    LIMIT 1
);

UPDATE system_menu
SET parent_id = COALESCE(@finance_root_id, parent_id),
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @assets_menu_id
  AND deleted = b'0';

SET @assets_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/assets/index'
            OR component_name IN ('FormalFinanceAssets', 'ProjectFinanceAssets')
            OR path = 'assets'
        )
    ORDER BY id
    LIMIT 1
);

SET @asset_query_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset:query'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产查询', 'erp:finance-asset:query', 3, 1, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_query_menu_id IS NULL;

SET @asset_create_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset:create'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产创建', 'erp:finance-asset:create', 3, 2, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_create_menu_id IS NULL;

SET @asset_update_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset:update'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产更新', 'erp:finance-asset:update', 3, 3, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_update_menu_id IS NULL;

SET @asset_delete_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset:delete'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产删除', 'erp:finance-asset:delete', 3, 4, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_delete_menu_id IS NULL;

SET @asset_status_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset:update-status'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产状态更新', 'erp:finance-asset:update-status', 3, 5, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @asset_status_menu_id IS NULL;

SET @candidate_query_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset-candidate:query'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产候选查询', 'erp:finance-asset-candidate:query', 3, 6, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @candidate_query_menu_id IS NULL;

SET @candidate_confirm_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset-candidate:confirm'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产候选确认', 'erp:finance-asset-candidate:confirm', 3, 7, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @candidate_confirm_menu_id IS NULL;

SET @depreciation_query_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset-depreciation:query'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产折旧查询', 'erp:finance-asset-depreciation:query', 3, 8, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @depreciation_query_menu_id IS NULL;

SET @depreciation_generate_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-asset-depreciation:generate'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '固定资产折旧生成', 'erp:finance-asset-depreciation:generate', 3, 9, @assets_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @assets_menu_id IS NOT NULL
  AND @depreciation_generate_menu_id IS NULL;

UPDATE system_menu
SET parent_id = @assets_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
                     'erp:finance-asset:query',
                     'erp:finance-asset:create',
                     'erp:finance-asset:update',
                     'erp:finance-asset:delete',
                     'erp:finance-asset:update-status',
                     'erp:finance-asset-candidate:query',
                     'erp:finance-asset-candidate:confirm',
                     'erp:finance-asset-depreciation:query',
                     'erp:finance-asset-depreciation:generate'
    );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_id IS NOT NULL
  AND m.deleted = b'0'
  AND (
    m.id = @assets_menu_id
        OR m.permission IN (
                            'erp:finance-asset:query',
                            'erp:finance-asset:create',
                            'erp:finance-asset:update',
                            'erp:finance-asset:delete',
                            'erp:finance-asset:update-status',
                            'erp:finance-asset-candidate:query',
                            'erp:finance-asset-candidate:confirm',
                            'erp:finance-asset-depreciation:query',
                            'erp:finance-asset-depreciation:generate'
        )
    )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
);

SELECT r.code AS role_code, m.id, m.name, m.permission
FROM system_role_menu rm
         JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
         JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND (
    m.id = @assets_menu_id
        OR m.permission LIKE 'erp:finance-asset%'
    )
ORDER BY m.id;

SET FOREIGN_KEY_CHECKS = 1;
