-- 159-purchase-return-page-verify-seed.sql
-- 用途：为采购退货台账页面提供最小可视化验证数据
-- 特点：
-- 1. 仅清理并重建 PRTEST-RET-20260703-* 测试退货单
-- 2. 复用现有已审核采购订单 / 订单项 / 仓库 / 产品 / 供应商 / 账户
-- 3. 覆盖草稿、审批中、已审核、已驳回、未退款、部分退款、全部退款、多商品、多仓库、长备注场景

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 仅清理本脚本生成的测试数据
UPDATE erp_purchase_order_items poi
JOIN erp_purchase_return_items pri ON pri.order_item_id = poi.id AND pri.deleted = b'0'
JOIN erp_purchase_return pr ON pr.id = pri.return_id
SET poi.return_count = GREATEST(IFNULL(poi.return_count, 0) - IFNULL(pri.count, 0), 0)
WHERE pr.no LIKE 'PRTEST-RET-20260703-%'
  AND pr.deleted = b'0';

DELETE pri
FROM erp_purchase_return_items pri
JOIN erp_purchase_return pr ON pr.id = pri.return_id
WHERE pr.no LIKE 'PRTEST-RET-20260703-%';

DELETE FROM erp_purchase_return
WHERE no LIKE 'PRTEST-RET-20260703-%';

-- 选择基础订单：优先取已审核且未删除的采购订单
SET @seed_creator := '920201';
SET @seed_updater := '920201';

SET @order_a_id := (
  SELECT id
  FROM erp_purchase_order
  WHERE status = 20 AND deleted = b'0'
  ORDER BY id DESC
  LIMIT 1
);

SET @order_b_id := (
  SELECT id
  FROM erp_purchase_order
  WHERE status = 20 AND deleted = b'0' AND id <> @order_a_id
  ORDER BY id DESC
  LIMIT 1
);

SET @order_c_id := (
  SELECT id
  FROM erp_purchase_order
  WHERE status = 20 AND deleted = b'0' AND id NOT IN (@order_a_id, @order_b_id)
  ORDER BY id DESC
  LIMIT 1
);

-- 如果样本不足，后续订单会回退复用
SET @order_b_id := COALESCE(@order_b_id, @order_a_id);
SET @order_c_id := COALESCE(@order_c_id, @order_b_id, @order_a_id);

SET @warehouse_a_id := (
  SELECT id
  FROM erp_warehouse
  WHERE deleted = b'0'
  ORDER BY id
  LIMIT 1
);

SET @warehouse_b_id := (
  SELECT id
  FROM erp_warehouse
  WHERE deleted = b'0' AND id <> @warehouse_a_id
  ORDER BY id
  LIMIT 1
);

SET @warehouse_b_id := COALESCE(@warehouse_b_id, @warehouse_a_id);

-- 从采购订单复制基础信息，确保 supplier/account/orderNo 一致
DROP TEMPORARY TABLE IF EXISTS tmp_purchase_return_seed_base;
CREATE TEMPORARY TABLE tmp_purchase_return_seed_base AS
SELECT
  po.id AS order_id,
  po.no AS order_no,
  po.supplier_id,
  po.account_id,
  poi.id AS order_item_id,
  poi.product_id,
  poi.product_unit_id,
  poi.product_price,
  poi.tax_percent,
  poi.tax_price,
  poi.count AS order_item_count,
  poi.total_price AS order_item_total_price
FROM erp_purchase_order po
JOIN erp_purchase_order_items poi ON poi.order_id = po.id AND poi.deleted = b'0'
WHERE po.deleted = b'0'
  AND po.id IN (@order_a_id, @order_b_id, @order_c_id);

-- 为多商品 / 多仓测试准备第二条订单项
SET @order_a_item_1 := (
  SELECT order_item_id
  FROM tmp_purchase_return_seed_base
  WHERE order_id = @order_a_id
  ORDER BY order_item_id
  LIMIT 1
);

SET @order_a_item_2 := (
  SELECT order_item_id
  FROM tmp_purchase_return_seed_base
  WHERE order_id = @order_a_id AND order_item_id <> @order_a_item_1
  ORDER BY order_item_id
  LIMIT 1
);

SET @order_b_item_1 := (
  SELECT order_item_id
  FROM tmp_purchase_return_seed_base
  WHERE order_id = @order_b_id
  ORDER BY order_item_id
  LIMIT 1
);

SET @order_c_item_1 := (
  SELECT order_item_id
  FROM tmp_purchase_return_seed_base
  WHERE order_id = @order_c_id
  ORDER BY order_item_id
  LIMIT 1
);

-- 如果同一个订单没有第二条明细，则回退到另一个已审核订单的首条明细
SET @order_a_item_2 := COALESCE(@order_a_item_2, @order_b_item_1, @order_c_item_1, @order_a_item_1);

DROP TEMPORARY TABLE IF EXISTS tmp_purchase_return_seed_items;
CREATE TEMPORARY TABLE tmp_purchase_return_seed_items AS
SELECT *
FROM tmp_purchase_return_seed_base
WHERE order_item_id IN (@order_a_item_1, @order_a_item_2, @order_b_item_1, @order_c_item_1);

-- 兜底：如果没有可用订单，直接阻断当前事务
SET @seed_order_count := (SELECT COUNT(*) FROM tmp_purchase_return_seed_items);

-- 6 张验证单主表
INSERT INTO erp_purchase_return (
  no,
  status,
  process_instance_id,
  supplier_id,
  account_id,
  return_time,
  order_id,
  order_no,
  total_count,
  total_price,
  refund_price,
  total_product_price,
  total_tax_price,
  discount_percent,
  discount_price,
  other_price,
  file_url,
  remark,
  creator,
  updater,
  create_time,
  update_time,
  deleted
)
SELECT *
FROM (
  SELECT
    'PRTEST-RET-20260703-001' AS no,
    0 AS status,
    NULL AS process_instance_id,
    i.supplier_id,
    i.account_id,
    '2026-07-03 09:10:00' AS return_time,
    i.order_id,
    i.order_no,
    2.000000 AS total_count,
    ROUND(2 * i.product_price + IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6) AS total_price,
    0.000000 AS refund_price,
    ROUND(2 * i.product_price, 6) AS total_product_price,
    ROUND(IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6) AS total_tax_price,
    0.000000 AS discount_percent,
    0.000000 AS discount_price,
    0.000000 AS other_price,
    NULL AS file_url,
    '页面验证-草稿-未退款' AS remark,
    @seed_creator AS creator,
    @seed_updater AS updater,
    '2026-07-03 09:10:00' AS create_time,
    '2026-07-03 09:10:00' AS update_time,
    b'0' AS deleted
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    'PRTEST-RET-20260703-002',
    10,
    'PRTEST-PROC-20260703-002',
    i.supplier_id,
    i.account_id,
    '2026-07-03 09:30:00',
    i.order_id,
    i.order_no,
    3.000000,
    ROUND(3 * i.product_price + IFNULL(i.tax_percent, 0) * (3 * i.product_price) / 100, 6),
    0.000000,
    ROUND(3 * i.product_price, 6),
    ROUND(IFNULL(i.tax_percent, 0) * (3 * i.product_price) / 100, 6),
    0.000000,
    0.000000,
    0.000000,
    NULL,
    '页面验证-审批中-未退款',
    @seed_creator,
    @seed_updater,
    '2026-07-03 09:30:00',
    '2026-07-03 09:30:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_b_item_1

  UNION ALL

  SELECT
    'PRTEST-RET-20260703-003',
    20,
    NULL,
    i.supplier_id,
    i.account_id,
    '2026-07-03 10:00:00',
    i.order_id,
    i.order_no,
    4.000000,
    ROUND(4 * i.product_price + IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100, 6),
    ROUND((4 * i.product_price + IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100) * 0.45, 6),
    ROUND(4 * i.product_price, 6),
    ROUND(IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100, 6),
    0.000000,
    0.000000,
    0.000000,
    NULL,
    '页面验证-已审核-部分退款',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:00:00',
    '2026-07-03 10:00:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_c_item_1

  UNION ALL

  SELECT
    'PRTEST-RET-20260703-004',
    20,
    NULL,
    i.supplier_id,
    i.account_id,
    '2026-07-03 10:20:00',
    i.order_id,
    i.order_no,
    1.000000,
    ROUND(1 * i.product_price + IFNULL(i.tax_percent, 0) * (1 * i.product_price) / 100, 6),
    ROUND(1 * i.product_price + IFNULL(i.tax_percent, 0) * (1 * i.product_price) / 100, 6),
    ROUND(1 * i.product_price, 6),
    ROUND(IFNULL(i.tax_percent, 0) * (1 * i.product_price) / 100, 6),
    0.000000,
    0.000000,
    0.000000,
    NULL,
    '页面验证-已审核-全部退款',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:20:00',
    '2026-07-03 10:20:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    'PRTEST-RET-20260703-005',
    30,
    NULL,
    i.supplier_id,
    i.account_id,
    '2026-07-03 10:40:00',
    i.order_id,
    i.order_no,
    7.000000,
    ROUND(
      (4 * i.product_price) +
      (3 * j.product_price) +
      IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100 +
      IFNULL(j.tax_percent, 0) * (3 * j.product_price) / 100,
      6
    ),
    0.000000,
    ROUND((4 * i.product_price) + (3 * j.product_price), 6),
    ROUND(
      IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100 +
      IFNULL(j.tax_percent, 0) * (3 * j.product_price) / 100,
      6
    ),
    0.000000,
    0.000000,
    0.000000,
    NULL,
    '页面验证-已驳回-多商品多仓-用于验证仓库聚合和操作文案',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:40:00',
    '2026-07-03 10:40:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  JOIN tmp_purchase_return_seed_items j ON j.order_item_id = @order_a_item_2
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    'PRTEST-RET-20260703-006',
    20,
    NULL,
    i.supplier_id,
    i.account_id,
    '2026-07-03 11:00:00',
    i.order_id,
    i.order_no,
    2.000000,
    ROUND(2 * i.product_price + IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6),
    0.000000,
    ROUND(2 * i.product_price, 6),
    ROUND(IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6),
    0.000000,
    0.000000,
    0.000000,
    NULL,
    '页面验证-超长备注-供应商与订单侧需验证截断表现；这是一个用于观察表格文本压力和移动端换行策略的长备注样本，请勿用于正式业务流转',
    @seed_creator,
    @seed_updater,
    '2026-07-03 11:00:00',
    '2026-07-03 11:00:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_b_item_1
) seeded_returns
WHERE @seed_order_count > 0;

-- 取回新生成的退货单主键
SET @ret_001_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-001' LIMIT 1);
SET @ret_002_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-002' LIMIT 1);
SET @ret_003_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-003' LIMIT 1);
SET @ret_004_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-004' LIMIT 1);
SET @ret_005_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-005' LIMIT 1);
SET @ret_006_id := (SELECT id FROM erp_purchase_return WHERE no = 'PRTEST-RET-20260703-006' LIMIT 1);

-- 明细
INSERT INTO erp_purchase_return_items (
  return_id,
  order_item_id,
  warehouse_id,
  product_id,
  product_unit_id,
  product_price,
  count,
  total_price,
  tax_percent,
  tax_price,
  remark,
  creator,
  updater,
  create_time,
  update_time,
  deleted
)
SELECT *
FROM (
  SELECT
    @ret_001_id,
    i.order_item_id,
    @warehouse_a_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    2.000000,
    ROUND(2 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6),
    '草稿样本',
    @seed_creator,
    @seed_updater,
    '2026-07-03 09:10:00',
    '2026-07-03 09:10:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    @ret_002_id,
    i.order_item_id,
    @warehouse_a_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    3.000000,
    ROUND(3 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (3 * i.product_price) / 100, 6),
    '审批中样本',
    @seed_creator,
    @seed_updater,
    '2026-07-03 09:30:00',
    '2026-07-03 09:30:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_b_item_1

  UNION ALL

  SELECT
    @ret_003_id,
    i.order_item_id,
    @warehouse_a_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    4.000000,
    ROUND(4 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100, 6),
    '部分退款样本',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:00:00',
    '2026-07-03 10:00:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_c_item_1

  UNION ALL

  SELECT
    @ret_004_id,
    i.order_item_id,
    @warehouse_a_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    1.000000,
    ROUND(1 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (1 * i.product_price) / 100, 6),
    '全部退款样本',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:20:00',
    '2026-07-03 10:20:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    @ret_005_id,
    i.order_item_id,
    @warehouse_a_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    4.000000,
    ROUND(4 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (4 * i.product_price) / 100, 6),
    '多仓样本-A',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:40:00',
    '2026-07-03 10:40:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_a_item_1

  UNION ALL

  SELECT
    @ret_005_id,
    j.order_item_id,
    @warehouse_b_id,
    j.product_id,
    j.product_unit_id,
    j.product_price,
    3.000000,
    ROUND(3 * j.product_price, 6),
    j.tax_percent,
    ROUND(IFNULL(j.tax_percent, 0) * (3 * j.product_price) / 100, 6),
    '多仓样本-B',
    @seed_creator,
    @seed_updater,
    '2026-07-03 10:40:00',
    '2026-07-03 10:40:00',
    b'0'
  FROM tmp_purchase_return_seed_items j
  WHERE j.order_item_id = @order_a_item_2

  UNION ALL

  SELECT
    @ret_006_id,
    i.order_item_id,
    @warehouse_b_id,
    i.product_id,
    i.product_unit_id,
    i.product_price,
    2.000000,
    ROUND(2 * i.product_price, 6),
    i.tax_percent,
    ROUND(IFNULL(i.tax_percent, 0) * (2 * i.product_price) / 100, 6),
    '长文本压力样本',
    @seed_creator,
    @seed_updater,
    '2026-07-03 11:00:00',
    '2026-07-03 11:00:00',
    b'0'
  FROM tmp_purchase_return_seed_items i
  WHERE i.order_item_id = @order_b_item_1
) seeded_items
WHERE @seed_order_count > 0
  AND seeded_items.return_id IS NOT NULL;

-- 回写采购订单项退货数量，确保采购订单/关联页显示一致
UPDATE erp_purchase_order_items poi
JOIN (
  SELECT order_item_id, SUM(count) AS return_count
  FROM erp_purchase_return_items
  WHERE return_id IN (@ret_001_id, @ret_002_id, @ret_003_id, @ret_004_id, @ret_005_id, @ret_006_id)
  GROUP BY order_item_id
) agg ON agg.order_item_id = poi.id
SET poi.return_count = IFNULL(poi.return_count, 0) + agg.return_count;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

-- 导入后建议验证：
-- 1. 采购退货台账：/scm/return
-- 2. 筛选“退款状态”应能看到 未退款 / 部分退款 / 全部退款
-- 3. 状态应覆盖 草稿 / 审批中 / 已审核 / 已驳回
-- 4. 编号 PRTEST-RET-20260703-005 应展示多商品、多仓库聚合
