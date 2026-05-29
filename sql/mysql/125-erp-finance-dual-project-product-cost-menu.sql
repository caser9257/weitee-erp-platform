-- 项目双账成本 & 产品双账成本菜单与权限
-- 参照 123-erp-finance-dual-ledger-result-menu.sql 的模式

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

-- ============================================================
-- 1. 定位父级菜单
-- ============================================================

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

-- ============================================================
-- 2. 项目双账成本 — 页面菜单
-- ============================================================

SET @project_dual_cost_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-project-cost/index'
        OR component_name = 'ErpFinanceDualProjectCost'
        OR (parent_id = @finance_root_id AND path = 'dual-project-cost')
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
SELECT @next_menu_id, '项目双账成本', 'erp:finance-dual-project-cost:query', 2, 80, @finance_root_id,
       'dual-project-cost', 'ep:office-building',
       'erp/finance/dual-project-cost/index', 'ErpFinanceDualProjectCost',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @project_dual_cost_page_id IS NULL;

SET @project_dual_cost_page_id := COALESCE(@project_dual_cost_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-project-cost/index'
    ORDER BY id
    LIMIT 1
));

-- ============================================================
-- 3. 项目双账成本 — 按钮权限
-- ============================================================

-- query 按钮
SET @project_dual_cost_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:query' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '项目双账查询', 'erp:finance-dual-project-cost:query', 3, 1,
       @project_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_dual_cost_page_id IS NOT NULL
  AND @project_dual_cost_query_id IS NULL;

-- rebuild 按钮
SET @project_dual_cost_rebuild_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:rebuild' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '项目双账重跑', 'erp:finance-dual-project-cost:rebuild', 3, 2,
       @project_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_dual_cost_page_id IS NOT NULL
  AND @project_dual_cost_rebuild_id IS NULL;

-- export 按钮
SET @project_dual_cost_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:export' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '项目双账导出', 'erp:finance-dual-project-cost:export', 3, 3,
       @project_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_dual_cost_page_id IS NOT NULL
  AND @project_dual_cost_export_id IS NULL;

-- ============================================================
-- 4. 产品双账成本 — 页面菜单
-- ============================================================

SET @product_dual_cost_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-product-cost/index'
        OR component_name = 'ErpFinanceDualProductCost'
        OR (parent_id = @finance_root_id AND path = 'dual-product-cost')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '产品双账成本', 'erp:finance-dual-product-cost:query', 2, 81, @finance_root_id,
       'dual-product-cost', 'ep:goods',
       'erp/finance/dual-product-cost/index', 'ErpFinanceDualProductCost',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @product_dual_cost_page_id IS NULL;

SET @product_dual_cost_page_id := COALESCE(@product_dual_cost_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-product-cost/index'
    ORDER BY id
    LIMIT 1
));

-- ============================================================
-- 5. 产品双账成本 — 按钮权限
-- ============================================================

-- query 按钮
SET @product_dual_cost_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:query' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '产品双账查询', 'erp:finance-dual-product-cost:query', 3, 1,
       @product_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_dual_cost_page_id IS NOT NULL
  AND @product_dual_cost_query_id IS NULL;

-- rebuild 按钮
SET @product_dual_cost_rebuild_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:rebuild' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '产品双账重跑', 'erp:finance-dual-product-cost:rebuild', 3, 2,
       @product_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_dual_cost_page_id IS NOT NULL
  AND @product_dual_cost_rebuild_id IS NULL;

-- export 按钮
SET @product_dual_cost_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:export' ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '产品双账导出', 'erp:finance-dual-product-cost:export', 3, 3,
       @product_dual_cost_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @product_dual_cost_page_id IS NOT NULL
  AND @product_dual_cost_export_id IS NULL;

-- ============================================================
-- 6. 角色授权（财务主管 + 财务经办）
-- ============================================================

-- 刷新变量
SET @project_dual_cost_page_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND component = 'erp/finance/dual-project-cost/index' ORDER BY id LIMIT 1
);
SET @project_dual_cost_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:query' ORDER BY id LIMIT 1
);
SET @project_dual_cost_rebuild_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:rebuild' ORDER BY id LIMIT 1
);
SET @project_dual_cost_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-project-cost:export' ORDER BY id LIMIT 1
);
SET @product_dual_cost_page_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND component = 'erp/finance/dual-product-cost/index' ORDER BY id LIMIT 1
);
SET @product_dual_cost_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:query' ORDER BY id LIMIT 1
);
SET @product_dual_cost_rebuild_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:rebuild' ORDER BY id LIMIT 1
);
SET @product_dual_cost_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-dual-product-cost:export' ORDER BY id LIMIT 1
);

-- 授权页面和查询按钮给财务主管和财务经办
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
JOIN (
    SELECT @project_dual_cost_page_id AS menu_id
    UNION ALL SELECT @project_dual_cost_query_id
    UNION ALL SELECT @project_dual_cost_export_id
    UNION ALL SELECT @product_dual_cost_page_id
    UNION ALL SELECT @product_dual_cost_query_id
    UNION ALL SELECT @product_dual_cost_export_id
) t
WHERE r.deleted = b'0'
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk')
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0' AND rm.tenant_id = @tenant_id
  );

-- 重跑按钮只授权给财务主管
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_role r
JOIN (
    SELECT @project_dual_cost_rebuild_id AS menu_id
    UNION ALL SELECT @product_dual_cost_rebuild_id
) t
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0' AND rm.tenant_id = @tenant_id
  );

-- ============================================================
-- 7. 验证输出
-- ============================================================

SELECT id, name, permission, type, parent_id, path, component, component_name
FROM system_menu
WHERE deleted = b'0'
  AND (
      component IN ('erp/finance/dual-project-cost/index', 'erp/finance/dual-product-cost/index')
      OR permission IN (
          'erp:finance-dual-project-cost:query',
          'erp:finance-dual-project-cost:rebuild',
          'erp:finance-dual-project-cost:export',
          'erp:finance-dual-product-cost:query',
          'erp:finance-dual-product-cost:rebuild',
          'erp:finance-dual-product-cost:export'
      )
  )
ORDER BY type, sort, id;

SET FOREIGN_KEY_CHECKS = 1;
