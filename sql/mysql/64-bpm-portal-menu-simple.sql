-- 审批中心菜单配置（简化版）
-- 请在数据库中执行此脚本

-- 1. 创建审批中心一级菜单
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (3000, '审批中心', '', 2, 50, 0, '/approval', 'ep:promotion', NULL, NULL, 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 2. 创建待我审批子菜单
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (3001, '待我审批', 'bpm:approval:query', 2, 1, 3000, 'todo', 'ep:clock', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 3. 创建我发起的子菜单
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (3002, '我发起的', 'bpm:approval:query', 2, 2, 3000, 'submitted', 'ep:upload', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 4. 创建抄送我的子菜单
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) 
VALUES (3003, '抄送我的', 'bpm:approval:query', 2, 3, 3000, 'cc', 'ep:message', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, 1, 1, 1, '1', NOW(), '1', NOW(), 0);

-- 5. 分配权限给管理员角色（role_id = 1）
INSERT IGNORE INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) VALUES
(1, 3000, '1', NOW(), '1', NOW(), 0),
(1, 3001, '1', NOW(), '1', NOW(), 0),
(1, 3002, '1', NOW(), '1', NOW(), 0),
(1, 3003, '1', NOW(), '1', NOW(), 0);
