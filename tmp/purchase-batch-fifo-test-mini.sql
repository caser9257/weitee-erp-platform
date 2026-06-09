USE `ruoyi-vue-pro`;

SET @tenant_id := 0;
SET @creator := 'system';

-- 目标：让生产领料推荐必须拆成两个批次
-- 依赖真实主数据：
-- - 生产工单 920601
-- - 工单物料 920701
-- - 物料 99103
-- - 仓库 99201

DELETE FROM erp_production_issue_batch WHERE issue_item_id = 990011;
DELETE FROM erp_production_issue_item WHERE id = 990001;
DELETE FROM erp_production_issue WHERE id = 990000;
DELETE FROM erp_stock_batch_record WHERE stock_batch_id IN (990101, 990102);
DELETE FROM erp_stock_batch WHERE id IN (990101, 990102);
DELETE FROM erp_purchase_in_stock_execute_item_batch WHERE id IN (990301, 990302);
DELETE FROM erp_purchase_in_stock_execute_item WHERE id = 990201;
DELETE FROM erp_purchase_in_stock_execute WHERE id = 990200;
DELETE FROM erp_purchase_in_items WHERE id = 990401;
DELETE FROM erp_purchase_in WHERE id = 990400;
DELETE FROM erp_purchase_source_batch WHERE id IN (990501, 990502);
DELETE FROM erp_purchase_order_items WHERE id = 990601;
DELETE FROM erp_purchase_order WHERE id = 990600;

INSERT INTO erp_purchase_order
(`id`, `no`, `status`, `supplier_id`, `account_id`, `order_time`, `total_count`, `total_price`, `total_product_price`, `total_tax_price`,
 `discount_percent`, `discount_price`, `deposit_price`, `file_url`, `remark`, `in_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990600, 'CGMINI-0001', 20, 1, 1, NOW(), 20.000000, 200.000000, 200.000000, 0.000000,
 0.000000, 0.000000, 0.000000, NULL, '最小批次测试采购单', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_order_items
(`id`, `order_id`, `product_id`, `product_unit_id`, `product_price`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `remark`, `in_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990601, 990600, 99103, 99002, 10.000000, 20.000000, 200.000000, 0.000000, 0.000000,
 '最小批次测试明细', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_source_batch
(`id`, `batch_no`, `product_id`, `purchase_order_id`, `purchase_order_item_id`, `supplier_id`, `status`, `biz_date`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990501, 'CGLY-MINI-001', 99103, 990600, 990601, 1, 20, CURDATE(), '最小来源批次1', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(990502, 'CGLY-MINI-002', 99103, 990600, 990601, 1, 20, CURDATE(), '最小来源批次2', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_in
(`id`, `no`, `status`, `qa_status`, `process_instance_id`, `supplier_id`, `account_id`, `in_time`, `order_id`, `order_no`,
 `total_count`, `total_price`, `payment_price`, `total_product_price`, `total_tax_price`, `discount_percent`, `discount_price`,
 `other_price`, `file_url`, `remark`, `last_reject_reason`, `last_reject_time`, `last_reject_user_id`, `qa_time`, `qa_user_id`,
 `qa_remark`, `qa_pass_count`, `qa_reject_count`, `stock_in_count`, `stock_in_status`, `stock_in_time`, `stock_in_user_id`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990400, 'CGRK-MINI-0001', 20, 20, NULL, 1, 1, NOW(), 990600, 'CGMINI-0001',
 20.000000, 200.000000, 0.000000, 200.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, '最小批次测试入库单', NULL, NULL, NULL, NULL, NULL,
 NULL, 20.000000, 0.000000, 20.000000, 20, NOW(), NULL,
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_in_items
(`id`, `in_id`, `order_item_id`, `warehouse_id`, `product_id`, `purchase_source_batch_id`, `product_unit_id`, `product_price`,
 `engineering_fee`, `pricing_bom_id`, `pricing_bom_version`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `qa_pass_count`, `qa_reject_count`, `stock_in_count`, `remark`, `qa_remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990401, 990400, 990601, 99201, 99103, 990501, 99002, 10.000000,
 0.000000, NULL, NULL, 20.000000, 200.000000, 0.000000, 0.000000,
 20.000000, 0.000000, 20.000000, '最小批次测试入库明细', NULL, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_stock_batch
(`id`, `product_id`, `warehouse_id`, `batch_no`, `inbound_time`, `produce_date`, `expire_date`, `total_qty`, `available_qty`, `locked_qty`,
 `virtual_flag`, `source_biz_type`, `source_biz_id`, `source_biz_item_id`, `purchase_source_batch_id`, `source_biz_no`, `purchase_source_batch_no`,
 `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990101, 99103, 99201, 'MINI-BATCH-001', '2026-01-01 08:00:00', '2025-12-30', '2026-12-30', 10.000000, 10.000000, 0.000000,
 b'0', 'PURCHASE_IN', 990400, 990401, 990501, 'CGRK-MINI-0001', 'CGLY-MINI-001', '最小批次1', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(990102, 99103, 99201, 'MINI-BATCH-002', '2026-01-02 08:00:00', '2025-12-31', '2026-12-31', 10.000000, 10.000000, 0.000000,
 b'0', 'PURCHASE_IN', 990400, 990401, 990502, 'CGRK-MINI-0001', 'CGLY-MINI-002', '最小批次2', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_in_stock_execute
(`id`, `no`, `purchase_in_id`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990200, 'RKZX-MINI-0001', 990400, 20, '最小批次测试执行', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_in_stock_execute_item
(`id`, `execute_id`, `purchase_in_id`, `purchase_in_item_id`, `product_id`, `warehouse_id`, `count`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990201, 990200, 990400, 990401, 99103, 99201, 20.000000, '最小批次测试执行明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_purchase_in_stock_execute_item_batch
(`id`, `execute_item_id`, `purchase_in_item_id`, `purchase_source_batch_id`, `stock_batch_id`, `product_id`, `warehouse_id`, `batch_no`,
 `purchase_source_batch_no`, `count`, `inbound_time`, `produce_date`, `expire_date`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990301, 990201, 990401, 990501, 990101, 99103, 99201, 'MINI-BATCH-001', 'CGLY-MINI-001', 10.000000, '2026-01-01 08:00:00', '2025-12-30', '2026-12-30', '最小批次A', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(990302, 990201, 990401, 990502, 990102, 99103, 99201, 'MINI-BATCH-002', 'CGLY-MINI-002', 10.000000, '2026-01-02 08:00:00', '2025-12-31', '2026-12-31', '最小批次B', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_production_issue
(`id`, `issue_no`, `production_order_id`, `issue_time`, `status`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990000, 'LL-MINI-0001', 920601, NOW(), 20, 150.000000, '最小批次测试领料单', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_production_issue_item
(`id`, `issue_id`, `production_material_id`, `material_id`, `warehouse_id`, `issue_qty`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990001, 990000, 920701, 99103, 99201, 15.000000, 150.000000, '最小批次领料主明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

INSERT INTO erp_production_issue_batch
(`id`, `issue_item_id`, `stock_batch_id`, `batch_no`, `issue_qty`, `inbound_time`, `produce_date`, `expire_date`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(990011, 990001, 990101, 'MINI-BATCH-001', 10.000000, '2026-01-01 08:00:00', '2025-12-30', '2026-12-30', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(990012, 990001, 990102, 'MINI-BATCH-002', 5.000000, '2026-01-02 08:00:00', '2025-12-31', '2026-12-31', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE `update_time` = NOW();

SELECT sb.id, sb.batch_no, sb.total_qty, sb.available_qty, sb.inbound_time, sb.purchase_source_batch_no
FROM erp_stock_batch sb
WHERE sb.id IN (990101, 990102)
ORDER BY sb.inbound_time, sb.id;
