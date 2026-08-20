-- MES-B 第一阶段：工序任务派工
-- 派工记录独立留痕，当前记录 active_flag=1，历史记录 active_flag=NULL。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `mes_work_task_dispatch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '派工记录编号',
  `task_id` bigint NOT NULL COMMENT 'MES 工序任务编号',
  `task_no` varchar(64) NOT NULL COMMENT '任务单号快照',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `production_order_no` varchar(64) NOT NULL COMMENT '生产工单号快照',
  `order_step_id` bigint NOT NULL COMMENT '工单工序编号',
  `step_no` int DEFAULT NULL COMMENT '工序序号快照',
  `step_code` varchar(64) DEFAULT NULL COMMENT '工序编码快照',
  `step_name` varchar(128) DEFAULT NULL COMMENT '工序名称快照',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号快照',
  `device_id` bigint DEFAULT NULL COMMENT '设备编号',
  `team_id` bigint DEFAULT NULL COMMENT '班组编号，首期复用系统部门编号',
  `worker_user_id` bigint DEFAULT NULL COMMENT '执行人员编号',
  `dispatch_status` tinyint NOT NULL DEFAULT 1 COMMENT '派工状态：1已派工 2已撤销',
  `active_flag` tinyint DEFAULT NULL COMMENT '当前记录标记：1当前，NULL历史',
  `dispatch_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '派工时间',
  `revoke_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '派工备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_mes_task_dispatch_task` (`task_id`, `deleted`),
  KEY `idx_mes_task_dispatch_active` (`active_flag`, `dispatch_status`),
  KEY `idx_mes_task_dispatch_worker` (`worker_user_id`, `dispatch_status`),
  UNIQUE KEY `uk_mes_task_dispatch_active_task` (`task_id`, `active_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES 工序任务派工记录';

-- 派工管理菜单及权限点，归属制造执行管理（/mes）。
SET @mes_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mes_root_id := IFNULL(@mes_root_id, 930170);

SET @dispatch_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'task-dispatch' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @dispatch_menu_id := IFNULL(@dispatch_menu_id, (
  SELECT CASE WHEN EXISTS (
    SELECT 1 FROM `system_menu` WHERE `id` = 931904
  ) THEN (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `system_menu`) ELSE 931904 END
));

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @dispatch_menu_id, '派工管理', '', 2, 47, @mes_root_id, 'task-dispatch', 'ep:user-filled',
       'mes/task-dispatch/index', 'MesTaskDispatch',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'task-dispatch' AND `deleted` = b'0'
);

SET @dispatch_query_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @dispatch_menu_id AND `permission` = 'mes:task-dispatch:query' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @dispatch_query_id := IFNULL(@dispatch_query_id, (
  SELECT CASE WHEN EXISTS (
    SELECT 1 FROM `system_menu` WHERE `id` = 931940
  ) THEN (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `system_menu`) ELSE 931940 END
));

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @dispatch_query_id, '派工查询', 'mes:task-dispatch:query', 3, 1, @dispatch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @dispatch_menu_id AND `permission` = 'mes:task-dispatch:query' AND `deleted` = b'0'
);

SET @dispatch_update_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @dispatch_menu_id AND `permission` = 'mes:task-dispatch:update' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @dispatch_update_id := IFNULL(@dispatch_update_id, (
  SELECT CASE WHEN EXISTS (
    SELECT 1 FROM `system_menu` WHERE `id` = 931941
  ) THEN (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `system_menu`) ELSE 931941 END
));

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @dispatch_update_id, '派工更新', 'mes:task-dispatch:update', 3, 2, @dispatch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @dispatch_menu_id AND `permission` = 'mes:task-dispatch:update' AND `deleted` = b'0'
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @dispatch_menu_id AS menu_id
  UNION ALL SELECT @dispatch_query_id
  UNION ALL SELECT @dispatch_update_id
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;

SELECT id, name, permission, type, parent_id, path, component
FROM system_menu
WHERE id IN (@dispatch_menu_id, @dispatch_query_id, @dispatch_update_id) AND deleted = 0;
