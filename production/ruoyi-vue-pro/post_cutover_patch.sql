USE `ruoyi-vue-pro`;

UPDATE `system_notify_template`
SET
  `name` = '【工作流】任务被分配',
  `nickname` = '审批助手',
  `content` = '您收到了一条新的待办任务：{processInstanceName}-{taskName}，申请人：{startUserNickname}，处理链接：{detailUrl}',
  `type` = 2,
  `params` = '["processInstanceName","taskName","startUserNickname","detailUrl"]',
  `status` = 0,
  `remark` = '审批任务到达负责人时发送站内信',
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0'
WHERE `code` = 'bpm_task_assigned';

INSERT INTO `system_notify_template`
(`name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  '【工作流】任务被分配',
  'bpm_task_assigned',
  '审批助手',
  '您收到了一条新的待办任务：{processInstanceName}-{taskName}，申请人：{startUserNickname}，处理链接：{detailUrl}',
  2,
  '["processInstanceName","taskName","startUserNickname","detailUrl"]',
  0,
  '审批任务到达负责人时发送站内信',
  '1',
  NOW(),
  '1',
  NOW(),
  b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_notify_template`
  WHERE `code` = 'bpm_task_assigned' AND `deleted` = b'0'
);
