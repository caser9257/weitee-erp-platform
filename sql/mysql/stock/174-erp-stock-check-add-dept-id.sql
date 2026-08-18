-- 为盘点单补充部门维度 dept_id
-- 背景：盘点单凭证（biz_type=60）在生成时缺少部门归属，导致部门数据权限（FinancePermissionScope）
--       无法覆盖盘点凭证。为盘点单增加 dept_id（默认取创建人所属部门），并在凭证生成时回填。
-- 幂等：以 information_schema 判列是否存在，重复执行安全。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

DROP PROCEDURE IF EXISTS `erp_add_stock_check_dept_id`;
DELIMITER //
CREATE PROCEDURE `erp_add_stock_check_dept_id`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'erp_stock_check'
          AND COLUMN_NAME = 'dept_id'
    ) THEN
        ALTER TABLE `erp_stock_check`
            ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID（数据权限）' AFTER `no`;
        ALTER TABLE `erp_stock_check`
            ADD KEY `idx_erp_stock_check_dept` (`dept_id`);
    END IF;
END //
DELIMITER ;
CALL `erp_add_stock_check_dept_id`();
DROP PROCEDURE IF EXISTS `erp_add_stock_check_dept_id`;
