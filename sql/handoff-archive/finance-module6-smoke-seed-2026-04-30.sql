-- Finance module 6 HTTP smoke seed data.
-- Scope:
--   - tenant: 微泰科技 when present, otherwise tenant_id = 1
--   - ledger no: SMOKE-M6-20260430
--   - period: 2026-04
-- This script is idempotent. It only upserts the smoke ledger and data below.

START TRANSACTION;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE name = '微泰科技'
      AND deleted = b'0'
    LIMIT 1
), 1);

INSERT INTO erp_finance_ledger (
    no, name, status, sort, default_status, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'SMOKE-M6-20260430', '模块6联调测试账簿', 0, 999, b'1', '模块6 HTTP smoke 专用测试账簿',
    'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
) ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status),
    sort = VALUES(sort),
    default_status = VALUES(default_status),
    remark = VALUES(remark),
    updater = VALUES(updater),
    update_time = NOW();

SET @ledger_id := (
    SELECT id
    FROM erp_finance_ledger
    WHERE tenant_id = @tenant_id
      AND no = 'SMOKE-M6-20260430'
      AND deleted = b'0'
    LIMIT 1
);

INSERT INTO erp_finance_period (
    ledger_id, period_code, period_year, period_month, period_sort,
    start_date, end_date, status, close_time, close_user_id, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    @ledger_id, '2026-04', 2026, 4, 202604,
    '2026-04-01', '2026-04-30', 10, NULL, NULL, '模块6 HTTP smoke 专用打开期间',
    'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
) ON DUPLICATE KEY UPDATE
    period_code = VALUES(period_code),
    period_year = VALUES(period_year),
    period_month = VALUES(period_month),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    status = VALUES(status),
    close_time = NULL,
    close_user_id = NULL,
    remark = VALUES(remark),
    updater = VALUES(updater),
    update_time = NOW();

SET @period_id := (
    SELECT id
    FROM erp_finance_period
    WHERE tenant_id = @tenant_id
      AND ledger_id = @ledger_id
      AND period_sort = 202604
      AND deleted = b'0'
    LIMIT 1
);

INSERT INTO erp_finance_subject (
    ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
    (@ledger_id, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '2202', '应付账款', 20, 20, b'1', 0, 30, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '4001', '实收资本', 30, 20, b'1', 0, 40, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '4103', '本年利润', 30, 20, b'1', 0, 50, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 60, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 70, '模块6 smoke 科目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    subject_type = VALUES(subject_type),
    balance_direction = VALUES(balance_direction),
    leaf = VALUES(leaf),
    status = VALUES(status),
    sort = VALUES(sort),
    remark = VALUES(remark),
    updater = VALUES(updater),
    update_time = NOW();

INSERT INTO erp_finance_report_item (
    ledger_id, report_type, item_category, item_code, item_name, status, sort, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
    (@ledger_id, 10, 10, 'BS-CASH', '货币资金', 0, 10, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 10, 20, 'BS-AP', '应付账款', 0, 20, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 10, 30, 'BS-CAPITAL', '实收资本', 0, 30, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 10, 30, 'BS-PROFIT', '本年利润', 0, 40, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 20, 40, 'IS-REVENUE', '营业收入', 0, 10, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 20, 50, 'IS-COST', '营业成本', 0, 20, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 30, 60, 'CF-INFLOW', '现金流入', 0, 10, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, 30, 70, 'CF-OUTFLOW', '现金流出', 0, 20, '模块6 smoke 报表项目', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    item_category = VALUES(item_category),
    item_name = VALUES(item_name),
    status = VALUES(status),
    sort = VALUES(sort),
    remark = VALUES(remark),
    updater = VALUES(updater),
    update_time = NOW();

INSERT INTO erp_finance_report_item_subject (
    item_id, subject_code, amount_rule, amount_sign,
    creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT item.id, mapping.subject_code, mapping.amount_rule, mapping.amount_sign,
       'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
FROM erp_finance_report_item item
JOIN (
    SELECT 10 AS report_type, 'BS-CASH' AS item_code, '1001' AS subject_code, 50 AS amount_rule, 1 AS amount_sign
    UNION ALL SELECT 10, 'BS-CASH', '1002', 50, 1
    UNION ALL SELECT 10, 'BS-AP', '2202', 60, 1
    UNION ALL SELECT 10, 'BS-CAPITAL', '4001', 60, 1
    UNION ALL SELECT 10, 'BS-PROFIT', '4103', 60, 1
    UNION ALL SELECT 20, 'IS-REVENUE', '6001', 40, 1
    UNION ALL SELECT 20, 'IS-COST', '6401', 30, 1
    UNION ALL SELECT 30, 'CF-INFLOW', '1001', 30, 1
    UNION ALL SELECT 30, 'CF-INFLOW', '1002', 30, 1
    UNION ALL SELECT 30, 'CF-OUTFLOW', '1001', 40, 1
    UNION ALL SELECT 30, 'CF-OUTFLOW', '1002', 40, 1
) mapping ON mapping.report_type = item.report_type
    AND mapping.item_code = item.item_code
WHERE item.tenant_id = @tenant_id
  AND item.ledger_id = @ledger_id
  AND item.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM erp_finance_report_item_subject existed
      WHERE existed.tenant_id = @tenant_id
        AND existed.item_id = item.id
        AND existed.subject_code = mapping.subject_code
        AND existed.amount_rule = mapping.amount_rule
        AND existed.deleted = b'0'
  );

INSERT INTO erp_finance_subject_balance (
    ledger_id, period_id, period_sort, subject_code, subject_name,
    opening_debit_amount, opening_credit_amount, current_debit_amount, current_credit_amount,
    ending_debit_amount, ending_credit_amount,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
    (@ledger_id, @period_id, 202604, '1001', '库存现金', 0, 0, 6800.000000, 0.000000, 6800.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '1002', '银行存款', 0, 0, 20000.000000, 5000.000000, 15000.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '2202', '应付账款', 0, 0, 0.000000, 8000.000000, 0.000000, 8000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '4001', '实收资本', 0, 0, 0.000000, 12000.000000, 0.000000, 12000.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '4103', '本年利润', 0, 0, 0.000000, 0.000000, 0.000000, 1800.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '6001', '主营业务收入', 0, 0, 0.000000, 3000.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id),
    (@ledger_id, @period_id, 202604, '6401', '主营业务成本', 0, 0, 1200.000000, 0.000000, 0.000000, 0.000000, 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name),
    opening_debit_amount = VALUES(opening_debit_amount),
    opening_credit_amount = VALUES(opening_credit_amount),
    current_debit_amount = VALUES(current_debit_amount),
    current_credit_amount = VALUES(current_credit_amount),
    ending_debit_amount = VALUES(ending_debit_amount),
    ending_credit_amount = VALUES(ending_credit_amount),
    updater = VALUES(updater),
    update_time = NOW();

SELECT @tenant_id AS tenant_id, @ledger_id AS ledger_id, @period_id AS period_id;

SELECT
    COUNT(*) AS subject_count
FROM erp_finance_subject
WHERE tenant_id = @tenant_id
  AND ledger_id = @ledger_id
  AND deleted = b'0';

SELECT
    COUNT(*) AS report_item_count
FROM erp_finance_report_item
WHERE tenant_id = @tenant_id
  AND ledger_id = @ledger_id
  AND deleted = b'0';

SELECT
    COUNT(*) AS subject_balance_count
FROM erp_finance_subject_balance
WHERE tenant_id = @tenant_id
  AND ledger_id = @ledger_id
  AND period_id = @period_id
  AND deleted = b'0';

COMMIT;
