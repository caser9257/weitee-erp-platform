-- =====================================================
-- ERP stock reconciliation snapshot（库存链对账快照）
-- 用途：S2 库存链闭环交付物，供 P7（对账与关账门禁）和 P8（报表）取数。
--       全部为只读 SELECT，可重复执行，无任何写入。
-- 口径：
--   1. 流水守恒：erp_stock.count 必须等于该产品+仓位的全部库存流水（erp_stock_record）
--      累计值。count 的唯一记账人是 createStockRecord → updateStockCountIncrement；
--      available_count / quality_hold_count 由 IQC、采购入库确认、生产发料等
--      专属链路单独维护，不参与本守恒校验。
--   2. 批次守恒：仅对 batch_control_flag = 1 的产品有意义。其它入库（OTHER_IN）、
--      销售退货（SALE_RETURN）、盘盈（CHECK_MORE_IN）等路径不建批次（P0 待确认口径），
--      此类差异需人工判断归属，脚本如实暴露不静默掩盖。
--   3. 盘点差异：以盘点项 diff_amount 与快照账面为源，未关闭单据同样输出，便于过程追踪。
-- 容差：数量列精度 decimal(24,6)，差异容差 0.000001，超出即视为不平。
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 1. 库存余额快照（P8 报表直接取数的余额底表）
-- =====================================================
SELECT
    s.`product_id`,
    s.`warehouse_id`,
    s.`count`              AS `stock_count`,
    s.`available_count`    AS `available_count`,
    s.`quality_hold_count` AS `quality_hold_count`,
    s.`average_cost`       AS `average_cost`,
    s.`total_cost`         AS `total_cost`
FROM `erp_stock` s
WHERE s.`deleted` = b'0'
ORDER BY s.`product_id`, s.`warehouse_id`;

-- =====================================================
-- 2. 流水守恒校验（count ≠ 全量流水累计 的差异行）
--    预期输出 0 行；任何输出即为账实不平，需按 bizType 定位责任单据
-- =====================================================
SELECT
    s.`product_id`,
    s.`warehouse_id`,
    s.`count`                        AS `stock_count`,
    COALESCE(r.`record_sum`, 0)      AS `record_sum`,
    s.`count` - COALESCE(r.`record_sum`, 0) AS `diff`
FROM `erp_stock` s
LEFT JOIN (
    SELECT `product_id`, `warehouse_id`, SUM(`count`) AS `record_sum`
    FROM `erp_stock_record`
    WHERE `deleted` = b'0'
    GROUP BY `product_id`, `warehouse_id`
) r ON r.`product_id` = s.`product_id`
   AND r.`warehouse_id` = s.`warehouse_id`
WHERE s.`deleted` = b'0'
  AND ABS(s.`count` - COALESCE(r.`record_sum`, 0)) > 0.000001
ORDER BY ABS(s.`count` - COALESCE(r.`record_sum`, 0)) DESC;

-- =====================================================
-- 3. 批次守恒校验（仅批次管理产品；差异可能来自不建批次的入库路径，见头部口径 2）
--    预期输出 0 行；输出行需结合产品链路判断是口径缺口还是数据缺陷
-- =====================================================
SELECT
    s.`product_id`,
    p.`name`                          AS `product_name`,
    s.`warehouse_id`,
    s.`count`                         AS `stock_count`,
    COALESCE(b.`batch_total`, 0)      AS `batch_total`,
    COALESCE(b.`batch_locked`, 0)     AS `batch_locked`,
    s.`count` - COALESCE(b.`batch_total`, 0) AS `diff`
FROM `erp_stock` s
JOIN `erp_product` p ON p.`id` = s.`product_id` AND p.`deleted` = b'0'
LEFT JOIN (
    SELECT `product_id`, `warehouse_id`,
           SUM(`total_qty`) AS `batch_total`,
           SUM(`locked_qty`) AS `batch_locked`
    FROM `erp_stock_batch`
    WHERE `deleted` = b'0'
    GROUP BY `product_id`, `warehouse_id`
) b ON b.`product_id` = s.`product_id`
   AND b.`warehouse_id` = s.`warehouse_id`
WHERE s.`deleted` = b'0'
  AND p.`batch_control_flag` = b'1'
  AND ABS(s.`count` - COALESCE(b.`batch_total`, 0)) > 0.000001
ORDER BY ABS(s.`count` - COALESCE(b.`batch_total`, 0)) DESC;

-- =====================================================
-- 4. 盘点差异快照（盘点项盈亏 + 快照账面 + 凭证关联，P7 关账门禁取数）
-- =====================================================
SELECT
    c.`id`                     AS `check_id`,
    c.`no`                     AS `check_no`,
    c.`status`                 AS `check_status`,
    c.`check_time`,
    c.`snapshot_time`,
    c.`year_end_flag`,
    c.`voucher_id`,
    i.`product_id`,
    i.`warehouse_id`,
    i.`stock_count`            AS `item_book_count`,
    i.`actual_count`           AS `item_actual_count`,
    i.`count`                  AS `item_diff_count`,
    i.`diff_amount`,
    sn.`book_qty`              AS `snapshot_book_qty`,
    sn.`book_amount`           AS `snapshot_book_amount`,
    sn.`average_cost`          AS `snapshot_average_cost`
FROM `erp_stock_check` c
JOIN `erp_stock_check_item` i ON i.`check_id` = c.`id` AND i.`deleted` = b'0'
LEFT JOIN `erp_stock_check_snapshot` sn ON sn.`check_id` = c.`id`
    AND sn.`product_id` = i.`product_id`
    AND sn.`warehouse_id` = i.`warehouse_id`
    AND sn.`deleted` = b'0'
WHERE c.`deleted` = b'0'
ORDER BY c.`id` DESC, i.`product_id`, i.`warehouse_id`;
