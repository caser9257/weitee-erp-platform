-- ERP 生产工单状态字典修正
-- 背景：09-erp-manufacturing-dict-menu.sql 中 erp_production_order_status 字典值为 0/1/2/3/4，
--       与后端枚举 ErpProductionOrderStatusEnum(0 已创建/10 已下达/20 已完工/30 已关闭) 不一致。
--       经排查无任何 Java/前端代码消费该字典，本次将字典数据对齐后端枚举，避免后续误用。
-- 幂等：UPDATE 以 (dict_type, value) 定位，重复执行无影响；多余"生产中"值 DELETE 前先判存。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 0 待下达 -> 0 已创建（label 与 sort 对齐）
UPDATE `system_dict_data`
SET `label` = '已创建', `sort` = 1, `color_type` = 'default'
WHERE `dict_type` = 'erp_production_order_status' AND `value` = '0' AND `deleted` = b'0';

-- 1 已下达 -> 10 已下达
UPDATE `system_dict_data`
SET `label` = '已下达', `sort` = 2, `value` = '10', `color_type` = 'primary'
WHERE `dict_type` = 'erp_production_order_status' AND `value` = '1' AND `deleted` = b'0';

-- 3 已完工 -> 20 已完工
UPDATE `system_dict_data`
SET `label` = '已完工', `sort` = 3, `value` = '20', `color_type` = 'success'
WHERE `dict_type` = 'erp_production_order_status' AND `value` = '3' AND `deleted` = b'0';

-- 4 已关闭 -> 30 已关闭
UPDATE `system_dict_data`
SET `label` = '已关闭', `sort` = 4, `value` = '30', `color_type` = 'info'
WHERE `dict_type` = 'erp_production_order_status' AND `value` = '4' AND `deleted` = b'0';

-- 2 生产中：后端枚举无此状态，删除
DELETE FROM `system_dict_data`
WHERE `dict_type` = 'erp_production_order_status' AND `value` = '2' AND `deleted` = b'0';

SET FOREIGN_KEY_CHECKS = 1;

-- 验证
SELECT id, sort, label, value, dict_type, color_type
FROM system_dict_data
WHERE dict_type = 'erp_production_order_status' AND deleted = 0
ORDER BY sort;
