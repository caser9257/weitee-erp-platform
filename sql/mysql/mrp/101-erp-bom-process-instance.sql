/*
   制造 BOM 停用审批在途实例跟踪字段
   背景：制造 BOM 停用申请在审批通过前主表 status 仍为 ENABLE（停用动作由 Handler 在通过后落库），
   需要一个在途标记让前端展示「停用审批中」并暴露「撤回」入口。
   内容：erp_bom 增加 process_instance_id 列（在途非空，通过后落 DISABLE 并清空）。
   幂等：列已存在则跳过。
 */
SET NAMES utf8mb4;

-- 在途流程实例（审批中非空；通过后清空）
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom' AND COLUMN_NAME = 'process_instance_id');
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE erp_bom ADD COLUMN process_instance_id varchar(64) DEFAULT NULL COMMENT ''停用审批在途流程实例ID（通过后清空）'' AFTER remark',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
