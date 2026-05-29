-- 财务主管财务模块权限尽量补全脚本
-- 目标：
-- 1. 面向角色 erp_finance_manager
-- 2. 尽量补齐当前财务模块涉及的主要页面菜单与按钮权限
-- 3. 幂等执行，可重复运行

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

-- 财务根菜单链
SET @erp_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/erp'
        OR component_name = 'ProjectErpRoot'
        OR name = 'ERP 系统'
      )
    ORDER BY id
    LIMIT 1
);

SET @project_finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component_name = 'ProjectFinanceRoot'
        OR (parent_id = @erp_root_id AND path = 'finance')
      )
    ORDER BY id
    LIMIT 1
);

SET @formal_finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
        OR component_name = 'FormalFinanceRoot'
      )
    ORDER BY id
    LIMIT 1
);

-- 页面菜单
SET @apar_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/apar/index'
        OR component_name IN ('FormalFinanceApar', 'ProjectFinanceApar')
        OR path = 'apar'
      )
    ORDER BY id
    LIMIT 1
);

SET @ap_estimate_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/ap-estimate/index'
        OR component_name IN ('FormalFinanceApEstimate')
        OR path = 'ap-estimate'
      )
    ORDER BY id
    LIMIT 1
);

SET @ap_invoice_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/ap-invoice/index'
        OR component_name = 'ErpApInvoice'
        OR path = 'ap-invoice'
      )
    ORDER BY id
    LIMIT 1
);

SET @expense_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component_name = 'FormalFinanceExpense'
        OR component = 'erp/finance/expense/index'
        OR path = 'expense'
      )
    ORDER BY id
    LIMIT 1
);

SET @payment_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/payment/index'
        OR component_name IN ('FormalFinancePayment', 'ErpFinancePayment')
        OR path = 'payment'
      )
    ORDER BY id
    LIMIT 1
);

SET @receipt_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/receipt/index'
        OR component_name IN ('FormalFinanceReceipt', 'ErpFinanceReceipt')
        OR path = 'receipt'
      )
    ORDER BY id
    LIMIT 1
);

SET @account_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/account/index'
        OR component_name IN ('FormalFinanceAccount', 'ErpAccount')
      )
    ORDER BY id
    LIMIT 1
);

SET @assets_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/assets/index'
        OR component_name = 'FormalFinanceAssets'
        OR path = 'assets'
      )
    ORDER BY id
    LIMIT 1
);

SET @ledger_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/ledger/index'
        OR component_name IN ('FormalFinanceLedger', 'ErpFinanceLedger')
        OR path = 'ledger'
      )
    ORDER BY id
    LIMIT 1
);

SET @period_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/period/index'
        OR component_name IN ('FormalFinancePeriod', 'ErpFinancePeriod')
        OR path = 'period'
      )
    ORDER BY id
    LIMIT 1
);

SET @subject_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/subject/index'
        OR component_name IN ('FormalFinanceSubject', 'ErpFinanceSubject')
        OR path = 'subject'
      )
    ORDER BY id
    LIMIT 1
);

SET @report_item_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/report-item/index'
        OR component_name IN ('FormalFinanceReportItem', 'ErpFinanceReportItem')
        OR path = 'report-item'
      )
    ORDER BY id
    LIMIT 1
);

SET @reports_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/reports/index'
        OR component_name IN ('FormalFinanceReports', 'ErpFinanceReports')
        OR path = 'reports'
      )
    ORDER BY id
    LIMIT 1
);

SET @voucher_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/voucher/index'
        OR component_name IN ('FormalFinanceVoucher', 'ErpFinanceVoucher')
        OR path = 'voucher'
      )
    ORDER BY id
    LIMIT 1
);

SET @report_query_menu_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-report:query'
    ORDER BY id
    LIMIT 1
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
  AND @report_query_menu_id IS NULL;

-- 根菜单授权
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @project_finance_root_id
    UNION ALL SELECT @formal_finance_root_id
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

-- 页面菜单授权
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT @apar_menu_id AS menu_id
    UNION ALL SELECT @ap_estimate_menu_id
    UNION ALL SELECT @ap_invoice_menu_id
    UNION ALL SELECT @expense_menu_id
    UNION ALL SELECT @payment_menu_id
    UNION ALL SELECT @receipt_menu_id
    UNION ALL SELECT @account_menu_id
    UNION ALL SELECT @assets_menu_id
    UNION ALL SELECT @ledger_menu_id
    UNION ALL SELECT @period_menu_id
    UNION ALL SELECT @subject_menu_id
    UNION ALL SELECT @report_item_menu_id
    UNION ALL SELECT @reports_menu_id
    UNION ALL SELECT @voucher_menu_id
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

-- 按钮/接口权限授权
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_id IS NOT NULL
  AND m.deleted = b'0'
  AND m.permission IN (
    'erp:ap-statement:query',
    'erp:ap-statement:update',
    'erp:ap-statement:export',
    'erp:ap-estimate:query',
    'erp:ap-estimate:scan',
    'erp:ap-estimate:update',
    'erp:ap-estimate:confirm',
    'erp:ap-estimate:reverse',
    'erp:ap-estimate:export',
    'erp:ap-invoice:query',
    'erp:ap-invoice:create',
    'erp:ap-invoice:update',
    'erp:purchase-in:query',
    'erp:purchase-return:query',
    'erp:finance-expense:query',
    'erp:finance-expense:create',
    'erp:finance-expense:update',
    'erp:finance-expense:update-status',
    'erp:finance-expense:delete',
    'erp:finance-expense:export',
    'erp:finance-payment:query',
    'erp:finance-payment:create',
    'erp:finance-payment:update',
    'erp:finance-payment:update-status',
    'erp:finance-payment:delete',
    'erp:finance-payment:export',
    'erp:finance-receipt:query',
    'erp:finance-receipt:create',
    'erp:finance-receipt:update',
    'erp:finance-receipt:update-status',
    'erp:finance-receipt:delete',
    'erp:finance-receipt:export',
    'erp:account:query',
    'erp:account:create',
    'erp:account:update',
    'erp:account:delete',
    'erp:account:export',
    'erp:finance-ledger:query',
    'erp:finance-ledger:create',
    'erp:finance-ledger:update',
    'erp:finance-ledger:delete',
    'erp:finance-ledger:export',
    'erp:finance-period:query',
    'erp:finance-period:create',
    'erp:finance-period:update',
    'erp:finance-period:export',
    'erp:finance-subject:query',
    'erp:finance-subject:create',
    'erp:finance-subject:update',
    'erp:finance-subject:delete',
    'erp:finance-report-item:query',
    'erp:finance-report-item:create',
    'erp:finance-report-item:update',
    'erp:finance-report-item:delete',
    'erp:finance-report:query',
    'erp:finance-voucher:query',
    'erp:finance-voucher:create',
    'erp:finance-voucher:update',
    'erp:finance-voucher-template:query',
    'erp:finance-voucher-template:create',
    'erp:finance-voucher-template:update',
    'erp:finance-voucher-template:delete',
    'erp:finance-prepayment:query',
    'erp:finance-prepayment:create',
    'erp:finance-prepayment:update',
    'erp:finance-prepayment:update-status',
    'erp:finance-prepayment:delete',
    'erp:finance-dual-ledger-config:query',
    'erp:finance-dual-ledger-config:create',
    'erp:finance-dual-ledger-config:update',
    'erp:finance-dual-ledger-config:delete',
    'erp:finance-dual-ledger-diff-config:query',
    'erp:finance-dual-ledger-diff-config:create',
    'erp:finance-dual-ledger-diff-config:update',
    'erp:finance-dual-ledger-diff-config:delete'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

-- 验证输出
SELECT r.code AS role_code, m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component_name
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND (
    m.path = '/finance'
    OR m.component LIKE 'erp/finance/%'
    OR m.component_name LIKE '%Finance%'
    OR m.permission LIKE 'erp:finance-%'
    OR m.permission LIKE 'erp:ap-%'
    OR m.permission LIKE 'erp:account:%'
  )
ORDER BY m.parent_id, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
