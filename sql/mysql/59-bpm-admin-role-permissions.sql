-- 流程管理员角色和权限配置（去除租户）
-- 创建日期：2026-06-11

-- 1. 创建流程管理员角色
INSERT IGNORE INTO `system_role` (`id`, `name`, `code`, `type`, `sort`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(100, '流程管理员', 'bpm_admin', 1, 100, 0, '负责管理审批流程、模板、场景等配置', '1', NOW(), '1', NOW(), b'0');

-- 2. 创建流程管理菜单（使用 INSERT IGNORE 避免重复插入）
INSERT IGNORE INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
-- 一级菜单：流程管理
(2000, '流程管理', '', 2, 100, 0, '/bpm', 'ep:promotion', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 二级菜单：审批场景
(2001, '审批场景', 'bpm:approval-scene:query', 2, 1, 2000, 'approval-scene', 'ep:coordinate', 'bpm/approvalScene/index', 'BpmApprovalScene', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2002, '审批场景查询', 'bpm:approval-scene:query', 3, 1, 2001, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2003, '审批场景创建', 'bpm:approval-scene:create', 3, 2, 2001, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2004, '审批场景更新', 'bpm:approval-scene:update', 3, 3, 2001, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2005, '审批场景删除', 'bpm:approval-scene:delete', 3, 4, 2001, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 二级菜单：审批模板
(2010, '审批模板', 'bpm:approval-template:query', 2, 2, 2000, 'approval-template', 'ep:document', 'bpm/approvalTemplate/index', 'BpmApprovalTemplate', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2011, '审批模板查询', 'bpm:approval-template:query', 3, 1, 2010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2012, '审批模板创建', 'bpm:approval-template:create', 3, 2, 2010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2013, '审批模板更新', 'bpm:approval-template:update', 3, 3, 2010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2014, '审批模板删除', 'bpm:approval-template:delete', 3, 4, 2010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2015, '审批模板使用', 'bpm:approval-template:use', 3, 5, 2010, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 二级菜单：审批委托
(2020, '审批委托', 'bpm:approval-delegation:query', 2, 3, 2000, 'approval-delegation', 'ep:connection', 'bpm/approvalDelegation/index', 'BpmApprovalDelegation', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2021, '审批委托查询', 'bpm:approval-delegation:query', 3, 1, 2020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2022, '审批委托创建', 'bpm:approval-delegation:create', 3, 2, 2020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2023, '审批委托更新', 'bpm:approval-delegation:update', 3, 3, 2020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2024, '审批委托删除', 'bpm:approval-delegation:delete', 3, 4, 2020, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),

-- 二级菜单：流程监控
(2030, '流程监控', 'bpm:monitor:query', 2, 4, 2000, 'monitor', 'ep:monitor', 'bpm/monitor/index', 'BpmMonitor', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2031, '流程监控查询', 'bpm:monitor:query', 3, 1, 2030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(2032, '流程终止', 'bpm:monitor:terminate', 3, 2, 2030, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 3. 分配权限给流程管理员角色（使用 INSERT IGNORE 避免重复插入）
INSERT IGNORE INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
-- 审批场景权限
(100, 2000, '1', NOW(), '1', NOW(), b'0'),
(100, 2001, '1', NOW(), '1', NOW(), b'0'),
(100, 2002, '1', NOW(), '1', NOW(), b'0'),
(100, 2003, '1', NOW(), '1', NOW(), b'0'),
(100, 2004, '1', NOW(), '1', NOW(), b'0'),
(100, 2005, '1', NOW(), '1', NOW(), b'0'),
-- 审批模板权限
(100, 2010, '1', NOW(), '1', NOW(), b'0'),
(100, 2011, '1', NOW(), '1', NOW(), b'0'),
(100, 2012, '1', NOW(), '1', NOW(), b'0'),
(100, 2013, '1', NOW(), '1', NOW(), b'0'),
(100, 2014, '1', NOW(), '1', NOW(), b'0'),
(100, 2015, '1', NOW(), '1', NOW(), b'0'),
-- 审批委托权限
(100, 2020, '1', NOW(), '1', NOW(), b'0'),
(100, 2021, '1', NOW(), '1', NOW(), b'0'),
(100, 2022, '1', NOW(), '1', NOW(), b'0'),
(100, 2023, '1', NOW(), '1', NOW(), b'0'),
(100, 2024, '1', NOW(), '1', NOW(), b'0'),
-- 流程监控权限
(100, 2030, '1', NOW(), '1', NOW(), b'0'),
(100, 2031, '1', NOW(), '1', NOW(), b'0'),
(100, 2032, '1', NOW(), '1', NOW(), b'0');
