SELECT id, username, nickname
FROM `weitee-erp`.system_users
WHERE username IN ('so_gm', 'so_leader', 'so_apply');

SHOW CREATE TABLE `weitee-erp`.system_notify_message;

SELECT
  m.id,
  m.user_id,
  u.username,
  m.user_type,
  m.template_id,
  m.template_code,
  m.template_nickname,
  m.template_type,
  m.template_content,
  m.read_status,
  m.read_time,
  m.create_time
FROM `weitee-erp`.system_notify_message m
LEFT JOIN `weitee-erp`.system_users u ON u.id = m.user_id
WHERE m.user_id IN (
  SELECT id FROM `weitee-erp`.system_users WHERE username IN ('so_gm', 'so_leader', 'so_apply')
)
ORDER BY m.id DESC
LIMIT 30;

SELECT
  u.username,
  COUNT(*) AS unread_count
FROM `weitee-erp`.system_notify_message m
JOIN `weitee-erp`.system_users u ON u.id = m.user_id
WHERE u.username IN ('so_gm', 'so_leader', 'so_apply')
  AND m.read_status = 0
GROUP BY u.username;
