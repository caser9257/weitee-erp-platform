-- post level finalization for existing data
-- purpose:
-- 1. backfill known post levels in system_post
-- 2. remove current unclassified dirty values for imported post codes
-- 3. keep a final check query at the end

SET @LEVEL_HIGH = CONVERT(0xE9AB98E7BAA7 USING utf8mb4);
SET @LEVEL_MIDDLE = CONVERT(0xE4B8ADE7BAA7 USING utf8mb4);
SET @LEVEL_PRIMARY = CONVERT(0xE5889DE7BAA7 USING utf8mb4);
SET @LEVEL_UNCLASSIFIED = CONVERT(0xE69CAAE58886E7BAA7 USING utf8mb4);

START TRANSACTION;

UPDATE system_post
SET level = CASE
    WHEN code IN (
        'stat_project_management',
        'marketing_customer_manager',
        'marketing_sales_director'
    ) THEN @LEVEL_HIGH
    WHEN code IN (
        'finance_accountant',
        'hr_it',
        'hr_admin',
        'hr_training',
        'hr_recruitment',
        'hr_confidentiality',
        'supply_purchase_engineer',
        'process_engineer',
        'quality_system_engineer',
        'expert_presales_support',
        'tiaoce_micro_assembly_test_engineer',
        'test_product_debug_engineer',
        'test_software_engineer',
        'test_machine_engineer',
        'rf_engineer',
        'structure_engineer',
        'software_cpp_engineer',
        'software_embedded_engineer',
        'software_information_engineer',
        'software_fpga_engineer',
        'software_pcb_engineer',
        'hardware_component_engineer',
        'hardware_engineer',
        'manufacture_production_planner',
        'software_rpga_engineer'
    ) THEN @LEVEL_MIDDLE
    WHEN code IN (
        'stat_project_assistant',
        'finance_cashier',
        'supply_warehouse_specialist',
        'marketing_sales_assistant',
        'quality_assistant',
        'quality_inspector',
        'tiaoce_test_technician',
        'software_embedded_intern',
        'hardware_rd_assistant',
        'manufacture_logistics_support',
        'manufacture_logistics_operator',
        'smt_operator',
        'assembly_operator',
        'bond1_chip_operator',
        'bond2_chip_operator',
        'solder_pack_helper',
        'solder_clean_helper',
        'solder_operator',
        'wire_bond_operator',
        'laser_sealing_operator'
    ) THEN @LEVEL_PRIMARY
    ELSE level
END,
updater = 'admin',
update_time = NOW()
  AND deleted = b'0'
  AND code IN (
    'stat_project_management',
    'marketing_customer_manager',
    'marketing_sales_director',
    'finance_accountant',
    'hr_it',
    'hr_admin',
    'hr_training',
    'hr_recruitment',
    'hr_confidentiality',
    'supply_purchase_engineer',
    'process_engineer',
    'quality_system_engineer',
    'expert_presales_support',
    'tiaoce_micro_assembly_test_engineer',
    'test_product_debug_engineer',
    'test_software_engineer',
    'test_machine_engineer',
    'rf_engineer',
    'structure_engineer',
    'software_cpp_engineer',
    'software_embedded_engineer',
    'software_information_engineer',
    'software_fpga_engineer',
    'software_pcb_engineer',
    'hardware_component_engineer',
    'hardware_engineer',
    'manufacture_production_planner',
    'software_rpga_engineer',
    'stat_project_assistant',
    'finance_cashier',
    'supply_warehouse_specialist',
    'marketing_sales_assistant',
    'quality_assistant',
    'quality_inspector',
    'tiaoce_test_technician',
    'software_embedded_intern',
    'hardware_rd_assistant',
    'manufacture_logistics_support',
    'manufacture_logistics_operator',
    'smt_operator',
    'assembly_operator',
    'bond1_chip_operator',
    'bond2_chip_operator',
    'solder_pack_helper',
    'solder_clean_helper',
    'solder_operator',
    'wire_bond_operator',
    'laser_sealing_operator'
  );

COMMIT;

-- if this result set is empty, current imported known posts no longer remain unclassified
SELECT id, dept_id, code, name, level
FROM system_post
  AND deleted = b'0'
  AND (level IS NULL OR TRIM(level) = '' OR level = @LEVEL_UNCLASSIFIED)
ORDER BY dept_id, sort, id;
