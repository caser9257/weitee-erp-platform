-- =====================================================
-- ERP 自制入库历史成品批次回填
-- =====================================================
-- 目的：为历史上已执行但没有成品批次的自制入库单补齐批次及批次流水。
-- 安全条件：仅回填“即时库存 = 已有批次总量 + 缺失自制入库数量”的产品/仓库组合。
-- 无法对账的组合会被跳过，避免把历史出库或其他库存差异误计入成品批次。
-- 幂等性：已存在同产品、仓库、批次号的记录不会重复插入，可重复执行。
-- 回滚：执行前可在事务内检查临时表；确认无误后再提交，脚本本身不执行 DELETE。

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_erp_production_inbound_batch_groups;
DROP TEMPORARY TABLE IF EXISTS tmp_erp_production_inbound_batch_candidates;

-- 先按产品和仓库汇总“缺失批次”的已执行自制入库数量。
CREATE TEMPORARY TABLE tmp_erp_production_inbound_batch_groups AS
SELECT pi.product_id,
       pi.warehouse_id,
       SUM(pi.inbound_qty) AS missing_inbound_qty
FROM erp_production_inbound pi
         LEFT JOIN erp_stock_batch existing_batch
                   ON existing_batch.product_id = pi.product_id
                       AND existing_batch.warehouse_id = pi.warehouse_id
                       AND existing_batch.batch_no = pi.no
                       AND existing_batch.deleted = b'0'
WHERE pi.deleted = b'0'
  AND pi.status = 20
  AND pi.inbound_qty > 0
  AND existing_batch.id IS NULL
GROUP BY pi.product_id, pi.warehouse_id;

-- 只保留即时库存与批次账可以完全对上的组合，再展开到具体入库单。
CREATE TEMPORARY TABLE tmp_erp_production_inbound_batch_candidates AS
SELECT pi.id,
       pi.product_id,
       pi.warehouse_id,
       pi.no,
       pi.inbound_qty,
       COALESCE(pi.inbound_time, pi.create_time) AS inbound_time,
       pi.remark
FROM erp_production_inbound pi
         JOIN tmp_erp_production_inbound_batch_groups missing_group
              ON missing_group.product_id = pi.product_id
                  AND missing_group.warehouse_id = pi.warehouse_id
         JOIN erp_stock stock
              ON stock.product_id = pi.product_id
                  AND stock.warehouse_id = pi.warehouse_id
                  AND stock.deleted = b'0'
         LEFT JOIN (
    SELECT product_id,
           warehouse_id,
           SUM(total_qty) AS existing_total_qty
    FROM erp_stock_batch
    WHERE deleted = b'0'
    GROUP BY product_id, warehouse_id
) existing_summary
                   ON existing_summary.product_id = pi.product_id
                       AND existing_summary.warehouse_id = pi.warehouse_id
         LEFT JOIN erp_stock_batch existing_batch
                   ON existing_batch.product_id = pi.product_id
                       AND existing_batch.warehouse_id = pi.warehouse_id
                       AND existing_batch.batch_no = pi.no
                       AND existing_batch.deleted = b'0'
WHERE pi.deleted = b'0'
  AND pi.status = 20
  AND pi.inbound_qty > 0
  AND existing_batch.id IS NULL
  AND stock.count = COALESCE(existing_summary.existing_total_qty, 0.000000)
                     + missing_group.missing_inbound_qty;

-- 审计本次实际可回填的单据；数量为 0 时不会写入任何数据。
SELECT id,
       product_id,
       warehouse_id,
       no,
       inbound_qty,
       inbound_time
FROM tmp_erp_production_inbound_batch_candidates
ORDER BY product_id, warehouse_id, inbound_time, id;

INSERT INTO erp_stock_batch (product_id,
                             warehouse_id,
                             batch_no,
                             inbound_time,
                             total_qty,
                             available_qty,
                             locked_qty,
                             virtual_flag,
                             source_biz_type,
                             source_biz_id,
                             source_biz_item_id,
                             source_biz_no,
                             remark)
SELECT candidate.product_id,
       candidate.warehouse_id,
       candidate.no,
       candidate.inbound_time,
       candidate.inbound_qty,
       candidate.inbound_qty,
       0.000000,
       b'0',
       'PRODUCTION_INBOUND',
       candidate.id,
       candidate.id,
       candidate.no,
       candidate.remark
FROM tmp_erp_production_inbound_batch_candidates candidate
         LEFT JOIN erp_stock_batch existing_batch
                   ON existing_batch.product_id = candidate.product_id
                       AND existing_batch.warehouse_id = candidate.warehouse_id
                       AND existing_batch.batch_no = candidate.no
                       AND existing_batch.deleted = b'0'
WHERE existing_batch.id IS NULL;

INSERT INTO erp_stock_batch_record (product_id,
                                    warehouse_id,
                                    stock_batch_id,
                                    batch_no,
                                    count,
                                    after_available_qty,
                                    biz_type,
                                    biz_id,
                                    biz_item_id,
                                    biz_no,
                                    remark)
SELECT candidate.product_id,
       candidate.warehouse_id,
       batch.id,
       batch.batch_no,
       candidate.inbound_qty,
       batch.available_qty,
       95,
       candidate.id,
       candidate.id,
       candidate.no,
       '历史自制入库批次回填'
FROM tmp_erp_production_inbound_batch_candidates candidate
         JOIN erp_stock_batch batch
              ON batch.product_id = candidate.product_id
                  AND batch.warehouse_id = candidate.warehouse_id
                  AND batch.batch_no = candidate.no
                  AND batch.deleted = b'0'
         LEFT JOIN erp_stock_batch_record existing_record
                   ON existing_record.biz_type = 95
                       AND existing_record.biz_id = candidate.id
                       AND existing_record.deleted = b'0'
WHERE existing_record.id IS NULL;

COMMIT;
