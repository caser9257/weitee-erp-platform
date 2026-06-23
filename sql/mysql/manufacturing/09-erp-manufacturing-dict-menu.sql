/*
 Navicat / MySQL Init Script
 Target: Manufacturing Dict/Menu
 Schema: ruoyi-vue-pro
 Date: 2026-04-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Dict types
-- ----------------------------
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES
(701, 'ERP 产品类型', 'erp_product_type', 0, '制造产品类型', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(702, 'ERP 生产方式', 'erp_produce_type', 0, '离散制造生产方式', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(703, 'ERP 工艺路线状态', 'erp_route_status', 0, '工艺路线状态', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(704, 'ERP 工序状态', 'erp_step_status', 0, '工序状态', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(705, 'ERP 设备状态', 'erp_device_status', 0, '设备状态', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(706, 'ERP 生产工单状态', 'erp_production_order_status', 0, '生产工单状态', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(707, 'ERP 报工类型', 'erp_report_type', 0, '生产报工类型', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(708, 'ERP 领料类型', 'erp_issue_type', 0, '领料和补料类型', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(709, 'ERP 质检类型', 'erp_inspection_type', 0, '质检来源类型', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(710, 'ERP 质检结果', 'erp_inspection_result', 0, '质检结果', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00'),
(711, 'ERP 成本方式', 'erp_cost_method', 0, '成本方式', '1', NOW(), '1', NOW(), b'0', '1970-01-01 00:00:00');

-- ----------------------------
-- Dict data
-- ----------------------------
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1701, 1, '采购件', '1', 'erp_product_type', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1702, 2, '自制件', '2', 'erp_product_type', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1703, 3, '委外件', '3', 'erp_product_type', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1704, 1, '按库存生产', '1', 'erp_produce_type', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1705, 2, '按订单生产', '2', 'erp_produce_type', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1706, 1, '草稿', '0', 'erp_route_status', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1707, 2, '启用', '1', 'erp_route_status', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1708, 3, '停用', '2', 'erp_route_status', 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'),
(1709, 1, '待开始', '0', 'erp_step_status', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1710, 2, '进行中', '1', 'erp_step_status', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1711, 3, '已完成', '2', 'erp_step_status', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1712, 4, '已暂停', '3', 'erp_step_status', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1713, 1, '待机', '1', 'erp_device_status', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1714, 2, '运行中', '2', 'erp_device_status', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1715, 3, '维修中', '3', 'erp_device_status', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1716, 4, '停用', '4', 'erp_device_status', 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'),
(1717, 1, '待下达', '0', 'erp_production_order_status', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1718, 2, '已下达', '1', 'erp_production_order_status', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1719, 3, '生产中', '2', 'erp_production_order_status', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1720, 4, '已完工', '3', 'erp_production_order_status', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1721, 5, '已关闭', '4', 'erp_production_order_status', 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'),
(1722, 1, '工序报工', '1', 'erp_report_type', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1723, 2, '完工报工', '2', 'erp_report_type', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1724, 1, '标准领料', '1', 'erp_issue_type', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1725, 2, '补料', '2', 'erp_issue_type', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1726, 1, '来料检验', '1', 'erp_inspection_type', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1727, 2, '工序检验', '2', 'erp_inspection_type', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0'),
(1728, 3, '完工检验', '3', 'erp_inspection_type', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1729, 4, '委外收货检验', '4', 'erp_inspection_type', 0, 'warning', '', '', '1', NOW(), '1', NOW(), b'0'),
(1730, 1, '待检', '0', 'erp_inspection_result', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1731, 2, '合格', '1', 'erp_inspection_result', 0, 'success', '', '', '1', NOW(), '1', NOW(), b'0'),
(1732, 3, '不合格', '2', 'erp_inspection_result', 0, 'danger', '', '', '1', NOW(), '1', NOW(), b'0'),
(1733, 1, '标准成本', '1', 'erp_cost_method', 0, 'default', '', '', '1', NOW(), '1', NOW(), b'0'),
(1734, 2, '移动平均', '2', 'erp_cost_method', 0, 'primary', '', '', '1', NOW(), '1', NOW(), b'0');

-- ----------------------------
-- Menu structure
-- ----------------------------
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(3200, '制造管理', '', 1, 320, 0, '/manufacturing', 'ep:management', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3201, '工艺管理', '', 2, 10, 3200, 'process-route', 'ep:share', 'erp/manufacturing/process-route/index', 'ErpProcessRoute', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3202, '工作中心', '', 2, 20, 3200, 'work-center', 'ep:office-building', 'erp/manufacturing/work-center/index', 'ErpWorkCenter', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3203, '设备台账', '', 2, 30, 3200, 'device', 'ep:cpu', 'erp/manufacturing/device/index', 'ErpDevice', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3204, '生产报工', '', 2, 40, 3200, 'production-report', 'ep:histogram', 'erp/manufacturing/production-report/index', 'ErpProductionReport', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3205, '生产领料', '', 2, 50, 3200, 'material-issue', 'ep:box', 'erp/manufacturing/material-issue/index', 'ErpMaterialIssue', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3206, '生产退料', '', 2, 60, 3200, 'material-return', 'ep:refresh-left', 'erp/manufacturing/material-return/index', 'ErpMaterialReturn', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- ----------------------------
-- Button permissions
-- ----------------------------
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(3211, '工艺路线查询', 'erp:process-route:query', 3, 1, 3201, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3212, '工艺路线创建', 'erp:process-route:create', 3, 2, 3201, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3213, '工艺路线更新', 'erp:process-route:update', 3, 3, 3201, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3214, '工艺路线删除', 'erp:process-route:delete', 3, 4, 3201, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3221, '工作中心查询', 'erp:work-center:query', 3, 1, 3202, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3222, '工作中心创建', 'erp:work-center:create', 3, 2, 3202, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3223, '工作中心更新', 'erp:work-center:update', 3, 3, 3202, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3224, '工作中心删除', 'erp:work-center:delete', 3, 4, 3202, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3231, '设备查询', 'erp:device:query', 3, 1, 3203, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3232, '设备创建', 'erp:device:create', 3, 2, 3203, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3233, '设备更新', 'erp:device:update', 3, 3, 3203, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3234, '设备删除', 'erp:device:delete', 3, 4, 3203, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3241, '报工查询', 'erp:production-report:query', 3, 1, 3204, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3242, '报工创建', 'erp:production-report:create', 3, 2, 3204, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3243, '报工更新', 'erp:production-report:update', 3, 3, 3204, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3244, '报工删除', 'erp:production-report:delete', 3, 4, 3204, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3251, '领料查询', 'erp:material-issue:query', 3, 1, 3205, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3252, '领料创建', 'erp:material-issue:create', 3, 2, 3205, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3253, '领料更新', 'erp:material-issue:update', 3, 3, 3205, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3254, '领料删除', 'erp:material-issue:delete', 3, 4, 3205, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3261, '退料查询', 'erp:material-return:query', 3, 1, 3206, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3262, '退料创建', 'erp:material-return:create', 3, 2, 3206, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3263, '退料更新', 'erp:material-return:update', 3, 3, 3206, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(3264, '退料删除', 'erp:material-return:delete', 3, 4, 3206, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- ----------------------------
-- Role menu for admin role
-- ----------------------------
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1, 3200, '1', NOW(), '1', NOW(), b'0'),
(1, 3201, '1', NOW(), '1', NOW(), b'0'),
(1, 3202, '1', NOW(), '1', NOW(), b'0'),
(1, 3203, '1', NOW(), '1', NOW(), b'0'),
(1, 3204, '1', NOW(), '1', NOW(), b'0'),
(1, 3205, '1', NOW(), '1', NOW(), b'0'),
(1, 3206, '1', NOW(), '1', NOW(), b'0'),
(1, 3211, '1', NOW(), '1', NOW(), b'0'),
(1, 3212, '1', NOW(), '1', NOW(), b'0'),
(1, 3213, '1', NOW(), '1', NOW(), b'0'),
(1, 3214, '1', NOW(), '1', NOW(), b'0'),
(1, 3221, '1', NOW(), '1', NOW(), b'0'),
(1, 3222, '1', NOW(), '1', NOW(), b'0'),
(1, 3223, '1', NOW(), '1', NOW(), b'0'),
(1, 3224, '1', NOW(), '1', NOW(), b'0'),
(1, 3231, '1', NOW(), '1', NOW(), b'0'),
(1, 3232, '1', NOW(), '1', NOW(), b'0'),
(1, 3233, '1', NOW(), '1', NOW(), b'0'),
(1, 3234, '1', NOW(), '1', NOW(), b'0'),
(1, 3241, '1', NOW(), '1', NOW(), b'0'),
(1, 3242, '1', NOW(), '1', NOW(), b'0'),
(1, 3243, '1', NOW(), '1', NOW(), b'0'),
(1, 3244, '1', NOW(), '1', NOW(), b'0'),
(1, 3251, '1', NOW(), '1', NOW(), b'0'),
(1, 3252, '1', NOW(), '1', NOW(), b'0'),
(1, 3253, '1', NOW(), '1', NOW(), b'0'),
(1, 3254, '1', NOW(), '1', NOW(), b'0'),
(1, 3261, '1', NOW(), '1', NOW(), b'0'),
(1, 3262, '1', NOW(), '1', NOW(), b'0'),
(1, 3263, '1', NOW(), '1', NOW(), b'0'),
(1, 3264, '1', NOW(), '1', NOW(), b'0');

SET FOREIGN_KEY_CHECKS = 1;
