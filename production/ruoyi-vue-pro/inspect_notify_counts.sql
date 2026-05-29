SELECT 'users' AS section, id, username, nickname
FROM `ruoyi-vue-pro`.system_users
WHERE username IN ('so_gm', 'so_leader', 'so_apply');

SELECT 'notify_total' AS section, COUNT(*) AS cnt
FROM `ruoyi-vue-pro`.system_notify_message;

SELECT 'notify_target_users' AS section, COUNT(*) AS cnt
FROM `ruoyi-vue-pro`.system_notify_message
WHERE user_id IN (
  SELECT id FROM `ruoyi-vue-pro`.system_users WHERE username IN ('so_gm', 'so_leader', 'so_apply')
);

SELECT 'notify_target_bpm' AS section, COUNT(*) AS cnt
FROM `ruoyi-vue-pro`.system_notify_message
WHERE user_id IN (
  SELECT id FROM `ruoyi-vue-pro`.system_users WHERE username IN ('so_gm', 'so_leader', 'so_apply')
)
AND template_code = 'bpm_task_assigned';

SELECT 'recent_notify' AS section, id, user_id, template_code, read_status, create_time
FROM `ruoyi-vue-pro`.system_notify_message
ORDER BY id DESC
LIMIT 20;
