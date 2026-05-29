-- 应付台账权限种子 + 财务主管授权修复
-- 适用场景：
-- 1. 页面已存在，但 /erp/ap-statement/page、/summary 等接口返回 403
-- 2. system_menu 里缺少 erp:ap-statement:query / update / export 权限记录

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

SET @apar_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/apar/index'
        OR component_name IN ('FormalFinanceApar', 'ProjectFinanceApar', 'ErpFinanceApar')
        OR (parent_id = @finance_root_id AND path = 'apar')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

SET @ap_statement_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:query'
    ORDER BY id LIMIT 1
);

SET @ap_statement_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:update'
    ORDER BY id LIMIT 1
);

SET @ap_statement_export_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:export'
    ORDER BY id LIMIT 1
);

-- 1. 确保应收应付账款池页面菜单存在于财务根下
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '应收应付账款池', '', 2, 20, @finance_root_id, 'apar', 'ep:wallet',
       'erp/finance/apar/index', 'FormalFinanceApar',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @apar_menu_id IS NULL;

SET @apar_menu_id := COALESCE(@apar_menu_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/apar/index'
    ORDER BY id
    LIMIT 1
));

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

-- 2. 补应付台账权限种子
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '应付台账查询', 'erp:ap-statement:query', 3, 1, @apar_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND @ap_statement_query_id IS NULL;

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '应付台账更新', 'erp:ap-statement:update', 3, 2, @apar_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND @ap_statement_update_id IS NULL;

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '应付台账导出', 'erp:ap-statement:export', 3, 3, @apar_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND @ap_statement_export_id IS NULL;

SET @ap_statement_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:query'
    ORDER BY id LIMIT 1
);

SET @ap_statement_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:update'
    ORDER BY id LIMIT 1
);

SET @ap_statement_export_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:ap-statement:export'
    ORDER BY id LIMIT 1
);

-- 3. 授权财务主管：ERP 根、财务根、页面菜单、按钮权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @apar_menu_id
    UNION ALL SELECT @ap_statement_query_id
    UNION ALL SELECT @ap_statement_update_id
    UNION ALL SELECT @ap_statement_export_id
    UNION ALL SELECT (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:purchase-in:query' ORDER BY id LIMIT 1)
    UNION ALL SELECT (SELECT id FROM system_menu WHERE deleted = b'0' AND permission = 'erp:purchase-return:query' ORDER BY id LIMIT 1)
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

-- 4. 验证输出
SELECT m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component, m.component_name
FROM system_menu m
WHERE m.deleted = b'0'
  AND (
      m.id = @apar_menu_id
       OR m.permission IN ('erp:ap-statement:query', 'erp:ap-statement:update', 'erp:ap-statement:export', 'erp:purchase-in:query', 'erp:purchase-return:query')
   )
ORDER BY m.type, m.sort, m.id;

SELECT rm.role_id, m.id AS menu_id, m.name, m.permission
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.role_id = @role_id
  AND rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND (
      m.id = @apar_menu_id
       OR m.permission IN ('erp:ap-statement:query', 'erp:ap-statement:update', 'erp:ap-statement:export', 'erp:purchase-in:query', 'erp:purchase-return:query')
   )
ORDER BY m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
