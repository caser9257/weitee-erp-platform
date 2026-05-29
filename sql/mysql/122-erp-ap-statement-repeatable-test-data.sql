/*
 * 应付台账可重复执行测试数据
 * 目标：
 * 1. 直接插入 erp_ap_statement，验证台账列表、详情、筛选与状态展示
 * 2. 脚本可重复执行，固定 statement_no 会先清理再重建
 * 3. 不依赖委外入库自动生成台账的方法
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

-- =========================
-- 一、基础清理
-- =========================
DELETE FROM erp_ap_statement
WHERE tenant_id = @tenant_id
  AND statement_no IN (
      'AP-FINENH-202605-001',
      'AP-FINENH-202605-002',
      'AP-FINENH-202605-003'
  );

-- =========================
-- 二、台账数据
-- =========================
INSERT INTO erp_ap_statement
(id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount,
 paid_amount, remain_amount, currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status,
 remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
(122001, 'AP-FINENH-202605-001', 11, 981201, 'PI-FINENH-20260525-001', 981101, 'PO-FINENH-20260525-001', 99301, 99501, 3200.000000,
 0.000000, 3200.000000, 'CNY', '2026-05-25 13:30:00', '2026-06-24 13:30:00', 0, NULL, NULL, 10,
 'FIN-ENH 未付款未收票应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(122002, 'AP-FINENH-202605-002', 11, 981202, 'PI-FINENH-20260525-002', 981102, 'PO-FINENH-20260525-002', 99302, 99501, 4800.000000,
 1800.000000, 3000.000000, 'CNY', '2026-05-25 14:30:00', '2026-06-24 14:30:00', 1, 'INV-FINENH-202605-002', 1500.000000, 20,
 'FIN-ENH 部分收票部分付款应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
(122003, 'AP-FINENH-202605-003', 11, 981203, 'PI-FINENH-20260525-003', 981103, 'PO-FINENH-20260525-003', 99305, 99503, 2400.000000,
 2400.000000, 0.000000, 'CNY', '2026-05-25 15:30:00', '2026-06-24 15:30:00', 2, 'INV-FINENH-202605-003', 2400.000000, 30,
 'FIN-ENH 已结清已收票应付台账', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
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

-- =========================
-- 三、执行结果
-- =========================
SELECT id, statement_no, biz_type, biz_no, supplier_id, account_id, amount, paid_amount, remain_amount, invoice_status, status, remark
FROM erp_ap_statement
WHERE tenant_id = @tenant_id
  AND statement_no IN (
      'AP-FINENH-202605-001',
      'AP-FINENH-202605-002',
      'AP-FINENH-202605-003'
  )
ORDER BY id;

SET FOREIGN_KEY_CHECKS = 1;
