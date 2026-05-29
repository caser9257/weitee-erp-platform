-- 70-erp-finance-formal-menu-quick-access.sql
-- 目的：
-- 1. 将正式财务菜单下的“应收应付”接到真实业务页
-- 2. 将旧财务根下已可用的“结算账户 / 收款单 / 付款单”平移到正式 /finance 根下
-- 3. 保留原菜单 ID，尽量复用既有角色授权，降低权限副作用

SET @formal_finance_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/finance'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

-- 正式财务菜单：应收应付 -> 真实业务页
UPDATE `system_menu`
SET `name` = '应收应付',
    `component` = 'erp/finance/apar/index',
    `component_name` = 'FormalFinanceApar',
    `icon` = 'ep:credit-card',
    `sort` = 30,
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = 930182
  AND `deleted` = b'0';

-- 旧财务菜单：结算账户 -> 挂到正式 /finance 根下
UPDATE `system_menu`
SET `parent_id` = @formal_finance_root_id,
    `name` = '结算账户',
    `path` = 'account',
    `icon` = 'fa:universal-access',
    `component` = 'erp/finance/account/index',
    `component_name` = 'ErpAccount',
    `sort` = 31,
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = 2646
  AND `deleted` = b'0'
  AND @formal_finance_root_id IS NOT NULL;

-- 旧财务菜单：收款单 -> 挂到正式 /finance 根下，并按正式信息架构更名
UPDATE `system_menu`
SET `parent_id` = @formal_finance_root_id,
    `name` = '收款管理',
    `path` = 'receipt',
    `icon` = 'ep:expand',
    `component` = 'erp/finance/receipt/index',
    `component_name` = 'ErpFinanceReceipt',
    `sort` = 32,
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = 2694
  AND `deleted` = b'0'
  AND @formal_finance_root_id IS NOT NULL;

-- 旧财务菜单：付款单 -> 挂到正式 /finance 根下，并按正式信息架构更名
UPDATE `system_menu`
SET `parent_id` = @formal_finance_root_id,
    `name` = '付款管理',
    `path` = 'payment',
    `icon` = 'ep:caret-right',
    `component` = 'erp/finance/payment/index',
    `component_name` = 'ErpFinancePayment',
    `sort` = 33,
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = 2687
  AND `deleted` = b'0'
  AND @formal_finance_root_id IS NOT NULL;
