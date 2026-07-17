-- 双账套规则安全收敛：停用旧库中无法保证“内账 < 外账”的启用配置。
-- 仅修改规则状态，不改写历史凭证、成本结果或金额数据。

USE `weitee-erp`;

UPDATE erp_finance_dual_ledger_diff_config
SET status = 1,
    updater = 'migration-166',
    update_time = NOW()
WHERE deleted = b'0'
  AND status = 0
  AND (
      calculation_type = 3
      OR calculation_type NOT IN (1, 2)
      OR (calculation_type = 1 AND (ratio IS NULL OR ratio <= 1))
      OR (calculation_type = 2 AND (fixed_amount IS NULL OR fixed_amount >= 0))
  );
