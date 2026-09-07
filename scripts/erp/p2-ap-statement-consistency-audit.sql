-- =====================================================================
-- P2 预备：AP 核销一致性只读巡检（不修改任何数据）
-- 用途：在启用 closeStatementByBiz 机制级守卫前，盘点存量"半条 AP 事实"，
--       供财务/业务逐条人工判定，禁止用刷库脚本批量改写。
-- 运行：java scripts/erp/JdbcSqlRunner.java scripts/erp/p2-ap-statement-consistency-audit.sql
--       （环境变量 JDBC_URL / JDBC_USERNAME / JDBC_PASSWORD 指向目标库）
-- 处置决策表：
--   A1/A2（已关闭台账仍有生效核销）：
--       核销属实 → 由业务重开台账复核金额后，先回滚核销再反审核来源单；
--       核销错误 → 手工将对应 allocate 置为已撤销(30) 并补台账流水说明（需财务签字）。
--   B1（台账金额与核销合计不一致）：先重跑一次正常核销/作废触发 refresh 验证能否自愈；
--       不能自愈的逐条排查 refreshStatementAmountByIds 被跳过的原因（多为 A1/A2 衍生）。
--   C1/C2（预付单余额与核销合计不一致）：同 B1，以核销事实为准重算。
--   D1（已审核付款单无生效核销）：核对付款单是否全部行为空台账引用；异常单走作废释放。
--   D2（已作废付款单仍有生效核销）：属守卫上线前的历史窗口产物，按 A1 流程人工回滚。
-- =====================================================================

-- A1. 已关闭(CLOSED=40)台账 × 生效(APPROVED=20)付款核销
SELECT 'A1 closed-statement-with-approved-payment-allocate' AS issue,
       s.id AS statement_id, s.statement_no, s.biz_type, s.biz_no,
       s.paid_amount, s.remain_amount,
       a.id AS allocate_id, a.payment_id, a.allocate_amount
FROM erp_ap_statement s
JOIN erp_finance_payment_allocate a
  ON a.ap_statement_id = s.id AND a.status = 20 AND a.deleted = 0
WHERE s.status = 40 AND s.deleted = 0;

-- A2. 已关闭台账 × 生效预付款核销
SELECT 'A2 closed-statement-with-approved-prepayment-allocate' AS issue,
       s.id AS statement_id, s.statement_no, s.biz_type, s.biz_no,
       s.paid_amount, s.remain_amount,
       a.id AS allocate_id, a.prepayment_id, a.allocate_amount
FROM erp_ap_statement s
JOIN erp_finance_prepayment_allocate a
  ON a.ap_statement_id = s.id AND a.status = 20 AND a.deleted = 0
WHERE s.status = 40 AND s.deleted = 0;

-- B1. 未关闭台账：paid_amount 与两类生效核销合计不一致（容差 0.01）
SELECT 'B1 statement-paid-amount-mismatch' AS issue,
       s.id AS statement_id, s.statement_no, s.status,
       s.amount, s.paid_amount, s.remain_amount,
       IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0) AS expected_paid,
       s.amount - (IFNULL(pa.pay_sum, 0) + IFNULL(prep.prep_sum, 0)) AS expected_remain
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

-- C1. 已审核(APPROVE=20)预付单：allocated_price 与生效核销合计不一致
SELECT 'C1 prepayment-allocated-price-mismatch' AS issue,
       p.id AS prepayment_id, p.no, p.prepayment_price,
       p.allocated_price, p.remain_price,
       IFNULL(a.alloc_sum, 0) AS expected_allocated,
       p.prepayment_price - IFNULL(a.alloc_sum, 0) AS expected_remain
FROM erp_finance_prepayment p
LEFT JOIN (SELECT prepayment_id, SUM(allocate_amount) AS alloc_sum
           FROM erp_finance_prepayment_allocate WHERE status = 20 AND deleted = 0
           GROUP BY prepayment_id) a ON a.prepayment_id = p.id
WHERE p.status = 20 AND p.deleted = 0
  AND (ABS(p.allocated_price - IFNULL(a.alloc_sum, 0)) > 0.01
       OR ABS(p.remain_price - (p.prepayment_price - IFNULL(a.alloc_sum, 0))) > 0.01);

-- D1. 已审核(APPROVE=20)付款单：存在台账行但无任何生效核销
SELECT 'D1 approved-payment-without-approved-allocate' AS issue,
       pay.id AS payment_id, pay.no, pay.total_price, pay.payment_price,
       COUNT(i.id) AS statement_item_count
FROM erp_finance_payment pay
JOIN erp_finance_payment_item i ON i.payment_id = pay.id AND i.deleted = 0
LEFT JOIN erp_finance_payment_allocate a
  ON a.payment_id = pay.id AND a.status = 20 AND a.deleted = 0
WHERE pay.status = 20 AND pay.deleted = 0 AND a.id IS NULL
GROUP BY pay.id, pay.no, pay.total_price, pay.payment_price;

-- D2. 已作废(VOID=50)付款单：仍残留生效核销（作废释放失败的历史窗口产物）
SELECT 'D2 voided-payment-with-approved-allocate' AS issue,
       pay.id AS payment_id, pay.no, pay.void_reason,
       a.id AS allocate_id, a.ap_statement_id, a.allocate_amount
FROM erp_finance_payment pay
JOIN erp_finance_payment_allocate a
  ON a.payment_id = pay.id AND a.status = 20 AND a.deleted = 0
WHERE pay.status = 50 AND pay.deleted = 0;
