-- =====================================================
-- 离散计量单位小数数量历史数据扫描
-- =====================================================
-- 说明：
-- 1. 本脚本只输出清单，不直接 UPDATE/DELETE 修正历史数据。
-- 2. quantity_precision = 0 的单位视为离散单位；数量存在小数时输出。
-- 3. 发现问题后，应通过受控库存/批次校正单或对应业务校正流程处理。

WITH suspect_quantities AS (
    SELECT 'erp_stock' AS source_table, 'count' AS source_column,
           s.id AS source_id, CAST(NULL AS SIGNED) AS source_item_id,
           s.product_id, s.count AS quantity_value, CAST(NULL AS CHAR(64)) AS biz_no
    FROM erp_stock s
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_batch', 'total_qty', s.id, NULL, s.product_id, s.total_qty, s.source_biz_no
    FROM erp_stock_batch s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_batch', 'available_qty', s.id, NULL, s.product_id, s.available_qty, s.source_biz_no
    FROM erp_stock_batch s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_batch', 'locked_qty', s.id, NULL, s.product_id, s.locked_qty, s.source_biz_no
    FROM erp_stock_batch s
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_record', 'count', s.id, s.biz_item_id, s.product_id, s.count, s.biz_no
    FROM erp_stock_record s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_record', 'total_count', s.id, s.biz_item_id, s.product_id, s.total_count, s.biz_no
    FROM erp_stock_record s
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_batch_record', 'count', s.id, s.biz_item_id, s.product_id, s.count, s.biz_no
    FROM erp_stock_batch_record s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_batch_record', 'after_available_qty', s.id, s.biz_item_id, s.product_id, s.after_available_qty, s.biz_no
    FROM erp_stock_batch_record s
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_in_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_stock_in_item s
    JOIN erp_stock_in h ON h.id = s.in_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_out_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_stock_out_item s
    JOIN erp_stock_out h ON h.id = s.out_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_move_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_stock_move_item s
    JOIN erp_stock_move h ON h.id = s.move_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_check_item', 'stock_count', h.id, s.id, s.product_id, s.stock_count, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_check_item', 'actual_count', h.id, s.id, s.product_id, s.actual_count, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_check_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_check_item', 'first_count', h.id, s.id, s.product_id, s.first_count, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_check_item', 'recount', h.id, s.id, s.product_id, s.recount, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_check_item', 'recount_diff', h.id, s.id, s.product_id, s.recount_diff, h.no
    FROM erp_stock_check_item s
    JOIN erp_stock_check h ON h.id = s.check_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_stock_assemble', 'count', s.id, NULL, s.product_id, s.count, s.no
    FROM erp_stock_assemble s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_stock_assemble_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_stock_assemble_item s
    JOIN erp_stock_assemble h ON h.id = s.assemble_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_purchase_order_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_order_items s
    JOIN erp_purchase_order h ON h.id = s.order_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_in_items s
    JOIN erp_purchase_in h ON h.id = s.in_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_items', 'qa_pass_count', h.id, s.id, s.product_id, s.qa_pass_count, h.no
    FROM erp_purchase_in_items s
    JOIN erp_purchase_in h ON h.id = s.in_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_items', 'qa_reject_count', h.id, s.id, s.product_id, s.qa_reject_count, h.no
    FROM erp_purchase_in_items s
    JOIN erp_purchase_in h ON h.id = s.in_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_items', 'stock_in_count', h.id, s.id, s.product_id, s.stock_in_count, h.no
    FROM erp_purchase_in_items s
    JOIN erp_purchase_in h ON h.id = s.in_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_quality_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_in_quality_item s
    JOIN erp_purchase_in_quality h ON h.id = s.quality_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_quality_item', 'qa_pass_count', h.id, s.id, s.product_id, s.qa_pass_count, h.no
    FROM erp_purchase_in_quality_item s
    JOIN erp_purchase_in_quality h ON h.id = s.quality_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_quality_item', 'qa_reject_count', h.id, s.id, s.product_id, s.qa_reject_count, h.no
    FROM erp_purchase_in_quality_item s
    JOIN erp_purchase_in_quality h ON h.id = s.quality_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_stock_execute_item', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_in_stock_execute_item s
    JOIN erp_purchase_in_stock_execute h ON h.id = s.execute_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_in_stock_execute_item_batch', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_in_stock_execute_item_batch s
    JOIN erp_purchase_in_stock_execute_item i ON i.id = s.execute_item_id AND i.deleted = b'0'
    JOIN erp_purchase_in_stock_execute h ON h.id = i.execute_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_purchase_return_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_purchase_return_items s
    JOIN erp_purchase_return h ON h.id = s.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_sale_order_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_sale_order_items s
    JOIN erp_sale_order h ON h.id = s.order_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_sale_out_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_sale_out_items s
    JOIN erp_sale_out h ON h.id = s.out_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_sale_return_items', 'count', h.id, s.id, s.product_id, s.count, h.no
    FROM erp_sale_return_items s
    JOIN erp_sale_return h ON h.id = s.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_production_finish_quality', 'report_qty', s.id, NULL, s.product_id, s.report_qty, s.no
    FROM erp_production_finish_quality s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_finish_quality', 'qualified_qty', s.id, NULL, s.product_id, s.qualified_qty, s.no
    FROM erp_production_finish_quality s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_finish_quality', 'unqualified_qty', s.id, NULL, s.product_id, s.unqualified_qty, s.no
    FROM erp_production_finish_quality s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_inbound', 'inbound_qty', s.id, NULL, s.product_id, s.inbound_qty, s.no
    FROM erp_production_inbound s
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_issue_item', 'issue_qty', h.id, s.id, s.material_id, s.issue_qty, h.issue_no
    FROM erp_production_issue_item s
    JOIN erp_production_issue h ON h.id = s.issue_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_issue_batch', 'issue_qty', h.id, s.id, i.material_id, s.issue_qty, h.issue_no
    FROM erp_production_issue_batch s
    JOIN erp_production_issue_item i ON i.id = s.issue_item_id AND i.deleted = b'0'
    JOIN erp_production_issue h ON h.id = i.issue_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_return_item', 'return_qty', h.id, s.id, s.material_id, s.return_qty, h.return_no
    FROM erp_production_return_item s
    JOIN erp_production_return h ON h.id = s.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_production_return_batch', 'return_qty', h.id, s.id, i.material_id, s.return_qty, h.return_no
    FROM erp_production_return_batch s
    JOIN erp_production_return_item i ON i.id = s.return_item_id AND i.deleted = b'0'
    JOIN erp_production_return h ON h.id = i.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'

    UNION ALL
    SELECT 'erp_outsource_issue', 'issue_qty', s.id, NULL, o.product_id, s.issue_qty, s.issue_no
    FROM erp_outsource_issue s
    JOIN erp_outsource_order o ON o.id = s.order_id AND o.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_issue_item', 'issue_qty', h.id, s.id, s.material_id, s.issue_qty, h.issue_no
    FROM erp_outsource_issue_item s
    JOIN erp_outsource_issue h ON h.id = s.issue_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_issue_batch', 'issue_qty', h.id, s.id, i.material_id, s.issue_qty, h.issue_no
    FROM erp_outsource_issue_batch s
    JOIN erp_outsource_issue_item i ON i.id = s.issue_item_id AND i.deleted = b'0'
    JOIN erp_outsource_issue h ON h.id = i.issue_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_return', 'return_qty', s.id, NULL, o.product_id, s.return_qty, s.return_no
    FROM erp_outsource_return s
    JOIN erp_outsource_order o ON o.id = s.order_id AND o.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_return_item', 'return_qty', h.id, s.id, s.material_id, s.return_qty, h.return_no
    FROM erp_outsource_return_item s
    JOIN erp_outsource_return h ON h.id = s.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_return_batch', 'return_qty', h.id, s.id, i.material_id, s.return_qty, h.return_no
    FROM erp_outsource_return_batch s
    JOIN erp_outsource_return_item i ON i.id = s.return_item_id AND i.deleted = b'0'
    JOIN erp_outsource_return h ON h.id = i.return_id AND h.deleted = b'0'
    WHERE s.deleted = b'0'
    UNION ALL
    SELECT 'erp_outsource_inbound', 'inbound_qty', s.id, NULL, o.product_id, s.inbound_qty, s.inbound_no
    FROM erp_outsource_inbound s
    JOIN erp_outsource_order o ON o.id = s.order_id AND o.deleted = b'0'
    WHERE s.deleted = b'0'
)
SELECT
    q.source_table,
    q.source_column,
    q.source_id,
    q.source_item_id,
    q.product_id,
    p.name AS product_name,
    u.name AS unit_name,
    q.quantity_value,
    q.biz_no,
    '离散单位数量含小数；不得直接 SQL 修正，请通过受控库存/批次校正单或对应业务校正流程处理' AS suggestion
FROM suspect_quantities q
JOIN erp_product p ON p.id = q.product_id AND p.deleted = b'0'
JOIN erp_product_unit u ON u.id = p.unit_id AND u.deleted = b'0' AND u.quantity_precision = 0
WHERE q.quantity_value IS NOT NULL
  AND ABS(q.quantity_value - TRUNCATE(q.quantity_value, 0)) > 0
ORDER BY q.source_table, q.source_column, q.source_id, q.source_item_id;
