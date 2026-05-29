-- =====================================================
-- ERP finance audit role seed data
-- 1. add external audit role
-- 2. assign menu permissions for audit role
-- =====================================================

-- 新增"外部审计"角色
INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (103, '外部审计', 'finance_audit', 100, 2, '', 0, 2, '外部审计人员专用角色，只能访问外部账簿数据', 'system', NOW(), 'system', NOW(), b'0');

-- 为"外部审计"角色分配菜单权限（只显示外部账相关功能）
-- 注意：具体的菜单ID需要根据实际系统中的菜单ID进行调整
-- 这里假设外部账查询页面的菜单ID为 1080
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`)
VALUES
    (103, 1080, 'system', NOW(), 'system', NOW()),  -- 外部账查询
    (103, 1081, 'system', NOW(), 'system', NOW());  -- 外部账导出
