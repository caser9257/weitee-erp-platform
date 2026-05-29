-- 财务角色只读权限补齐脚本
-- 目标：
-- 1. 确保 finance-module6 只读页面对应的 query 权限菜单存在
-- 2. 将这些 query 权限授予 财务主管（erp_finance_manager）
-- 3. 同时授予 财务经办（erp_finance_clerk）
-- 4. 幂等执行，可重复运行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

-- 1) 定位 ERP / 财务父菜单
SET @erp_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/erp'
        OR component_name IN ('ProjectErpRoot')
        OR name = 'ERP 系统'
      )
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       'ERP 系统',
       '',
       1,
       50,
       0,
       '/erp',
       'simple-icons:erpnext',
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
    WHERE deleted = b'0'
      AND (
        path = '/erp'
        OR component_name IN ('ProjectErpRoot')
        OR name = 'ERP 系统'
      )
    ORDER BY id
    LIMIT 1
);

SET @finance_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND parent_id = @erp_menu_id
      AND (
        path = 'finance'
        OR component_name IN ('ProjectFinanceRoot')
        OR name = '财务管理'
      )
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
    WHERE deleted = b'0'
      AND parent_id = @erp_menu_id
      AND (
        path = 'finance'
        OR component_name IN ('ProjectFinanceRoot')
        OR name = '财务管理'
      )
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

-- 2) 确保页面菜单存在
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'ledger'
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'period'
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'subject'
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'report-item'
  );

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务报表查询', 'erp:finance-report:query', 3, 1, @reports_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @reports_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-report:query'
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'reports'
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
    WHERE deleted = b'0'
      AND parent_id = @finance_menu_id
      AND path = 'voucher'
  );

SET @ledger_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'ledger'
    ORDER BY id LIMIT 1
);
SET @period_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'period'
    ORDER BY id LIMIT 1
);
SET @subject_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'subject'
    ORDER BY id LIMIT 1
);
SET @report_item_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'report-item'
    ORDER BY id LIMIT 1
);
SET @reports_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'reports'
    ORDER BY id LIMIT 1
);
SET @voucher_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND parent_id = @finance_menu_id AND path = 'voucher'
    ORDER BY id LIMIT 1
);

-- 3) 确保 query 权限菜单存在
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
    WHERE deleted = b'0'
      AND permission = 'erp:finance-ledger:query'
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
    WHERE deleted = b'0'
      AND permission = 'erp:finance-period:query'
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
    WHERE deleted = b'0'
      AND permission = 'erp:finance-subject:query'
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
    WHERE deleted = b'0'
      AND permission = 'erp:finance-report-item:query'
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
    WHERE deleted = b'0'
      AND permission = 'erp:finance-voucher:query'
  );

-- 保证权限菜单挂到正确页面下
UPDATE system_menu
SET parent_id = @ledger_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE permission = 'erp:finance-ledger:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @period_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE permission = 'erp:finance-period:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @subject_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE permission = 'erp:finance-subject:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @report_item_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE permission = 'erp:finance-report-item:query'
  AND deleted = b'0';

UPDATE system_menu
SET parent_id = @voucher_menu_id,
    status = 0,
    updater = '1',
    update_time = NOW()
WHERE permission = 'erp:finance-voucher:query'
  AND deleted = b'0';

-- 4) 给财务主管补只读页面菜单 + query 权限
INSERT INTO system_role_menu (
    role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT r.id, m.id, '1', NOW(), '1', NOW(), b'0', r.tenant_id
FROM system_role r
JOIN system_menu m ON m.deleted = b'0'
WHERE r.code = 'erp_finance_manager'
  AND r.deleted = b'0'
  AND (
      m.id IN (
          @finance_menu_id, @ledger_menu_id, @period_menu_id,
          @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
      )
       OR m.permission IN (
           'erp:finance-ledger:query',
           'erp:finance-period:query',
           'erp:finance-subject:query',
           'erp:finance-report-item:query',
           'erp:finance-report:query',
           'erp:finance-voucher:query',
           'erp:purchase-in:query',
           'erp:purchase-return:query'
       )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
        AND rm.tenant_id = r.tenant_id
        AND rm.deleted = b'0'
  );

-- 5) 给财务经办补只读页面菜单 + query 权限
INSERT INTO system_role_menu (
    role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT r.id, m.id, '1', NOW(), '1', NOW(), b'0', r.tenant_id
FROM system_role r
JOIN system_menu m ON m.deleted = b'0'
WHERE r.code = 'erp_finance_clerk'
  AND r.deleted = b'0'
  AND (
      m.id IN (
          @finance_menu_id, @ledger_menu_id, @period_menu_id,
          @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
      )
       OR m.permission IN (
           'erp:finance-ledger:query',
           'erp:finance-period:query',
           'erp:finance-subject:query',
           'erp:finance-report-item:query',
           'erp:finance-report:query',
           'erp:finance-voucher:query',
           'erp:purchase-in:query',
           'erp:purchase-return:query'
       )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
        AND rm.tenant_id = r.tenant_id
        AND rm.deleted = b'0'
  );

-- 6) 验证输出
SELECT
    r.code AS role_code,
    m.name AS menu_name,
    m.permission
FROM system_role r
JOIN system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE r.code IN ('erp_finance_manager', 'erp_finance_clerk')
  AND (
      m.permission IN (
          'erp:finance-ledger:query',
          'erp:finance-period:query',
          'erp:finance-subject:query',
          'erp:finance-report-item:query',
          'erp:finance-report:query',
          'erp:finance-voucher:query',
          'erp:purchase-in:query',
          'erp:purchase-return:query'
      )
      OR m.id IN (
          @finance_menu_id, @ledger_menu_id, @period_menu_id,
          @subject_menu_id, @report_item_menu_id, @reports_menu_id, @voucher_menu_id
      )
  )
ORDER BY r.code, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
