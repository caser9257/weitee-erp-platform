-- FIN-07: voucher-backed general-ledger and report acceptance fixture.
-- The script is idempotent and only changes ledger FIN07-ACCEPT-202607.

START TRANSACTION;

INSERT INTO erp_finance_ledger (
    no, name, status, sort, default_status, remark,
    creator, create_time, updater, update_time, deleted
) VALUES (
    'FIN07-ACCEPT-202607', 'FIN-07 验收账簿', 0, 9907, b'0', 'FIN-07 受控验收数据',
    'acceptance', NOW(), 'acceptance', NOW(), b'0'
) ON DUPLICATE KEY UPDATE
    name = VALUES(name), status = VALUES(status), sort = VALUES(sort),
    remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = b'0';

SET @ledger_id := (
    SELECT id FROM erp_finance_ledger
    WHERE no = 'FIN07-ACCEPT-202607' AND deleted = b'0' LIMIT 1
);

INSERT INTO erp_finance_period (
    ledger_id, period_code, period_year, period_month, period_sort,
    start_date, end_date, status, remark,
    creator, create_time, updater, update_time, deleted
) VALUES (
    @ledger_id, '2026-07', 2026, 7, 202607,
    '2026-07-01', '2026-07-31', 10, 'FIN-07 受控验收期间',
    'acceptance', NOW(), 'acceptance', NOW(), b'0'
) ON DUPLICATE KEY UPDATE
    period_code = VALUES(period_code), period_year = VALUES(period_year), period_month = VALUES(period_month),
    start_date = VALUES(start_date), end_date = VALUES(end_date), status = 10,
    remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = b'0';

SET @period_id := (
    SELECT id FROM erp_finance_period
    WHERE ledger_id = @ledger_id AND period_sort = 202607 AND deleted = b'0' LIMIT 1
);

INSERT INTO erp_finance_subject (
    ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark,
    creator, create_time, updater, update_time, deleted
) VALUES
    (@ledger_id, NULL, '1002', '银行存款', 10, 10, b'1', 0, 10, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, NULL, '2202', '应付账款', 20, 20, b'1', 0, 20, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, NULL, '4001', '实收资本', 30, 20, b'1', 0, 30, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, NULL, '4103', '本年利润', 30, 20, b'1', 0, 40, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 50, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 60, 'FIN-07 验收科目', 'acceptance', NOW(), 'acceptance', NOW(), b'0')
ON DUPLICATE KEY UPDATE
    subject_name = VALUES(subject_name), subject_type = VALUES(subject_type), balance_direction = VALUES(balance_direction),
    leaf = VALUES(leaf), status = VALUES(status), sort = VALUES(sort), remark = VALUES(remark),
    updater = VALUES(updater), update_time = NOW(), deleted = b'0';

INSERT INTO erp_finance_voucher_template (
    ledger_id, biz_type, name, status, auto_generate, default_summary, remark,
    creator, create_time, updater, update_time, deleted
) VALUES (
    @ledger_id, 40, 'FIN-07 验收手工凭证模板', 0, b'0', 'FIN-07 验收凭证', '仅用于总账与报表验收',
    'acceptance', NOW(), 'acceptance', NOW(), b'0'
);

SET @template_id := (
    SELECT id FROM erp_finance_voucher_template
    WHERE ledger_id = @ledger_id AND biz_type = 40 AND name = 'FIN-07 验收手工凭证模板' AND deleted = b'0'
    ORDER BY id DESC LIMIT 1
);

DELETE entry_row
FROM erp_finance_voucher_entry entry_row
JOIN erp_finance_voucher voucher ON voucher.id = entry_row.voucher_id
WHERE voucher.ledger_id = @ledger_id AND voucher.voucher_no LIKE 'FIN07-ACC-%';

DELETE FROM erp_finance_voucher
WHERE ledger_id = @ledger_id AND voucher_no LIKE 'FIN07-ACC-%';

DELETE FROM erp_finance_subject_balance WHERE ledger_id = @ledger_id;

INSERT INTO erp_finance_voucher (
    voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status,
    total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time, remark,
    creator, create_time, updater, update_time, deleted
) VALUES
    ('FIN07-ACC-001', @ledger_id, @period_id, @template_id, 40, 970701, 'FIN07-CAPITAL', '2026-07-01 09:00:00', 30,
     100000.000000, 100000.000000, 145, '2026-07-01 09:01:00', 145, '2026-07-01 09:02:00', '实收资本注入',
     'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    ('FIN07-ACC-002', @ledger_id, @period_id, @template_id, 40, 970702, 'FIN07-SALE', '2026-07-10 10:00:00', 30,
     30000.000000, 30000.000000, 145, '2026-07-10 10:01:00', 145, '2026-07-10 10:02:00', '销售确认',
     'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    ('FIN07-ACC-003', @ledger_id, @period_id, @template_id, 40, 970703, 'FIN07-COST', '2026-07-10 10:05:00', 30,
     18000.000000, 18000.000000, 145, '2026-07-10 10:06:00', 145, '2026-07-10 10:07:00', '销售成本结转',
     'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    ('FIN07-ACC-004', @ledger_id, @period_id, @template_id, 40, 970704, 'FIN07-PROFIT', '2026-07-31 23:00:00', 30,
     48000.000000, 48000.000000, 145, '2026-07-31 23:01:00', 145, '2026-07-31 23:02:00', '损益结转',
     'acceptance', NOW(), 'acceptance', NOW(), b'0');

INSERT INTO erp_finance_voucher_entry (
    voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount,
    creator, create_time, updater, update_time, deleted
)
SELECT voucher.id, entry_row.entry_no, entry_row.summary, entry_row.subject_code, entry_row.subject_name,
       entry_row.debit_amount, entry_row.credit_amount, 'acceptance', NOW(), 'acceptance', NOW(), b'0'
FROM erp_finance_voucher voucher
JOIN (
    SELECT 'FIN07-ACC-001' AS voucher_no, 1 AS entry_no, '实收资本注入' AS summary, '1002' AS subject_code, '银行存款' AS subject_name, 100000.000000 AS debit_amount, 0.000000 AS credit_amount
    UNION ALL SELECT 'FIN07-ACC-001', 2, '实收资本注入', '4001', '实收资本', 0.000000, 100000.000000
    UNION ALL SELECT 'FIN07-ACC-002', 1, '销售确认', '1002', '银行存款', 30000.000000, 0.000000
    UNION ALL SELECT 'FIN07-ACC-002', 2, '销售确认', '6001', '主营业务收入', 0.000000, 30000.000000
    UNION ALL SELECT 'FIN07-ACC-003', 1, '销售成本结转', '6401', '主营业务成本', 18000.000000, 0.000000
    UNION ALL SELECT 'FIN07-ACC-003', 2, '销售成本结转', '2202', '应付账款', 0.000000, 18000.000000
    UNION ALL SELECT 'FIN07-ACC-004', 1, '损益结转', '6001', '主营业务收入', 30000.000000, 0.000000
    UNION ALL SELECT 'FIN07-ACC-004', 2, '损益结转', '4103', '本年利润', 0.000000, 30000.000000
    UNION ALL SELECT 'FIN07-ACC-004', 3, '损益结转', '4103', '本年利润', 18000.000000, 0.000000
    UNION ALL SELECT 'FIN07-ACC-004', 4, '损益结转', '6401', '主营业务成本', 0.000000, 18000.000000
) entry_row ON entry_row.voucher_no = voucher.voucher_no
WHERE voucher.ledger_id = @ledger_id AND voucher.voucher_no LIKE 'FIN07-ACC-%';

INSERT INTO erp_finance_report_item (
    ledger_id, report_type, item_category, item_code, item_name, status, sort, remark,
    creator, create_time, updater, update_time, deleted
) VALUES
    (@ledger_id, 10, 10, 'FIN07-BS-CASH', '货币资金', 0, 10, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, 10, 20, 'FIN07-BS-AP', '应付账款', 0, 20, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, 10, 30, 'FIN07-BS-CAPITAL', '实收资本', 0, 30, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, 10, 30, 'FIN07-BS-PROFIT', '本年利润', 0, 40, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, 20, 40, 'FIN07-IS-REVENUE', '营业收入', 0, 10, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0'),
    (@ledger_id, 20, 50, 'FIN07-IS-COST', '营业成本', 0, 20, 'FIN-07 验收报表项目', 'acceptance', NOW(), 'acceptance', NOW(), b'0')
ON DUPLICATE KEY UPDATE
    item_category = VALUES(item_category), item_name = VALUES(item_name), status = VALUES(status), sort = VALUES(sort),
    remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = b'0';

DELETE mapping
FROM erp_finance_report_item_subject mapping
JOIN erp_finance_report_item item ON item.id = mapping.item_id
WHERE item.ledger_id = @ledger_id AND item.item_code LIKE 'FIN07-%';

INSERT INTO erp_finance_report_item_subject (
    item_id, subject_code, amount_rule, amount_sign,
    creator, create_time, updater, update_time, deleted
)
SELECT item.id, mapping.subject_code, mapping.amount_rule, 1,
       'acceptance', NOW(), 'acceptance', NOW(), b'0'
FROM erp_finance_report_item item
JOIN (
    SELECT 'FIN07-BS-CASH' AS item_code, '1002' AS subject_code, 50 AS amount_rule
    UNION ALL SELECT 'FIN07-BS-AP', '2202', 60
    UNION ALL SELECT 'FIN07-BS-CAPITAL', '4001', 60
    UNION ALL SELECT 'FIN07-BS-PROFIT', '4103', 60
    UNION ALL SELECT 'FIN07-IS-REVENUE', '6001', 40
    UNION ALL SELECT 'FIN07-IS-COST', '6401', 30
) mapping ON mapping.item_code = item.item_code
WHERE item.ledger_id = @ledger_id AND item.deleted = b'0';

COMMIT;

SELECT @ledger_id AS ledger_id, @period_id AS period_id;
