-- 138-supply-chain-bpm-integration.sql
-- 供应链模块 BPM 审批接入：给三张表加 process_instance_id 字段

-- 采购退货
ALTER TABLE erp_purchase_return
ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID';

-- 其它入库
ALTER TABLE erp_stock_in
ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID';

-- 其它出库
ALTER TABLE erp_stock_out
ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT 'BPM 流程实例 ID';