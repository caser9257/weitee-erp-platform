SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SELECT
  root.`path` AS root_path,
  root.`name` AS root_name,
  menu.`id` AS menu_id,
  menu.`name` AS menu_name,
  menu.`path` AS menu_path,
  menu.`component` AS menu_component
FROM `system_menu` menu
JOIN `system_menu` root ON root.`id` = menu.`parent_id`
WHERE root.`deleted` = b'0'
  AND menu.`deleted` = b'0'
  AND root.`path` = '/project'
  AND menu.`path` = 'warning';

SET FOREIGN_KEY_CHECKS = 1;
