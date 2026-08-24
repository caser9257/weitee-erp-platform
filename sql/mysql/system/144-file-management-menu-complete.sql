-- 文件管理模块菜单补全
-- 父菜单 "文件管理" id=150000, parent_id=0, path='/file'
-- 依次为 5 个子页面 + 1 个文件版本页面 + 1 个文件配置页面创建菜单和权限点

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- 1. 文件标签管理
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150001,'文件标签管理','',2,10,150000,'file-tag','ep:collection-tag','infra/file-tag/index','InfraFileTag',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150001 AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150011,'文件标签查询','infra:file-tag:query',3,1,150001,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150011 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150012,'文件标签新增','infra:file-tag:create',3,2,150001,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150012 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150013,'文件标签修改','infra:file-tag:update',3,3,150001,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150013 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150014,'文件标签删除','infra:file-tag:delete',3,4,150001,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150014 AND `deleted`=b'0');

-- 2. 文件夹管理
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150002,'文件夹管理','',2,20,150000,'file-folder','ep:folder-opened','infra/file-folder/index','InfraFileFolder',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150002 AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150021,'文件夹查询','infra:file-folder:query',3,1,150002,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150021 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150022,'文件夹新增','infra:file-folder:create',3,2,150002,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150022 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150023,'文件夹修改','infra:file-folder:update',3,3,150002,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150023 AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150024,'文件夹删除','infra:file-folder:delete',3,4,150002,'','','','',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150024 AND `deleted`=b'0');

-- 3. 文件权限管理
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150003,'文件权限管理','',2,30,150000,'file-permission','ep:lock','infra/file-permission/index','InfraFilePermission',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150003 AND `deleted`=b'0');

-- 文件权限复用已有的 infra:file:query / infra:file:update 权限点

-- 4. 文件操作日志
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150004,'文件操作日志','',2,40,150000,'file-operation-log','ep:notebook','infra/file-operation-log/index','InfraFileOperationLog',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150004 AND `deleted`=b'0');

-- 操作日志复用 infra:file:query

-- 5. 文件访问统计
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 150005,'文件访问统计','',2,50,150000,'file-access-stats','ep:data-analysis','infra/file-access-stats/index','InfraFileAccessStats',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id`=150005 AND `deleted`=b'0');

-- 访问统计复用 infra:file:query

SET FOREIGN_KEY_CHECKS = 1;