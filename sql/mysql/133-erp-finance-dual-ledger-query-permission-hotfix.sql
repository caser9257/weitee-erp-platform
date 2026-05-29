-- 双账套账簿映射 / 口径配置 query 权限热修复
-- 目标：
-- 1. 确保双账套两个页面的 query 按钮权限菜单存在
-- 2. 将页面菜单 + query 权限授予财务主管、财务经办
-- 3. 幂等执行，可重复运行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
          path = '/finance'
          OR component_name = 'FormalFinanceRoot'
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

SET @dual_ledger_config_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-config:query'
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_diff_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-diff-config:query'
    ORDER BY id
    LIMIT 1
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
SELECT @next_menu_id, '双账套口径配置查询', 'erp:finance-dual-ledger-diff-config:query', 3, 1, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_query_id IS NULL;

SET @dual_ledger_config_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-config:query'
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_diff_query_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-ledger-diff-config:query'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', r.tenant_id
FROM system_role r
JOIN (
    SELECT @finance_root_id AS menu_id
    UNION ALL SELECT @dual_ledger_config_page_id
    UNION ALL SELECT @dual_ledger_diff_config_page_id
    UNION ALL SELECT @dual_ledger_config_query_id
    UNION ALL SELECT @dual_ledger_diff_query_id
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
        AND rm.tenant_id = r.tenant_id
  );

SELECT r.code AS role_code,
       m.id,
       m.name,
       m.permission,
       m.type,
       m.parent_id
FROM system_role_menu rm
JOIN system_role r
  ON r.id = rm.role_id
 AND r.deleted = b'0'
JOIN system_menu m
  ON m.id = rm.menu_id
 AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk')
  AND (
      m.id IN (@dual_ledger_config_page_id, @dual_ledger_diff_config_page_id)
      OR m.permission IN (
          'erp:finance-dual-ledger-config:query',
          'erp:finance-dual-ledger-diff-config:query'
      )
  )
ORDER BY r.code, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
