-- 84-erp-finance-expense-menu.sql
-- 目的：
-- 1. 为正式财务菜单补充“研发报销 / 零星采购”入口
-- 2. 补齐费用单页面对应权限

SET @formal_finance_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/finance'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @finance_expense_menu_id := COALESCE(
  (
    SELECT `id`
    FROM `system_menu`
    WHERE `component` = 'erp/finance/expense/index'
      AND `deleted` = b'0'
    ORDER BY `id`
    LIMIT 1
  ),
  930187
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id,'研发报销 / 零星采购','',2,34,@formal_finance_root_id,'expense','ep:document','erp/finance/expense/index','FormalFinanceExpense',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu`
    WHERE `component` = 'erp/finance/expense/index'
      AND `deleted` = b'0'
  );

SET @finance_ap_invoice_menu_id := COALESCE(
  (
    SELECT `id`
    FROM `system_menu`
    WHERE `component` = 'erp/finance/ap-invoice/index'
      AND `deleted` = b'0'
    ORDER BY `id`
    LIMIT 1
  ),
  930186
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_ap_invoice_menu_id,'采购发票匹配','',2,33,@formal_finance_root_id,'ap-invoice','ep:document-checked','erp/finance/ap-invoice/index','ErpApInvoice',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu`
    WHERE `component` = 'erp/finance/ap-invoice/index'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 1,'费用单查询','erp:finance-expense:query',3,1,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:query'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 2,'费用单创建','erp:finance-expense:create',3,2,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:create'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 3,'费用单更新','erp:finance-expense:update',3,3,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:update'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 4,'费用单删除','erp:finance-expense:delete',3,4,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:delete'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 5,'费用单导出','erp:finance-expense:export',3,5,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:export'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_expense_menu_id + 6,'费用单审批','erp:finance-expense:update-status',3,6,@finance_expense_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:finance-expense:update-status'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_ap_invoice_menu_id + 1,'采购发票匹配查询','erp:ap-invoice:query',3,1,@finance_ap_invoice_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:ap-invoice:query'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_ap_invoice_menu_id + 2,'采购发票匹配创建','erp:ap-invoice:create',3,2,@finance_ap_invoice_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:ap-invoice:create'
    AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT @finance_ap_invoice_menu_id + 3,'采购发票匹配更新','erp:ap-invoice:update',3,3,@finance_ap_invoice_menu_id,'','','',NULL,0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @formal_finance_root_id IS NOT NULL
  AND NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `permission` = 'erp:ap-invoice:update'
    AND `deleted` = b'0'
);
