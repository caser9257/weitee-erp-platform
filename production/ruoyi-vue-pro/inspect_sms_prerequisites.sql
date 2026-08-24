SELECT 'users' AS section, id, username, nickname, mobile, status
FROM `weitee-erp`.system_users
WHERE username IN ('so_gm', 'so_leader', 'so_apply');

SELECT 'sms_template' AS section, id, code, name, status, channel_id, channel_code, api_template_id
FROM `weitee-erp`.system_sms_template
WHERE code = 'bpm_task_assigned';

SELECT 'sms_channel' AS section, id, code, signature, status
FROM `weitee-erp`.system_sms_channel;

SELECT 'recent_sms_log' AS section, id, mobile, user_id, template_code, send_status, send_time, create_time, template_params
FROM `weitee-erp`.system_sms_log
ORDER BY id DESC
LIMIT 20;
