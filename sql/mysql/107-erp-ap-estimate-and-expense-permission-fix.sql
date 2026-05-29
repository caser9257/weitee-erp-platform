-- 采购暂估入库 + 研发报销/零星采购 权限补全
-- 目标：
-- 1. 补 ap-estimate 页面菜单及按钮权限种子（如缺失）
-- 2. 补 finance-expense 页面菜单及按钮权限种子（如缺失）
-- 3. 授权给 erp_finance_manager，解决 403 与页面初始化报错

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
        id = 2645
        OR (parent_id = @erp_root_id AND path = 'finance')
        OR name = '财务管理'
      )
    ORDER BY id
    LIMIT 1
);

SET @ap_estimate_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/ap-estimate/index'
        OR component_name IN ('FormalFinanceApEstimate', 'ProjectFinanceApEstimate', 'ErpApEstimate')
        OR (parent_id = @finance_root_id AND path = 'ap-estimate')
      )
    ORDER BY id
    LIMIT 1
);

SET @expense_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/expense/index'
        OR component_name IN ('FormalFinanceExpense', 'ProjectFinanceExpense', 'ErpFinanceExpense')
        OR (parent_id = @finance_root_id AND path = 'expense')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

-- 1. 如缺页面菜单，先补页面菜单
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '采购暂估入库', '', 2, 30, @finance_root_id, 'ap-estimate', 'ep:document',
       'erp/finance/ap-estimate/index', 'FormalFinanceApEstimate',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @ap_estimate_menu_id IS NULL;

SET @ap_estimate_menu_id := COALESCE(@ap_estimate_menu_id, (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND component = 'erp/finance/ap-estimate/index'
    ORDER BY id LIMIT 1
));

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '研发报销 / 零星采购', '', 2, 40, @finance_root_id, 'expense', 'ep:wallet-filled',
       'erp/finance/expense/index', 'FormalFinanceExpense',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @expense_menu_id IS NULL;

SET @expense_menu_id := COALESCE(@expense_menu_id, (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND component = 'erp/finance/expense/index'
    ORDER BY id LIMIT 1
));

-- 2. 补 ap-estimate 权限种子
SET @ap_estimate_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:query' ORDER BY id LIMIT 1
);
SET @ap_estimate_scan_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:scan' ORDER BY id LIMIT 1
);
SET @ap_estimate_update_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:update' ORDER BY id LIMIT 1
);
SET @ap_estimate_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:export' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '暂估单查询', 'erp:ap-estimate:query', 3, 1, @ap_estimate_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND @ap_estimate_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '暂估单扫描', 'erp:ap-estimate:scan', 3, 2, @ap_estimate_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND @ap_estimate_scan_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '暂估单更新', 'erp:ap-estimate:update', 3, 3, @ap_estimate_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND @ap_estimate_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '暂估单导出', 'erp:ap-estimate:export', 3, 4, @ap_estimate_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND @ap_estimate_export_id IS NULL;

-- 3. 补 finance-expense 权限种子
SET @expense_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:query' ORDER BY id LIMIT 1
);
SET @expense_create_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:create' ORDER BY id LIMIT 1
);
SET @expense_update_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update' ORDER BY id LIMIT 1
);
SET @expense_status_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update-status' ORDER BY id LIMIT 1
);
SET @expense_delete_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:delete' ORDER BY id LIMIT 1
);
SET @expense_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:export' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单查询', 'erp:finance-expense:query', 3, 1, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单创建', 'erp:finance-expense:create', 3, 2, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_create_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单更新', 'erp:finance-expense:update', 3, 3, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单审批', 'erp:finance-expense:update-status', 3, 4, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_status_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单删除', 'erp:finance-expense:delete', 3, 5, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_delete_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '费用单导出', 'erp:finance-expense:export', 3, 6, @expense_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND @expense_export_id IS NULL;

-- 4. 重新取 ID
SET @ap_estimate_query_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:query' ORDER BY id LIMIT 1);
SET @ap_estimate_scan_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:scan' ORDER BY id LIMIT 1);
SET @ap_estimate_update_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:update' ORDER BY id LIMIT 1);
SET @ap_estimate_export_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:export' ORDER BY id LIMIT 1);
SET @expense_query_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:query' ORDER BY id LIMIT 1);
SET @expense_create_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:create' ORDER BY id LIMIT 1);
SET @expense_update_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update' ORDER BY id LIMIT 1);
SET @expense_status_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update-status' ORDER BY id LIMIT 1);
SET @expense_delete_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:delete' ORDER BY id LIMIT 1);
SET @expense_export_id := (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:export' ORDER BY id LIMIT 1);

-- 5. 授权财务主管
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @ap_estimate_menu_id
    UNION ALL SELECT @ap_estimate_query_id
    UNION ALL SELECT @ap_estimate_scan_id
    UNION ALL SELECT @ap_estimate_update_id
    UNION ALL SELECT @ap_estimate_export_id
    UNION ALL SELECT @expense_menu_id
    UNION ALL SELECT @expense_query_id
    UNION ALL SELECT @expense_create_id
    UNION ALL SELECT @expense_update_id
    UNION ALL SELECT @expense_status_id
    UNION ALL SELECT @expense_delete_id
    UNION ALL SELECT @expense_export_id
) t
WHERE @role_id IS NOT NULL
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = @role_id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

-- 6. 验证输出
SELECT m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component
FROM system_menu m
WHERE m.deleted = b'0'
  AND (
      m.id IN (@ap_estimate_menu_id, @expense_menu_id)
      OR m.permission IN (
          'erp:ap-estimate:query', 'erp:ap-estimate:scan', 'erp:ap-estimate:update', 'erp:ap-estimate:export',
          'erp:finance-expense:query', 'erp:finance-expense:create', 'erp:finance-expense:update',
          'erp:finance-expense:update-status', 'erp:finance-expense:delete', 'erp:finance-expense:export'
      )
  )
ORDER BY m.parent_id, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
