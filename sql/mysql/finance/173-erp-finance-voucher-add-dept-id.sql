-- 补充 erp_finance_voucher.dept_id 列
-- 背景：ErpFinanceVoucherDO 已声明 deptId 字段（用于部门数据权限），但表结构缺失该列，
--       导致 MyBatis-Plus 生成的 SELECT 引用不存在列，盘点/其它出库等审批关闭生成凭证时 SQL 语法错误。
-- 幂等：以 information_schema 判列是否存在，重复执行安全。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

DROP PROCEDURE IF EXISTS `erp_add_voucher_dept_id`;
DELIMITER //
CREATE PROCEDURE `erp_add_voucher_dept_id`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'erp_finance_voucher'
          AND COLUMN_NAME = 'dept_id'
    ) THEN
        ALTER TABLE `erp_finance_voucher`
            ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID（数据权限）' AFTER `ledger_id`;
    END IF;
END //
DELIMITER ;
CALL `erp_add_voucher_dept_id`();
DROP PROCEDURE IF EXISTS `erp_add_voucher_dept_id`;
