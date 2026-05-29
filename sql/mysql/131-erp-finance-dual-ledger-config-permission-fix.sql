-- 双账套账簿映射 / 口径配置 权限补全
-- 目标：
-- 1. 补齐两个页面菜单（如缺失）
-- 2. 补齐 query / create / update / delete 按钮权限
-- 3. 授权给管理员、财务主管；财务经办授予 query

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @erp_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (id = 2563 OR path = '/erp' OR name = 'ERP 系统')
    ORDER BY id
    LIMIT 1
);

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
        OR component_name = 'FormalFinanceRoot'
        OR (parent_id = @erp_root_id AND path = 'finance')
        OR name = '财务管理'
      )
    ORDER BY
      CASE
        WHEN path = '/finance' THEN 0
        WHEN component_name = 'FormalFinanceRoot' THEN 1
        ELSE 2
      END,
      id
    LIMIT 1
);

SET @dual_ledger_config_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-config/index'
        OR component_name IN ('ErpFinanceDualLedgerConfig', 'FormalFinanceDualLedgerConfig')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-config')
      )
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_diff_config_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-diff-config/index'
        OR component_name IN ('ErpFinanceDualLedgerDiffConfig', 'FormalFinanceDualLedgerDiffConfig')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-diff-config')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射', '', 2, 45, @finance_root_id, 'dual-ledger-config', 'ep:connection',
       'erp/finance/dual-ledger-config/index', 'ErpFinanceDualLedgerConfig',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_config_page_id IS NULL;

SET @dual_ledger_config_page_id := COALESCE(@dual_ledger_config_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-config/index'
    ORDER BY id
    LIMIT 1
));

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置', '', 2, 46, @finance_root_id, 'dual-ledger-diff-config', 'ep:operation',
       'erp/finance/dual-ledger-diff-config/index', 'ErpFinanceDualLedgerDiffConfig',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_diff_config_page_id IS NULL;

SET @dual_ledger_diff_config_page_id := COALESCE(@dual_ledger_diff_config_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-diff-config/index'
    ORDER BY id
    LIMIT 1
));

SET @dual_ledger_config_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:delete'
    ORDER BY id LIMIT 1
);

SET @dual_ledger_diff_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:delete'
    ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射查询', 'erp:finance-dual-ledger-config:query', 3, 1, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射创建', 'erp:finance-dual-ledger-config:create', 3, 2, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_create_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射更新', 'erp:finance-dual-ledger-config:update', 3, 3, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射删除', 'erp:finance-dual-ledger-config:delete', 3, 4, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_delete_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置查询', 'erp:finance-dual-ledger-diff-config:query', 3, 1, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置创建', 'erp:finance-dual-ledger-diff-config:create', 3, 2, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_create_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置更新', 'erp:finance-dual-ledger-diff-config:update', 3, 3, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置删除', 'erp:finance-dual-ledger-diff-config:delete', 3, 4, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_delete_id IS NULL;

SET @dual_ledger_config_query_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:query' ORDER BY id LIMIT 1);
SET @dual_ledger_config_create_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:create' ORDER BY id LIMIT 1);
SET @dual_ledger_config_update_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:update' ORDER BY id LIMIT 1);
SET @dual_ledger_config_delete_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:delete' ORDER BY id LIMIT 1);
SET @dual_ledger_diff_query_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:query' ORDER BY id LIMIT 1);
SET @dual_ledger_diff_create_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:create' ORDER BY id LIMIT 1);
SET @dual_ledger_diff_update_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:update' ORDER BY id LIMIT 1);
SET @dual_ledger_diff_delete_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:delete' ORDER BY id LIMIT 1);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_config_page_id
    UNION ALL SELECT @dual_ledger_diff_config_page_id
    UNION ALL SELECT @dual_ledger_config_query_id
    UNION ALL SELECT @dual_ledger_config_create_id
    UNION ALL SELECT @dual_ledger_config_update_id
    UNION ALL SELECT @dual_ledger_config_delete_id
    UNION ALL SELECT @dual_ledger_diff_query_id
    UNION ALL SELECT @dual_ledger_diff_create_id
    UNION ALL SELECT @dual_ledger_diff_update_id
    UNION ALL SELECT @dual_ledger_diff_delete_id
) t
WHERE r.deleted = b'0'
  AND (r.id = 1 OR r.code IN ('super_admin', 'admin', 'erp_finance_manager'))
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_config_page_id
    UNION ALL SELECT @dual_ledger_diff_config_page_id
    UNION ALL SELECT @dual_ledger_config_query_id
    UNION ALL SELECT @dual_ledger_diff_query_id
) t
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_clerk'
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

SELECT id, name, permission, type, parent_id, path, component, component_name
FROM system_menu
WHERE deleted = b'0'
  AND (
      id IN (@dual_ledger_config_page_id, @dual_ledger_diff_config_page_id)
      OR permission IN (
          'erp:finance-dual-ledger-config:query',
          'erp:finance-dual-ledger-config:create',
          'erp:finance-dual-ledger-config:update',
          'erp:finance-dual-ledger-config:delete',
          'erp:finance-dual-ledger-diff-config:query',
          'erp:finance-dual-ledger-diff-config:create',
          'erp:finance-dual-ledger-diff-config:update',
          'erp:finance-dual-ledger-diff-config:delete'
      )
  )
ORDER BY parent_id, type, sort, id;

SET FOREIGN_KEY_CHECKS = 1;
