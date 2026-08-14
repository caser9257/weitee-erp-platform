SELECT COUNT(*) AS unread_count
FROM `weitee-erp`.system_notify_message
WHERE deleted = b'0' AND read_status = b'0' AND user_id = 910203 AND user_type = 2;

SELECT id, user_id, user_type, template_code, template_content, read_status, deleted, create_time
FROM `weitee-erp`.system_notify_message
WHERE user_id = 910203
ORDER BY id DESC;

SELECT id, user_id, user_type, template_code, template_content, read_status, deleted, create_time
FROM `weitee-erp`.system_notify_message
WHERE user_id = 910202
ORDER BY id DESC;
