SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SELECT
  menu.`id`,
  menu.`name`,
  menu.`path`,
  menu.`component`,
  menu.`parent_id`,
  menu.`type`,
  menu.`status`,
  menu.`visible`,
  menu.`sort`,
  root.`path` AS root_path,
  root.`name` AS root_name
FROM `system_menu` menu
LEFT JOIN `system_menu` root ON root.`id` = menu.`parent_id`
WHERE menu.`id` = 930107
  AND menu.`deleted` = b'0';

SET FOREIGN_KEY_CHECKS = 1;
