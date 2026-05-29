-- Finance module 6 readonly menu permission fix.
-- Scope:
--   - ensure enabled /erp/finance readonly page menus exist
--   - ensure query button permissions exist under enabled page menus
--   - grant page menus and query permissions to local super_admin role
-- This script is idempotent and does not change business data or API contracts.

SET @erp_menu_id := (
    SELECT id
    FROM system_menu
    WHERE path = '/erp'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       'ERP',
       '',
       1,
       50,
       0,
       '/erp',
       '',
       '',
       'ProjectErpRoot',
       0,
       b'1',
       b'0',
       b'1',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE @erp_menu_id IS NULL;

SET @erp_menu_id := (
    SELECT id
    FROM system_menu
    WHERE path = '/erp'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @finance_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @erp_menu_id
      AND path = 'finance'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务管理',
       '',
       1,
       10,
       @erp_menu_id,
       'finance',
       'ep:money',
       '',
       'ProjectFinanceRoot',
       0,
       b'1',
       b'0',
       b'1',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE @erp_menu_id IS NOT NULL
  AND @finance_menu_id IS NULL;

SET @finance_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @erp_menu_id
      AND path = 'finance'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

UPDATE system_menu
SET status = 0,
    visible = b'1',
    updater = '1',
    update_time = NOW()
WHERE id IN (@erp_menu_id, @finance_menu_id)
  AND deleted = b'0';

-- Page menus.
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务账簿', '', 2, 41, @finance_menu_id, 'ledger', 'ep:collection',
       'erp/finance/ledger/index', 'ErpFinanceLedger', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'ledger' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '会计期间', '', 2, 42, @finance_menu_id, 'period', 'ep:calendar',
       'erp/finance/period/index', 'ErpFinancePeriod', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'period' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务科目', '', 2, 43, @finance_menu_id, 'subject', 'ep:document',
       'erp/finance/subject/index', 'ErpFinanceSubject', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'subject' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '报表项目', '', 2, 44, @finance_menu_id, 'report-item', 'ep:list',
       'erp/finance/report-item/index', 'ErpFinanceReportItem', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'report-item' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务报表', '', 2, 45, @finance_menu_id, 'reports', 'ep:data-analysis',
       'erp/finance/reports/index', 'ErpFinanceReports', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'reports' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务凭证', '', 2, 46, @finance_menu_id, 'voucher', 'ep:tickets',
       'erp/finance/voucher/index', 'ErpFinanceVoucher', 0, b'1', b'0', b'0',
       '1', NOW(), '1', NOW(), b'0'
WHERE @finance_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE parent_id = @finance_menu_id AND path = 'voucher' AND deleted = b'0'
  );

SET @ledger_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'ledger' AND deleted = b'0' ORDER BY id LIMIT 1
);
SET @period_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'period' AND deleted = b'0' ORDER BY id LIMIT 1
);
SET @subject_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'subject' AND deleted = b'0' ORDER BY id LIMIT 1
);
SET @report_item_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'report-item' AND deleted = b'0' ORDER BY id LIMIT 1
);
SET @reports_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'reports' AND deleted = b'0' ORDER BY id LIMIT 1
);
SET @voucher_menu_id := (
    SELECT id FROM system_menu WHERE parent_id = @finance_menu_id AND path = 'voucher' AND deleted = b'0' ORDER BY id LIMIT 1
);

-- Query permissions.
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '账簿查询', 'erp:finance-ledger:query', 3, 1, @ledger_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ledger_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-ledger:query' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '期间查询', 'erp:finance-period:query', 3, 1, @period_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @period_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-period:query' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '科目查询', 'erp:finance-subject:query', 3, 1, @subject_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @subject_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-subject:query' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '报表项目查询', 'erp:finance-report-item:query', 3, 1, @report_item_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @report_item_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-report-item:query' AND deleted = b'0'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '凭证查询', 'erp:finance-voucher:query', 3, 1, @voucher_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-voucher:query' AND deleted = b'0'
  );

UPDATE system_menu
SET parent_id = @ledger_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE @ledger_menu_id IS NOT NULL
  AND permission = 'erp:finance-ledger:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @period_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE @period_menu_id IS NOT NULL
  AND permission = 'erp:finance-period:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @subject_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE @subject_menu_id IS NOT NULL
  AND permission = 'erp:finance-subject:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @report_item_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE @report_item_menu_id IS NOT NULL
  AND permission = 'erp:finance-report-item:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @voucher_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE @voucher_menu_id IS NOT NULL
  AND permission = 'erp:finance-voucher:query'
  AND deleted = b'0';

INSERT INTO system_role_menu (
    role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT role.id,
       menu.id,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0',
       role.tenant_id
FROM system_role role
JOIN system_menu menu ON menu.deleted = b'0'
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND (
      menu.id IN (
          @erp_menu_id, @finance_menu_id, @ledger_menu_id, @period_menu_id,
          @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
      )
      OR menu.permission IN (
          'erp:finance-ledger:query',
          'erp:finance-period:query',
          'erp:finance-subject:query',
          'erp:finance-report-item:query',
          'erp:finance-voucher:query'
      )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu role_menu
      WHERE role_menu.role_id = role.id
        AND role_menu.menu_id = menu.id
        AND role_menu.tenant_id = role.tenant_id
        AND role_menu.deleted = b'0'
  );

SELECT
    @erp_menu_id AS erp_menu_id,
    @finance_menu_id AS finance_menu_id,
    @ledger_menu_id AS ledger_menu_id,
    @period_menu_id AS period_menu_id,
    @subject_menu_id AS subject_menu_id,
    @report_item_menu_id AS report_item_menu_id,
    @reports_menu_id AS reports_menu_id,
    @voucher_menu_id AS voucher_menu_id;

SELECT
    id,
    name,
    permission,
    type,
    status,
    parent_id,
    path,
    component,
    deleted
FROM system_menu
WHERE id IN (
        @erp_menu_id, @finance_menu_id, @ledger_menu_id, @period_menu_id,
        @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
    )
   OR permission IN (
        'erp:finance-ledger:query',
        'erp:finance-period:query',
        'erp:finance-subject:query',
        'erp:finance-report-item:query',
        'erp:finance-voucher:query'
    )
ORDER BY type, sort, id;

SELECT
    COUNT(*) AS super_admin_finance_module6_permission_count
FROM system_role_menu role_menu
JOIN system_role role ON role.id = role_menu.role_id
JOIN system_menu menu ON menu.id = role_menu.menu_id
WHERE role.code = 'super_admin'
  AND role.deleted = b'0'
  AND role_menu.deleted = b'0'
  AND (
      menu.id IN (
          @erp_menu_id, @finance_menu_id, @ledger_menu_id, @period_menu_id,
          @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
      )
      OR menu.permission IN (
          'erp:finance-ledger:query',
          'erp:finance-period:query',
          'erp:finance-subject:query',
          'erp:finance-report-item:query',
          'erp:finance-voucher:query'
      )
  );
