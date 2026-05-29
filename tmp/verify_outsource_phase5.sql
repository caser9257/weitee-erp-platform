SELECT
    INDEX_NAME,
    NON_UNIQUE,
    SEQ_IN_INDEX,
    COLUMN_NAME
FROM information_schema.statistics
WHERE table_schema = 'ruoyi-vue-pro'
  AND table_name = 'erp_outsource_loss_detail'
  AND index_name IN ('uk_outsource_loss_order_issue_batch', 'idx_outsource_loss_order_issue_batch')
ORDER BY index_name, seq_in_index;

SELECT
    o.id AS order_id,
    o.no AS order_no,
    o.status,
    IFNULL(o.loss_qty, 0) AS order_loss_qty,
    IFNULL(ld.recorded_loss_qty, 0) AS recorded_loss_qty,
    GREATEST(IFNULL(o.loss_qty, 0) - IFNULL(ld.recorded_loss_qty, 0), 0) AS pending_backfill_qty,
    IFNULL(ib.issue_batch_count, 0) AS issue_batch_count,
    IFNULL(ld.loss_entry_count, 0) AS loss_entry_count,
    o.close_time
FROM erp_outsource_order o
LEFT JOIN (
    SELECT
        order_id,
        SUM(loss_qty) AS recorded_loss_qty,
        COUNT(*) AS loss_entry_count
    FROM erp_outsource_loss_detail
    WHERE deleted = 0
    GROUP BY order_id
) ld ON ld.order_id = o.id
LEFT JOIN (
    SELECT
        i.order_id,
        COUNT(*) AS issue_batch_count
    FROM erp_outsource_issue i
    INNER JOIN erp_outsource_issue_item ii
        ON ii.issue_id = i.id
       AND ii.deleted = 0
    INNER JOIN erp_outsource_issue_batch ib
        ON ib.issue_item_id = ii.id
       AND ib.deleted = 0
    WHERE i.deleted = 0
    GROUP BY i.order_id
) ib ON ib.order_id = o.id
WHERE o.deleted = 0
  AND o.status = 40
  AND IFNULL(o.loss_qty, 0) > 0
ORDER BY pending_backfill_qty DESC, o.id DESC
LIMIT 50;

SELECT
    o.id AS order_id,
    o.no AS order_no,
    IFNULL(o.loss_qty, 0) AS order_loss_qty,
    IFNULL(ld.recorded_loss_qty, 0) AS recorded_loss_qty,
    GREATEST(IFNULL(o.loss_qty, 0) - IFNULL(ld.recorded_loss_qty, 0), 0) AS pending_backfill_qty,
    IFNULL(ib.issue_batch_count, 0) AS issue_batch_count
FROM erp_outsource_order o
LEFT JOIN (
    SELECT
        order_id,
        SUM(loss_qty) AS recorded_loss_qty
    FROM erp_outsource_loss_detail
    WHERE deleted = 0
    GROUP BY order_id
) ld ON ld.order_id = o.id
LEFT JOIN (
    SELECT
        i.order_id,
        COUNT(*) AS issue_batch_count
    FROM erp_outsource_issue i
    INNER JOIN erp_outsource_issue_item ii
        ON ii.issue_id = i.id
       AND ii.deleted = 0
    INNER JOIN erp_outsource_issue_batch ib
        ON ib.issue_item_id = ii.id
       AND ib.deleted = 0
    WHERE i.deleted = 0
    GROUP BY i.order_id
) ib ON ib.order_id = o.id
WHERE o.deleted = 0
  AND o.status = 40
  AND IFNULL(o.loss_qty, 0) > 0
  AND GREATEST(IFNULL(o.loss_qty, 0) - IFNULL(ld.recorded_loss_qty, 0), 0) > 0
  AND IFNULL(ib.issue_batch_count, 0) = 0
ORDER BY o.id DESC;

SELECT
    o.id AS order_id,
    o.no AS order_no,
    IFNULL(o.loss_qty, 0) AS order_loss_qty,
    IFNULL(ld.recorded_loss_qty, 0) AS recorded_loss_qty,
    IFNULL(ld.recorded_loss_qty, 0) - IFNULL(o.loss_qty, 0) AS over_recorded_qty
FROM erp_outsource_order o
INNER JOIN (
    SELECT
        order_id,
        SUM(loss_qty) AS recorded_loss_qty
    FROM erp_outsource_loss_detail
    WHERE deleted = 0
    GROUP BY order_id
) ld ON ld.order_id = o.id
WHERE o.deleted = 0
  AND IFNULL(ld.recorded_loss_qty, 0) > IFNULL(o.loss_qty, 0)
ORDER BY over_recorded_qty DESC, o.id DESC;

SELECT
    o.id AS order_id,
    o.no AS order_no,
    IFNULL(o.loss_qty, 0) AS order_loss_qty,
    IFNULL(ld.recorded_loss_qty, 0) AS recorded_loss_qty,
    GREATEST(IFNULL(o.loss_qty, 0) - IFNULL(ld.recorded_loss_qty, 0), 0) AS pending_backfill_qty,
    IFNULL(cap.available_loss_qty, 0) AS allocatable_loss_qty
FROM erp_outsource_order o
LEFT JOIN (
    SELECT
        order_id,
        SUM(loss_qty) AS recorded_loss_qty
    FROM erp_outsource_loss_detail
    WHERE deleted = 0
    GROUP BY order_id
) ld ON ld.order_id = o.id
LEFT JOIN (
    SELECT
        i.order_id,
        SUM(GREATEST(
            IFNULL(ib.issue_qty, 0) - IFNULL(rb.returned_qty, 0) - IFNULL(lb.loss_qty, 0),
            0
        )) AS available_loss_qty
    FROM erp_outsource_issue i
    INNER JOIN erp_outsource_issue_item ii
        ON ii.issue_id = i.id
       AND ii.deleted = 0
    INNER JOIN erp_outsource_issue_batch ib
        ON ib.issue_item_id = ii.id
       AND ib.deleted = 0
    LEFT JOIN (
        SELECT
            issue_batch_id,
            SUM(return_qty) AS returned_qty
        FROM erp_outsource_return_batch
        WHERE deleted = 0
        GROUP BY issue_batch_id
    ) rb ON rb.issue_batch_id = ib.id
    LEFT JOIN (
        SELECT
            issue_batch_id,
            SUM(loss_qty) AS loss_qty
        FROM erp_outsource_loss_detail
        WHERE deleted = 0
        GROUP BY issue_batch_id
    ) lb ON lb.issue_batch_id = ib.id
    WHERE i.deleted = 0
    GROUP BY i.order_id
) cap ON cap.order_id = o.id
WHERE o.deleted = 0
  AND o.status = 40
  AND GREATEST(IFNULL(o.loss_qty, 0) - IFNULL(ld.recorded_loss_qty, 0), 0) > IFNULL(cap.available_loss_qty, 0)
ORDER BY pending_backfill_qty DESC, o.id DESC;

SELECT
    o.id AS order_id,
    o.no AS order_no,
    o.status,
    IFNULL(o.loss_qty, 0) AS order_loss_qty,
    IFNULL(o.finished_qty, 0) AS finished_qty,
    IFNULL(ib.issue_batch_count, 0) AS issue_batch_count,
    o.close_time
FROM erp_outsource_order o
LEFT JOIN (
    SELECT
        i.order_id,
        COUNT(*) AS issue_batch_count
    FROM erp_outsource_issue i
    INNER JOIN erp_outsource_issue_item ii
        ON ii.issue_id = i.id
       AND ii.deleted = 0
    INNER JOIN erp_outsource_issue_batch ib
        ON ib.issue_item_id = ii.id
       AND ib.deleted = 0
    WHERE i.deleted = 0
    GROUP BY i.order_id
) ib ON ib.order_id = o.id
WHERE o.deleted = 0
ORDER BY o.id DESC
LIMIT 20;
