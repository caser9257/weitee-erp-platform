CREATE TABLE IF NOT EXISTS `erp_finance_role_dept` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `role_id` BIGINT NOT NULL COMMENT '角色编号',
    `dept_id` BIGINT NOT NULL COMMENT '部门编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_role_dept` (`role_id`, `dept_id`, `deleted`),
    KEY `idx_finance_role_dept_role` (`role_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务角色部门权限';

CREATE TABLE IF NOT EXISTS `erp_finance_role_subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `role_id` BIGINT NOT NULL COMMENT '角色编号',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_role_subject` (`role_id`, `ledger_id`, `subject_code`, `deleted`),
    KEY `idx_finance_role_subject_role` (`role_id`, `ledger_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务角色科目权限';

DROP PROCEDURE IF EXISTS `erp_finance_add_voucher_dept_id`;

DELIMITER //
CREATE PROCEDURE `erp_finance_add_voucher_dept_id`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'erp_finance_voucher'
          AND COLUMN_NAME = 'dept_id'
    ) THEN
        ALTER TABLE `erp_finance_voucher`
            ADD COLUMN `dept_id` BIGINT NULL COMMENT '部门快照编号' AFTER `ledger_id`;
    END IF;
END//
DELIMITER ;

CALL `erp_finance_add_voucher_dept_id`();
DROP PROCEDURE `erp_finance_add_voucher_dept_id`;
