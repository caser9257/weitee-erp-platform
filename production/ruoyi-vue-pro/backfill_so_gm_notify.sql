USE `weitee-erp`;

INSERT INTO `system_notify_message`
(`user_id`, `user_type`, `template_id`, `template_code`, `template_nickname`, `template_content`,
 `template_type`, `template_params`, `read_status`, `read_time`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT
  910203,
  2,
  t.id,
  t.code,
  t.nickname,
  '您收到了一条新的待办任务：ERP Sale Order Approval-General Manager Approval，申请人：销售下单员，处理链接：http://dashboard.yudao.iocoder.cn/bpm/process-instance/detail?id=2b6dee6b-34ac-11f1-87a7-50e9710710fd',
  t.type,
  '{"startUserNickname":"销售下单员","taskName":"General Manager Approval","detailUrl":"http://dashboard.yudao.iocoder.cn/bpm/process-instance/detail?id=2b6dee6b-34ac-11f1-87a7-50e9710710fd","processInstanceName":"ERP Sale Order Approval"}',
  b'0',
  NULL,
  '910202',
  NOW(),
  '910202',
  NOW(),
  b'0',
  1
FROM `system_notify_template` t
WHERE t.code = 'bpm_task_assigned'
  AND t.deleted = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM `system_notify_message` m
    WHERE m.user_id = 910203
      AND m.user_type = 2
      AND m.template_code = 'bpm_task_assigned'
      AND m.deleted = b'0'
      AND m.template_content = '您收到了一条新的待办任务：ERP Sale Order Approval-General Manager Approval，申请人：销售下单员，处理链接：http://dashboard.yudao.iocoder.cn/bpm/process-instance/detail?id=2b6dee6b-34ac-11f1-87a7-50e9710710fd'
  );

SELECT id, user_id, user_type, template_code, read_status, create_time
FROM `system_notify_message`
WHERE user_id = 910203
  AND user_type = 2
  AND template_code = 'bpm_task_assigned'
ORDER BY id DESC;
