-- 财务菜单与权限整合脚本
-- 统一承接并替代以下分片：
-- 96-erp-finance-secondary-menu-restore.sql
-- 102-erp-finance-role-readonly-permission-fix.sql
-- 106-erp-ap-statement-permission-seed-and-grant.sql
-- 107-erp-ap-estimate-and-expense-permission-fix.sql
-- 114-erp-finance-payment-bpm-permission-fix.sql
-- 119-erp-finance-payment-void-permission-fix.sql
-- 119-erp-finance-prepayment-menu-permission-fix.sql
-- 134-erp-finance-voucher-template-menu-permission-fix.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @erp_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      path = '/erp'
      OR component_name IN ('ProjectErpRoot', 'FormalErpRoot')
      OR name = 'ERP 系统'
    )
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       'ERP 系统', '', 1, 50, 0, '/erp', 'simple-icons:erpnext', '', 'ProjectErpRoot',
       0, b'1', b'0', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @erp_root_id IS NULL;

SET @erp_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      path = '/erp'
      OR component_name IN ('ProjectErpRoot', 'FormalErpRoot')
      OR name = 'ERP 系统'
    )
  ORDER BY id
  LIMIT 1
);

SET @finance_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      path = '/finance'
      OR (parent_id = @erp_root_id AND path = 'finance')
      OR component_name IN ('ProjectFinanceRoot', 'FormalFinanceRoot')
      OR name = '财务管理'
    )
  ORDER BY
    CASE
      WHEN path = '/finance' THEN 0
      WHEN path = 'finance' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '财务管理', '', 1, 10, @erp_root_id, 'finance', 'ep:money', '', 'FormalFinanceRoot',
       0, b'1', b'0', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @erp_root_id IS NOT NULL
  AND @finance_root_id IS NULL;

SET @finance_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      path = '/finance'
      OR (parent_id = @erp_root_id AND path = 'finance')
      OR component_name IN ('ProjectFinanceRoot', 'FormalFinanceRoot')
      OR name = '财务管理'
    )
  ORDER BY
    CASE
      WHEN path = '/finance' THEN 0
      WHEN path = 'finance' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET status = 0,
    visible = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id IN (@erp_root_id, @finance_root_id)
  AND deleted = b'0';

SET @apar_menu_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/apar/index'
      OR component_name IN ('FormalFinanceApar', 'ProjectFinanceApar')
      OR (parent_id = @finance_root_id AND path = 'apar')
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
      OR component_name IN ('FormalFinanceApEstimate', 'ProjectFinanceApEstimate')
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
      OR component_name IN ('FormalFinanceExpense', 'ProjectFinanceExpense')
      OR (parent_id = @finance_root_id AND path = 'expense')
    )
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '研发报销 / 零星采购', '', 2, 35, @finance_root_id, 'expense', 'ep:document',
       'erp/finance/expense/index', 'FormalFinanceExpense',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @expense_menu_id IS NULL;

SET @expense_menu_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/expense/index'
      OR component_name IN ('FormalFinanceExpense', 'ProjectFinanceExpense')
      OR (parent_id = @finance_root_id AND path = 'expense')
    )
  ORDER BY id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @finance_root_id,
    name = '研发报销 / 零星采购',
    path = 'expense',
    component = 'erp/finance/expense/index',
    component_name = 'FormalFinanceExpense',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    updater = '1',
    update_time = NOW()
WHERE id = @expense_menu_id
  AND deleted = b'0';

SET @prepayment_page_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/prepayment/index'
      OR component_name IN ('ErpFinancePrepayment', 'FormalFinancePrepayment')
      OR (parent_id = @finance_root_id AND path = 'prepayment')
    )
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款', '', 2, 115, @finance_root_id, 'prepayment', 'ep:wallet-filled',
       'erp/finance/prepayment/index', 'ErpFinancePrepayment',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @prepayment_page_id IS NULL;

SET @prepayment_page_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/prepayment/index'
      OR component_name IN ('ErpFinancePrepayment', 'FormalFinancePrepayment')
      OR (parent_id = @finance_root_id AND path = 'prepayment')
    )
  ORDER BY id
  LIMIT 1
);

SET @voucher_template_page_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/voucher-template/index'
      OR component_name IN ('ErpFinanceVoucherTemplate', 'FormalFinanceVoucherTemplate')
      OR (parent_id = @finance_root_id AND path = 'voucher-template')
    )
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证模板', '', 2, 95, @finance_root_id, 'voucher-template', 'ep:files',
       'erp/finance/voucher-template/index', 'ErpFinanceVoucherTemplate',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @voucher_template_page_id IS NULL;

SET @voucher_template_page_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/voucher-template/index'
      OR component_name IN ('ErpFinanceVoucherTemplate', 'FormalFinanceVoucherTemplate')
      OR (parent_id = @finance_root_id AND path = 'voucher-template')
    )
  ORDER BY id
  LIMIT 1
);

SET @payment_menu_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND (
      component = 'erp/finance/payment/index'
      OR component_name IN ('FormalFinancePayment', 'ErpFinancePayment')
      OR (parent_id = @finance_root_id AND path = 'payment')
      OR path = 'payment'
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
      OR (parent_id = @finance_root_id AND path = 'ledger')
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
      OR (parent_id = @finance_root_id AND path = 'period')
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
      OR (parent_id = @finance_root_id AND path = 'subject')
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
      OR (parent_id = @finance_root_id AND path = 'report-item')
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
      OR (parent_id = @finance_root_id AND path = 'reports')
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
      OR (parent_id = @finance_root_id AND path = 'voucher')
    )
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '应付台账查询', 'erp:ap-statement:query', 3, 1, @apar_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-statement:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '应付台账更新', 'erp:ap-statement:update', 3, 2, @apar_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-statement:update');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '应付台账导出', 'erp:ap-statement:export', 3, 3, @apar_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @apar_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-statement:export');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单查询', 'erp:ap-estimate:query', 3, 1, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单扫描', 'erp:ap-estimate:scan', 3, 2, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:scan');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单更新', 'erp:ap-estimate:update', 3, 3, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:update');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单确认', 'erp:ap-estimate:confirm', 3, 4, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:confirm');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单反确认', 'erp:ap-estimate:reverse', 3, 5, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:reverse');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '暂估单导出', 'erp:ap-estimate:export', 3, 6, @ap_estimate_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_estimate_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:ap-estimate:export');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单查询', 'erp:finance-expense:query', 3, 1, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单创建', 'erp:finance-expense:create', 3, 2, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:create');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单更新', 'erp:finance-expense:update', 3, 3, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单审批', 'erp:finance-expense:update-status', 3, 4, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:update-status');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单删除', 'erp:finance-expense:delete', 3, 5, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:delete');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '费用单导出', 'erp:finance-expense:export', 3, 6, @expense_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @expense_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-expense:export');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '账簿查询', 'erp:finance-ledger:query', 3, 1, @ledger_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @ledger_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-ledger:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '期间查询', 'erp:finance-period:query', 3, 1, @period_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @period_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-period:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '科目查询', 'erp:finance-subject:query', 3, 1, @subject_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @subject_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-subject:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '报表项目查询', 'erp:finance-report-item:query', 3, 1, @report_item_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @report_item_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-report-item:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '财务报表查询', 'erp:finance-report:query', 3, 1, @reports_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @reports_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-report:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证查询', 'erp:finance-voucher:query', 3, 1, @voucher_menu_id, '', '', '', '',
       0, b'1', b'1', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '付款单提交审批', 'erp:finance-payment:submit', 3, 7, @payment_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @payment_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-payment:submit');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '付款单撤回审批', 'erp:finance-payment:cancel-approval', 3, 8, @payment_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @payment_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-payment:cancel-approval');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '付款单作废', 'erp:finance-payment:void', 3, 9, @payment_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @payment_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-payment:void');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款查询', 'erp:finance-prepayment:query', 3, 1, @prepayment_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-prepayment:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款创建', 'erp:finance-prepayment:create', 3, 2, @prepayment_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-prepayment:create');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款更新', 'erp:finance-prepayment:update', 3, 3, @prepayment_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-prepayment:update');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款删除', 'erp:finance-prepayment:delete', 3, 4, @prepayment_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-prepayment:delete');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '预付款审批', 'erp:finance-prepayment:update-status', 3, 5, @prepayment_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-prepayment:update-status');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证模板查询', 'erp:finance-voucher-template:query', 3, 1, @voucher_template_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_template_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-template:query');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证模板创建', 'erp:finance-voucher-template:create', 3, 2, @voucher_template_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_template_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-template:create');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证模板更新', 'erp:finance-voucher-template:update', 3, 3, @voucher_template_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_template_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-template:update');

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
       '凭证模板删除', 'erp:finance-voucher-template:delete', 3, 4, @voucher_template_page_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @voucher_template_page_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher-template:delete');

UPDATE system_menu
SET parent_id = @apar_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN ('erp:ap-statement:query', 'erp:ap-statement:update', 'erp:ap-statement:export');

UPDATE system_menu
SET parent_id = @ap_estimate_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:ap-estimate:query',
    'erp:ap-estimate:scan',
    'erp:ap-estimate:update',
    'erp:ap-estimate:confirm',
    'erp:ap-estimate:reverse',
    'erp:ap-estimate:export'
  );

UPDATE system_menu
SET parent_id = @expense_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:finance-expense:query',
    'erp:finance-expense:create',
    'erp:finance-expense:update',
    'erp:finance-expense:update-status',
    'erp:finance-expense:delete',
    'erp:finance-expense:export'
  );

UPDATE system_menu
SET parent_id = @ledger_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-ledger:query';

UPDATE system_menu
SET parent_id = @period_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-period:query';

UPDATE system_menu
SET parent_id = @subject_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-subject:query';

UPDATE system_menu
SET parent_id = @report_item_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-report-item:query';

UPDATE system_menu
SET parent_id = @reports_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-report:query';

UPDATE system_menu
SET parent_id = @voucher_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0' AND permission = 'erp:finance-voucher:query';

UPDATE system_menu
SET parent_id = @payment_menu_id, status = 0, updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN ('erp:finance-payment:submit', 'erp:finance-payment:cancel-approval', 'erp:finance-payment:void');

UPDATE system_menu
SET parent_id = @prepayment_page_id, status = 0, visible = b'1', updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:finance-prepayment:query',
    'erp:finance-prepayment:create',
    'erp:finance-prepayment:update',
    'erp:finance-prepayment:delete',
    'erp:finance-prepayment:update-status'
  );

UPDATE system_menu
SET parent_id = @voucher_template_page_id, status = 0, visible = b'1', updater = '1', update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:finance-voucher-template:query',
    'erp:finance-voucher-template:create',
    'erp:finance-voucher-template:update',
    'erp:finance-voucher-template:delete'
  );

SET @role_mgr_id := (
  SELECT id FROM system_role
  WHERE deleted = b'0' AND code = 'erp_finance_manager'
  ORDER BY id
  LIMIT 1
);

SET @role_clerk_id := (
  SELECT id FROM system_role
  WHERE deleted = b'0' AND code = 'erp_finance_clerk'
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_mgr_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_mgr_id IS NOT NULL
  AND m.deleted = b'0'
  AND (
    m.id IN (
      @erp_root_id, @finance_root_id, @apar_menu_id, @ap_estimate_menu_id, @expense_menu_id,
      @ledger_menu_id, @period_menu_id, @subject_menu_id, @report_item_menu_id, @reports_menu_id,
      @voucher_menu_id, @payment_menu_id, @prepayment_page_id, @voucher_template_page_id
    )
    OR m.permission IN (
      'erp:account:query',
      'erp:account:create',
      'erp:account:update',
      'erp:account:delete',
      'erp:account:export',
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
      'erp:finance-payment:submit',
      'erp:finance-payment:cancel-approval',
      'erp:finance-payment:void',
      'erp:finance-receipt:query',
      'erp:finance-receipt:create',
      'erp:finance-receipt:update',
      'erp:finance-receipt:update-status',
      'erp:finance-receipt:delete',
      'erp:finance-receipt:export',
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
  )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_mgr_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_clerk_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_clerk_id IS NOT NULL
  AND m.deleted = b'0'
  AND (
    m.id IN (
      @finance_root_id, @apar_menu_id, @ledger_menu_id, @period_menu_id, @subject_menu_id,
      @report_item_menu_id, @reports_menu_id, @voucher_menu_id, @payment_menu_id,
      @prepayment_page_id, @voucher_template_page_id
    )
    OR m.permission IN (
      'erp:ap-statement:query',
      'erp:ap-statement:update',
      'erp:ap-statement:export',
      'erp:purchase-in:query',
      'erp:purchase-return:query',
      'erp:finance-ledger:query',
      'erp:finance-period:query',
      'erp:finance-subject:query',
      'erp:finance-report-item:query',
      'erp:finance-report:query',
      'erp:finance-voucher:query',
      'erp:finance-payment:submit',
      'erp:finance-payment:cancel-approval',
      'erp:finance-payment:void',
      'erp:finance-prepayment:query',
      'erp:finance-prepayment:create',
      'erp:finance-prepayment:update',
      'erp:finance-prepayment:update-status',
      'erp:finance-prepayment:delete',
      'erp:finance-voucher-template:query',
      'erp:finance-voucher-template:create',
      'erp:finance-voucher-template:update',
      'erp:finance-voucher-template:delete'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_clerk_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT DISTINCT role_pool.role_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
  SELECT 1 AS role_id
  UNION ALL
  SELECT id FROM system_role
  WHERE deleted = b'0'
    AND code IN ('super_admin', 'admin', 'erp_finance_purchase_collab')
) role_pool
JOIN system_menu m
  ON m.deleted = b'0'
 AND (
   m.id IN (@finance_root_id, @prepayment_page_id, @voucher_template_page_id)
   OR m.permission IN (
     'erp:finance-prepayment:query',
     'erp:finance-prepayment:create',
     'erp:finance-prepayment:update',
     'erp:finance-prepayment:update-status',
     'erp:finance-prepayment:delete',
     'erp:finance-voucher-template:query',
     'erp:finance-voucher-template:create',
     'erp:finance-voucher-template:update',
     'erp:finance-voucher-template:delete'
   )
 )
WHERE NOT EXISTS (
  SELECT 1
  FROM system_role_menu rm
  WHERE rm.role_id = role_pool.role_id
    AND rm.menu_id = m.id
    AND rm.deleted = b'0'
    AND rm.tenant_id = @tenant_id
);

DELETE rm
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id
WHERE rm.role_id = @role_mgr_id
  AND rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND m.deleted = b'0'
  AND m.component = 'mp/account/index'
  AND m.path = 'account';

SELECT r.code AS role_code, m.name, m.permission
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND r.code IN ('erp_finance_manager', 'erp_finance_clerk', 'erp_finance_purchase_collab')
  AND (
    m.id IN (
      @finance_root_id, @apar_menu_id, @ap_estimate_menu_id, @expense_menu_id,
      @ledger_menu_id, @period_menu_id, @subject_menu_id, @report_item_menu_id,
      @reports_menu_id, @voucher_menu_id, @payment_menu_id, @prepayment_page_id,
      @voucher_template_page_id
    )
    OR m.permission IN (
      'erp:ap-statement:query',
      'erp:ap-statement:update',
      'erp:ap-statement:export',
      'erp:ap-estimate:query',
      'erp:ap-estimate:scan',
      'erp:ap-estimate:update',
      'erp:ap-estimate:confirm',
      'erp:ap-estimate:reverse',
      'erp:ap-estimate:export',
      'erp:finance-expense:query',
      'erp:finance-expense:create',
      'erp:finance-expense:update',
      'erp:finance-expense:update-status',
      'erp:finance-expense:delete',
      'erp:finance-expense:export',
      'erp:finance-ledger:query',
      'erp:finance-period:query',
      'erp:finance-subject:query',
      'erp:finance-report-item:query',
      'erp:finance-report:query',
      'erp:finance-voucher:query',
      'erp:finance-payment:submit',
      'erp:finance-payment:cancel-approval',
      'erp:finance-payment:void',
      'erp:finance-prepayment:query',
      'erp:finance-prepayment:create',
      'erp:finance-prepayment:update',
      'erp:finance-prepayment:update-status',
      'erp:finance-prepayment:delete',
      'erp:finance-voucher-template:query',
      'erp:finance-voucher-template:create',
      'erp:finance-voucher-template:update',
      'erp:finance-voucher-template:delete'
    )
  )
ORDER BY r.code, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
