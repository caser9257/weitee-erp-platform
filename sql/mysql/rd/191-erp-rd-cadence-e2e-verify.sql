-- Cadence 端到端联调校验脚本（ERP → v_cadence_component → ODBC 数据边界）
-- 目标：验证“已审核 + 启用 + is_pcb_component=1 + schematic_part/pcb_footprint 完整”的物料出现在视图；
--       非 PCB 元器件 / 不完整（缺 schematic_part）的物料被排除。脚本自带清理，可重复执行。

-- 1) 清理历史残留，保证可重复执行
DELETE FROM erp_product_cadence WHERE product_id IN (SELECT id FROM erp_product WHERE material_code = 'E2E_TMP_CADENCE_0001' AND deleted = b'0');
DELETE FROM erp_product WHERE material_code = 'E2E_TMP_CADENCE_0001' AND deleted = b'0';

-- 2) 插入一个“已审核 + 启用 + PCB 元器件”的临时物料
INSERT INTO erp_product
  (name, material_code, bar_code, category_id, unit_id, product_type, produce_type,
   batch_enable, sn_enable, qc_enable, outsource_enable, cost_method,
   status, audit_status, batch_control_flag, inspection_required_flag, mrp_enable,
   is_pcb_component, creator, create_time, update_time, deleted)
VALUES
  ('E2E临时PCB元件', 'E2E_TMP_CADENCE_0001', 'E2E_TMP_BC_0001', 1, 1, 1, 1,
   1, 0, 0, 0, 1,
   0, 20, 0, 0, 1,
   1, '1', NOW(), NOW(), b'0');

SET @pid = LAST_INSERT_ID();

-- 3) 写入完整 Cadence 扩展数据
INSERT INTO erp_product_cadence
  (product_id, schematic_part, pcb_footprint, cadence_description, manufacturer_part_number, create_time, update_time, deleted)
VALUES
  (@pid, 'R_0603_10K', '0603', '10K 1%', 'MFR-001', NOW(), NOW(), b'0');

-- 4) CASE1：完整 PCB 元件应出现在英文视图中
SELECT 'Case1_完整PCB元件应出现在视图' AS case_desc, COUNT(*) AS row_count
FROM v_cadence_component WHERE Part_Number = 'E2E_TMP_CADENCE_0001';

-- 5) CASE2：模拟不完整（清空 schematic_part），应被排除
UPDATE erp_product_cadence SET schematic_part = NULL WHERE product_id = @pid;
SELECT 'Case2_缺schematic_part应被排除' AS case_desc, COUNT(*) AS row_count
FROM v_cadence_component WHERE Part_Number = 'E2E_TMP_CADENCE_0001';

-- 6) 复原并模拟非 PCB 元器件，应被排除
UPDATE erp_product_cadence SET schematic_part = 'R_0603_10K' WHERE product_id = @pid;
UPDATE erp_product SET is_pcb_component = 0 WHERE id = @pid;
SELECT 'Case3_非PCB元件应被排除' AS case_desc, COUNT(*) AS row_count
FROM v_cadence_component WHERE Part_Number = 'E2E_TMP_CADENCE_0001';

-- 7) 清理临时数据
DELETE FROM erp_product_cadence WHERE product_id = @pid;
DELETE FROM erp_product WHERE id = @pid;
SELECT 'CLEANUP_DONE' AS result;
