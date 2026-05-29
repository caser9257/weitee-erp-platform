USE `ruoyi-vue-pro`;
UPDATE `system_menu`
SET `visible` = b'1', `status` = 0, `updater` = '1', `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `parent_id` = 0
  AND `path` IN ('/project','/master-data','/sales','/pmo','/rd','/scm','/process','/mes','/qms','/finance','/hr','/system');

SELECT ROW_COUNT() AS affected_rows;
SELECT path, name, visible, status
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0 AND path IN ('/project','/master-data','/sales','/pmo','/rd','/scm','/process','/mes','/qms','/finance','/hr','/system')
ORDER BY sort, id;
