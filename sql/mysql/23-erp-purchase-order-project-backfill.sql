/*
 Target: ERP purchase order project backfill
 Schema: ruoyi-vue-pro
 Date: 2026-04-08
 Note:
   1. Safe backfill only
   2. Only fills purchase order items when one purchase order + one material maps to exactly one project
   3. Only fills purchase order header when all item projects under the order resolve to exactly one project
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- ====================
-- Preview 1: item rows that can be safely backfilled
-- ====================
SELECT
  poi.id AS purchase_item_id,
  po.no AS purchase_order_no,
  poi.order_id,
  poi.product_id,
  poi.project_id AS current_item_project_id,
  x.project_id AS resolved_project_id
FROM `erp_purchase_order_items` poi
JOIN `erp_purchase_order` po ON po.id = poi.order_id
JOIN (
  SELECT
    ps.convert_purchase_order_id AS order_id,
    ps.material_id AS product_id,
    MIN(ps.project_id) AS project_id
  FROM `erp_purchase_suggest` ps
  WHERE ps.convert_purchase_order_id IS NOT NULL
    AND ps.project_id IS NOT NULL
  GROUP BY ps.convert_purchase_order_id, ps.material_id
  HAVING COUNT(DISTINCT ps.project_id) = 1
) x
  ON x.order_id = poi.order_id
 AND x.product_id = poi.product_id
WHERE poi.project_id IS NULL
ORDER BY poi.order_id, poi.id;

-- ====================
-- Preview 2: item rows skipped because one order + one material points to multiple projects
-- ====================
SELECT
  po.no AS purchase_order_no,
  ps.convert_purchase_order_id AS order_id,
  ps.material_id AS product_id,
  COUNT(DISTINCT ps.project_id) AS distinct_project_count,
  GROUP_CONCAT(DISTINCT ps.project_id ORDER BY ps.project_id SEPARATOR ',') AS project_ids
FROM `erp_purchase_suggest` ps
JOIN `erp_purchase_order` po ON po.id = ps.convert_purchase_order_id
WHERE ps.convert_purchase_order_id IS NOT NULL
  AND ps.project_id IS NOT NULL
GROUP BY ps.convert_purchase_order_id, ps.material_id
HAVING COUNT(DISTINCT ps.project_id) > 1
ORDER BY ps.convert_purchase_order_id, ps.material_id;

-- ====================
-- Preview 3: order headers that can be safely backfilled after item backfill
-- ====================
SELECT
  po.id AS purchase_order_id,
  po.no AS purchase_order_no,
  po.project_id AS current_order_project_id,
  x.project_id AS resolved_project_id
FROM `erp_purchase_order` po
JOIN (
  SELECT
    poi.order_id,
    MIN(poi.project_id) AS project_id
  FROM `erp_purchase_order_items` poi
  WHERE poi.project_id IS NOT NULL
  GROUP BY poi.order_id
  HAVING COUNT(DISTINCT poi.project_id) = 1
) x ON x.order_id = po.id
WHERE po.project_id IS NULL
ORDER BY po.id;

START TRANSACTION;

-- ====================
-- Step 1: backfill purchase order item project_id
-- ====================
UPDATE `erp_purchase_order_items` poi
JOIN (
  SELECT
    ps.convert_purchase_order_id AS order_id,
    ps.material_id AS product_id,
    MIN(ps.project_id) AS project_id
  FROM `erp_purchase_suggest` ps
  WHERE ps.convert_purchase_order_id IS NOT NULL
    AND ps.project_id IS NOT NULL
  GROUP BY ps.convert_purchase_order_id, ps.material_id
  HAVING COUNT(DISTINCT ps.project_id) = 1
) x
  ON x.order_id = poi.order_id
 AND x.product_id = poi.product_id
SET poi.project_id = x.project_id
WHERE poi.project_id IS NULL;

-- ====================
-- Step 2: backfill purchase order header project_id
-- ====================
UPDATE `erp_purchase_order` po
JOIN (
  SELECT
    poi.order_id,
    MIN(poi.project_id) AS project_id
  FROM `erp_purchase_order_items` poi
  WHERE poi.project_id IS NOT NULL
  GROUP BY poi.order_id
  HAVING COUNT(DISTINCT poi.project_id) = 1
) x ON x.order_id = po.id
SET po.project_id = x.project_id
WHERE po.project_id IS NULL;

COMMIT;

-- ====================
-- Verify
-- ====================
SELECT
  po.id AS purchase_order_id,
  po.no AS purchase_order_no,
  po.project_id AS order_project_id,
  poi.id AS item_id,
  poi.product_id,
  poi.project_id AS item_project_id,
  poi.count
FROM `erp_purchase_order` po
LEFT JOIN `erp_purchase_order_items` poi ON poi.order_id = po.id
WHERE po.id IN (
  SELECT DISTINCT convert_purchase_order_id
  FROM `erp_purchase_suggest`
  WHERE convert_purchase_order_id IS NOT NULL
)
ORDER BY po.id, poi.id;

SET FOREIGN_KEY_CHECKS = 1;
