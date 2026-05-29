SELECT COUNT(*) AS unread_count
FROM `ruoyi-vue-pro`.system_notify_message
WHERE deleted = b'0'
  AND read_status = b'0'
  AND user_id = 910203
  AND user_type = 2;
