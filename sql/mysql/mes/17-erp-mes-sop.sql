-- MES-A：SOP 管理（文档 + 工序绑定 + OCR 导入记录）
-- 归属：制造执行管理（/mes）下新增"SOP 管理"菜单

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. SOP 文档
CREATE TABLE IF NOT EXISTS `mes_sop_document` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SOP 编号',
  `sop_no` varchar(64) NOT NULL COMMENT 'SOP 编码',
  `title` varchar(200) NOT NULL COMMENT 'SOP 标题',
  `version` varchar(32) NOT NULL DEFAULT 'V1.0' COMMENT '版本',
  `content` text COMMENT '步骤内容（结构化 JSON：标题/步骤列表）',
  `attachment_url` varchar(500) DEFAULT NULL COMMENT '附件地址',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已发布 2停用',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mes_sop_document_no` (`sop_no`),
  KEY `idx_mes_sop_document_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES SOP 文档';

-- 2. SOP-工序绑定
CREATE TABLE IF NOT EXISTS `mes_sop_step_binding` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绑定编号',
  `sop_id` bigint NOT NULL COMMENT 'SOP 编号',
  `route_step_id` bigint NOT NULL COMMENT '工艺路线工序编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mes_sop_step_binding` (`sop_id`, `route_step_id`),
  KEY `idx_mes_sop_step_binding_step` (`route_step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES SOP 工序绑定';

-- 3. SOP OCR 导入记录
CREATE TABLE IF NOT EXISTS `mes_sop_import_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '导入记录编号',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `file_url` varchar(500) DEFAULT NULL COMMENT '图片地址',
  `ocr_text` text COMMENT 'OCR 识别文本（草稿）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待校对 1已确认 2已转正式 3识别失败',
  `sop_id` bigint DEFAULT NULL COMMENT '转正式后的 SOP 编号',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_mes_sop_import_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MES SOP OCR 导入记录';

-- 4. 菜单：SOP 管理（挂制造执行管理 /mes 下）
SET @mes_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mes_root_id := IFNULL(@mes_root_id, 930170);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931960, 'SOP 管理', '', 2, 65, @mes_root_id, 'sop', 'ep:reading',
       'mes/sop/index', 'MesSop',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'sop' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931970, 'SOP 查询', 'mes:sop:query', 3, 1, 931960, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931960 AND `permission` = 'mes:sop:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931971, 'SOP 创建', 'mes:sop:create', 3, 2, 931960, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931960 AND `permission` = 'mes:sop:create' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931972, 'SOP 更新', 'mes:sop:update', 3, 3, 931960, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931960 AND `permission` = 'mes:sop:update' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931973, 'SOP 删除', 'mes:sop:delete', 3, 4, 931960, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931960 AND `permission` = 'mes:sop:delete' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931974, 'SOP OCR 导入', 'mes:sop:import', 3, 5, 931960, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931960 AND `permission` = 'mes:sop:import' AND `deleted` = b'0'
);

-- 5. 管理员授权
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 931960 AS menu_id
  UNION ALL SELECT 931970
  UNION ALL SELECT 931971
  UNION ALL SELECT 931972
  UNION ALL SELECT 931973
  UNION ALL SELECT 931974
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;

SELECT id, name, permission, type, sort, parent_id, path FROM system_menu
WHERE id IN (931960, 931970, 931971, 931972, 931973, 931974) AND deleted = 0;
