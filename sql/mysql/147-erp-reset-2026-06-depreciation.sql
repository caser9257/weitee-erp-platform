-- =====================================================
-- 重置 2026-06 期间的摊销/折旧记录，以便重新生成凭证
-- 
-- 执行前请确认：
-- 1. 已执行 146-erp-intangible-asset-voucher-template-fix.sql
-- 2. 已重启后端服务
-- =====================================================

-- 1. 查看待删除的记录
SELECT '待删除的摊销/折旧记录' AS '操作说明',
    d.id, d.asset_no, d.period, d.depreciation_amount, d.voucher_id
FROM erp_finance_asset_depreciation d
WHERE d.period = '2026-06'
  AND d.deleted = b'0';

-- 2. 查看受影响的资产
SELECT '受影响的资产' AS '操作说明',
    a.id, a.no, a.name, a.original_amount,
    a.depreciated_amount AS 当前累计折旧,
    a.current_amount AS 当前净值,
    a.last_depreciation_period AS 最近计提期间
FROM erp_finance_asset a
WHERE a.last_depreciation_period = '2026-06'
  AND a.deleted = b'0';

-- =====================================================
-- 3. 重置资产的折旧/摊销状态（回退到 2026-06 之前的状态）
-- =====================================================
UPDATE erp_finance_asset a
INNER JOIN (
    -- 计算每个资产在 2026-06 之前的累计折旧
    SELECT 
        asset_id,
        MAX(after_depreciated_amount) AS prev_depreciated_amount,
        MIN(after_current_amount) AS prev_current_amount
    FROM erp_finance_asset_depreciation
    WHERE period < '2026-06'
      AND deleted = b'0'
    GROUP BY asset_id
) prev ON a.id = prev.asset_id
SET 
    a.depreciated_amount = prev.prev_depreciated_amount,
    a.current_amount = prev.prev_current_amount,
    a.last_depreciation_period = (
        SELECT MAX(period) 
        FROM erp_finance_asset_depreciation 
        WHERE asset_id = a.id 
          AND period < '2026-06'
          AND deleted = b'0'
    );

-- 对于没有历史记录的资产（首次计提就是 2026-06），重置为初始状态
UPDATE erp_finance_asset a
SET 
    a.depreciated_amount = 0,
    a.current_amount = a.original_amount,
    a.last_depreciation_period = NULL
WHERE a.last_depreciation_period = '2026-06'
  AND a.id NOT IN (
      SELECT DISTINCT asset_id 
      FROM erp_finance_asset_depreciation 
      WHERE period < '2026-06' 
        AND deleted = b'0'
  )
  AND a.deleted = b'0';

-- =====================================================
-- 4. 软删除 2026-06 的摊销/折旧记录
-- =====================================================
UPDATE erp_finance_asset_depreciation
SET deleted = b'1', updater = 'system', update_time = NOW()
WHERE period = '2026-06'
  AND deleted = b'0';

-- 5. 如果 2026-06 期间有关联的凭证，也要清理
UPDATE erp_finance_voucher
SET deleted = b'1', updater = 'system', update_time = NOW()
WHERE biz_type IN (70, 71, 72)
  AND biz_id IN (
      SELECT id FROM (
          SELECT id FROM erp_finance_asset_depreciation 
          WHERE period = '2026-06'
      ) tmp
  )
  AND deleted = b'0';

-- =====================================================
-- 6. 验证结果
-- =====================================================
SELECT '重置后资产状态' AS '验证项',
    a.no, a.name, a.original_amount, 
    a.depreciated_amount, a.current_amount, 
    a.last_depreciation_period
FROM erp_finance_asset a
WHERE a.id IN (
    SELECT DISTINCT asset_id 
    FROM erp_finance_asset_depreciation 
    WHERE period = '2026-06'
      AND deleted = b'1'
)
AND a.deleted = b'0';

SELECT '剩余 2026-06 记录数' AS '验证项',
    COUNT(*) AS 数量
FROM erp_finance_asset_depreciation
WHERE period = '2026-06'
  AND deleted = b'0';
-- 预期结果：0
