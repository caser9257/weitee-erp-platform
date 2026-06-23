/*
 * Target: ERP 项目角色任务站内信模板
 * Schema: ruoyi-vue-pro
 * Date: 2026-04-20
 */

INSERT INTO `system_notify_template`
(`id`, `name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920582,
       '【ERP】项目角色任务待办提醒',
       'erp_project_role_task_assigned',
       '系统消息',
       '您有一条项目待办：{projectNo}（{projectName}）- {roleName}，任务：{taskSummary}，截止：{dueDate}，请前往 {detailUrl} 处理。',
       2,
       '[\"projectNo\",\"projectName\",\"roleName\",\"taskSummary\",\"dueDate\",\"detailUrl\"]',
       0,
       '项目 PC / MC 任务创建或刷新时发送站内信',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_notify_template`
    WHERE `code` = 'erp_project_role_task_assigned'
      AND `deleted` = b'0'
);
