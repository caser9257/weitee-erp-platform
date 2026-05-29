/*
 * ERP 财务报表测试数据验证脚本
 *
 * 用途：
 * - 验证 129-erp-finance-report-test-data.sql 是否导入成功
 * - 不依赖前端下拉或页面展示，直接在数据库侧确认
 *
 * 预期：
 * - 账簿存在：财务报表测试账簿
 * - 期间存在：2026-05
 * - 科目余额 11 条
 * - 试算平衡：本期借贷相等，期末借贷相等
 * - 资产负债表：资产 = 171000.000000，负债 + 权益 = 171000.000000
 * - 利润表：收入 = 88000.000000，成本费用 = 64000.000000，利润 = 24000.000000
 * - 现金流量表：流入 = 90000.000000，流出 = 47000.000000，净额 = 43000.000000
 */

SET NAMES utf8mb4;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @ledger_id := 129001;
SET @period_id := 129011;

SELECT
    'ledger' AS check_name,
    l.id,
    l.no,
    l.name,
    l.status,
    l.deleted,
    l.tenant_id
FROM erp_finance_ledger l
WHERE l.id = @ledger_id
  AND l.tenant_id = @tenant_id;

SELECT
    'period' AS check_name,
    p.id,
    p.ledger_id,
    p.period_code,
    p.period_year,
    p.period_month,
    p.period_sort,
    p.status,
    p.deleted,
    p.tenant_id
FROM erp_finance_period p
WHERE p.id = @period_id
  AND p.tenant_id = @tenant_id;

SELECT
    'subject_balance_count' AS check_name,
    COUNT(*) AS actual_count,
    11 AS expected_count
FROM erp_finance_subject_balance sb
WHERE sb.ledger_id = @ledger_id
  AND sb.period_id = @period_id
  AND sb.deleted = b'0'
  AND sb.tenant_id = @tenant_id;

SELECT
    'trial_balance_totals' AS check_name,
    SUM(sb.current_debit_amount) AS total_current_debit_amount,
    SUM(sb.current_credit_amount) AS total_current_credit_amount,
    SUM(sb.ending_debit_amount) AS total_ending_debit_amount,
    SUM(sb.ending_credit_amount) AS total_ending_credit_amount,
    CASE WHEN SUM(sb.current_debit_amount) = SUM(sb.current_credit_amount) THEN 'PASS' ELSE 'FAIL' END AS current_balanced,
    CASE WHEN SUM(sb.ending_debit_amount) = SUM(sb.ending_credit_amount) THEN 'PASS' ELSE 'FAIL' END AS ending_balanced
FROM erp_finance_subject_balance sb
WHERE sb.ledger_id = @ledger_id
  AND sb.period_id = @period_id
  AND sb.deleted = b'0'
  AND sb.tenant_id = @tenant_id;

SELECT
    'trial_balance_rows' AS check_name,
    sb.subject_code,
    sb.subject_name,
    sb.current_debit_amount,
    sb.current_credit_amount,
    sb.ending_debit_amount,
    sb.ending_credit_amount
FROM erp_finance_subject_balance sb
WHERE sb.ledger_id = @ledger_id
  AND sb.period_id = @period_id
  AND sb.deleted = b'0'
  AND sb.tenant_id = @tenant_id
ORDER BY sb.subject_code;

SELECT
    'report_item_count' AS check_name,
    COUNT(*) AS actual_count,
    12 AS expected_count
FROM erp_finance_report_item ri
WHERE ri.ledger_id = @ledger_id
  AND ri.status = 0
  AND ri.deleted = b'0'
  AND ri.tenant_id = @tenant_id;

SELECT
    'report_mapping_count' AS check_name,
    COUNT(*) AS actual_count,
    15 AS expected_count
FROM erp_finance_report_item_subject ris
INNER JOIN erp_finance_report_item ri ON ri.id = ris.item_id
WHERE ri.ledger_id = @ledger_id
  AND ri.deleted = b'0'
  AND ris.deleted = b'0'
  AND ri.tenant_id = @tenant_id
  AND ris.tenant_id = @tenant_id;

WITH report_amounts AS (
    SELECT
        ri.report_type,
        ri.item_category,
        ri.item_code,
        ri.item_name,
        COALESCE(SUM(
            CASE ris.amount_rule
                WHEN 10 THEN sb.opening_debit_amount
                WHEN 20 THEN sb.opening_credit_amount
                WHEN 30 THEN sb.current_debit_amount
                WHEN 40 THEN sb.current_credit_amount
                WHEN 50 THEN sb.ending_debit_amount
                WHEN 60 THEN sb.ending_credit_amount
                WHEN 70 THEN sb.current_debit_amount - sb.current_credit_amount
                WHEN 80 THEN sb.current_credit_amount - sb.current_debit_amount
                WHEN 90 THEN sb.ending_debit_amount - sb.ending_credit_amount
                WHEN 100 THEN sb.ending_credit_amount - sb.ending_debit_amount
                ELSE 0
            END * ris.amount_sign
        ), 0) AS amount
    FROM erp_finance_report_item ri
    LEFT JOIN erp_finance_report_item_subject ris
        ON ris.item_id = ri.id
       AND ris.deleted = b'0'
       AND ris.tenant_id = ri.tenant_id
    LEFT JOIN erp_finance_subject_balance sb
        ON sb.ledger_id = ri.ledger_id
       AND sb.period_id = @period_id
       AND sb.subject_code = ris.subject_code
       AND sb.deleted = b'0'
       AND sb.tenant_id = ri.tenant_id
    WHERE ri.ledger_id = @ledger_id
      AND ri.status = 0
      AND ri.deleted = b'0'
      AND ri.tenant_id = @tenant_id
    GROUP BY ri.report_type, ri.item_category, ri.item_code, ri.item_name
)
SELECT
    'balance_sheet' AS check_name,
    SUM(CASE WHEN item_category = 10 THEN amount ELSE 0 END) AS asset_amount,
    SUM(CASE WHEN item_category = 20 THEN amount ELSE 0 END) AS liability_amount,
    SUM(CASE WHEN item_category = 30 THEN amount ELSE 0 END) AS equity_amount,
    SUM(CASE WHEN item_category = 10 THEN amount ELSE 0 END) AS expected_asset_amount,
    SUM(CASE WHEN item_category IN (20, 30) THEN amount ELSE 0 END) AS expected_liability_equity_amount,
    CASE
        WHEN SUM(CASE WHEN item_category = 10 THEN amount ELSE 0 END)
           = SUM(CASE WHEN item_category IN (20, 30) THEN amount ELSE 0 END)
        THEN 'PASS' ELSE 'FAIL'
    END AS balanced
FROM report_amounts
WHERE report_type = 10;

WITH report_amounts AS (
    SELECT
        ri.report_type,
        ri.item_category,
        ri.item_code,
        ri.item_name,
        COALESCE(SUM(
            CASE ris.amount_rule
                WHEN 10 THEN sb.opening_debit_amount
                WHEN 20 THEN sb.opening_credit_amount
                WHEN 30 THEN sb.current_debit_amount
                WHEN 40 THEN sb.current_credit_amount
                WHEN 50 THEN sb.ending_debit_amount
                WHEN 60 THEN sb.ending_credit_amount
                WHEN 70 THEN sb.current_debit_amount - sb.current_credit_amount
                WHEN 80 THEN sb.current_credit_amount - sb.current_debit_amount
                WHEN 90 THEN sb.ending_debit_amount - sb.ending_credit_amount
                WHEN 100 THEN sb.ending_credit_amount - sb.ending_debit_amount
                ELSE 0
            END * ris.amount_sign
        ), 0) AS amount
    FROM erp_finance_report_item ri
    LEFT JOIN erp_finance_report_item_subject ris
        ON ris.item_id = ri.id
       AND ris.deleted = b'0'
       AND ris.tenant_id = ri.tenant_id
    LEFT JOIN erp_finance_subject_balance sb
        ON sb.ledger_id = ri.ledger_id
       AND sb.period_id = @period_id
       AND sb.subject_code = ris.subject_code
       AND sb.deleted = b'0'
       AND sb.tenant_id = ri.tenant_id
    WHERE ri.ledger_id = @ledger_id
      AND ri.status = 0
      AND ri.deleted = b'0'
      AND ri.tenant_id = @tenant_id
    GROUP BY ri.report_type, ri.item_category, ri.item_code, ri.item_name
)
SELECT
    'income_statement' AS check_name,
    SUM(CASE WHEN item_category = 40 THEN amount ELSE 0 END) AS revenue_amount,
    SUM(CASE WHEN item_category = 50 THEN amount ELSE 0 END) AS cost_expense_amount,
    SUM(CASE WHEN item_category = 40 THEN amount ELSE 0 END) - SUM(CASE WHEN item_category = 50 THEN amount ELSE 0 END) AS profit_amount
FROM report_amounts
WHERE report_type = 20;

WITH report_amounts AS (
    SELECT
        ri.report_type,
        ri.item_category,
        ri.item_code,
        ri.item_name,
        COALESCE(SUM(
            CASE ris.amount_rule
                WHEN 10 THEN sb.opening_debit_amount
                WHEN 20 THEN sb.opening_credit_amount
                WHEN 30 THEN sb.current_debit_amount
                WHEN 40 THEN sb.current_credit_amount
                WHEN 50 THEN sb.ending_debit_amount
                WHEN 60 THEN sb.ending_credit_amount
                WHEN 70 THEN sb.current_debit_amount - sb.current_credit_amount
                WHEN 80 THEN sb.current_credit_amount - sb.current_debit_amount
                WHEN 90 THEN sb.ending_debit_amount - sb.ending_credit_amount
                WHEN 100 THEN sb.ending_credit_amount - sb.ending_debit_amount
                ELSE 0
            END * ris.amount_sign
        ), 0) AS amount
    FROM erp_finance_report_item ri
    LEFT JOIN erp_finance_report_item_subject ris
        ON ris.item_id = ri.id
       AND ris.deleted = b'0'
       AND ris.tenant_id = ri.tenant_id
    LEFT JOIN erp_finance_subject_balance sb
        ON sb.ledger_id = ri.ledger_id
       AND sb.period_id = @period_id
       AND sb.subject_code = ris.subject_code
       AND sb.deleted = b'0'
       AND sb.tenant_id = ri.tenant_id
    WHERE ri.ledger_id = @ledger_id
      AND ri.status = 0
      AND ri.deleted = b'0'
      AND ri.tenant_id = @tenant_id
    GROUP BY ri.report_type, ri.item_category, ri.item_code, ri.item_name
)
SELECT
    'cash_flow_statement' AS check_name,
    SUM(CASE WHEN item_category = 60 THEN amount ELSE 0 END) AS cash_inflow_amount,
    SUM(CASE WHEN item_category = 70 THEN amount ELSE 0 END) AS cash_outflow_amount,
    SUM(CASE WHEN item_category = 60 THEN amount ELSE 0 END) - SUM(CASE WHEN item_category = 70 THEN amount ELSE 0 END) AS net_cash_flow_amount
FROM report_amounts
WHERE report_type = 30;
