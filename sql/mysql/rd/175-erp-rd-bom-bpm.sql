-- 研发 BOM 接审批：新增流程实例编号字段，支撑提交/撤回/流程创建失败回写
ALTER TABLE `erp_rd_bom`
    ADD COLUMN `process_instance_id` varchar(64) NULL DEFAULT NULL COMMENT 'BPM 流程实例编号' AFTER `status`,
    ADD INDEX `idx_process_instance_id` (`process_instance_id`);
