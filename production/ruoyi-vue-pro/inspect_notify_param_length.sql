SELECT
  LENGTH(
    JSON_OBJECT(
      'startUserNickname', '销售下单员',
      'taskName', 'General Manager Approval',
      'detailUrl', 'http://dashboard.yudao.iocoder.cn/bpm/process-instance/detail?id=2b6dee6b-34ac-11f1-87a7-50e9710710fd',
      'processInstanceName', 'ERP Sale Order Approval'
    )
  ) AS json_length;

SELECT COLUMN_NAME, COLUMN_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'weitee-erp'
  AND TABLE_NAME = 'system_notify_message'
  AND COLUMN_NAME = 'template_params';
