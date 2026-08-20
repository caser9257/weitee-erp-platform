-- 新物料审核：新增审核状态与流程实例编号
ALTER TABLE `erp_product`
    ADD COLUMN `audit_status` int NOT NULL DEFAULT 0 COMMENT '审核状态：0草稿 10审批中 20已审批 30已驳回 60处理失败' AFTER `status`,
    ADD COLUMN `process_instance_id` varchar(64) NULL DEFAULT NULL COMMENT 'BPM 流程实例编号' AFTER `audit_status`,
    ADD INDEX `idx_audit_status` (`audit_status`),
    ADD INDEX `idx_process_instance_id` (`process_instance_id`);
-- 存量数据：已启用(1)视为已审批(20)，已停用(0)保持草稿(0)
UPDATE `erp_product` SET `audit_status` = 20 WHERE `status` = 1;
UPDATE `erp_product` SET `audit_status` = 0 WHERE `status` = 0;
