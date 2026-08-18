-- 审批场景增加通用审批接入配置 generic_config
-- 背景：为支持"新单据无代码接入 BPM 审批"，场景可配置通用接入参数（业务表、状态列、
--       状态映射、上下文字段映射），由通用审批桥（Generic 适配器）驱动，无需编写 Java 三件套。
-- 幂等：以 information_schema 判列是否存在，重复执行安全。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

DROP PROCEDURE IF EXISTS `bpm_add_scene_generic_config`;
DELIMITER //
CREATE PROCEDURE `bpm_add_scene_generic_config`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scene'
          AND COLUMN_NAME = 'generic_config'
    ) THEN
        ALTER TABLE `bpm_approval_scene`
            ADD COLUMN `generic_config` MEDIUMTEXT DEFAULT NULL
            COMMENT '通用审批接入配置 JSON（业务表/状态列/状态映射/上下文字段）' AFTER `remark`;
    END IF;
END //
DELIMITER ;
CALL `bpm_add_scene_generic_config`();
DROP PROCEDURE IF EXISTS `bpm_add_scene_generic_config`;
