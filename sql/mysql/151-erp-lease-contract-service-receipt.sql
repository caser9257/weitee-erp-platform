-- 租赁合同、服务接收单、三单匹配表结构

SET NAMES utf8mb4;

-- ============================================================
-- 1. 租赁合同表
-- ============================================================
CREATE TABLE IF NOT EXISTS `erp_lease_contract` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no` VARCHAR(64) NOT NULL COMMENT '合同编号',
    `name` VARCHAR(200) NOT NULL COMMENT '合同名称',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称（冗余）',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `monthly_rent` DECIMAL(20,2) NOT NULL COMMENT '月租金',
    `payment_cycle` INT DEFAULT 1 COMMENT '付款周期（月）',
    `total_amount` DECIMAL(20,2) COMMENT '合同总金额',
    `cost_center_id` BIGINT COMMENT '成本中心ID（部门ID）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 10-生效 20-到期 30-终止',
    `remark` VARCHAR(500) COMMENT '备注',
    `file_url` VARCHAR(500) COMMENT '合同附件URL',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_no` (`no`),
    INDEX `idx_supplier_id` (`supplier_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁合同表';

-- ============================================================
-- 2. 服务接收单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `erp_service_receipt` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no` VARCHAR(64) NOT NULL COMMENT '单号',
    `lease_contract_id` BIGINT COMMENT '租赁合同ID',
    `lease_contract_no` VARCHAR(64) COMMENT '租赁合同编号（冗余）',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(200) COMMENT '供应商名称（冗余）',
    `receipt_date` DATE NOT NULL COMMENT '接收日期',
    `period` VARCHAR(7) NOT NULL COMMENT '归属期间（YYYY-MM）',
    `amount` DECIMAL(20,2) NOT NULL COMMENT '金额',
    `cost_center_id` BIGINT COMMENT '成本中心ID（部门ID）',
    `cost_center_name` VARCHAR(200) COMMENT '成本中心名称（冗余）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 10-已确认 20-已生成应付',
    `ap_statement_id` BIGINT COMMENT '生成的应付台账ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `file_url` VARCHAR(500) COMMENT '附件URL',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_no` (`no`),
    INDEX `idx_lease_contract_id` (`lease_contract_id`),
    INDEX `idx_supplier_id` (`supplier_id`),
    INDEX `idx_period` (`period`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务接收单表';

-- ============================================================
-- 3. 三单匹配记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `erp_three_way_match` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `lease_contract_id` BIGINT NOT NULL COMMENT '租赁合同ID',
    `lease_contract_no` VARCHAR(64) COMMENT '租赁合同编号',
    `service_receipt_id` BIGINT NOT NULL COMMENT '服务接收单ID',
    `service_receipt_no` VARCHAR(64) COMMENT '服务接收单编号',
    `invoice_no` VARCHAR(64) COMMENT '发票号',
    `invoice_amount` DECIMAL(20,2) COMMENT '发票金额',
    `contract_amount` DECIMAL(20,2) COMMENT '合同金额',
    `receipt_amount` DECIMAL(20,2) COMMENT '接收单金额',
    `match_result` TINYINT COMMENT '匹配结果：0-不匹配 1-完全匹配 2-部分匹配',
    `match_remark` VARCHAR(500) COMMENT '匹配说明',
    `ap_statement_id` BIGINT COMMENT '生成的应付台账ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待匹配 10-已匹配 20-已生成应付',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_lease_contract_id` (`lease_contract_id`),
    INDEX `idx_service_receipt_id` (`service_receipt_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三单匹配记录表';

-- ============================================================
-- 4. 菜单种子
-- ============================================================

-- 查询供应链管理菜单ID
SET @scm_menu_id = (SELECT id FROM system_menu WHERE name = '供应链管理' AND deleted = 0 LIMIT 1);

-- 租赁管理菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (160000, @scm_menu_id, '租赁管理', NULL, NULL, NULL, NULL, 'ep:document', 50, 0, 0, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 租赁合同菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (160001, 160000, '租赁合同', 'erp:lease-contract:query', 'lease-contract', 'erp/finance/lease-contract/index', 'ErpLeaseContract', 'ep:document', 10, 0, 1, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 服务接收单菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (160002, 160000, '服务接收单', 'erp:service-receipt:query', 'service-receipt', 'erp/finance/service-receipt/index', 'ErpServiceReceipt', 'ep:check', 20, 0, 1, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 三单匹配菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (160003, 160000, '三单匹配', 'erp:three-way-match:query', 'three-way-match', 'erp/finance/three-way-match/index', 'ErpThreeWayMatch', 'ep:connection', 30, 0, 1, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 授权给超级管理员
INSERT INTO system_role_menu (role_id, menu_id)
SELECT 1, id FROM system_menu WHERE id IN (160000, 160001, 160002, 160003)
AND NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = system_menu.id);
