/*
 ERP 制造 / MRP 演示数据
 目标：
 - 补齐生产领料、生产退料、委外订单、委外发料、委外退料、委外入库的验证样本
 - 提供足够多的可打印、可回显、可筛选数据
 - 仅写测试数据，保持幂等
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @tenant_id := COALESCE((SELECT id FROM system_tenant WHERE deleted = b'0' ORDER BY id LIMIT 1), 1);
SET @creator := 'tester';
SET @warehouse_id := 99201;
SET @finished_warehouse_id := 99202;
SET @outsource_warehouse_id := 99203;

DELETE FROM erp_production_return_batch WHERE return_item_id IN (920401, 920402, 920403, 920404, 920405, 920406);
DELETE FROM erp_production_return_item WHERE id BETWEEN 920301 AND 920306;
DELETE FROM erp_production_return WHERE id BETWEEN 920201 AND 920203;
DELETE FROM erp_production_issue_batch WHERE issue_item_id IN (920101, 920102, 920103, 920104, 920105, 920106);
DELETE FROM erp_production_issue_item WHERE id BETWEEN 920001 AND 920006;
DELETE FROM erp_production_issue WHERE id BETWEEN 919901 AND 919903;

DELETE FROM erp_outsource_return_batch WHERE return_item_id IN (921401, 921402, 921403, 921404);
DELETE FROM erp_outsource_return_item WHERE id BETWEEN 921301 AND 921304;
DELETE FROM erp_outsource_return WHERE id BETWEEN 921201 AND 921203;
DELETE FROM erp_outsource_issue_batch WHERE issue_item_id IN (921101, 921102, 921103, 921104, 921105, 921106);
DELETE FROM erp_outsource_issue_item WHERE id BETWEEN 921001 AND 921006;
DELETE FROM erp_outsource_issue WHERE id BETWEEN 920901 AND 920903;
DELETE FROM erp_outsource_inbound WHERE id BETWEEN 922001 AND 922004;
DELETE FROM erp_outsource_fee WHERE id BETWEEN 922201 AND 922203;
DELETE FROM erp_outsource_order WHERE id BETWEEN 920701 AND 920704;

DELETE FROM erp_stock_batch_record WHERE biz_id IN (919901, 919902, 919903, 920901, 920902, 922001, 922002, 922003);
DELETE FROM erp_stock_batch WHERE id IN (919801, 919802, 919803, 919804, 920801, 920802, 920803, 920804, 922101, 922102, 922103, 922104, 920805, 920806);

DELETE FROM erp_production_order_step WHERE production_order_id IN (920601, 920602, 920603);
DELETE FROM erp_production_material WHERE production_order_id IN (920601, 920602, 920603);
DELETE FROM erp_production_order WHERE id BETWEEN 920601 AND 920603;

INSERT INTO erp_production_order
(`id`, `order_no`, `product_id`, `plan_qty`, `finished_qty`, `plan_start_time`, `plan_end_time`, `status`, `source_type`, `source_id`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920601, 'MO-20260512-001', 99104, 120.000000, 80.000000, '2026-05-10 08:00:00', '2026-05-18 18:00:00', 2, 'MRP', 6001, '测试工单-生产领料和退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920602, 'MO-20260512-002', 99102, 200.000000, 200.000000, '2026-05-08 08:00:00', '2026-05-16 18:00:00', 3, 'SALE_ORDER', 6002, '测试工单-已完工样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920603, 'MO-20260512-003', 99101, 500.000000, 0.000000, '2026-05-12 08:00:00', '2026-05-22 18:00:00', 1, 'MANUAL', NULL, '测试工单-待下达样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_no = VALUES(order_no), product_id = VALUES(product_id), plan_qty = VALUES(plan_qty), finished_qty = VALUES(finished_qty), plan_start_time = VALUES(plan_start_time), plan_end_time = VALUES(plan_end_time), status = VALUES(status), source_type = VALUES(source_type), source_id = VALUES(source_id), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_material
(`id`, `production_order_id`, `production_order_step_id`, `bom_item_id`, `material_id`, `required_qty`, `issued_qty`, `returned_qty`, `scrap_qty`, `supply_warehouse_id`, `issue_mode`, `backflush_flag`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920701, 920601, NULL, NULL, 99103, 1200.000000, 800.000000, 20.000000, 5.000000, @warehouse_id, 1, b'0', '电机工单-垫片', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920702, 920601, NULL, NULL, 99101, 240.000000, 150.000000, 0.000000, 0.000000, @warehouse_id, 1, b'0', '电机工单-螺栓', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920703, 920602, NULL, NULL, 99102, 200.000000, 200.000000, 0.000000, 0.000000, @warehouse_id, 1, b'1', '控制板工单-电子件', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE production_order_id = VALUES(production_order_id), material_id = VALUES(material_id), required_qty = VALUES(required_qty), issued_qty = VALUES(issued_qty), returned_qty = VALUES(returned_qty), scrap_qty = VALUES(scrap_qty), supply_warehouse_id = VALUES(supply_warehouse_id), issue_mode = VALUES(issue_mode), backflush_flag = VALUES(backflush_flag), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_stock_batch
(`id`, `product_id`, `warehouse_id`, `batch_no`, `inbound_time`, `produce_date`, `expire_date`, `total_qty`, `available_qty`, `locked_qty`, `virtual_flag`, `source_biz_type`, `source_biz_id`, `source_biz_item_id`, `source_biz_no`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(919801, 99101, @warehouse_id, 'MB-20260512-001', '2026-05-01 08:30:00', '2026-04-28', '2027-04-28', 1000.000000, 760.000000, 0.000000, b'0', 'PURCHASE_IN', 101101, 101201, 'PI-20260501-001', '生产领料测试批次-螺栓', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919802, 99103, @warehouse_id, 'MB-20260512-002', '2026-05-02 09:00:00', '2026-04-30', '2027-04-30', 2000.000000, 1450.000000, 0.000000, b'0', 'PURCHASE_IN', 101103, 101204, 'PI-20260501-003', '生产领料测试批次-垫片', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919803, 99102, @warehouse_id, 'MB-20260512-003', '2026-05-03 10:00:00', '2026-05-01', '2027-05-01', 300.000000, 180.000000, 0.000000, b'0', 'PURCHASE_IN', 101102, 101203, 'PI-20260501-002', '生产领料测试批次-控制板', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919804, 99104, @finished_warehouse_id, 'MB-20260512-004', '2026-05-08 16:20:00', '2026-05-06', '2028-05-06', 200.000000, 200.000000, 0.000000, b'0', 'PRODUCTION_COMPLETION', 920602, NULL, 'MO-20260512-002', '完工入库样本批次', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920801, 99105, @outsource_warehouse_id, 'OUT-20260512-001', '2026-05-07 11:10:00', '2026-05-04', '2027-05-04', 500.000000, 450.000000, 0.000000, b'0', 'PURCHASE_IN', 101105, 101206, 'PI-20260501-005', '委外发料测试批次-包装材料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920802, 99104, @outsource_warehouse_id, 'OUT-20260512-002', '2026-05-07 11:10:00', '2026-05-04', '2028-05-04', 60.000000, 40.000000, 0.000000, b'0', 'PURCHASE_IN', 101104, 101205, 'PI-20260501-004', '委外发料测试批次-电机', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920803, 99102, @outsource_warehouse_id, 'OUT-20260512-003', '2026-05-09 14:00:00', '2026-05-05', '2027-05-05', 120.000000, 120.000000, 0.000000, b'0', 'PURCHASE_IN', 101102, 101203, 'PI-20260501-002', '委外入库测试批次-控制板', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920804, 99101, @outsource_warehouse_id, 'OUT-20260512-004', '2026-05-09 14:00:00', '2026-05-05', '2027-05-05', 80.000000, 80.000000, 0.000000, b'0', 'PURCHASE_IN', 101101, 101201, 'PI-20260501-001', '委外入库测试批次-螺栓', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), batch_no = VALUES(batch_no), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), total_qty = VALUES(total_qty), available_qty = VALUES(available_qty), locked_qty = VALUES(locked_qty), virtual_flag = VALUES(virtual_flag), source_biz_type = VALUES(source_biz_type), source_biz_id = VALUES(source_biz_id), source_biz_item_id = VALUES(source_biz_item_id), source_biz_no = VALUES(source_biz_no), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_stock_batch_record
(`id`, `product_id`, `warehouse_id`, `stock_batch_id`, `batch_no`, `count`, `after_available_qty`, `biz_type`, `biz_id`, `biz_item_id`, `biz_no`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(919701, 99101, @warehouse_id, 919801, 'MB-20260512-001', -240.000000, 520.000000, 801, 919901, 920001, 'PI-20260512-001', '生产领料扣减', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919702, 99103, @warehouse_id, 919802, 'MB-20260512-002', -550.000000, 900.000000, 801, 919901, 920002, 'PI-20260512-001', '生产领料扣减', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919703, 99102, @warehouse_id, 919803, 'MB-20260512-003', -120.000000, 60.000000, 801, 919902, 920003, 'PI-20260512-002', '生产领料扣减', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919704, 99104, @finished_warehouse_id, 919804, 'MB-20260512-004', 200.000000, 200.000000, 502, 920602, NULL, 'MO-20260512-002', '完工入库样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919705, 99105, @outsource_warehouse_id, 920801, 'OUT-20260512-001', -50.000000, 400.000000, 901, 920901, 921101, 'OO-20260512-001', '委外发料扣减', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919706, 99104, @outsource_warehouse_id, 920802, 'OUT-20260512-002', -20.000000, 20.000000, 901, 920901, 921102, 'OO-20260512-001', '委外发料扣减', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), batch_no = VALUES(batch_no), count = VALUES(count), after_available_qty = VALUES(after_available_qty), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_item_id = VALUES(biz_item_id), biz_no = VALUES(biz_no), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_issue
(`id`, `issue_no`, `production_order_id`, `issue_time`, `status`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(919901, 'PI-20260512-001', 920601, '2026-05-12 09:10:00', 20, 3680.000000, '生产领料样本-一车间', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919902, 'PI-20260512-002', 920601, '2026-05-12 10:30:00', 20, 820.000000, '生产领料样本-补领', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(919903, 'PI-20260512-003', 920602, '2026-05-12 11:00:00', 20, 6200.000000, '生产领料样本-已完工工单', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE issue_no = VALUES(issue_no), production_order_id = VALUES(production_order_id), issue_time = VALUES(issue_time), status = VALUES(status), issue_amount = VALUES(issue_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_issue_item
(`id`, `issue_id`, `production_material_id`, `material_id`, `warehouse_id`, `issue_qty`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920001, 919901, 920701, 99103, @warehouse_id, 550.000000, 1650.000000, '垫片领料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920002, 919901, 920702, 99101, @warehouse_id, 240.000000, 288.000000, '螺栓领料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920003, 919902, 920701, 99103, @warehouse_id, 150.000000, 450.000000, '补料样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920004, 919902, 920702, 99101, @warehouse_id, 80.000000, 96.000000, '补料样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920005, 919903, 920703, 99102, @warehouse_id, 200.000000, 6200.000000, '已完工工单领料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920006, 919903, 920703, 99102, @warehouse_id, 0.000000, 0.000000, '空占位明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE issue_id = VALUES(issue_id), production_material_id = VALUES(production_material_id), material_id = VALUES(material_id), warehouse_id = VALUES(warehouse_id), issue_qty = VALUES(issue_qty), issue_amount = VALUES(issue_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_issue_batch
(`id`, `issue_item_id`, `stock_batch_id`, `batch_no`, `issue_qty`, `inbound_time`, `produce_date`, `expire_date`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920101, 920001, 919802, 'MB-20260512-002', 350.000000, '2026-05-02 09:00:00', '2026-04-30', '2027-04-30', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920102, 920001, 919802, 'MB-20260512-002', 200.000000, '2026-05-02 09:00:00', '2026-04-30', '2027-04-30', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920103, 920002, 919801, 'MB-20260512-001', 240.000000, '2026-05-01 08:30:00', '2026-04-28', '2027-04-28', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920104, 920003, 919802, 'MB-20260512-002', 150.000000, '2026-05-02 09:00:00', '2026-04-30', '2027-04-30', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920105, 920004, 919801, 'MB-20260512-001', 80.000000, '2026-05-01 08:30:00', '2026-04-28', '2027-04-28', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920106, 920005, 919803, 'MB-20260512-003', 200.000000, '2026-05-03 10:00:00', '2026-05-01', '2027-05-01', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE issue_item_id = VALUES(issue_item_id), stock_batch_id = VALUES(stock_batch_id), batch_no = VALUES(batch_no), issue_qty = VALUES(issue_qty), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_return
(`id`, `return_no`, `production_order_id`, `return_time`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920201, 'PR-20260512-001', 920601, '2026-05-12 15:20:00', 20, '生产退料样本-正常退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920202, 'PR-20260512-002', 920601, '2026-05-12 16:10:00', 20, '生产退料样本-多批次退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920203, 'PR-20260512-003', 920602, '2026-05-12 16:30:00', 20, '已完工工单退料样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_no = VALUES(return_no), production_order_id = VALUES(production_order_id), return_time = VALUES(return_time), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_return_item
(`id`, `return_id`, `production_material_id`, `material_id`, `warehouse_id`, `return_qty`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920301, 920201, 920701, 99103, @warehouse_id, 20.000000, '垫片退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920302, 920202, 920702, 99101, @warehouse_id, 10.000000, '螺栓退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920303, 920202, 920701, 99103, @warehouse_id, 5.000000, '垫片退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920304, 920203, 920703, 99102, @warehouse_id, 3.000000, '已完工工单退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_id = VALUES(return_id), production_material_id = VALUES(production_material_id), material_id = VALUES(material_id), warehouse_id = VALUES(warehouse_id), return_qty = VALUES(return_qty), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_production_return_batch
(`id`, `return_item_id`, `issue_batch_id`, `stock_batch_id`, `batch_no`, `return_qty`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920401, 920301, 920104, 919802, 'MB-20260512-002', 20.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920402, 920302, 920103, 919801, 'MB-20260512-001', 10.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920403, 920303, 920102, 919802, 'MB-20260512-002', 5.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920404, 920304, 920106, 919803, 'MB-20260512-003', 3.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_item_id = VALUES(return_item_id), issue_batch_id = VALUES(issue_batch_id), stock_batch_id = VALUES(stock_batch_id), batch_no = VALUES(batch_no), return_qty = VALUES(return_qty), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_order
(`id`, `no`, `order_type`, `supplier_id`, `product_id`, `bom_id`, `project_id`, `process_name`, `planned_qty`, `issued_qty`, `returned_qty`, `finished_qty`, `loss_qty`, `status`, `close_remark`, `close_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920701, 'OO-20260512-001', 20, 99304, 99104, NULL, NULL, '外协总装', 80.000000, 60.000000, 5.000000, 40.000000, 1.500000, 20, NULL, NULL, '委外订单样本-已完工', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920702, 'OO-20260512-002', 20, 99305, 99102, NULL, NULL, '外协检测', 120.000000, 90.000000, 10.000000, 50.000000, 2.000000, 20, NULL, NULL, '委外订单样本-入库验证', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920703, 'OO-20260512-003', 20, 99301, 99101, NULL, NULL, '外协装配', 200.000000, 140.000000, 12.000000, 100.000000, 3.500000, 20, NULL, NULL, '委外订单样本-打印验证', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920704, 'OO-20260512-004', 10, 99302, 99105, NULL, NULL, '外协包装', 160.000000, 0.000000, 0.000000, 0.000000, 0.000000, 10, NULL, NULL, '委外订单样本-待加工', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_type = VALUES(order_type), supplier_id = VALUES(supplier_id), product_id = VALUES(product_id), process_name = VALUES(process_name), planned_qty = VALUES(planned_qty), issued_qty = VALUES(issued_qty), returned_qty = VALUES(returned_qty), finished_qty = VALUES(finished_qty), loss_qty = VALUES(loss_qty), status = VALUES(status), close_remark = VALUES(close_remark), close_time = VALUES(close_time), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_issue
(`id`, `issue_no`, `order_id`, `issue_type`, `issue_time`, `status`, `issue_qty`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(920901, 'OI-20260512-001', 920701, 10, '2026-05-12 09:30:00', 20, 60.000000, 4800.000000, '委外发料样本-首批', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920902, 'OI-20260512-002', 920702, 10, '2026-05-12 10:20:00', 20, 30.000000, 2100.000000, '委外发料样本-已完成', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(920903, 'OI-20260512-003', 920703, 20, '2026-05-12 11:25:00', 20, 50.000000, 3650.000000, '委外发料样本-补发', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), issue_type = VALUES(issue_type), issue_time = VALUES(issue_time), status = VALUES(status), issue_qty = VALUES(issue_qty), issue_amount = VALUES(issue_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_issue_item
(`id`, `issue_id`, `material_id`, `warehouse_id`, `issue_qty`, `issue_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(921001, 920901, 99105, @outsource_warehouse_id, 50.000000, 1500.000000, '包装材料发料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921002, 920901, 99104, @outsource_warehouse_id, 10.000000, 3300.000000, '电机发料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921003, 920902, 99102, @outsource_warehouse_id, 30.000000, 2100.000000, '控制板发料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921004, 920902, 99101, @outsource_warehouse_id, 0.000000, 0.000000, '占位样本', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921005, 920903, 99105, @outsource_warehouse_id, 35.000000, 1225.000000, '包装材料补发', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921006, 920903, 99104, @outsource_warehouse_id, 15.000000, 2425.000000, '电机补发', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE issue_id = VALUES(issue_id), material_id = VALUES(material_id), warehouse_id = VALUES(warehouse_id), issue_qty = VALUES(issue_qty), issue_amount = VALUES(issue_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_issue_batch
(`id`, `issue_item_id`, `stock_batch_id`, `batch_no`, `issue_qty`, `issue_amount`, `inbound_time`, `produce_date`, `expire_date`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(921101, 921001, 920801, 'OUT-20260512-001', 50.000000, 1500.000000, '2026-05-07 11:10:00', '2026-05-04', '2027-05-04', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921102, 921002, 920802, 'OUT-20260512-002', 10.000000, 3300.000000, '2026-05-07 11:10:00', '2026-05-04', '2028-05-04', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921103, 921003, 920803, 'OUT-20260512-003', 30.000000, 2100.000000, '2026-05-09 14:00:00', '2026-05-05', '2027-05-05', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921104, 921004, 920804, 'OUT-20260512-004', 0.000000, 0.000000, '2026-05-09 14:00:00', '2026-05-05', '2027-05-05', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921105, 921005, 920801, 'OUT-20260512-001', 35.000000, 1225.000000, '2026-05-07 11:10:00', '2026-05-04', '2027-05-04', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921106, 921006, 920802, 'OUT-20260512-002', 15.000000, 2425.000000, '2026-05-07 11:10:00', '2026-05-04', '2028-05-04', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE issue_item_id = VALUES(issue_item_id), stock_batch_id = VALUES(stock_batch_id), batch_no = VALUES(batch_no), issue_qty = VALUES(issue_qty), issue_amount = VALUES(issue_amount), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_return
(`id`, `return_no`, `order_id`, `return_time`, `status`, `return_qty`, `return_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(921201, 'OR-20260512-001', 920701, '2026-05-12 16:40:00', 20, 5.000000, 650.000000, '委外退料样本-正常', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921202, 'OR-20260512-002', 920702, '2026-05-12 16:55:00', 20, 2.000000, 300.000000, '委外退料样本-多明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921203, 'OR-20260512-003', 920703, '2026-05-12 17:10:00', 20, 4.000000, 380.000000, '委外退料样本-打印验证', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), return_time = VALUES(return_time), status = VALUES(status), return_qty = VALUES(return_qty), return_amount = VALUES(return_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_return_item
(`id`, `return_id`, `material_id`, `warehouse_id`, `return_qty`, `return_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(921301, 921201, 99105, @outsource_warehouse_id, 5.000000, 650.000000, '包装材料退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921302, 921202, 99102, @outsource_warehouse_id, 2.000000, 300.000000, '控制板退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921303, 921202, 99101, @outsource_warehouse_id, 0.000000, 0.000000, '占位明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921304, 921201, 99104, @outsource_warehouse_id, 0.000000, 0.000000, '占位明细', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921305, 921203, 99105, @outsource_warehouse_id, 4.000000, 380.000000, '包装材料退料', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_id = VALUES(return_id), material_id = VALUES(material_id), warehouse_id = VALUES(warehouse_id), return_qty = VALUES(return_qty), return_amount = VALUES(return_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_return_batch
(`id`, `return_item_id`, `issue_batch_id`, `stock_batch_id`, `batch_no`, `return_qty`, `return_amount`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(921401, 921301, 921101, 920801, 'OUT-20260512-001', 5.000000, 650.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921402, 921302, 921103, 920803, 'OUT-20260512-003', 2.000000, 300.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921403, 921303, 921102, 920802, 'OUT-20260512-002', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(921404, 921304, 921101, 920801, 'OUT-20260512-001', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_item_id = VALUES(return_item_id), issue_batch_id = VALUES(issue_batch_id), stock_batch_id = VALUES(stock_batch_id), batch_no = VALUES(batch_no), return_qty = VALUES(return_qty), return_amount = VALUES(return_amount), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_inbound
(`id`, `inbound_no`, `order_id`, `warehouse_id`, `batch_no`, `inbound_time`, `produce_date`, `expire_date`, `status`, `inbound_qty`, `material_cost`, `process_fee`, `total_cost`, `unit_cost`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(922001, 'OI-20260512-001', 920701, @outsource_warehouse_id, 'OUT-IN-20260512-001', '2026-05-12 18:20:00', '2026-05-09', '2027-05-09', 20, 40.000000, 2600.000000, 800.000000, 3400.000000, 85.000000, '委外入库样本-控制板', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(922002, 'OI-20260512-002', 920702, @outsource_warehouse_id, 'OUT-IN-20260512-002', '2026-05-12 18:30:00', '2026-05-10', '2027-05-10', 20, 20.000000, 1300.000000, 600.000000, 1900.000000, 95.000000, '委外入库样本-小批量', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(922003, 'OI-20260512-003', 920703, @outsource_warehouse_id, 'OUT-IN-20260512-003', '2026-05-12 18:40:00', '2026-05-10', '2027-05-10', 20, 100.000000, 3650.000000, 720.000000, 3990.000000, 39.900000, '委外入库样本-打印验证', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(922004, 'OI-20260512-004', 920704, @outsource_warehouse_id, 'OUT-IN-20260512-004', '2026-05-12 18:50:00', '2026-05-11', '2027-05-11', 10, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, '委外入库样本-待完成', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), warehouse_id = VALUES(warehouse_id), batch_no = VALUES(batch_no), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), status = VALUES(status), inbound_qty = VALUES(inbound_qty), material_cost = VALUES(material_cost), process_fee = VALUES(process_fee), total_cost = VALUES(total_cost), unit_cost = VALUES(unit_cost), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_outsource_fee
(`id`, `fee_no`, `order_id`, `fee_time`, `status`, `fee_amount`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(922201, 'OF-20260512-001', 920701, '2026-05-12 17:30:00', 20, 800.000000, '委外加工费样本-总装', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(922202, 'OF-20260512-002', 920702, '2026-05-12 17:40:00', 20, 600.000000, '委外加工费样本-检测', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(922203, 'OF-20260512-003', 920703, '2026-05-12 17:50:00', 20, 720.000000, '委外加工费样本-打印验证', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE fee_no = VALUES(fee_no), order_id = VALUES(order_id), fee_time = VALUES(fee_time), status = VALUES(status), fee_amount = VALUES(fee_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
