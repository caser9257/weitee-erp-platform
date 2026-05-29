-- 项目双账成本结果表
CREATE TABLE `erp_finance_dual_project_cost_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `project_no` VARCHAR(64) DEFAULT NULL COMMENT '项目编号',
    `project_name` VARCHAR(255) DEFAULT NULL COMMENT '项目名称',
    `period` VARCHAR(20) NOT NULL COMMENT '期间（YYYY-MM）',
    `cost_type` TINYINT NOT NULL COMMENT '成本类别（10-材料, 20-人工, 30-折旧, 40-电费, 50-其他）',
    `external_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '外部账金额',
    `internal_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '内部账金额',
    `diff_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '差异金额（内部-外部）',
    `source_count` INT DEFAULT 0 COMMENT '来源单据数',
    `status` TINYINT DEFAULT 0 COMMENT '状态（0-正常, 10-重建中）',
    `version_no` INT DEFAULT 1 COMMENT '版本号',
    `last_rebuild_time` DATETIME DEFAULT NULL COMMENT '最后重建时间',
    `last_rebuild_by` BIGINT DEFAULT NULL COMMENT '最后重建人',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_period_cost_type` (`project_id`, `period`, `cost_type`, `deleted`),
    KEY `idx_period` (`period`),
    KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目双账成本结果表';

-- 项目双账成本明细表
CREATE TABLE `erp_finance_dual_project_cost_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `result_id` BIGINT NOT NULL COMMENT '结果ID',
    `source_type` TINYINT NOT NULL COMMENT '来源类型（10-费用报销, 20-研发费用, 30-租赁折旧, 40-其他）',
    `source_biz_type` INT DEFAULT NULL COMMENT '来源业务类型',
    `source_biz_id` BIGINT DEFAULT NULL COMMENT '来源业务ID',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单号',
    `cost_type` TINYINT NOT NULL COMMENT '成本类别',
    `external_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '外部账金额',
    `internal_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '内部账金额',
    `diff_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '差异金额',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `period` VARCHAR(20) NOT NULL COMMENT '期间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_result_id` (`result_id`),
    KEY `idx_project_period` (`project_id`, `period`),
    KEY `idx_source` (`source_biz_type`, `source_biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目双账成本明细表';

-- 项目双账成本重跑日志表
CREATE TABLE `erp_finance_dual_project_cost_rebuild_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `period` VARCHAR(20) NOT NULL COMMENT '期间',
    `status` TINYINT NOT NULL COMMENT '状态（10-成功, 20-失败, 30-进行中）',
    `trigger_type` TINYINT DEFAULT 10 COMMENT '触发类型（10-手动, 20-定时）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `started_at` DATETIME DEFAULT NULL COMMENT '开始时间',
    `finished_at` DATETIME DEFAULT NULL COMMENT '结束时间',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    `affected_count` INT DEFAULT 0 COMMENT '影响记录数',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_project_period` (`project_id`, `period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目双账成本重跑日志表';
