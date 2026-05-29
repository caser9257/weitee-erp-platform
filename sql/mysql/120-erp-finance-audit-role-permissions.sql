-- =====================================================
-- ERP finance audit role permissions
-- 1. assign menu permissions for audit role
-- 2. assign ledger permissions for audit role
-- =====================================================

-- 为"外部审计"角色分配菜单权限
-- 注意：以下菜单ID需要根据实际系统中的菜单ID进行调整
-- 外部审计角色不应获得双账套相关菜单权限

-- 清除旧的菜单权限（如果存在）
DELETE FROM `system_role_menu` WHERE `role_id` = 103;

-- 不再分配双账套菜单权限，外部审计只允许查看外部账相关页面

-- 为"外部审计"角色分配账簿权限（只能访问外部账簿）
-- 假设外部账簿ID为1，内部账簿ID为2
-- 实际使用时需要根据真实的账簿ID进行配置
INSERT INTO `erp_finance_ledger_role` (`ledger_id`, `role_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (1, 103, 0, '外部审计角色-外部账簿权限', 'system', NOW(), 'system', NOW(), b'0', 0);

-- 注意：不要为审计角色分配内部账簿的权限
-- 这样审计角色登录后只能看到外部账簿的数据
