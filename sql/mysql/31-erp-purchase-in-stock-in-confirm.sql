ALTER TABLE `erp_purchase_in`
    ADD COLUMN `stock_in_status` INT NULL DEFAULT NULL COMMENT '入库确认状态' AFTER `qa_reject_count`,
    ADD COLUMN `stock_in_time` DATETIME NULL DEFAULT NULL COMMENT '最终入库时间' AFTER `stock_in_status`,
    ADD COLUMN `stock_in_user_id` BIGINT NULL DEFAULT NULL COMMENT '最终入库人' AFTER `stock_in_time`;

UPDATE `erp_purchase_in`
SET `stock_in_status` = 20,
    `stock_in_time` = COALESCE(`stock_in_time`, `qa_time`, `update_time`, `create_time`),
    `stock_in_user_id` = COALESCE(`stock_in_user_id`, `qa_user_id`)
WHERE `qa_status` IN (20, 30)
  AND `stock_in_status` IS NULL;
