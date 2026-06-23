-- 添加预警记录去重支持
-- 1. 添加 trigger_date 列（存储触发日期，用于唯一索引去重）
ALTER TABLE `erp_market_alert_record`
ADD COLUMN `trigger_date` DATE NULL COMMENT '触发日期（用于去重）' AFTER `trigger_time`;

-- 2. 更新已有记录的 trigger_date
UPDATE `erp_market_alert_record`
SET `trigger_date` = DATE(`trigger_time`)
WHERE `trigger_date` IS NULL;

-- 3. 添加唯一索引：同一订单同一规则同一天只记录一次
ALTER TABLE `erp_market_alert_record`
ADD UNIQUE INDEX `uk_rule_order_date` (`rule_code`, `order_id`, `trigger_date`);
