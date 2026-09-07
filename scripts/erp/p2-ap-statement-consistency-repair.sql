-- =====================================================================
-- P2 预备：AP 核销一致性受守卫重算修复脚本（幂等，可重复执行）
--
-- 性质说明：本脚本不是"补数据补丁"，而是把 refreshStatementAmountByIds /
--   refreshPrepaymentAmountById 的既有重算公式（事实源 = APPROVED 核销行）
--   以等价 SQL 执行一遍；所有 UPDATE 带不变量守卫，
--   违反"核销不得超过可核销余额"的存量（如核销合计超过台账金额）不会被写入，
--   而是保留在巡检清单中等待业务人工判定。
--
-- 前置：先跑 scripts/erp/p2-ap-statement-consistency-audit.sql 留存修复前清单。
-- 运行：java scripts/erp/JdbcSqlRunner.java scripts/erp/p2-ap-statement-consistency-repair.sql
-- 后置：再跑一次巡检 SQL 复核；仍出现的条目按巡检文件头决策表人工处置。
--
-- 假设声明（Step 1）：核销行符号与台账金额符号相反，只可能是旧代码违反
--   resolveAllocateAmount 不变量写入的脏数据；修复保留金额绝对值、仅翻转符号。
--   若业务判定"金额本身也错"，需人工回滚该核销后重新核销。
-- =====================================================================

-- Step 0. dry-run：列出符号违规核销行（付款）
SELECT 'S0 payment-allocate-sign-violation' AS step, a.id AS allocate_id, a.payment_id,
       a.ap_statement_id, s.statement_no, s.amount AS statement_amount, a.allocate_amount
FROM erp_finance_payment_allocate a
JOIN erp_ap_statement s ON s.id = a.ap_statement_id AND s.deleted = 0
WHERE a.status = 20 AND a.deleted = 0
  AND ((s.amount < 0 AND a.allocate_amount > 0) OR (s.amount > 0 AND a.allocate_amount < 0));

-- Step 1. 修复付款核销行符号（仅符号翻转，金额绝对值不变）
UPDATE erp_finance_payment_allocate a
JOIN erp_ap_statement s ON s.id = a.ap_statement_id AND s.deleted = 0
SET a.allocate_amount = -a.allocate_amount
WHERE a.status = 20 AND a.deleted = 0
  AND ((s.amount < 0 AND a.allocate_amount > 0) OR (s.amount > 0 AND a.allocate_amount < 0));

-- Step 0b. dry-run：列出符号违规核销行（预付款）
SELECT 'S0b prepayment-allocate-sign-violation' AS step, a.id AS allocate_id, a.prepayment_id,
       a.ap_statement_id, s.statement_no, s.amount AS statement_amount, a.allocate_amount
FROM erp_finance_prepayment_allocate a
JOIN erp_ap_statement s ON s.id = a.ap_statement_id AND s.deleted = 0
WHERE a.status = 20 AND a.deleted = 0
  AND ((s.amount < 0 AND a.allocate_amount > 0) OR (s.amount > 0 AND a.allocate_amount < 0));

-- Step 1b. 修复预付款核销行符号
UPDATE erp_finance_prepayment_allocate a
JOIN erp_ap_statement s ON s.id = a.ap_statement_id AND s.deleted = 0
SET a.allocate_amount = -a.allocate_amount
WHERE a.status = 20 AND a.deleted = 0
  AND ((s.amount < 0 AND a.allocate_amount > 0) OR (s.amount > 0 AND a.allocate_amount < 0));

-- Step 2. 重算未关闭台账 paid/remain/status（等价 refreshStatementAmountByIds）
-- 守卫：正台账要求 0 <= paid <= amount；负台账要求 amount <= paid <= 0。
-- 不满足守卫（核销事实本身超额）的台账不写入，保留在巡检清单等待人工判定。
UPDATE erp_ap_statement s
JOIN (
    SELECT st.id, IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0) AS new_paid
    FROM erp_ap_statement st
    LEFT JOIN (SELECT ap_statement_id, SUM(allocate_amount) AS pay_sum
               FROM erp_finance_payment_allocate WHERE status = 20 AND deleted = 0
               GROUP BY ap_statement_id) pa ON pa.ap_statement_id = st.id
    LEFT JOIN (SELECT ap_statement_id, SUM(allocate_amount) AS prep_sum
               FROM erp_finance_prepayment_allocate WHERE status = 20 AND deleted = 0
               GROUP BY ap_statement_id) prep ON prep.ap_statement_id = st.id
    WHERE st.status <> 40 AND st.deleted = 0
) x ON x.id = s.id
SET s.paid_amount = x.new_paid,
    s.remain_amount = s.amount - x.new_paid,
    s.status = CASE WHEN s.amount - x.new_paid = 0 THEN 30
                    WHEN x.new_paid = 0 THEN 10
                    ELSE 20 END
WHERE (ABS(s.paid_amount - x.new_paid) > 0.01
       OR ABS(s.remain_amount - (s.amount - x.new_paid)) > 0.01)
  AND ((s.amount >= 0 AND x.new_paid >= 0 AND x.new_paid <= s.amount)
       OR (s.amount <= 0 AND x.new_paid <= 0 AND x.new_paid >= s.amount));

-- Step 3. 重算预付款单 allocated/remain（等价 refreshPrepaymentAmountById）
-- 守卫：allocated <= prepayment_price（不变量：单次核销合计不超过当时 remainPrice）。
UPDATE erp_finance_prepayment p
JOIN (
    SELECT pp.id, IFNULL(a.alloc_sum, 0) AS new_alloc
    FROM erp_finance_prepayment pp
    LEFT JOIN (SELECT prepayment_id, SUM(allocate_amount) AS alloc_sum
               FROM erp_finance_prepayment_allocate WHERE status = 20 AND deleted = 0
               GROUP BY prepayment_id) a ON a.prepayment_id = pp.id
    WHERE pp.deleted = 0
) x ON x.id = p.id
SET p.allocated_price = x.new_alloc,
    p.remain_price = p.prepayment_price - x.new_alloc
WHERE (ABS(p.allocated_price - x.new_alloc) > 0.01
       OR ABS(p.remain_price - (p.prepayment_price - x.new_alloc)) > 0.01)
  AND x.new_alloc <= p.prepayment_price;

-- Step 4. 复核：重算后仍存在的台账失配（预期仅剩守卫拦截的超额核销脏数据）
SELECT 'S4 remaining-mismatch-needs-human-decision' AS step,
       s.id AS statement_id, s.statement_no, s.status, s.amount, s.paid_amount, s.remain_amount,
       IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0) AS expected_paid
FROM erp_ap_statement s
LEFT JOIN (SELECT ap_statement_id, SUM(allocate_amount) AS pay_sum
           FROM erp_finance_payment_allocate WHERE status = 20 AND deleted = 0
           GROUP BY ap_statement_id) pa ON pa.ap_statement_id = s.id
LEFT JOIN (SELECT ap_statement_id, SUM(allocate_amount) AS prep_sum
           FROM erp_finance_prepayment_allocate WHERE status = 20 AND deleted = 0
           GROUP BY ap_statement_id) prep ON prep.ap_statement_id = s.id
WHERE s.status <> 40 AND s.deleted = 0
  AND (ABS(s.paid_amount - (IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0))) > 0.01
       OR ABS(s.remain_amount - (s.amount - (IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0)))) > 0.01);

-- Step 5. 复核：已审核但无生效核销的付款单（需业务判定：补核销或作废，脚本不自动处置）
SELECT 'S5 approved-payment-without-allocate-needs-human-decision' AS step,
       pay.id AS payment_id, pay.no, pay.payment_price
FROM erp_finance_payment pay
JOIN erp_finance_payment_item i ON i.payment_id = pay.id AND i.deleted = 0
LEFT JOIN erp_finance_payment_allocate a
  ON a.payment_id = pay.id AND a.status = 20 AND a.deleted = 0
WHERE pay.status = 20 AND pay.deleted = 0 AND a.id IS NULL
GROUP BY pay.id, pay.no, pay.payment_price;
