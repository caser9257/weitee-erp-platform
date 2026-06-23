-- 文件版本管理表
CREATE TABLE IF NOT EXISTS `infra_file_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `version` INT NOT NULL COMMENT '版本号',
    `name` VARCHAR(200) NOT NULL COMMENT '文件名',
    `url` VARCHAR(500) NOT NULL COMMENT '文件URL',
    `size` BIGINT COMMENT '文件大小',
    `type` VARCHAR(100) COMMENT '文件类型',
    `description` VARCHAR(500) COMMENT '版本说明',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_file_id` (`file_id`),
    UNIQUE KEY `uk_file_version` (`file_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件版本表';
