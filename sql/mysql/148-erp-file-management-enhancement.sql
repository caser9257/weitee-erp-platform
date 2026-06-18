-- 文件管理增强 - 文件夹、标签、关联、版本

SET NAMES utf8mb4;

-- ============================================================
-- 1. 文件夹表
-- ============================================================
CREATE TABLE IF NOT EXISTS `infra_file_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` VARCHAR(200) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父文件夹ID（0=根目录）',
    `path` VARCHAR(500) NOT NULL COMMENT '文件夹路径',
    `icon` VARCHAR(50) DEFAULT 'ep:folder' COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-停用',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_path` (`path`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

-- ============================================================
-- 2. 文件标签表
-- ============================================================
CREATE TABLE IF NOT EXISTS `infra_file_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `icon` VARCHAR(50) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件标签表';

-- ============================================================
-- 3. 文件-标签关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS `infra_file_tag_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_file_tag` (`file_id`, `tag_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件-标签关联表';

-- ============================================================
-- 4. 文件-业务关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS `infra_file_biz_rel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `biz_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务ID',
    `biz_no` VARCHAR(64) COMMENT '业务单号',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_file` (`file_id`, `biz_type`, `biz_id`),
    INDEX `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件-业务关联表';

-- ============================================================
-- 5. 文件表扩展字段（如字段已存在会报错，可忽略）
-- ============================================================
ALTER TABLE `infra_file`
    ADD COLUMN `folder_id` BIGINT NULL COMMENT '文件夹ID' AFTER `type`;

ALTER TABLE `infra_file`
    ADD COLUMN `version` INT DEFAULT 1 COMMENT '版本号' AFTER `folder_id`;

ALTER TABLE `infra_file`
    ADD COLUMN `parent_id` BIGINT NULL COMMENT '父版本ID' AFTER `version`;

ALTER TABLE `infra_file`
    ADD COLUMN `description` VARCHAR(500) NULL COMMENT '文件描述' AFTER `parent_id`;

-- ============================================================
-- 6. 初始数据
-- ============================================================

-- 默认文件夹
INSERT INTO `infra_file_folder` (`id`, `name`, `parent_id`, `path`, `icon`, `sort`, `creator`)
VALUES 
(1, '全部文件', 0, '/', 'ep:folder', 0, '1'),
(2, '合同文件', 1, '/合同文件', 'ep:document', 1, '1'),
(3, '费用附件', 1, '/费用附件', 'ep:money', 2, '1'),
(4, '产品资料', 1, '/产品资料', 'ep:goods', 3, '1'),
(5, '其他', 1, '/其他', 'ep:more', 4, '1')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 默认标签
INSERT INTO `infra_file_tag` (`id`, `name`, `color`, `icon`, `sort`, `creator`)
VALUES 
(1, '合同', '#409EFF', 'ep:document', 1, '1'),
(2, '发票', '#67C23A', 'ep:money', 2, '1'),
(3, '报告', '#9C27B0', 'ep:data-analysis', 3, '1'),
(4, '图纸', '#E6A23C', 'ep:picture', 4, '1'),
(5, '重要', '#F56C6C', 'ep:warning', 5, '1')
ON DUPLICATE KEY UPDATE name = VALUES(name);
