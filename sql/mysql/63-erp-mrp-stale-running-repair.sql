/*
 Target: ERP MRP stale running plan repair
 Schema: ruoyi-vue-pro
 Date: 2026-04-23
 Purpose:
   - Repair historical plans stuck in RUNNING(10)
   - Mark plans with generated artifacts as FINISHED(20)
   - Mark plans without generated artifacts as FAILED(40)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @stale_minutes = 10;

-- Preview: stale RUNNING plans that already produced MRP artifacts
SELECT
  p.id,
  p.plan_no,
  p.plan_name,
  p.status,
  p.run_time,
  p.update_time,
  'FINISHED' AS target_status
FROM `erp_mrp_plan` p
WHERE p.deleted = b'0'
  AND p.status = 10
  AND p.run_time IS NOT NULL
  AND p.run_time <= DATE_SUB(NOW(), INTERVAL @stale_minutes MINUTE)
  AND (
    EXISTS (SELECT 1 FROM `erp_mrp_demand` d WHERE d.plan_id = p.id AND d.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_mrp_result` r WHERE r.plan_id = p.id AND r.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_mrp_shortage` s WHERE s.plan_id = p.id AND s.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_purchase_suggest` ps WHERE ps.plan_id = p.id AND ps.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_production_suggest` ms WHERE ms.plan_id = p.id AND ms.deleted = b'0')
  )
ORDER BY p.id DESC;

-- Preview: stale RUNNING plans that produced no MRP artifacts
SELECT
  p.id,
  p.plan_no,
  p.plan_name,
  p.status,
  p.run_time,
  p.update_time,
  'FAILED' AS target_status
FROM `erp_mrp_plan` p
WHERE p.deleted = b'0'
  AND p.status = 10
  AND p.run_time IS NOT NULL
  AND p.run_time <= DATE_SUB(NOW(), INTERVAL @stale_minutes MINUTE)
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_demand` d WHERE d.plan_id = p.id AND d.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_result` r WHERE r.plan_id = p.id AND r.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_shortage` s WHERE s.plan_id = p.id AND s.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_purchase_suggest` ps WHERE ps.plan_id = p.id AND ps.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_production_suggest` ms WHERE ms.plan_id = p.id AND ms.deleted = b'0')
ORDER BY p.id DESC;

-- Repair to FINISHED(20)
UPDATE `erp_mrp_plan` p
SET p.status = 20
WHERE p.deleted = b'0'
  AND p.status = 10
  AND p.run_time IS NOT NULL
  AND p.run_time <= DATE_SUB(NOW(), INTERVAL @stale_minutes MINUTE)
  AND (
    EXISTS (SELECT 1 FROM `erp_mrp_demand` d WHERE d.plan_id = p.id AND d.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_mrp_result` r WHERE r.plan_id = p.id AND r.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_mrp_shortage` s WHERE s.plan_id = p.id AND s.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_purchase_suggest` ps WHERE ps.plan_id = p.id AND ps.deleted = b'0')
    OR EXISTS (SELECT 1 FROM `erp_production_suggest` ms WHERE ms.plan_id = p.id AND ms.deleted = b'0')
  );

-- Repair to FAILED(40)
UPDATE `erp_mrp_plan` p
SET p.status = 40
WHERE p.deleted = b'0'
  AND p.status = 10
  AND p.run_time IS NOT NULL
  AND p.run_time <= DATE_SUB(NOW(), INTERVAL @stale_minutes MINUTE)
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_demand` d WHERE d.plan_id = p.id AND d.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_result` r WHERE r.plan_id = p.id AND r.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_mrp_shortage` s WHERE s.plan_id = p.id AND s.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_purchase_suggest` ps WHERE ps.plan_id = p.id AND ps.deleted = b'0')
  AND NOT EXISTS (SELECT 1 FROM `erp_production_suggest` ms WHERE ms.plan_id = p.id AND ms.deleted = b'0');

SET FOREIGN_KEY_CHECKS = 1;
