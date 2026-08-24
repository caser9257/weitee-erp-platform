/*
 Target: ERP dual-layer BOM
 Schema: ruoyi-vue-pro
 Date: 2026-04-13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @erp_root_menu_id := 2563;

SET @add_source_rd_bom_id_sql := (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_bom'
        AND COLUMN_NAME = 'source_rd_bom_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_bom` ADD COLUMN `source_rd_bom_id` bigint DEFAULT NULL COMMENT ''来源研发BOM编号'' AFTER `status`'
  )
);
PREPARE stmt_add_source_rd_bom_id FROM @add_source_rd_bom_id_sql;
EXECUTE stmt_add_source_rd_bom_id;
DEALLOCATE PREPARE stmt_add_source_rd_bom_id;

CREATE TABLE IF NOT EXISTS `erp_rd_bom` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `bom_code` varchar(64) NOT NULL COMMENT 'BOM编码',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `version` varchar(32) DEFAULT NULL COMMENT '版本',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布',
  `published_bom_id` bigint DEFAULT NULL COMMENT '最近发布的制造BOM编号',
  `last_published_time` datetime DEFAULT NULL COMMENT '最近发布时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 研发BOM';

CREATE TABLE IF NOT EXISTS `erp_rd_bom_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `bom_id` bigint NOT NULL COMMENT '研发BOM编号',
  `material_id` bigint NOT NULL COMMENT '物料编号',
  `material_type` tinyint DEFAULT NULL COMMENT '物料类型',
  `unit_id` bigint DEFAULT NULL COMMENT '单位编号',
  `usage_qty` decimal(24,6) NOT NULL COMMENT '用量',
  `loss_rate` decimal(24,6) DEFAULT NULL COMMENT '损耗率',
  `lead_time_day` int DEFAULT NULL COMMENT '提前期(天)',
  `sort` int DEFAULT NULL COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_erp_rd_bom_item_bom_id` (`bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 研发BOM子件';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发管理', '', 1, 55, @erp_root_menu_id,
       'rd', 'ep:cpu', '', '', 0, b'1', b'1', b'1',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @erp_root_menu_id AND `path` = 'rd' AND `deleted` = b'0'
);

SET @rd_parent_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_menu_id AND `path` = 'rd' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP管理', '', 1, 60, @erp_root_menu_id,
       'mrp', 'ep:data-analysis', '', '', 0, b'1', b'1', b'1',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @erp_root_menu_id AND `path` = 'mrp' AND `deleted` = b'0'
);

SET @mrp_parent_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @erp_root_menu_id AND `path` = 'mrp' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @manufacture_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '制造BOM',
    `parent_id` = @mrp_parent_id,
    `type` = 2,
    `sort` = 15,
    `path` = 'bom',
    `icon` = 'ep:operation',
    `component` = 'erp/mrp/bom/index',
    `component_name` = 'ErpManufactureBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @manufacture_bom_menu_id;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM', '', 2, 15,
       @mrp_parent_id, 'bom', 'ep:operation', 'erp/mrp/bom/index', 'ErpManufactureBom',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NULL;

SET @manufacture_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '制造BOM查询',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM创建',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM编辑',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM删除',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:delete'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM生效停用',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 5,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update-status'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM查询', 'erp:bom:query',
       3, 1, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM创建', 'erp:bom:create',
       3, 2, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM编辑', 'erp:bom:update',
       3, 3, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM删除', 'erp:bom:delete',
       3, 4, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:delete' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM生效停用', 'erp:bom:update-status',
       3, 5, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:update-status' AND `deleted` = b'0'
  );

SET @rd_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/rd/rd-bom/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM', '', 2, 10,
       @rd_parent_id, 'rd-bom', 'ep:document', 'erp/rd/rd-bom/index', 'ErpRdBom',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NULL;

UPDATE `system_menu`
SET `name` = '研发BOM',
    `parent_id` = @rd_parent_id,
    `type` = 2,
    `sort` = 10,
    `path` = 'rd-bom',
    `icon` = 'ep:document',
    `component` = 'erp/rd/rd-bom/index',
    `component_name` = 'ErpRdBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` IN ('erp/rd/rd-bom/index')
  AND `deleted` = b'0';

SET @rd_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/rd/rd-bom/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '研发BOM查询',
    `parent_id` = @rd_bom_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:rd-bom:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '研发BOM创建',
    `parent_id` = @rd_bom_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:rd-bom:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '研发BOM编辑',
    `parent_id` = @rd_bom_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:rd-bom:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '研发BOM删除',
    `parent_id` = @rd_bom_menu_id,
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:rd-bom:delete'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '研发BOM发布',
    `parent_id` = @rd_bom_menu_id,
    `sort` = 5,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:rd-bom:publish'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM查询', 'erp:rd-bom:query',
       3, 1, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:rd-bom:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM创建', 'erp:rd-bom:create',
       3, 2, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:rd-bom:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM编辑', 'erp:rd-bom:update',
       3, 3, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:rd-bom:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM删除', 'erp:rd-bom:delete',
       3, 4, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:rd-bom:delete' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发BOM发布', 'erp:rd-bom:publish',
       3, 5, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:rd-bom:publish' AND `deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`id` IN (@rd_parent_id, @mrp_parent_id, @manufacture_bom_menu_id, @rd_bom_menu_id)
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN (
  'erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete', 'erp:bom:update-status',
  'erp:rd-bom:query', 'erp:rd-bom:create', 'erp:rd-bom:update', 'erp:rd-bom:delete', 'erp:rd-bom:publish'
)
  AND m.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
