-- 财务主管结算账户菜单修复脚本
-- 根因：
-- 1. 角色已有 erp:account:* 按钮权限
-- 2. 但误授了非财务模块的 account 页面菜单（如公众号账号管理）
-- 3. 缺少真正的财务“结算账户”页面菜单，导致页面入口与权限链不完整

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

SET @finance_root_id := (
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

SET @account_menu_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/account/index'
        OR component_name IN ('FormalFinanceAccount', 'ErpAccount')
      )
    ORDER BY id
    LIMIT 1
);

-- 补真正的财务结算账户页面菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, @account_menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_id IS NOT NULL
  AND @account_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = @role_id
        AND rm.menu_id = @account_menu_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

-- 补财务根菜单，确保父链完整
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT @role_id, @finance_root_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE @role_id IS NOT NULL
  AND @finance_root_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = @role_id
        AND rm.menu_id = @finance_root_id
        AND rm.deleted = b'0'
        AND rm.tenant_id = @tenant_id
  );

-- 可选：移除误授的公众号“账号管理”菜单，避免污染财务菜单树
DELETE rm
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id
WHERE rm.role_id = @role_id
  AND rm.deleted = b'0'
  AND rm.tenant_id = @tenant_id
  AND m.deleted = b'0'
  AND m.component = 'mp/account/index'
  AND m.path = 'account';

-- 验证
SELECT rm.menu_id, m.name, m.permission, m.type, m.parent_id, m.path, m.component, m.component_name
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.role_id = @role_id
  AND rm.deleted = b'0'
  AND (
      m.component = 'erp/finance/account/index'
      OR m.component = 'mp/account/index'
      OR m.permission LIKE 'erp:account:%'
      OR m.path = '/finance'
  )
ORDER BY m.type, m.sort, m.id;

SET FOREIGN_KEY_CHECKS = 1;
