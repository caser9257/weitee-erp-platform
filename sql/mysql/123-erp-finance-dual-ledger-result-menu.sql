-- 双账套结果查询菜单与内部角色授权
-- 目标：
-- 1. 补齐“双账套结果查询”页面菜单
-- 2. 补齐 query / recompute 按钮权限
-- 3. 授权给内部财务角色（财务主管、财务经办）
-- 4. 外部审计角色不授予该菜单，保持无感知

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

SET @dual_ledger_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-result/index'
        OR component_name IN ('ErpFinanceDualLedgerResult', 'FormalFinanceDualLedgerResult')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-result')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果查询', '', 2, 70, @finance_root_id, 'dual-ledger-result', 'ep:files',
       'erp/finance/dual-ledger-result/index', 'ErpFinanceDualLedgerResult',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_page_id IS NULL;

SET @dual_ledger_page_id := COALESCE(@dual_ledger_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-result/index'
    ORDER BY id
    LIMIT 1
));

SET @dual_ledger_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-result:query'
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果查询', 'erp:finance-dual-ledger-result:query', 3, 1, @dual_ledger_page_id,
       '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_page_id IS NOT NULL
  AND @dual_ledger_query_id IS NULL;

SET @dual_ledger_recompute_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-result:recompute'
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (
    SELECT IFNULL(MAX(id), 900000) + 1
    FROM system_menu
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果重算', 'erp:finance-dual-ledger-result:recompute', 3, 2, @dual_ledger_page_id,
       '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_page_id IS NOT NULL
  AND @dual_ledger_recompute_id IS NULL;

SET @dual_ledger_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-result:query'
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_recompute_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-result:recompute'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_page_id
    UNION ALL SELECT @dual_ledger_query_id
) t
WHERE r.deleted = b'0'
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk')
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
SELECT r.id, @dual_ledger_recompute_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND @dual_ledger_recompute_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = @dual_ledger_recompute_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

SELECT id, name, permission, type, parent_id, path, component, component_name
FROM system_menu
WHERE deleted = b'0'
  AND (
      id = @dual_ledger_page_id
      OR permission IN (
          'erp:finance-dual-ledger-result:query',
          'erp:finance-dual-ledger-result:recompute'
      )
  )
ORDER BY type, sort, id;

SELECT r.code AS role_code, m.id AS menu_id, m.name, m.permission
FROM system_role_menu rm
JOIN system_role r
  ON r.id = rm.role_id
 AND r.deleted = b'0'
JOIN system_menu m
  ON m.id = rm.menu_id
 AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk')
  AND (
      m.id = @dual_ledger_page_id
      OR m.permission IN (
          'erp:finance-dual-ledger-result:query',
          'erp:finance-dual-ledger-result:recompute'
      )
  )
ORDER BY r.code, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
