-- 产品成本分析报表菜单与权限
-- 参照 125-erp-finance-dual-project-product-cost-menu.sql 的模式

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


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
-- 2. 产品成本分析报表 — 页面菜单
-- ============================================================

SET @cost_report_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/cost-report/index'
        OR component_name = 'ErpFinanceCostReport'
        OR (parent_id = @finance_root_id AND path = 'cost-report')
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
SELECT @next_menu_id, '产品成本分析报表', 'erp:finance-report:query', 2, 82, @finance_root_id,
       'cost-report', 'ep:data-analysis',
       'erp/finance/cost-report/index', 'ErpFinanceCostReport',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @cost_report_page_id IS NULL;

SET @cost_report_page_id := COALESCE(@cost_report_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/cost-report/index'
    ORDER BY id
    LIMIT 1
));

-- ============================================================
-- 3. 产品成本分析报表 — 按钮权限
-- ============================================================

-- query 按钮
SET @cost_report_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-report:query' AND parent_id = @cost_report_page_id
    ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '报表查询', 'erp:finance-report:query', 3, 1,
       @cost_report_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @cost_report_page_id IS NOT NULL
  AND @cost_report_query_id IS NULL;

-- export 按钮
SET @cost_report_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-report:export' AND parent_id = @cost_report_page_id
    ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '报表导出', 'erp:finance-report:export', 3, 2,
       @cost_report_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @cost_report_page_id IS NOT NULL
  AND @cost_report_export_id IS NULL;

-- ============================================================
-- 4. 角色授权（财务主管 + 财务经办）
-- ============================================================

-- 刷新变量
SET @cost_report_page_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND component = 'erp/finance/cost-report/index' ORDER BY id LIMIT 1
);
SET @cost_report_query_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-report:query' AND parent_id = @cost_report_page_id
    ORDER BY id LIMIT 1
);
SET @cost_report_export_id := (
    SELECT id FROM system_menu WHERE deleted = b'0'
      AND permission = 'erp:finance-report:export' AND parent_id = @cost_report_page_id
    ORDER BY id LIMIT 1
);

-- 授权页面和查询按钮给财务主管和财务经办
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN (
    SELECT @cost_report_page_id AS menu_id
    UNION ALL SELECT @cost_report_query_id
    UNION ALL SELECT @cost_report_export_id
) t
WHERE r.deleted = b'0'
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk')
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
  );

-- ============================================================
-- 5. 验证输出
-- ============================================================

SELECT id, name, permission, type, parent_id, path, component, component_name
FROM system_menu
WHERE deleted = b'0'
  AND (
      component = 'erp/finance/cost-report/index'
      OR permission IN (
          'erp:finance-report:query',
          'erp:finance-report:export'
      )
  )
ORDER BY type, sort, id;

SET FOREIGN_KEY_CHECKS = 1;
