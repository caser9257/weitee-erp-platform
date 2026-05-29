-- 财务主管财务模块权限完整补全脚本
-- 目标：
-- 1. 面向角色 erp_finance_manager
-- 2. 按正式财务菜单树（ERP -> 财务管理）整体补齐页面与按钮权限
-- 3. 对控制器存在但菜单树可能未完整挂出的权限点做兜底补齐
-- 4. 幂等执行，可重复运行

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

SET @erp_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        id = 2563
        OR path = '/erp'
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
        id = 2645
        OR (parent_id = @erp_root_id AND path = 'finance')
        OR name = '财务管理'
      )
    ORDER BY id
    LIMIT 1
);

SET @report_query_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND permission = 'erp:finance-report:query'
    ORDER BY id
    LIMIT 1
);

-- 1. 补 ERP 根菜单 + 正式财务根菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, t.menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT @erp_root_id AS menu_id
    UNION ALL
    SELECT @finance_root_id AS menu_id
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

-- 2. 按正式财务菜单树整棵补权限
-- 说明：
-- - type=2 页面菜单
-- - type=3 按钮/接口权限
-- 这样可以把结算账户、付款单、收款单，以及后续挂在财务管理下的页面一并授给财务主管
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component,
    component_name, status, visible, keep_alive, always_show,
    creator, create_time, updater, update_time, deleted
)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM system_menu t),
       '财务报表查询', 'erp:finance-report:query', 3, 1, @finance_root_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @report_query_menu_id IS NULL;

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_id IS NOT NULL
  AND @finance_root_id IS NOT NULL
  AND m.deleted = b'0'
  AND (
        m.id = @finance_root_id
        OR m.parent_id = @finance_root_id
        OR m.parent_id IN (
            SELECT id
            FROM system_menu
            WHERE deleted = b'0'
              AND parent_id = @finance_root_id
        )
      )
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

-- 3. 对控制器权限点做兜底补齐
-- 说明：
-- - 有些接口权限点不一定完整挂在正式财务菜单树下面
-- - 按控制器实际使用的 permission 统一补齐，重点解决页面能进但接口 403 的问题
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, m.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM system_menu m
WHERE @role_id IS NOT NULL
  AND m.deleted = b'0'
  AND m.permission IN (
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
  AND NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = @role_id
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND rm.tenant_id = @tenant_id
  );

-- 4. 如历史误授了公众号账号管理菜单，顺手移除，避免污染财务菜单树
DELETE rm
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id
WHERE rm.role_id = @role_id
  AND rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND m.deleted = b'0'
  AND m.component = 'mp/account/index'
  AND m.path = 'account';

-- 5. 验证输出
SELECT
    r.code AS role_code,
    m.id,
    m.name,
    m.permission,
    m.type,
    m.parent_id,
    m.path,
    m.component,
    m.component_name
FROM system_role_menu rm
JOIN system_role r
  ON r.id = rm.role_id
 AND r.deleted = b'0'
JOIN system_menu m
  ON m.id = rm.menu_id
 AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND r.code = 'erp_finance_manager'
  AND (
      m.id = @finance_root_id
      OR m.parent_id = @finance_root_id
      OR m.parent_id IN (
          SELECT id
          FROM system_menu
          WHERE deleted = b'0'
            AND parent_id = @finance_root_id
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
          'erp:finance-dual-ledger-diff-config:delete',
          'erp:finance-voucher-template:query',
          'erp:finance-voucher-template:create',
          'erp:finance-voucher-template:update',
          'erp:finance-voucher-template:delete'
      )
  )
ORDER BY m.parent_id, m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
