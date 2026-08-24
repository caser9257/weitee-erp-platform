/*
  研发 BOM 审批视图权限 + 审批人角色归一种子
  背景：
    1. BPM 审批详情页嵌入 RdBomApprovalPanel，调用 /erp/rd-bom/approval-view，
       审批人通常没有 erp:rd-bom:query，须独立只读权限点 erp:rd-bom:approval-view。
    2. L1 审批人由「岗位53」切换为「角色910005 hardware_component_engineer」，
       审批人身份体系统一归入 RBAC（用户→角色→权限），岗位退回纯人事属性。
       对应 bpmn 变更：erp_rd_bom_approval.bpmn task_approve_level1 candidateStrategy 22→10。
  内容：
    A. 权限按钮「研发BOM审批视图」（挂研发BOM菜单下）
    B. 角色 910005 器件工程师（固定 id，与 bpmn candidateParam=910005 对齐；惯例同 supply_chain_manager=910004）
    C. approval-view 授权：超级管理员 / supply_chain_manager / 器件工程师
    D. 器件工程师角色的 BPM 待办/已办/抄送菜单与任务操作权限（含递归补父级菜单链）
    E. 同名岗位（system_post.code='hardware_component_engineer'）存量用户幂等同步进角色
  幂等：全部按自然键判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库（如：mysql ... weitee-erp < 本文件）
*/
SET NAMES utf8mb4;

-- ===================== A. 权限按钮 =====================
SET @rd_bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/rd/rd-bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '研发BOM审批视图', 'erp:rd-bom:approval-view',
       3, 10, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:rd-bom:approval-view' AND `deleted`=b'0');

-- ===================== B. 器件工程师角色 =====================
INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 910005, '器件工程师', 'hardware_component_engineer', 55, 1, '', 0, 2,
       '研发BOM一级审批人（与同名岗位对应，权限与审批圈人均走此角色）', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role`
  WHERE (`id` = 910005 OR `code` = 'hardware_component_engineer') AND `deleted` = b'0'
);

-- 自愈：名称/状态对齐，复活误删
UPDATE `system_role`
SET `name` = '器件工程师', `code` = 'hardware_component_engineer', `sort` = 55,
    `status` = 0, `deleted` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `id` = 910005;

-- ===================== C. approval-view 授权 =====================
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT r.`role_id`, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS `role_id`
  UNION
  SELECT `id` AS `role_id` FROM `system_role` WHERE `code` = 'supply_chain_manager' AND `deleted` = b'0'
  UNION
  SELECT 910005 AS `role_id`
) r
JOIN `system_menu` m
  ON m.`permission` = 'erp:rd-bom:approval-view' AND m.`deleted` = b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = r.`role_id` AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
);

-- ===================== F. 补建审批链路缺失的标准权限点 =====================
-- 存量缺陷：bpm/process-instance/get-approval-detail 要求 bpm:process-instance:query、
-- 任务列表/待办接口要求 bpm:task:query，但 system_menu 中无此二权限行，
-- 非超管角色无法获得 → 打开审批详情页必 403。此处补建并随后授予器件工程师。
SET @approval_portal_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'bpm/approval/portal/index' AND `permission` = 'bpm:approval:query' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '流程实例查询', 'bpm:process-instance:query',
       3, 99, @approval_portal_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @approval_portal_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='bpm:process-instance:query' AND `deleted`=b'0');

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '流程任务查询', 'bpm:task:query',
       3, 98, @approval_portal_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @approval_portal_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='bpm:task:query' AND `deleted`=b'0');

-- ===================== D. BPM 菜单/接口权限授予器件工程师 =====================
-- 目标：审批门户三页（待我审批/我发起的/抄送我的）+ 审批操作与详情接口权限
-- 授予时用递归 CTE 把命中菜单的全部祖先目录一并授权，保证左侧导航可见
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
WITH RECURSIVE
hit_menus AS (
  SELECT `id`, `parent_id` FROM `system_menu`
  WHERE `deleted` = b'0'
    AND (
      (`component` = 'bpm/approval/portal/index' AND `permission` = 'bpm:approval:query')
      OR `permission` IN ('bpm:approval:query', 'bpm:task:update', 'bpm:task:query',
                          'bpm:process-instance:query')
    )
),
ancestor_tree AS (
    SELECT `id`, `parent_id` FROM `hit_menus`
    UNION ALL
    SELECT m.`id`, m.`parent_id`
    FROM `system_menu` m
    JOIN `ancestor_tree` a ON a.`parent_id` = m.`id`
    WHERE m.`deleted` = b'0'
  )
SELECT DISTINCT 910005, a.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `ancestor_tree` a
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 910005 AND rm.`menu_id` = a.`id` AND rm.`deleted` = b'0'
);

-- ===================== E. 同名岗位存量用户同步进角色 =====================
-- 先复活被逻辑删除的关联，再插入缺失关联（两段式避免判重口径漏洞）
UPDATE `system_user_role` ur
JOIN `system_user_post` up ON up.`user_id` = ur.`user_id`
JOIN `system_post` p ON p.`id` = up.`post_id` AND p.`deleted` = b'0'
SET ur.`deleted` = b'0', ur.`updater` = '1', ur.`update_time` = NOW()
WHERE p.`code` = 'hardware_component_engineer'
  AND ur.`role_id` = 910005 AND ur.`deleted` = b'1';

INSERT INTO `system_user_role` (`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT up.`user_id`, 910005, '1', NOW(), '1', NOW(), b'0'
FROM `system_user_post` up
JOIN `system_post` p ON p.`id` = up.`post_id` AND p.`deleted` = b'0'
WHERE p.`code` = 'hardware_component_engineer'
  AND NOT EXISTS (
    SELECT 1 FROM `system_user_role` ur
    WHERE ur.`user_id` = up.`user_id` AND ur.`role_id` = 910005
  );

-- 同步进来的用户若曾被禁用该角色以外的异常态，无需处理；角色启用状态见 B 段自愈
