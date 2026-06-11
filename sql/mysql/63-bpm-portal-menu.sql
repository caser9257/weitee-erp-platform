-- 审批门户和模板管理菜单配置
-- 创建日期：2026-06-11

-- 1. 创建审批门户菜单
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
-- 审批门户（一级菜单）
(3000, '审批中心', '', 2, 50, 0, '/approval', 'ep:promotion', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 审批门户子菜单
(3001, '待我审批', 'bpm:approval:query', 2, 1, 3000, 'todo', 'ep:clock', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3002, '我发起的', 'bpm:approval:query', 2, 2, 3000, 'submitted', 'ep:upload', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3003, '抄送我的', 'bpm:approval:query', 2, 3, 3000, 'cc', 'ep:message', 'bpm/approval/portal/index', 'BpmApprovalPortal', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 审批模板菜单（在流程管理下）
(3010, '审批模板', 'bpm:approval-template:query', 2, 5, 2000, 'approval-template', 'ep:document', 'bpm/approval/template/index', 'BpmApprovalTemplate', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3011, '审批模板查询', 'bpm:approval-template:query', 3, 1, 3010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3012, '审批模板使用', 'bpm:approval-template:use', 3, 2, 3010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 审批委托菜单（在流程管理下）
(3020, '审批委托', 'bpm:approval-delegation:query', 2, 6, 2000, 'approval-delegation', 'ep:connection', 'bpm/approval/delegation/index', 'BpmApprovalDelegation', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3021, '审批委托查询', 'bpm:approval-delegation:query', 3, 1, 3020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3022, '审批委托创建', 'bpm:approval-delegation:create', 3, 2, 3020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3023, '审批委托更新', 'bpm:approval-delegation:update', 3, 3, 3020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3024, '审批委托删除', 'bpm:approval-delegation:delete', 3, 4, 3020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 2. 分配权限给流程管理员角色
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
-- 审批门户权限
(100, 3000, '1', NOW(), '1', NOW(), b'0'),
(100, 3001, '1', NOW(), '1', NOW(), b'0'),
(100, 3002, '1', NOW(), '1', NOW(), b'0'),
(100, 3003, '1', NOW(), '1', NOW(), b'0'),
-- 审批模板权限
(100, 3010, '1', NOW(), '1', NOW(), b'0'),
(100, 3011, '1', NOW(), '1', NOW(), b'0'),
(100, 3012, '1', NOW(), '1', NOW(), b'0'),
-- 审批委托权限
(100, 3020, '1', NOW(), '1', NOW(), b'0'),
(100, 3021, '1', NOW(), '1', NOW(), b'0'),
(100, 3022, '1', NOW(), '1', NOW(), b'0'),
(100, 3023, '1', NOW(), '1', NOW(), b'0'),
(100, 3024, '1', NOW(), '1', NOW(), b'0');

-- 3. 分配权限给管理员角色（假设管理员角色 ID 为 1）
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
-- 审批门户权限
(1, 3000, '1', NOW(), '1', NOW(), b'0'),
(1, 3001, '1', NOW(), '1', NOW(), b'0'),
(1, 3002, '1', NOW(), '1', NOW(), b'0'),
(1, 3003, '1', NOW(), '1', NOW(), b'0'),
-- 审批模板权限
(1, 3010, '1', NOW(), '1', NOW(), b'0'),
(1, 3011, '1', NOW(), '1', NOW(), b'0'),
(1, 3012, '1', NOW(), '1', NOW(), b'0'),
-- 审批委托权限
(1, 3020, '1', NOW(), '1', NOW(), b'0'),
(1, 3021, '1', NOW(), '1', NOW(), b'0'),
(1, 3022, '1', NOW(), '1', NOW(), b'0'),
(1, 3023, '1', NOW(), '1', NOW(), b'0'),
(1, 3024, '1', NOW(), '1', NOW(), b'0');
