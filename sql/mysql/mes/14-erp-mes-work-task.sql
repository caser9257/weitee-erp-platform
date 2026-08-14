-- MES 排程模块第一阶段：工作日历 + 工序任务
-- 归属：制造执行管理（/mes）下新增"工序任务""工作日历"菜单

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 工作日历
CREATE TABLE IF NOT EXISTS `mes_work_calendar` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作日历编号',
  `calendar_name` varchar(64) NOT NULL COMMENT '日历名称',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号（空=全局默认）',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `week_mask` varchar(7) NOT NULL DEFAULT '1111100' COMMENT '周几开工，如 1111100（周一~周五）',
  `daily_hours` decimal(6,2) NOT NULL DEFAULT 8.00 COMMENT '每日可用小时',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_mes_work_calendar_center` (`work_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工作日历';

-- 2. 工序任务
CREATE TABLE IF NOT EXISTS `mes_work_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工序任务编号',
  `task_no` varchar(64) NOT NULL COMMENT '任务单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `production_order_no` varchar(64) NOT NULL COMMENT '生产工单号快照',
  `order_step_id` bigint NOT NULL COMMENT '工单工序编号（erp 引用）',
  `step_no` int DEFAULT NULL COMMENT '工序序号快照',
  `step_code` varchar(64) DEFAULT NULL COMMENT '工序编码快照',
  `step_name` varchar(128) DEFAULT NULL COMMENT '工序名称快照',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号',
  `plan_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '计划数量',
  `plan_start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待排程 1已排程 2进行中 3已完成 4已取消',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mes_work_task_no` (`task_no`),
  UNIQUE KEY `uk_mes_work_task_order_step` (`order_step_id`),
  KEY `idx_mes_work_task_order` (`production_order_id`),
  KEY `idx_mes_work_task_center_status` (`work_center_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工序任务';

-- 3. 菜单：工序任务 + 工作日历（挂制造执行管理 /mes 下）
SET @mes_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mes_root_id := IFNULL(@mes_root_id, 930170);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931900, '工序任务', '', 2, 45, @mes_root_id, 'work-task', 'ep:timer',
       'mes/work-task/index', 'MesWorkTask',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'work-task' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931901, '工作日历', '', 2, 50, @mes_root_id, 'work-calendar', 'ep:calendar',
       'mes/work-calendar/index', 'MesWorkCalendar',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'work-calendar' AND `deleted` = b'0'
);

-- 4. 按钮权限点
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931910, '工序任务查询', 'mes:work-task:query', 3, 1, 931900, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931900 AND `permission` = 'mes:work-task:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931911, '工序任务更新', 'mes:work-task:update', 3, 2, 931900, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931900 AND `permission` = 'mes:work-task:update' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931920, '工作日历查询', 'mes:work-calendar:query', 3, 1, 931901, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931901 AND `permission` = 'mes:work-calendar:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931921, '工作日历创建', 'mes:work-calendar:create', 3, 2, 931901, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931901 AND `permission` = 'mes:work-calendar:create' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931922, '工作日历更新', 'mes:work-calendar:update', 3, 3, 931901, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931901 AND `permission` = 'mes:work-calendar:update' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931923, '工作日历删除', 'mes:work-calendar:delete', 3, 4, 931901, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931901 AND `permission` = 'mes:work-calendar:delete' AND `deleted` = b'0'
);

-- 5. 管理员角色授权
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 931900 AS menu_id
  UNION ALL SELECT 931901
  UNION ALL SELECT 931910
  UNION ALL SELECT 931911
  UNION ALL SELECT 931920
  UNION ALL SELECT 931921
  UNION ALL SELECT 931922
  UNION ALL SELECT 931923
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;

-- 验证
SELECT id, name, permission, type, sort, parent_id, path, component, component_name
FROM system_menu WHERE id IN (931900, 931901, 931910, 931911, 931920, 931921, 931922, 931923) AND deleted = 0;
