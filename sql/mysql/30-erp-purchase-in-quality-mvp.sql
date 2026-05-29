ALTER TABLE `erp_purchase_in`
    ADD COLUMN `qa_status` INT NULL DEFAULT 10 COMMENT '质检状态' AFTER `status`,
    ADD COLUMN `qa_time` DATETIME NULL DEFAULT NULL COMMENT '质检时间' AFTER `last_reject_user_id`,
    ADD COLUMN `qa_user_id` BIGINT NULL DEFAULT NULL COMMENT '质检人' AFTER `qa_time`,
    ADD COLUMN `qa_remark` VARCHAR(255) NULL DEFAULT NULL COMMENT '质检备注' AFTER `qa_user_id`,
    ADD COLUMN `qa_pass_count` DECIMAL(24, 6) NULL DEFAULT 0 COMMENT '合格总数量' AFTER `qa_remark`,
    ADD COLUMN `qa_reject_count` DECIMAL(24, 6) NULL DEFAULT 0 COMMENT '不合格总数量' AFTER `qa_pass_count`;

ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `qa_pass_count` DECIMAL(24, 6) NULL DEFAULT NULL COMMENT '合格数量' AFTER `tax_price`,
    ADD COLUMN `qa_reject_count` DECIMAL(24, 6) NULL DEFAULT NULL COMMENT '不合格数量' AFTER `qa_pass_count`,
    ADD COLUMN `qa_remark` VARCHAR(255) NULL DEFAULT NULL COMMENT '质检备注' AFTER `qa_reject_count`;
