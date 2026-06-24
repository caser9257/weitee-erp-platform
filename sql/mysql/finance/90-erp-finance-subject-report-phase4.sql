-- =====================================================
-- ERP finance subject / report phase4
-- 1. add finance subject master table
-- 2. add finance statement report item table
-- 3. add report item subject mapping table
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `parent_id` BIGINT NULL COMMENT '上级科目编号',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `subject_name` VARCHAR(128) NOT NULL COMMENT '科目名称',
    `subject_type` INT NOT NULL COMMENT '科目类型',
    `balance_direction` INT NOT NULL COMMENT '余额方向',
    `leaf` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否末级科目',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_subject_code` (`ledger_id`, `subject_code`, `deleted`),
    KEY `idx_finance_subject_page` (`ledger_id`, `subject_type`, `status`, `deleted`, `sort`),
    KEY `idx_finance_subject_parent` (`ledger_id`, `parent_id`, `deleted`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务科目';

CREATE TABLE IF NOT EXISTS `erp_finance_report_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `report_type` INT NOT NULL COMMENT '报表类型',
    `item_category` INT NOT NULL COMMENT '项目分类',
    `item_code` VARCHAR(64) NOT NULL COMMENT '项目编码',
    `item_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_report_item_code` (`ledger_id`, `report_type`, `item_code`, `deleted`),
    KEY `idx_finance_report_item_page` (`ledger_id`, `report_type`, `item_category`, `status`, `deleted`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务报表项目';

CREATE TABLE IF NOT EXISTS `erp_finance_report_item_subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `item_id` BIGINT NOT NULL COMMENT '报表项目编号',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `amount_rule` INT NOT NULL COMMENT '取数规则',
    `amount_sign` INT NOT NULL DEFAULT 1 COMMENT '金额符号，1 为加，-1 为减',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_finance_report_item_subject_item` (`item_id`, `deleted`),
    KEY `idx_finance_report_item_subject_subject` (`subject_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务报表项目取数科目';
