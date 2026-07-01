/*
 Target: Normalize system and file-management menu boundary
 Schema: current connection database
 Date: 2026-07-01
 Rule:
 1. Restore the original top-level System Management entry when it is missing.
 2. Keep File Management as a temporary top-level page.
 3. Hide duplicate file-management entries that were accidentally attached to Finance.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(1,'系统管理','',1,10,0,'/system','ep:tools',NULL,NULL,
 0,b'1',b'1',b'1','admin',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `name` = '系统管理',
  `permission` = '',
  `type` = 1,
  `sort` = 10,
  `parent_id` = 0,
  `path` = '/system',
  `icon` = 'ep:tools',
  `component` = NULL,
  `component_name` = NULL,
  `status` = 0,
  `visible` = b'1',
  `keep_alive` = b'1',
  `always_show` = b'1',
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

SET @finance_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = 0
    AND `path` IN ('/finance', 'finance')
  ORDER BY `id`
  LIMIT 1
);

SET @file_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND (
      `id` = 150000
      OR (`name` = '文件管理' AND `path` IN ('/file', 'file'))
      OR `component` = 'infra/file/index'
    )
  ORDER BY
    CASE
      WHEN `id` = 150000 THEN 0
      WHEN `parent_id` = 0 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150000,'文件管理','',2,990,0,'/file','ep:folder-opened','infra/file/index','InfraFile',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @file_menu_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `id` = 150000
  );

SET @file_menu_id := COALESCE(
  @file_menu_id,
  (
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND `id` = 150000
      AND `name` = '文件管理'
    LIMIT 1
  )
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '文件管理','',2,990,0,'/file','ep:folder-opened','infra/file/index','InfraFile',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @file_menu_id IS NULL;

SET @file_menu_id := COALESCE(
  @file_menu_id,
  (
    SELECT `id`
    FROM `system_menu`
    WHERE `deleted` = b'0'
      AND `name` = '文件管理'
      AND `path` IN ('/file', 'file')
    ORDER BY `id`
    LIMIT 1
  )
);

UPDATE `system_menu`
SET `name` = '文件管理',
    `permission` = '',
    `type` = 2,
    `sort` = 990,
    `parent_id` = 0,
    `path` = '/file',
    `icon` = 'ep:folder-opened',
    `component` = 'infra/file/index',
    `component_name` = 'InfraFile',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW(),
    `deleted` = b'0'
WHERE `id` = @file_menu_id;

UPDATE `system_menu`
SET `parent_id` = @file_menu_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 3
  AND `permission` LIKE 'infra:file:%'
  AND @file_menu_id IS NOT NULL;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `id` <> COALESCE(@file_menu_id, -1)
  AND (
    `component` = 'infra/file/index'
    OR (
      @finance_root_id IS NOT NULL
      AND `parent_id` = @finance_root_id
      AND (
        `name` IN ('文件管理', '文件列表')
        OR `path` IN ('file', '/file')
      )
    )
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, 1, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
JOIN `system_menu` system_child
  ON system_child.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND system_child.`deleted` = b'0'
  AND system_child.`parent_id` = 1
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = 1
      AND exists_rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, @file_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
JOIN `system_menu` file_scope
  ON file_scope.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND file_scope.`deleted` = b'0'
  AND @file_menu_id IS NOT NULL
  AND (
    file_scope.`id` = @file_menu_id
    OR file_scope.`permission` LIKE 'infra:file:%'
    OR file_scope.`component` = 'infra/file/index'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = @file_menu_id
      AND exists_rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS `menu_id`
  UNION SELECT @file_menu_id
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`deleted` = b'0'
  );

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
