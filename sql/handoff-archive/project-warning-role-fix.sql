SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SET @tenant_id = 1;

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, 930107, '1', NOW(), '1', NOW(), b'0', @tenant_id
WHERE EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `id` = 930107
    AND `deleted` = b'0'
)
AND NOT EXISTS (
  SELECT 1
  FROM `system_role_menu`
  WHERE `role_id` = 1
    AND `menu_id` = 930107
    AND `tenant_id` = @tenant_id
    AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
