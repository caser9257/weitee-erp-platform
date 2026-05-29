/*
 * 财务增强模块批量测试数据
 *
 * 适用范围（按当前代码与数据库实际落地范围）：
 * - 应付台账 / 收票状态 / 暂估入库
 * - 付款单 / 付款核销 / 核销回滚痕迹
 * - 预付款 / 预付核销 / 预付冲回痕迹
 * - 费用单（普通费用、研发费用化、研发资本化）
 * - 财务凭证（已过账、已冲销）
 *
 * 设计目标：
 * - 固定 ID，可重复执行
 * - 尽量复用当前库已存在主数据与模板配置
 * - 场景覆盖正常、草稿、已审核、已驳回、部分核销、已结清、已关闭、暂估已冲回、负数退货应付
 *
 * 说明：
 * - 本脚本不会删除现有数据
 * - 本脚本不会主动修正历史汇总字段，只插入当前测试所需的新业务样本
 * - 当前数据库实表未看到付款单作废附加字段（void_reason / void_time / void_by），因此本脚本不单独造“已作废付款单头字段样本”
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @tenant_id := COALESCE((
    SELECT tenant_id FROM erp_account WHERE id = 99501 LIMIT 1
), (
    SELECT id FROM system_tenant WHERE deleted = b'0' ORDER BY id LIMIT 1
), 1);

SET @finance_supervisor_id := COALESCE((
    SELECT id FROM system_users WHERE nickname = '财务主管' AND deleted = b'0' ORDER BY id LIMIT 1
), 940202);
SET @finance_clerk_id := COALESCE((
    SELECT id FROM system_users WHERE nickname = '财务经办' AND deleted = b'0' ORDER BY id LIMIT 1
), @finance_supervisor_id);
SET @finance_dept_id := COALESCE((
    SELECT id FROM system_dept WHERE name = '财务部' AND deleted = b'0' ORDER BY id LIMIT 1
), 920712);
SET @project_id := (
    SELECT id FROM erp_project WHERE deleted = b'0' ORDER BY id DESC LIMIT 1
);

SET @ledger_id := COALESCE((
    SELECT id FROM erp_finance_ledger WHERE id = 99601 AND deleted = b'0' LIMIT 1
), (
    SELECT id FROM erp_finance_ledger WHERE deleted = b'0' ORDER BY id LIMIT 1
), 99601);
SET @period_202605 := COALESCE((
    SELECT id FROM erp_finance_period
    WHERE ledger_id = @ledger_id AND period_code = '2026-05' AND deleted = b'0'
    ORDER BY id LIMIT 1
), (
    SELECT id FROM erp_finance_period
    WHERE ledger_id = @ledger_id AND deleted = b'0'
    ORDER BY period_sort DESC, id DESC LIMIT 1
), 99615);
SET @tpl_purchase_in := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 11 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99821);
SET @tpl_expense := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 40 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99822);
SET @tpl_rd_expense := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 41 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99824);
SET @tpl_rd_capital := COALESCE((
    SELECT id FROM erp_finance_voucher_template
    WHERE biz_type = 42 AND deleted = b'0'
    ORDER BY id LIMIT 1
), 99825);

/*
 * 一、采购链路基础单据
 */
INSERT INTO erp_purchase_order
(id, no, status, process_instance_id, supplier_id, project_id, business_type, source_project_id, account_id,
 order_time, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price,
 deposit_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, in_count, return_count,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981101, 'PO-FINENH-20260525-001', 20, NULL, 99301, @project_id, NULL, NULL, 99501,
 '2026-05-25 09:00:00', 40.000000, 3200.000000, 3200.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, 'FIN-ENH 正常采购订单', NULL, NULL, NULL, 40.000000, 4.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981102, 'PO-FINENH-20260525-002', 20, NULL, 99302, @project_id, NULL, NULL, 99501,
 '2026-05-25 10:00:00', 60.000000, 4800.000000, 4800.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, 'FIN-ENH 部分收票部分付款采购订单', NULL, NULL, NULL, 60.000000, 2.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981103, 'PO-FINENH-20260525-003', 20, NULL, 99305, @project_id, NULL, NULL, 99503,
 '2026-05-25 11:00:00', 400.000000, 2400.000000, 2400.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, 'FIN-ENH 已结清采购订单', NULL, NULL, NULL, 400.000000, 0.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
process_instance_id = VALUES(process_instance_id),
supplier_id = VALUES(supplier_id),
project_id = VALUES(project_id),
business_type = VALUES(business_type),
source_project_id = VALUES(source_project_id),
account_id = VALUES(account_id),
order_time = VALUES(order_time),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
deposit_price = VALUES(deposit_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
last_reject_reason = VALUES(last_reject_reason),
last_reject_time = VALUES(last_reject_time),
last_reject_user_id = VALUES(last_reject_user_id),
in_count = VALUES(in_count),
return_count = VALUES(return_count),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order_items
(id, order_id, product_id, project_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version,
 count, total_price, tax_percent, tax_price, remark, in_count, return_count,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981111, 981101, 99102, @project_id, 99004, 80.000000, 0.000000, NULL, NULL,
 40.000000, 3200.000000, 0.000000, 0.000000, 'FIN-ENH 正常采购订单明细', 40.000000, 4.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981112, 981102, 99102, @project_id, 99004, 80.000000, 0.000000, NULL, NULL,
 60.000000, 4800.000000, 0.000000, 0.000000, 'FIN-ENH 部分核销采购订单明细', 60.000000, 2.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981113, 981103, 99105, @project_id, 99003, 6.000000, 0.000000, NULL, NULL,
 400.000000, 2400.000000, 0.000000, 0.000000, 'FIN-ENH 已结清采购订单明细', 400.000000, 0.000000,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
order_id = VALUES(order_id),
product_id = VALUES(product_id),
project_id = VALUES(project_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
engineering_fee = VALUES(engineering_fee),
pricing_bom_id = VALUES(pricing_bom_id),
pricing_bom_version = VALUES(pricing_bom_version),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
remark = VALUES(remark),
in_count = VALUES(in_count),
return_count = VALUES(return_count),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in
(id, no, status, qa_status, process_instance_id, supplier_id, account_id, in_time, order_id, order_no, total_count,
 total_price, payment_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price,
 file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, qa_time, qa_user_id, qa_remark,
 qa_pass_count, qa_reject_count, stock_in_count, stock_in_status, stock_in_time, stock_in_user_id,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981201, 'PI-FINENH-20260525-001', 20, 50, NULL, 99301, 99501, '2026-05-25 13:00:00', 981101, 'PO-FINENH-20260525-001', 40.000000,
 3200.000000, 3200.000000, 3200.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 NULL, 'FIN-ENH 正常采购入库', NULL, NULL, NULL, '2026-05-25 13:20:00', @finance_clerk_id, '全部合格',
 40.000000, 0.000000, 40.000000, 20, '2026-05-25 13:30:00', @finance_clerk_id,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981202, 'PI-FINENH-20260525-002', 20, 50, NULL, 99302, 99501, '2026-05-25 14:00:00', 981102, 'PO-FINENH-20260525-002', 60.000000,
 4800.000000, 4800.000000, 4800.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 NULL, 'FIN-ENH 部分收票部分付款采购入库', NULL, NULL, NULL, '2026-05-25 14:20:00', @finance_clerk_id, '全部合格',
 60.000000, 0.000000, 60.000000, 20, '2026-05-25 14:30:00', @finance_clerk_id,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981203, 'PI-FINENH-20260525-003', 20, 50, NULL, 99305, 99503, '2026-05-25 15:00:00', 981103, 'PO-FINENH-20260525-003', 400.000000,
 2400.000000, 2400.000000, 2400.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 NULL, 'FIN-ENH 已结清采购入库', NULL, NULL, NULL, '2026-05-25 15:20:00', @finance_clerk_id, '全部合格',
 400.000000, 0.000000, 400.000000, 20, '2026-05-25 15:30:00', @finance_clerk_id,
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
qa_status = VALUES(qa_status),
process_instance_id = VALUES(process_instance_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
in_time = VALUES(in_time),
order_id = VALUES(order_id),
order_no = VALUES(order_no),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
payment_price = VALUES(payment_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
other_price = VALUES(other_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
last_reject_reason = VALUES(last_reject_reason),
last_reject_time = VALUES(last_reject_time),
last_reject_user_id = VALUES(last_reject_user_id),
qa_time = VALUES(qa_time),
qa_user_id = VALUES(qa_user_id),
qa_remark = VALUES(qa_remark),
qa_pass_count = VALUES(qa_pass_count),
qa_reject_count = VALUES(qa_reject_count),
stock_in_count = VALUES(stock_in_count),
stock_in_status = VALUES(stock_in_status),
stock_in_time = VALUES(stock_in_time),
stock_in_user_id = VALUES(stock_in_user_id),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_items
(id, in_id, order_item_id, warehouse_id, product_id, purchase_source_batch_id, product_unit_id, product_price, engineering_fee,
 pricing_bom_id, pricing_bom_version, count, total_price, tax_percent, tax_price, qa_pass_count, qa_reject_count,
 stock_in_count, remark, qa_remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981211, 981201, 981111, 99202, 99102, NULL, 99004, 80.000000, 0.000000,
 NULL, NULL, 40.000000, 3200.000000, 0.000000, 0.000000, 40.000000, 0.000000,
 40.000000, 'FIN-ENH 正常采购入库明细', '合格', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981212, 981202, 981112, 99202, 99102, NULL, 99004, 80.000000, 0.000000,
 NULL, NULL, 60.000000, 4800.000000, 0.000000, 0.000000, 60.000000, 0.000000,
 60.000000, 'FIN-ENH 部分核销采购入库明细', '合格', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981213, 981203, 981113, 99203, 99105, NULL, 99003, 6.000000, 0.000000,
 NULL, NULL, 400.000000, 2400.000000, 0.000000, 0.000000, 400.000000, 0.000000,
 400.000000, 'FIN-ENH 已结清采购入库明细', '合格', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
in_id = VALUES(in_id),
order_item_id = VALUES(order_item_id),
warehouse_id = VALUES(warehouse_id),
product_id = VALUES(product_id),
purchase_source_batch_id = VALUES(purchase_source_batch_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
engineering_fee = VALUES(engineering_fee),
pricing_bom_id = VALUES(pricing_bom_id),
pricing_bom_version = VALUES(pricing_bom_version),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
qa_pass_count = VALUES(qa_pass_count),
qa_reject_count = VALUES(qa_reject_count),
stock_in_count = VALUES(stock_in_count),
remark = VALUES(remark),
qa_remark = VALUES(qa_remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_return
(id, no, status, supplier_id, account_id, return_time, order_id, order_no, total_count, total_price,
 refund_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url,
 remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981301, 'PR-FINENH-20260525-001', 20, 99301, 99501, '2026-05-26 10:00:00', 981101, 'PO-FINENH-20260525-001', 4.000000, 320.000000,
 120.000000, 320.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL,
 'FIN-ENH 采购退货部分退款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981302, 'PR-FINENH-20260525-002', 20, 99302, 99501, '2026-05-26 11:00:00', 981102, 'PO-FINENH-20260525-002', 2.000000, 160.000000,
 0.000000, 160.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL,
 'FIN-ENH 采购退货关闭场景', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
return_time = VALUES(return_time),
order_id = VALUES(order_id),
order_no = VALUES(order_no),
total_count = VALUES(total_count),
total_price = VALUES(total_price),
refund_price = VALUES(refund_price),
total_product_price = VALUES(total_product_price),
total_tax_price = VALUES(total_tax_price),
discount_percent = VALUES(discount_percent),
discount_price = VALUES(discount_price),
other_price = VALUES(other_price),
file_url = VALUES(file_url),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_return_items
(id, return_id, order_item_id, warehouse_id, product_id, product_unit_id, product_price, count, total_price,
 tax_percent, tax_price, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981311, 981301, 981111, 99202, 99102, 99004, 80.000000, 4.000000, 320.000000,
 0.000000, 0.000000, 'FIN-ENH 采购退货明细-部分退款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981312, 981302, 981112, 99202, 99102, 99004, 80.000000, 2.000000, 160.000000,
 0.000000, 0.000000, 'FIN-ENH 采购退货明细-已关闭', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
return_id = VALUES(return_id),
order_item_id = VALUES(order_item_id),
warehouse_id = VALUES(warehouse_id),
product_id = VALUES(product_id),
product_unit_id = VALUES(product_unit_id),
product_price = VALUES(product_price),
count = VALUES(count),
total_price = VALUES(total_price),
tax_percent = VALUES(tax_percent),
tax_price = VALUES(tax_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 二、暂估入库
 */
INSERT INTO erp_ap_estimate
(id, estimate_no, estimate_month, source_biz_type, source_biz_id, source_biz_no, source_purchase_in_id, source_purchase_in_no,
 source_order_id, source_order_no, supplier_id, account_id, currency_code, source_amount, amount, status,
 confirm_user_id, confirm_time, reverse_user_id, reverse_time, reverse_type, reverse_source_id, reverse_source_no,
 reverse_remark, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981801, 'APE-FINENH-202605-001', '2026-05', 11, 981201, 'PI-FINENH-20260525-001', 981201, 'PI-FINENH-20260525-001',
 981101, 'PO-FINENH-20260525-001', 99301, 99501, 'CNY', 3200.000000, 3200.000000, 10,
 NULL, NULL, NULL, NULL, NULL, NULL, NULL,
 NULL, 'FIN-ENH 暂估待确认', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981802, 'APE-FINENH-202605-002', '2026-05', 11, 981202, 'PI-FINENH-20260525-002', 981202, 'PI-FINENH-20260525-002',
 981102, 'PO-FINENH-20260525-002', 99302, 99501, 'CNY', 4800.000000, 4800.000000, 20,
 @finance_supervisor_id, '2026-05-28 10:00:00', NULL, NULL, NULL, NULL, NULL,
 NULL, 'FIN-ENH 暂估已确认', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981803, 'APE-FINENH-202605-003', '2026-05', 11, 981203, 'PI-FINENH-20260525-003', 981203, 'PI-FINENH-20260525-003',
 981103, 'PO-FINENH-20260525-003', 99305, 99503, 'CNY', 2400.000000, 2400.000000, 30,
 @finance_supervisor_id, '2026-05-28 11:00:00', @finance_supervisor_id, '2026-05-29 09:30:00', NULL, 981403, 'AP-FINENH-202605-003',
 '收票后自动冲回', 'FIN-ENH 暂估已冲回', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
estimate_no = VALUES(estimate_no),
estimate_month = VALUES(estimate_month),
source_biz_type = VALUES(source_biz_type),
source_biz_id = VALUES(source_biz_id),
source_biz_no = VALUES(source_biz_no),
source_purchase_in_id = VALUES(source_purchase_in_id),
source_purchase_in_no = VALUES(source_purchase_in_no),
source_order_id = VALUES(source_order_id),
source_order_no = VALUES(source_order_no),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
currency_code = VALUES(currency_code),
source_amount = VALUES(source_amount),
amount = VALUES(amount),
status = VALUES(status),
confirm_user_id = VALUES(confirm_user_id),
confirm_time = VALUES(confirm_time),
reverse_user_id = VALUES(reverse_user_id),
reverse_time = VALUES(reverse_time),
reverse_type = VALUES(reverse_type),
reverse_source_id = VALUES(reverse_source_id),
reverse_source_no = VALUES(reverse_source_no),
reverse_remark = VALUES(reverse_remark),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_estimate_item
(id, estimate_id, source_purchase_in_item_id, source_purchase_in_id, source_purchase_in_no, source_order_id,
 source_order_item_id, source_order_no, product_id, warehouse_id, project_id, count, source_amount, tax_amount, amount,
 remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981811, 981801, 981211, 981201, 'PI-FINENH-20260525-001', 981101,
 981111, 'PO-FINENH-20260525-001', 99102, 99202, @project_id, 40.000000, 3200.000000, 0.000000, 3200.000000,
 'FIN-ENH 暂估待确认明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981812, 981802, 981212, 981202, 'PI-FINENH-20260525-002', 981102,
 981112, 'PO-FINENH-20260525-002', 99102, 99202, @project_id, 60.000000, 4800.000000, 0.000000, 4800.000000,
 'FIN-ENH 暂估已确认明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981813, 981803, 981213, 981203, 'PI-FINENH-20260525-003', 981103,
 981113, 'PO-FINENH-20260525-003', 99105, 99203, @project_id, 400.000000, 2400.000000, 0.000000, 2400.000000,
 'FIN-ENH 暂估已冲回明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
estimate_id = VALUES(estimate_id),
source_purchase_in_item_id = VALUES(source_purchase_in_item_id),
source_purchase_in_id = VALUES(source_purchase_in_id),
source_purchase_in_no = VALUES(source_purchase_in_no),
source_order_id = VALUES(source_order_id),
source_order_item_id = VALUES(source_order_item_id),
source_order_no = VALUES(source_order_no),
product_id = VALUES(product_id),
warehouse_id = VALUES(warehouse_id),
project_id = VALUES(project_id),
count = VALUES(count),
source_amount = VALUES(source_amount),
tax_amount = VALUES(tax_amount),
amount = VALUES(amount),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 三、应付台账与台账流水
 */
INSERT INTO erp_ap_statement
(id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount,
 paid_amount, remain_amount, currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status,
 remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981401, 'AP-FINENH-202605-001', 11, 981201, 'PI-FINENH-20260525-001', 981101, 'PO-FINENH-20260525-001', 99301, 99501, 3200.000000,
 0.000000, 3200.000000, 'CNY', '2026-05-25 13:30:00', '2026-06-24 13:30:00', 0, NULL, NULL, 10,
 'FIN-ENH 未付款未收票应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981402, 'AP-FINENH-202605-002', 11, 981202, 'PI-FINENH-20260525-002', 981102, 'PO-FINENH-20260525-002', 99302, 99501, 4800.000000,
 1800.000000, 3000.000000, 'CNY', '2026-05-25 14:30:00', '2026-06-24 14:30:00', 1, 'INV-FINENH-202605-002', 1500.000000, 20,
 'FIN-ENH 部分收票部分付款应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981403, 'AP-FINENH-202605-003', 11, 981203, 'PI-FINENH-20260525-003', 981103, 'PO-FINENH-20260525-003', 99305, 99503, 2400.000000,
 2400.000000, 0.000000, 'CNY', '2026-05-25 15:30:00', '2026-06-24 15:30:00', 2, 'INV-FINENH-202605-003', 2400.000000, 30,
 'FIN-ENH 已结清已收票应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981404, 'AP-FINENH-202605-004', 12, 981301, 'PR-FINENH-20260525-001', 981101, 'PO-FINENH-20260525-001', 99301, 99501, -320.000000,
 -120.000000, -200.000000, 'CNY', '2026-05-26 10:00:00', '2026-06-25 10:00:00', 0, NULL, NULL, 20,
 'FIN-ENH 采购退货部分退款应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981405, 'AP-FINENH-202605-005', 12, 981302, 'PR-FINENH-20260525-002', 981102, 'PO-FINENH-20260525-002', 99302, 99501, -160.000000,
 0.000000, -160.000000, 'CNY', '2026-05-26 11:00:00', '2026-06-25 11:00:00', 0, NULL, NULL, 40,
 'FIN-ENH 采购退货已关闭应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
statement_no = VALUES(statement_no),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
source_order_id = VALUES(source_order_id),
source_order_no = VALUES(source_order_no),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
amount = VALUES(amount),
paid_amount = VALUES(paid_amount),
remain_amount = VALUES(remain_amount),
currency_code = VALUES(currency_code),
biz_date = VALUES(biz_date),
due_date = VALUES(due_date),
invoice_status = VALUES(invoice_status),
invoice_no = VALUES(invoice_no),
invoice_amount = VALUES(invoice_amount),
status = VALUES(status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement_item
(id, statement_id, item_type, ref_type, ref_id, ref_no, amount, after_paid_amount, after_remain_amount, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981421, 981401, 10, 11, 981201, 'PI-FINENH-20260525-001', 3200.000000, 0.000000, 3200.000000, '采购入库生成应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981422, 981402, 10, 11, 981202, 'PI-FINENH-20260525-002', 4800.000000, 0.000000, 4800.000000, '采购入库生成应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981423, 981402, 60, 11, 981601, 'PRE-FINENH-202605-001', 400.000000, 400.000000, 4400.000000, '预付核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981424, 981402, 70, 11, 981601, 'PRE-FINENH-202605-001', -400.000000, 0.000000, 4800.000000, '预付核销回滚',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981425, 981402, 60, 11, 981601, 'PRE-FINENH-202605-001', 600.000000, 600.000000, 4200.000000, '预付重新核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981426, 981402, 20, 11, 981501, 'PAY-FINENH-202605-001', 300.000000, 900.000000, 3900.000000, '付款核销-后续回滚',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981427, 981402, 30, 11, 981505, 'PAY-FINENH-202605-005', -300.000000, 600.000000, 4200.000000, '付款核销回滚',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981428, 981402, 20, 11, 981501, 'PAY-FINENH-202605-001', 1200.000000, 1800.000000, 3000.000000, '付款核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981429, 981402, 50, 11, 981202, 'PI-FINENH-20260525-002', 1500.000000, 1800.000000, 3000.000000, '收票登记：INV-FINENH-202605-002',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981430, 981403, 10, 11, 981203, 'PI-FINENH-20260525-003', 2400.000000, 0.000000, 2400.000000, '采购入库生成应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981431, 981403, 60, 11, 981602, 'PRE-FINENH-202605-002', 900.000000, 900.000000, 1500.000000, '预付核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981432, 981403, 20, 11, 981503, 'PAY-FINENH-202605-003', 1500.000000, 2400.000000, 0.000000, '付款核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981433, 981403, 50, 11, 981203, 'PI-FINENH-20260525-003', 2400.000000, 2400.000000, 0.000000, '收票登记：INV-FINENH-202605-003',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981434, 981404, 10, 12, 981301, 'PR-FINENH-20260525-001', -320.000000, 0.000000, -320.000000, '采购退货生成负数应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981435, 981404, 20, 12, 981504, 'PAY-FINENH-202605-004', 120.000000, -120.000000, -200.000000, '退款核销',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981436, 981405, 10, 12, 981302, 'PR-FINENH-20260525-002', -160.000000, 0.000000, -160.000000, '采购退货生成负数应付',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981437, 981405, 40, 12, 981302, 'PR-FINENH-20260525-002', 0.000000, 0.000000, -160.000000, '台账关闭',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
statement_id = VALUES(statement_id),
item_type = VALUES(item_type),
ref_type = VALUES(ref_type),
ref_id = VALUES(ref_id),
ref_no = VALUES(ref_no),
amount = VALUES(amount),
after_paid_amount = VALUES(after_paid_amount),
after_remain_amount = VALUES(after_remain_amount),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 四、付款单、付款明细、付款核销
 */
INSERT INTO erp_finance_payment
(id, no, status, process_instance_id, payment_time, finance_user_id, supplier_id, account_id,
 total_price, discount_price, payment_price, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981501, 'PAY-FINENH-202605-001', 20, NULL, '2026-05-28 14:00:00', @finance_supervisor_id, 99302, 99501,
 1200.000000, 0.000000, 1200.000000, 'FIN-ENH 部分付款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981502, 'PAY-FINENH-202605-002', 10, NULL, '2026-05-28 15:00:00', @finance_clerk_id, 99301, 99501,
 800.000000, 0.000000, 800.000000, 'FIN-ENH 草稿付款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981503, 'PAY-FINENH-202605-003', 20, NULL, '2026-05-28 16:00:00', @finance_supervisor_id, 99305, 99503,
 1500.000000, 0.000000, 1500.000000, 'FIN-ENH 已结清付款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981504, 'PAY-FINENH-202605-004', 20, NULL, '2026-05-29 09:00:00', @finance_supervisor_id, 99301, 99501,
 120.000000, 0.000000, 120.000000, 'FIN-ENH 采购退货退款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981505, 'PAY-FINENH-202605-005', 20, NULL, '2026-05-29 10:00:00', @finance_supervisor_id, 99302, 99501,
 300.000000, 0.000000, 300.000000, 'FIN-ENH 已回滚付款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981506, 'PAY-FINENH-202605-006', 30, NULL, '2026-05-29 11:00:00', @finance_clerk_id, 99301, 99501,
 500.000000, 0.000000, 500.000000, 'FIN-ENH 已驳回付款单', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
process_instance_id = VALUES(process_instance_id),
payment_time = VALUES(payment_time),
finance_user_id = VALUES(finance_user_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
total_price = VALUES(total_price),
discount_price = VALUES(discount_price),
payment_price = VALUES(payment_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_item
(id, payment_id, ap_statement_id, biz_type, biz_id, biz_no, total_price, paid_price, payment_price,
 remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981511, 981501, 981402, 11, 981202, 'PI-FINENH-20260525-002', 4800.000000, 600.000000, 1200.000000,
 'FIN-ENH 部分付款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981512, 981502, 981401, 11, 981201, 'PI-FINENH-20260525-001', 3200.000000, 0.000000, 800.000000,
 'FIN-ENH 草稿付款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981513, 981503, 981403, 11, 981203, 'PI-FINENH-20260525-003', 2400.000000, 900.000000, 1500.000000,
 'FIN-ENH 已结清付款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981514, 981504, 981404, 12, 981301, 'PR-FINENH-20260525-001', -320.000000, 0.000000, 120.000000,
 'FIN-ENH 采购退货退款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981515, 981505, 981402, 11, 981202, 'PI-FINENH-20260525-002', 4800.000000, 600.000000, 300.000000,
 'FIN-ENH 回滚付款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981516, 981506, 981401, 11, 981201, 'PI-FINENH-20260525-001', 3200.000000, 0.000000, 500.000000,
 'FIN-ENH 驳回付款明细', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
payment_id = VALUES(payment_id),
ap_statement_id = VALUES(ap_statement_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
total_price = VALUES(total_price),
paid_price = VALUES(paid_price),
payment_price = VALUES(payment_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_allocate
(id, payment_id, payment_item_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no,
 status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981521, 981501, 981511, 981402, 1200.000000, 99302, 11, 981202, 'PI-FINENH-20260525-002',
 20, 'FIN-ENH 付款核销生效', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981522, 981503, 981513, 981403, 1500.000000, 99305, 11, 981203, 'PI-FINENH-20260525-003',
 20, 'FIN-ENH 付款核销生效', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981523, 981504, 981514, 981404, 120.000000, 99301, 12, 981301, 'PR-FINENH-20260525-001',
 20, 'FIN-ENH 采购退货退款核销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981524, 981505, 981515, 981402, 300.000000, 99302, 11, 981202, 'PI-FINENH-20260525-002',
 30, 'FIN-ENH 付款核销已回滚', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
payment_id = VALUES(payment_id),
payment_item_id = VALUES(payment_item_id),
ap_statement_id = VALUES(ap_statement_id),
allocate_amount = VALUES(allocate_amount),
supplier_id = VALUES(supplier_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
status = VALUES(status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 五、预付款、预付核销、预付冲回
 */
INSERT INTO erp_finance_prepayment
(id, no, status, prepayment_time, finance_user_id, supplier_id, account_id, prepayment_price,
 allocated_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981601, 'PRE-FINENH-202605-001', 20, '2026-05-27 10:00:00', @finance_supervisor_id, 99302, 99501, 1000.000000,
 600.000000, 400.000000, 'FIN-ENH 预付部分核销并发生冲回', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981602, 'PRE-FINENH-202605-002', 20, '2026-05-27 11:00:00', @finance_supervisor_id, 99305, 99503, 1500.000000,
 900.000000, 600.000000, 'FIN-ENH 预付已生效部分核销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981603, 'PRE-FINENH-202605-003', 10, '2026-05-27 12:00:00', @finance_clerk_id, 99301, 99501, 800.000000,
 0.000000, 800.000000, 'FIN-ENH 草稿预付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981604, 'PRE-FINENH-202605-004', 30, '2026-05-27 13:00:00', @finance_clerk_id, 99301, 99501, 500.000000,
 0.000000, 500.000000, 'FIN-ENH 驳回预付款', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
no = VALUES(no),
status = VALUES(status),
prepayment_time = VALUES(prepayment_time),
finance_user_id = VALUES(finance_user_id),
supplier_id = VALUES(supplier_id),
account_id = VALUES(account_id),
prepayment_price = VALUES(prepayment_price),
allocated_price = VALUES(allocated_price),
remain_price = VALUES(remain_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment_allocate
(id, prepayment_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no,
 status, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981611, 981601, 981402, 600.000000, 99302, 11, 981202, 'PI-FINENH-20260525-002',
 20, 'FIN-ENH 预付生效核销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981612, 981601, 981402, 400.000000, 99302, 11, 981202, 'PI-FINENH-20260525-002',
 30, 'FIN-ENH 预付核销已冲回', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981613, 981602, 981403, 900.000000, 99305, 11, 981203, 'PI-FINENH-20260525-003',
 20, 'FIN-ENH 预付部分核销', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
prepayment_id = VALUES(prepayment_id),
ap_statement_id = VALUES(ap_statement_id),
allocate_amount = VALUES(allocate_amount),
supplier_id = VALUES(supplier_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
status = VALUES(status),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 六、费用单与费用明细
 */
INSERT INTO erp_finance_expense
(id, no, status, process_instance_id, expense_time, expense_type, rd_accounting_type, research_category, dept_id,
 project_id, supplier_id, finance_user_id, account_id, expense_price, paid_price, remain_price, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981701, 'EXP-FINENH-202605-001', 20, NULL, '2026-05-27 14:00:00', 10, 10, 10, @finance_dept_id,
 @project_id, 99302, @finance_supervisor_id, 99501, 1500.000000, 1500.000000, 0.000000, 'FIN-ENH 研发费用化费用单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981702, 'EXP-FINENH-202605-002', 20, NULL, '2026-05-27 15:00:00', 10, 20, 20, @finance_dept_id,
 @project_id, 99302, @finance_supervisor_id, 99501, 3200.000000, 3200.000000, 0.000000, 'FIN-ENH 研发资本化费用单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981703, 'EXP-FINENH-202605-003', 20, NULL, '2026-05-27 16:00:00', 20, NULL, NULL, @finance_dept_id,
 @project_id, NULL, @finance_supervisor_id, 99503, 860.000000, 860.000000, 0.000000, 'FIN-ENH 普通费用单（已冲销凭证）',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981704, 'EXP-FINENH-202605-004', 10, NULL, '2026-05-27 17:00:00', 30, NULL, NULL, @finance_dept_id,
 @project_id, NULL, @finance_clerk_id, 99502, 600.000000, 0.000000, 600.000000, 'FIN-ENH 草稿费用单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981705, 'EXP-FINENH-202605-005', 30, NULL, '2026-05-27 18:00:00', 10, 10, 10, @finance_dept_id,
 @project_id, NULL, @finance_clerk_id, 99501, 900.000000, 0.000000, 900.000000, 'FIN-ENH 驳回研发费用单',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
status = VALUES(status),
process_instance_id = VALUES(process_instance_id),
expense_time = VALUES(expense_time),
expense_type = VALUES(expense_type),
rd_accounting_type = VALUES(rd_accounting_type),
research_category = VALUES(research_category),
dept_id = VALUES(dept_id),
project_id = VALUES(project_id),
supplier_id = VALUES(supplier_id),
finance_user_id = VALUES(finance_user_id),
account_id = VALUES(account_id),
expense_price = VALUES(expense_price),
paid_price = VALUES(paid_price),
remain_price = VALUES(remain_price),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_expense_item
(id, expense_id, item_name, amount, remark, asset_candidate_flag, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981711, 981701, '研发差旅费', 900.000000, 'FIN-ENH 研发费用化明细', b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981712, 981701, '研发试制辅料', 600.000000, 'FIN-ENH 研发费用化明细', b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981713, 981702, '试验设备采购', 3200.000000, 'FIN-ENH 研发资本化明细', b'1', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981714, 981703, '办公用品', 860.000000, 'FIN-ENH 普通费用明细', b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981715, 981704, '招待费', 600.000000, 'FIN-ENH 草稿费用明细', b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981716, 981705, '研发差旅费', 900.000000, 'FIN-ENH 驳回费用明细', b'0', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
expense_id = VALUES(expense_id),
item_name = VALUES(item_name),
amount = VALUES(amount),
remark = VALUES(remark),
asset_candidate_flag = VALUES(asset_candidate_flag),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

/*
 * 七、凭证与凭证明细
 */
INSERT INTO erp_finance_voucher
(id, voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status,
 total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time,
 reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id, reverse_remark, remark,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981901, 'VCH-FINENH-202605-001', @ledger_id, @period_202605, @tpl_purchase_in, 11, 981201, 'PI-FINENH-20260525-001', '2026-05-28 09:00:00', 30,
 3200.000000, 3200.000000, @finance_supervisor_id, '2026-05-28 09:10:00', @finance_supervisor_id, '2026-05-28 09:20:00',
 NULL, NULL, NULL, NULL, NULL, 'FIN-ENH 采购入库已过账凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981902, 'VCH-FINENH-202605-002', @ledger_id, @period_202605, @tpl_rd_expense, 41, 981701, 'EXP-FINENH-202605-001', '2026-05-28 10:00:00', 30,
 1500.000000, 1500.000000, @finance_supervisor_id, '2026-05-28 10:10:00', @finance_supervisor_id, '2026-05-28 10:20:00',
 NULL, NULL, NULL, NULL, NULL, 'FIN-ENH 研发费用化凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981903, 'VCH-FINENH-202605-003', @ledger_id, @period_202605, @tpl_rd_capital, 42, 981702, 'EXP-FINENH-202605-002', '2026-05-28 11:00:00', 30,
 3200.000000, 3200.000000, @finance_supervisor_id, '2026-05-28 11:10:00', @finance_supervisor_id, '2026-05-28 11:20:00',
 NULL, NULL, NULL, NULL, NULL, 'FIN-ENH 研发资本化凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981904, 'VCH-FINENH-202605-004', @ledger_id, @period_202605, @tpl_expense, 40, 981703, 'EXP-FINENH-202605-003', '2026-05-28 12:00:00', 40,
 860.000000, 860.000000, @finance_supervisor_id, '2026-05-28 12:10:00', @finance_supervisor_id, '2026-05-28 12:20:00',
 @finance_supervisor_id, '2026-05-29 13:10:00', 981905, NULL, 'FIN-ENH 费用凭证冲销', 'FIN-ENH 已冲销原凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981905, 'VCH-FINENH-202605-005', @ledger_id, @period_202605, @tpl_expense, 40, 981703, 'EXP-FINENH-202605-003-REV', '2026-05-29 13:00:00', 30,
 860.000000, 860.000000, @finance_supervisor_id, '2026-05-29 13:05:00', @finance_supervisor_id, '2026-05-29 13:15:00',
 NULL, NULL, NULL, 981904, NULL, 'FIN-ENH 冲销凭证',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
voucher_no = VALUES(voucher_no),
ledger_id = VALUES(ledger_id),
period_id = VALUES(period_id),
template_id = VALUES(template_id),
biz_type = VALUES(biz_type),
biz_id = VALUES(biz_id),
biz_no = VALUES(biz_no),
voucher_time = VALUES(voucher_time),
status = VALUES(status),
total_debit_amount = VALUES(total_debit_amount),
total_credit_amount = VALUES(total_credit_amount),
approve_user_id = VALUES(approve_user_id),
approve_time = VALUES(approve_time),
post_user_id = VALUES(post_user_id),
post_time = VALUES(post_time),
reverse_user_id = VALUES(reverse_user_id),
reverse_time = VALUES(reverse_time),
reverse_voucher_id = VALUES(reverse_voucher_id),
reverse_from_voucher_id = VALUES(reverse_from_voucher_id),
reverse_remark = VALUES(reverse_remark),
remark = VALUES(remark),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_entry
(id, voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
 creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(981911, 981901, 1, '采购入库确认', '1405', '库存商品', 3200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981912, 981901, 2, '采购入库确认', '2202', '应付账款', 0.000000, 3200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981913, 981902, 1, '研发费用化', '6601', '销售费用', 1500.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981914, 981902, 2, '研发费用化', '1002', '银行存款', 0.000000, 1500.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981915, 981903, 1, '研发资本化', '1405', '库存商品', 3200.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981916, 981903, 2, '研发资本化', '1002', '银行存款', 0.000000, 3200.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981917, 981904, 1, '普通费用报销', '6601', '销售费用', 860.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981918, 981904, 2, '普通费用报销', '1002', '银行存款', 0.000000, 860.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981919, 981905, 1, '冲销普通费用报销', '1002', '银行存款', 860.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(981920, 981905, 2, '冲销普通费用报销', '6601', '销售费用', 0.000000, 860.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
voucher_id = VALUES(voucher_id),
entry_no = VALUES(entry_no),
summary = VALUES(summary),
subject_code = VALUES(subject_code),
subject_name = VALUES(subject_name),
debit_amount = VALUES(debit_amount),
credit_amount = VALUES(credit_amount),
updater = VALUES(updater),
update_time = NOW(),
deleted = VALUES(deleted),
tenant_id = VALUES(tenant_id);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
