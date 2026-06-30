-- 销售闭环菜单补充脚本
-- 幂等写法：动态解析父节点 + 定点回查真实菜单 ID + INSERT / UPDATE 双保险
-- 日期: 2026-06-29 重写（根据数据库实证修正发票菜单口径）

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- ========== 1. 动态解析一级父节点 ==========

SET @sales_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0' AND parent_id = 0 AND path = '/sales'
  ORDER BY id
  LIMIT 1
);

SET @finance_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0' AND parent_id = 0 AND path = '/finance'
  ORDER BY id
  LIMIT 1
);

SET @pmo_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0' AND parent_id = 0 AND path = '/pmo'
  ORDER BY id
  LIMIT 1
);

-- ========== 2. 页面菜单：先回查，再插入，再统一 UPDATE 到目标口径 ==========

SET @shipment_release_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931308)
    AND (
      id = 931308
      OR component = 'erp/sale/shipment-release/index'
      OR (parent_id = @sales_root_id AND path = 'shipment-release')
    )
  ORDER BY
    CASE
      WHEN id = 931308 THEN 0
      WHEN component = 'erp/sale/shipment-release/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931308, '发货放行审核', 'erp:shipment-release:query', 2, 45, @sales_root_id,
       'shipment-release', 'ep:release', 'erp/sale/shipment-release/index', 'ErpShipmentReleasePage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @shipment_release_menu_id IS NULL;

SET @shipment_release_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931308)
    AND (
      id = 931308
      OR component = 'erp/sale/shipment-release/index'
      OR (parent_id = @sales_root_id AND path = 'shipment-release')
    )
  ORDER BY
    CASE
      WHEN id = 931308 THEN 0
      WHEN component = 'erp/sale/shipment-release/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '发货放行审核',
    permission = 'erp:shipment-release:query',
    type = 2,
    sort = 45,
    path = 'shipment-release',
    icon = 'ep:release',
    component = 'erp/sale/shipment-release/index',
    component_name = 'ErpShipmentReleasePage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @shipment_release_menu_id
  AND @sales_root_id IS NOT NULL;

SET @market_ledger_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931309)
    AND (
      id = 931309
      OR component = 'erp/sale/market-ledger/index'
      OR (parent_id = @sales_root_id AND path = 'market-ledger')
    )
  ORDER BY
    CASE
      WHEN id = 931309 THEN 0
      WHEN component = 'erp/sale/market-ledger/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931309, '市场执行台账', 'erp:market-ledger:query', 2, 46, @sales_root_id,
       'market-ledger', 'ep:data-board', 'erp/sale/market-ledger/index', 'ErpMarketLedgerPage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @market_ledger_menu_id IS NULL;

SET @market_ledger_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931309)
    AND (
      id = 931309
      OR component = 'erp/sale/market-ledger/index'
      OR (parent_id = @sales_root_id AND path = 'market-ledger')
    )
  ORDER BY
    CASE
      WHEN id = 931309 THEN 0
      WHEN component = 'erp/sale/market-ledger/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '市场执行台账',
    permission = 'erp:market-ledger:query',
    type = 2,
    sort = 46,
    path = 'market-ledger',
    icon = 'ep:data-board',
    component = 'erp/sale/market-ledger/index',
    component_name = 'ErpMarketLedgerPage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @market_ledger_menu_id
  AND @sales_root_id IS NOT NULL;

SET @market_alert_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931310)
    AND (
      id = 931310
      OR component = 'erp/sale/market-alert/index'
      OR (parent_id = @sales_root_id AND path = 'market-alert')
    )
  ORDER BY
    CASE
      WHEN id = 931310 THEN 0
      WHEN component = 'erp/sale/market-alert/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931310, '市场预警与统计', 'erp:market-alert:query', 2, 47, @sales_root_id,
       'market-alert', 'ep:warning', 'erp/sale/market-alert/index', 'ErpMarketAlertPage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @market_alert_menu_id IS NULL;

SET @market_alert_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931310)
    AND (
      id = 931310
      OR component = 'erp/sale/market-alert/index'
      OR (parent_id = @sales_root_id AND path = 'market-alert')
    )
  ORDER BY
    CASE
      WHEN id = 931310 THEN 0
      WHEN component = 'erp/sale/market-alert/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '市场预警与统计',
    permission = 'erp:market-alert:query',
    type = 2,
    sort = 47,
    path = 'market-alert',
    icon = 'ep:warning',
    component = 'erp/sale/market-alert/index',
    component_name = 'ErpMarketAlertPage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @market_alert_menu_id
  AND @sales_root_id IS NOT NULL;

SET @contract_import_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931311)
    AND (
      id = 931311
      OR component = 'crm/contract/import/index'
      OR (parent_id = @sales_root_id AND path = 'contract-import')
      OR permission = 'crm:contract:import'
    )
  ORDER BY
    CASE
      WHEN id = 931311 THEN 0
      WHEN component = 'crm/contract/import/index' THEN 1
      WHEN permission = 'crm:contract:import' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931311, '合同导入', 'crm:contract:import', 2, 15, @sales_root_id,
       'contract-import', 'ep:upload', 'crm/contract/import/index', 'CrmContractImportPage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @contract_import_menu_id IS NULL;

SET @contract_import_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931311)
    AND (
      id = 931311
      OR component = 'crm/contract/import/index'
      OR (parent_id = @sales_root_id AND path = 'contract-import')
      OR permission = 'crm:contract:import'
    )
  ORDER BY
    CASE
      WHEN id = 931311 THEN 0
      WHEN component = 'crm/contract/import/index' THEN 1
      WHEN permission = 'crm:contract:import' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '合同导入',
    permission = 'crm:contract:import',
    type = 2,
    sort = 15,
    path = 'contract-import',
    icon = 'ep:upload',
    component = 'crm/contract/import/index',
    component_name = 'CrmContractImportPage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @contract_import_menu_id
  AND @sales_root_id IS NOT NULL;

SET @invoice_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 932020)
    AND (
      id = 932020
      OR (parent_id = @finance_root_id AND path = 'invoice')
      OR (permission = 'erp:invoice:query' AND type = 2)
      OR component = 'erp/finance/invoice/index'
    )
  ORDER BY
    CASE
      WHEN id = 932020 THEN 0
      WHEN component = 'erp/finance/invoice/index' THEN 1
      WHEN parent_id = @finance_root_id AND path = 'invoice' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 932020, '销项发票管理', 'erp:invoice:query', 2, 105, @finance_root_id,
       'invoice', 'ep:receipt', 'erp/finance/invoice/index', 'ErpInvoicePage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @invoice_menu_id IS NULL;

SET @invoice_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 932020)
    AND (
      id = 932020
      OR (parent_id = @finance_root_id AND path = 'invoice')
      OR (permission = 'erp:invoice:query' AND type = 2)
      OR component = 'erp/finance/invoice/index'
    )
  ORDER BY
    CASE
      WHEN id = 932020 THEN 0
      WHEN component = 'erp/finance/invoice/index' THEN 1
      WHEN parent_id = @finance_root_id AND path = 'invoice' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @finance_root_id,
    name = '销项发票管理',
    permission = 'erp:invoice:query',
    type = 2,
    sort = 105,
    path = 'invoice',
    icon = 'ep:receipt',
    component = 'erp/finance/invoice/index',
    component_name = 'ErpInvoicePage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @invoice_menu_id
  AND @finance_root_id IS NOT NULL;

SET @project_lifecycle_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931408)
    AND (
      id = 931408
      OR component = 'erp/project/lifecycle/index'
      OR (parent_id = @pmo_root_id AND path = 'lifecycle')
      OR permission = 'pmo:project:lifecycle:query'
    )
  ORDER BY
    CASE
      WHEN id = 931408 THEN 0
      WHEN component = 'erp/project/lifecycle/index' THEN 1
      WHEN permission = 'pmo:project:lifecycle:query' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931408, '项目生命周期追踪', 'pmo:project:lifecycle:query', 2, 35, @pmo_root_id,
       'lifecycle', 'ep:guide', 'erp/project/lifecycle/index', 'ErpProjectLifecyclePage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @pmo_root_id IS NOT NULL
  AND @project_lifecycle_menu_id IS NULL;

SET @project_lifecycle_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931408)
    AND (
      id = 931408
      OR component = 'erp/project/lifecycle/index'
      OR (parent_id = @pmo_root_id AND path = 'lifecycle')
      OR permission = 'pmo:project:lifecycle:query'
    )
  ORDER BY
    CASE
      WHEN id = 931408 THEN 0
      WHEN component = 'erp/project/lifecycle/index' THEN 1
      WHEN permission = 'pmo:project:lifecycle:query' THEN 2
      ELSE 3
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @pmo_root_id,
    name = '项目生命周期追踪',
    permission = 'pmo:project:lifecycle:query',
    type = 2,
    sort = 35,
    path = 'lifecycle',
    icon = 'ep:guide',
    component = 'erp/project/lifecycle/index',
    component_name = 'ErpProjectLifecyclePage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @project_lifecycle_menu_id
  AND @pmo_root_id IS NOT NULL;

-- ========== 3. 按钮权限菜单：使用回查后的真实父菜单 ID ==========

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130801, '校验', 'erp:shipment-release:check', 3, 1, @shipment_release_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @shipment_release_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130801 OR (deleted = b'0' AND permission = 'erp:shipment-release:check'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130802, '提交审核', 'erp:shipment-release:submit', 3, 2, @shipment_release_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @shipment_release_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130802 OR (deleted = b'0' AND permission = 'erp:shipment-release:submit'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130803, '审核通过', 'erp:shipment-release:approve', 3, 3, @shipment_release_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @shipment_release_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130803 OR (deleted = b'0' AND permission = 'erp:shipment-release:approve'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130804, '审核驳回', 'erp:shipment-release:reject', 3, 4, @shipment_release_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @shipment_release_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130804 OR (deleted = b'0' AND permission = 'erp:shipment-release:reject'));

UPDATE system_menu
SET parent_id = @shipment_release_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93130801, 93130802, 93130803, 93130804))
  AND (id IN (93130801, 93130802, 93130803, 93130804)
       OR permission IN ('erp:shipment-release:check', 'erp:shipment-release:submit', 'erp:shipment-release:approve', 'erp:shipment-release:reject'))
  AND type = 3
  AND @shipment_release_menu_id IS NOT NULL;

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130901, '查询', 'erp:market-ledger:query', 3, 1, @market_ledger_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @market_ledger_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130901 OR (deleted = b'0' AND permission = 'erp:market-ledger:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93130902, '导出', 'erp:market-ledger:export', 3, 2, @market_ledger_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @market_ledger_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93130902 OR (deleted = b'0' AND permission = 'erp:market-ledger:export'));

UPDATE system_menu
SET parent_id = @market_ledger_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93130901, 93130902))
  AND (id IN (93130901, 93130902)
       OR permission IN ('erp:market-ledger:query', 'erp:market-ledger:export'))
  AND type = 3
  AND @market_ledger_menu_id IS NOT NULL;

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131001, '查询', 'erp:market-alert:query', 3, 1, @market_alert_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @market_alert_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131001 OR (deleted = b'0' AND permission = 'erp:market-alert:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131002, '更新规则', 'erp:market-alert:update', 3, 2, @market_alert_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @market_alert_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131002 OR (deleted = b'0' AND permission = 'erp:market-alert:update'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131003, '检查预警', 'erp:market-alert:check', 3, 3, @market_alert_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @market_alert_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131003 OR (deleted = b'0' AND permission = 'erp:market-alert:check'));

UPDATE system_menu
SET parent_id = @market_alert_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93131001, 93131002, 93131003))
  AND (id IN (93131001, 93131002, 93131003)
       OR permission IN ('erp:market-alert:query', 'erp:market-alert:update', 'erp:market-alert:check'))
  AND type = 3
  AND @market_alert_menu_id IS NOT NULL;

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93202001, '查询', 'erp:invoice:query', 3, 1, @invoice_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @invoice_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93202001 OR (deleted = b'0' AND permission = 'erp:invoice:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93202002, '新增', 'erp:invoice:create', 3, 2, @invoice_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @invoice_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93202002 OR (deleted = b'0' AND permission = 'erp:invoice:create'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93202003, '修改', 'erp:invoice:update', 3, 3, @invoice_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @invoice_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93202003 OR (deleted = b'0' AND permission = 'erp:invoice:update'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93202004, '删除', 'erp:invoice:delete', 3, 4, @invoice_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @invoice_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93202004 OR (deleted = b'0' AND permission = 'erp:invoice:delete'));

UPDATE system_menu
SET parent_id = @invoice_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93202001, 93202002, 93202003, 93202004))
  AND (id IN (93202001, 93202002, 93202003, 93202004)
       OR permission IN ('erp:invoice:query', 'erp:invoice:create', 'erp:invoice:update', 'erp:invoice:delete'))
  AND type = 3
  AND @invoice_menu_id IS NOT NULL;

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93140801, '查询', 'pmo:project:lifecycle:query', 3, 1, @project_lifecycle_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @project_lifecycle_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93140801 OR (deleted = b'0' AND permission = 'pmo:project:lifecycle:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93140802, '更新阶段', 'pmo:project:lifecycle:update', 3, 2, @project_lifecycle_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @project_lifecycle_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93140802 OR (deleted = b'0' AND permission = 'pmo:project:lifecycle:update'));

UPDATE system_menu
SET parent_id = @project_lifecycle_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93140801, 93140802))
  AND (id IN (93140801, 93140802)
       OR permission IN ('pmo:project:lifecycle:query', 'pmo:project:lifecycle:update'))
  AND type = 3
  AND @project_lifecycle_menu_id IS NOT NULL;

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131101, '下载模板', 'crm:contract:import:template', 3, 1, @contract_import_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @contract_import_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131101 OR (deleted = b'0' AND permission = 'crm:contract:import:template'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131102, '导入', 'crm:contract:import:import', 3, 2, @contract_import_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @contract_import_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131102 OR (deleted = b'0' AND permission = 'crm:contract:import:import'));

UPDATE system_menu
SET parent_id = @contract_import_menu_id,
    type = 3,
    status = 0,
    visible = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE (deleted = b'0' OR id IN (93131101, 93131102))
  AND (id IN (93131101, 93131102)
       OR permission IN ('crm:contract:import:template', 'crm:contract:import:import'))
  AND type = 3
  AND @contract_import_menu_id IS NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
