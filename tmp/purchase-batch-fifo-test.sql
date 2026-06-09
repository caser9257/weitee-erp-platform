-- 采购来源批次 + 库存批次 + FIFO 领料测试脚本
-- 说明:
-- 1. 该脚本用于本地测试/冒烟验证，不建议直接在生产库执行。
-- 2. 脚本默认 tenant_id = 0。
-- 3. 需要保证以下基础主数据已存在:
--    - supplier_id = 1
--    - account_id = 1
--    - product_id = 99104
--    - warehouse_id = 99201
--    - unit_id = 1
--    - production_order_id = 920601
--    - production_material_id = 920701
--
-- 若你的环境主数据 ID 不同，请把脚本中的 ID 一次性替换掉。

SET @tenant_id := 0;
SET @creator := 'system';
SET @supplier_id := 1;
SET @account_id := 1;
SET @product_id := 99104;
SET @warehouse_id := 99201;
SET @unit_id := 1;
SET @production_order_id := 990601;
SET @production_material_id := 990701;

-- -----------------------------------------------------
-- 1. 清理本次测试数据
-- -----------------------------------------------------
DELETE FROM erp_production_issue_batch WHERE issue_item_id IN (900011, 900021);
DELETE FROM erp_production_issue_item WHERE id IN (900001, 900002);
DELETE FROM erp_production_issue WHERE id = 900000;

DELETE FROM erp_stock_batch_record WHERE stock_batch_id IN (900101, 900102);
DELETE FROM erp_stock_batch WHERE id IN (900101, 900102);
DELETE FROM erp_purchase_in_stock_execute_item_batch WHERE id IN (900301, 900302);
DELETE FROM erp_purchase_in_items WHERE id = 900201;
DELETE FROM erp_purchase_in WHERE id = 900200;
DELETE FROM erp_purchase_source_batch WHERE id IN (900401, 900402);
DELETE FROM erp_purchase_order_items WHERE id = 900501;
DELETE FROM erp_purchase_order WHERE id = 900500;

-- -----------------------------------------------------
-- 2. 基础采购订单 / 订单明细
-- -----------------------------------------------------
INSERT INTO erp_purchase_order
(`id`, `no`, `status`, `supplier_id`, `account_id`, `order_time`, `total_count`, `total_price`, `total_product_price`, `total_tax_price`,
 `discount_percent`, `discount_price`, `deposit_price`, `file_url`, `remark`, `in_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900500, 'CGTEST-0001', 20, @supplier_id, @account_id, NOW(), 100.000000, 1000.000000, 1000.000000, 0.000000,
 0.000000, 0.000000, 0.000000, NULL, '采购批次测试单', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`no` = VALUES(`no`),
`status` = VALUES(`status`),
`supplier_id` = VALUES(`supplier_id`),
`account_id` = VALUES(`account_id`),
`order_time` = VALUES(`order_time`),
`total_count` = VALUES(`total_count`),
`total_price` = VALUES(`total_price`),
`total_product_price` = VALUES(`total_product_price`),
`total_tax_price` = VALUES(`total_tax_price`),
`discount_percent` = VALUES(`discount_percent`),
`discount_price` = VALUES(`discount_price`),
`deposit_price` = VALUES(`deposit_price`),
`remark` = VALUES(`remark`),
`in_count` = VALUES(`in_count`),
`return_count` = VALUES(`return_count`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_purchase_order_items
(`id`, `order_id`, `product_id`, `product_unit_id`, `product_price`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `remark`, `in_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900501, 900500, @product_id, @unit_id, 10.000000, 100.000000, 1000.000000, 0.000000, 0.000000,
 '采购来源批次测试明细', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`order_id` = VALUES(`order_id`),
`product_id` = VALUES(`product_id`),
`product_unit_id` = VALUES(`product_unit_id`),
`product_price` = VALUES(`product_price`),
`count` = VALUES(`count`),
`total_price` = VALUES(`total_price`),
`tax_percent` = VALUES(`tax_percent`),
`tax_price` = VALUES(`tax_price`),
`remark` = VALUES(`remark`),
`in_count` = VALUES(`in_count`),
`return_count` = VALUES(`return_count`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- -----------------------------------------------------
-- 3. 采购来源批次
-- -----------------------------------------------------
INSERT INTO erp_purchase_source_batch
(`id`, `batch_no`, `product_id`, `purchase_order_id`, `purchase_order_item_id`, `supplier_id`, `status`, `biz_date`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900401, 'CGLY-TEST-0001', @product_id, 900500, 900501, @supplier_id, 20, CURDATE(), '来源批次A',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(900402, 'CGLY-TEST-0002', @product_id, 900500, 900501, @supplier_id, 20, CURDATE(), '来源批次B',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`batch_no` = VALUES(`batch_no`),
`product_id` = VALUES(`product_id`),
`purchase_order_id` = VALUES(`purchase_order_id`),
`purchase_order_item_id` = VALUES(`purchase_order_item_id`),
`supplier_id` = VALUES(`supplier_id`),
`status` = VALUES(`status`),
`biz_date` = VALUES(`biz_date`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- -----------------------------------------------------
-- 4. 采购入库主表 / 明细
-- -----------------------------------------------------
INSERT INTO erp_purchase_in
(`id`, `no`, `status`, `qa_status`, `process_instance_id`, `supplier_id`, `account_id`, `in_time`, `order_id`, `order_no`,
 `total_count`, `total_price`, `payment_price`, `total_product_price`, `total_tax_price`, `discount_percent`, `discount_price`,
 `other_price`, `file_url`, `remark`, `last_reject_reason`, `last_reject_time`, `last_reject_user_id`, `qa_time`, `qa_user_id`,
 `qa_remark`, `qa_pass_count`, `qa_reject_count`, `stock_in_count`, `stock_in_status`, `stock_in_time`, `stock_in_user_id`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900200, 'CGRK-TEST-0001', 20, 20, NULL, @supplier_id, @account_id, NOW(), 900500, 'CGTEST-0001',
 100.000000, 1000.000000, 0.000000, 1000.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, '采购入库测试单', NULL, NULL, NULL, NULL, NULL,
 NULL, 100.000000, 0.000000, 100.000000, 20, NOW(), NULL,
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`no` = VALUES(`no`),
`status` = VALUES(`status`),
`qa_status` = VALUES(`qa_status`),
`supplier_id` = VALUES(`supplier_id`),
`account_id` = VALUES(`account_id`),
`in_time` = VALUES(`in_time`),
`order_id` = VALUES(`order_id`),
`order_no` = VALUES(`order_no`),
`total_count` = VALUES(`total_count`),
`total_price` = VALUES(`total_price`),
`payment_price` = VALUES(`payment_price`),
`total_product_price` = VALUES(`total_product_price`),
`total_tax_price` = VALUES(`total_tax_price`),
`remark` = VALUES(`remark`),
`qa_pass_count` = VALUES(`qa_pass_count`),
`qa_reject_count` = VALUES(`qa_reject_count`),
`stock_in_count` = VALUES(`stock_in_count`),
`stock_in_status` = VALUES(`stock_in_status`),
`stock_in_time` = VALUES(`stock_in_time`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_purchase_in_items
(`id`, `in_id`, `order_item_id`, `warehouse_id`, `product_id`, `purchase_source_batch_id`, `product_unit_id`, `product_price`,
 `engineering_fee`, `pricing_bom_id`, `pricing_bom_version`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `qa_pass_count`, `qa_reject_count`, `stock_in_count`, `remark`, `qa_remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900201, 900200, 900501, @warehouse_id, @product_id, 900401, @unit_id, 10.000000,
 0.000000, NULL, NULL, 60.000000, 600.000000, 0.000000, 0.000000,
 60.000000, 0.000000, 60.000000, '入库到来源批次A', NULL, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`in_id` = VALUES(`in_id`),
`order_item_id` = VALUES(`order_item_id`),
`warehouse_id` = VALUES(`warehouse_id`),
`product_id` = VALUES(`product_id`),
`purchase_source_batch_id` = VALUES(`purchase_source_batch_id`),
`product_unit_id` = VALUES(`product_unit_id`),
`product_price` = VALUES(`product_price`),
`engineering_fee` = VALUES(`engineering_fee`),
`pricing_bom_id` = VALUES(`pricing_bom_id`),
`pricing_bom_version` = VALUES(`pricing_bom_version`),
`count` = VALUES(`count`),
`total_price` = VALUES(`total_price`),
`tax_percent` = VALUES(`tax_percent`),
`tax_price` = VALUES(`tax_price`),
`qa_pass_count` = VALUES(`qa_pass_count`),
`qa_reject_count` = VALUES(`qa_reject_count`),
`stock_in_count` = VALUES(`stock_in_count`),
`remark` = VALUES(`remark`),
`qa_remark` = VALUES(`qa_remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- -----------------------------------------------------
-- 5. 库存批次 + 入库批次明细
-- -----------------------------------------------------
INSERT INTO erp_stock_batch
(`id`, `product_id`, `warehouse_id`, `batch_no`, `inbound_time`, `produce_date`, `expire_date`, `total_qty`, `available_qty`, `locked_qty`,
 `virtual_flag`, `source_biz_type`, `source_biz_id`, `source_biz_item_id`, `purchase_source_batch_id`, `source_biz_no`, `purchase_source_batch_no`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900101, @product_id, @warehouse_id, 'BATCH-TEST-001', '2026-05-12 08:00:00', '2026-05-10', '2027-05-10', 60.000000, 60.000000, 0.000000,
 b'0', 'PURCHASE_IN', 900200, 900201, 900401, 'CGRK-TEST-0001', 'CGLY-TEST-0001',
 '批次A，最早入库', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(900102, @product_id, @warehouse_id, 'BATCH-TEST-002', '2026-05-13 08:00:00', '2026-05-11', '2027-05-11', 40.000000, 40.000000, 0.000000,
 b'0', 'PURCHASE_IN', 900200, 900201, 900402, 'CGRK-TEST-0001', 'CGLY-TEST-0002',
 '批次B，后入库', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`product_id` = VALUES(`product_id`),
`warehouse_id` = VALUES(`warehouse_id`),
`batch_no` = VALUES(`batch_no`),
`inbound_time` = VALUES(`inbound_time`),
`produce_date` = VALUES(`produce_date`),
`expire_date` = VALUES(`expire_date`),
`total_qty` = VALUES(`total_qty`),
`available_qty` = VALUES(`available_qty`),
`locked_qty` = VALUES(`locked_qty`),
`virtual_flag` = VALUES(`virtual_flag`),
`source_biz_type` = VALUES(`source_biz_type`),
`source_biz_id` = VALUES(`source_biz_id`),
`source_biz_item_id` = VALUES(`source_biz_item_id`),
`purchase_source_batch_id` = VALUES(`purchase_source_batch_id`),
`source_biz_no` = VALUES(`source_biz_no`),
`purchase_source_batch_no` = VALUES(`purchase_source_batch_no`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_purchase_in_stock_execute
(`id`, `no`, `purchase_in_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900300, 'RKZX-TEST-0001', 900200, 20, '采购入库执行测试', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`no` = VALUES(`no`),
`purchase_in_id` = VALUES(`purchase_in_id`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_purchase_in_stock_execute_item
(`id`, `execute_id`, `purchase_in_id`, `purchase_in_item_id`, `product_id`, `warehouse_id`, `count`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900301, 900300, 900200, 900201, @product_id, @warehouse_id, 100.000000, '执行明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`execute_id` = VALUES(`execute_id`),
`purchase_in_id` = VALUES(`purchase_in_id`),
`purchase_in_item_id` = VALUES(`purchase_in_item_id`),
`product_id` = VALUES(`product_id`),
`warehouse_id` = VALUES(`warehouse_id`),
`count` = VALUES(`count`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_purchase_in_stock_execute_item_batch
(`id`, `execute_item_id`, `purchase_in_item_id`, `purchase_source_batch_id`, `stock_batch_id`, `product_id`, `warehouse_id`, `batch_no`,
 `purchase_source_batch_no`, `count`, `inbound_time`, `produce_date`, `expire_date`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900302, 900301, 900201, 900401, 900101, @product_id, @warehouse_id, 'BATCH-TEST-001', 'CGLY-TEST-0001', 60.000000, '2026-05-12 08:00:00', '2026-05-10', '2027-05-10', '来源批次A对应库存批次A', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(900303, 900301, 900201, 900402, 900102, @product_id, @warehouse_id, 'BATCH-TEST-002', 'CGLY-TEST-0002', 40.000000, '2026-05-13 08:00:00', '2026-05-11', '2027-05-11', '来源批次B对应库存批次B', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`execute_item_id` = VALUES(`execute_item_id`),
`purchase_in_item_id` = VALUES(`purchase_in_item_id`),
`purchase_source_batch_id` = VALUES(`purchase_source_batch_id`),
`stock_batch_id` = VALUES(`stock_batch_id`),
`product_id` = VALUES(`product_id`),
`warehouse_id` = VALUES(`warehouse_id`),
`batch_no` = VALUES(`batch_no`),
`purchase_source_batch_no` = VALUES(`purchase_source_batch_no`),
`count` = VALUES(`count`),
`inbound_time` = VALUES(`inbound_time`),
`produce_date` = VALUES(`produce_date`),
`expire_date` = VALUES(`expire_date`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- -----------------------------------------------------
-- 6. 生产领料：按入库时间 FIFO
-- -----------------------------------------------------
INSERT INTO erp_production_issue
(`id`, `issue_no`, `production_order_id`, `issue_time`, `status`, `issue_amount`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900000, 'LL-TEST-0001', @production_order_id, NOW(), 20, 1000.000000, '生产领料测试单',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`issue_no` = VALUES(`issue_no`),
`production_order_id` = VALUES(`production_order_id`),
`issue_time` = VALUES(`issue_time`),
`status` = VALUES(`status`),
`issue_amount` = VALUES(`issue_amount`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_production_issue_item
(`id`, `issue_id`, `production_material_id`, `material_id`, `warehouse_id`, `issue_qty`, `issue_amount`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900001, 900000, @production_material_id, @product_id, @warehouse_id, 70.000000, 700.000000, 'FIFO领料-主明细',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`issue_id` = VALUES(`issue_id`),
`production_material_id` = VALUES(`production_material_id`),
`material_id` = VALUES(`material_id`),
`warehouse_id` = VALUES(`warehouse_id`),
`issue_qty` = VALUES(`issue_qty`),
`issue_amount` = VALUES(`issue_amount`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

INSERT INTO erp_production_issue_batch
(`id`, `issue_item_id`, `stock_batch_id`, `batch_no`, `issue_qty`, `inbound_time`, `produce_date`, `expire_date`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(900011, 900001, 900101, 'BATCH-TEST-001', 60.000000, '2026-05-12 08:00:00', '2026-05-10', '2027-05-10', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(900012, 900001, 900102, 'BATCH-TEST-002', 10.000000, '2026-05-13 08:00:00', '2026-05-11', '2027-05-11', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`issue_item_id` = VALUES(`issue_item_id`),
`stock_batch_id` = VALUES(`stock_batch_id`),
`batch_no` = VALUES(`batch_no`),
`issue_qty` = VALUES(`issue_qty`),
`inbound_time` = VALUES(`inbound_time`),
`produce_date` = VALUES(`produce_date`),
`expire_date` = VALUES(`expire_date`),
`updater` = VALUES(`updater`),
`update_time` = NOW(),
`deleted` = VALUES(`deleted`),
`tenant_id` = VALUES(`tenant_id`);

-- -----------------------------------------------------
-- 7. 验证查询
-- -----------------------------------------------------
SELECT
    sb.id,
    sb.batch_no,
    sb.product_id,
    sb.warehouse_id,
    sb.total_qty,
    sb.available_qty,
    sb.inbound_time,
    sb.purchase_source_batch_id,
    psb.batch_no AS purchase_source_batch_no
FROM erp_stock_batch sb
LEFT JOIN erp_purchase_source_batch psb ON psb.id = sb.purchase_source_batch_id
WHERE sb.id IN (900101, 900102)
ORDER BY sb.inbound_time, sb.id;

SELECT
    pib.id,
    pib.batch_no,
    pib.issue_qty,
    pib.inbound_time
FROM erp_production_issue_batch pib
WHERE pib.issue_item_id = 900001
ORDER BY pib.inbound_time, pib.id;
