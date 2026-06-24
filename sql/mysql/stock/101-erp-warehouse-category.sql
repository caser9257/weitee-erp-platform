/*
  仓库分类管理基础表与菜单
  1. 新增仓库分类表
  2. 为 erp_warehouse 增加 category_id
  3. 注册仓库分类菜单
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_warehouse_category_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_warehouse'
        AND COLUMN_NAME = 'category_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_warehouse` ADD COLUMN `category_id` bigint NULL DEFAULT NULL COMMENT ''仓库分类编号'' AFTER `name`'
  )
);
PREPARE stmt FROM @add_warehouse_category_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `erp_warehouse_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类编号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_parent_name` (`parent_id`, `name`) USING BTREE,
  UNIQUE KEY `uk_parent_code` (`parent_id`, `code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 仓库分类表';

SET @scm_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/scm'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @warehouse_category_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/stock/warehouse-category/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@warehouse_category_menu_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t)),
       '仓库分类管理', '', 2, 15,
       @scm_root_id, 'warehouse-category', 'ep:folder-opened', 'erp/stock/warehouse-category/index', 'ErpWarehouseCategoryPage',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @scm_root_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @warehouse_category_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/stock/warehouse-category/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @warehouse_category_query_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `permission` = 'erp:warehouse-category:query'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@warehouse_category_query_menu_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t)),
       '仓库分类查询', 'erp:warehouse-category:query', 3, 1,
       @warehouse_category_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @warehouse_category_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

DROP TEMPORARY TABLE IF EXISTS `tmp_warehouse_category_role_ids`;
CREATE TEMPORARY TABLE `tmp_warehouse_category_role_ids` (
  `role_id` BIGINT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO `tmp_warehouse_category_role_ids` (`role_id`)
SELECT DISTINCT rm.`role_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`deleted` = b'0'
  AND (
    sm.`id` = @scm_root_id
    OR sm.`parent_id` = @scm_root_id
  );

INSERT IGNORE INTO `tmp_warehouse_category_role_ids` (`role_id`) VALUES (1);

SET @supply_chain_role_id := (
  SELECT `id`
  FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT IGNORE INTO `tmp_warehouse_category_role_ids` (`role_id`)
SELECT @supply_chain_role_id
WHERE @supply_chain_role_id IS NOT NULL;

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT role_ids.`role_id`, @warehouse_category_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `tmp_warehouse_category_role_ids` role_ids
WHERE @warehouse_category_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @warehouse_category_menu_id

      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT role_ids.`role_id`, @warehouse_category_query_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `tmp_warehouse_category_role_ids` role_ids
WHERE @warehouse_category_query_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @warehouse_category_query_menu_id

      AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_warehouse_category_role_ids`;

INSERT INTO `erp_warehouse_category`
(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(99211, 0, '原料仓', 'RAW', 1, 0, 'tester', NOW(), 'tester', NOW(), b'0'),
(99212, 0, '成品仓', 'FINISHED', 2, 0, 'tester', NOW(), 'tester', NOW(), b'0'),
(99213, 0, '在途仓', 'IN_TRANSIT', 3, 0, 'tester', NOW(), 'tester', NOW(), b'0'),
(99214, 0, '测试仓库', 'TEST', 4, 0, 'tester', NOW(), 'tester', NOW(), b'0')
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`code` = VALUES(`code`),
`sort` = VALUES(`sort`),
`status` = VALUES(`status`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`),

UPDATE `erp_warehouse`
SET `category_id` = CASE `id`
  WHEN 99201 THEN 99211
  WHEN 99202 THEN 99212
  WHEN 99203 THEN 99213
  ELSE 99214
END
WHERE `id` IN (99201, 99202, 99203);

UPDATE `erp_warehouse`
SET `category_id` = CASE
  WHEN `name` = '原料仓' THEN 99211
  WHEN `name` = '成品仓' THEN 99212
  WHEN `name` = '在途仓' THEN 99213
  WHEN `name` LIKE '测试%' THEN 99214
  ELSE `category_id`
END
WHERE `category_id` IS NULL
  AND `name` IN ('原料仓', '成品仓', '在途仓', '测试', '测试仓库');

SET FOREIGN_KEY_CHECKS = 1;
