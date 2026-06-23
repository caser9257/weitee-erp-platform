-- 租赁合同审批记录表
CREATE TABLE IF NOT EXISTS `erp_lease_contract_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `lease_contract_id` BIGINT NOT NULL COMMENT '租赁合同ID',
    `lease_contract_no` VARCHAR(64) COMMENT '租赁合同编号',
    `action` VARCHAR(20) NOT NULL COMMENT '操作：SUBMIT/APPROVE/REJECT',
    `status_before` INT COMMENT '操作前状态',
    `status_after` INT COMMENT '操作后状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(64) COMMENT '操作人名称',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_lease_contract_id` (`lease_contract_id`),
    INDEX `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁合同审批记录表';
