-- 最终部门岗位模板导入 SQL（按最新图片修订）
-- 适用：ruoyi-vue-pro / MySQL
-- 模型：同部门 + 同岗位 = 一条岗位模板；重复岗位合并到 staff_quota（编制人数）
-- 修订点：
-- 1. 供应链部改为：采购工程师、库房专员
-- 2. 软件部改为：FPGA工程师 2 人，不再保留 RPGA工程师
-- 3. 其它重复岗位按图片数量汇总到 staff_quota

SET @ROOT_DEPT_ID = 100;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_post_template;
CREATE TEMPORARY TABLE tmp_post_template (
    code VARCHAR(64) NOT NULL,
    dept_id BIGINT NOT NULL,
    post_name VARCHAR(64) NOT NULL,
    staff_quota INT NOT NULL,
    sort INT NOT NULL,
    level VARCHAR(16) NOT NULL DEFAULT '未分级',
    key_position BIT(1) NOT NULL DEFAULT b'0',
    allow_part_time BIT(1) NOT NULL DEFAULT b'0',
    remark VARCHAR(255) NULL
);

-- ========== 1. 一级部门 ==========
INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT t.name, @ROOT_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT '统计部' AS name, 10 AS sort
    UNION ALL SELECT '财务部', 20
    UNION ALL SELECT '人力资源部', 30
    UNION ALL SELECT '供应链部', 40
    UNION ALL SELECT '工艺部', 50
    UNION ALL SELECT '市场营销部', 60
    UNION ALL SELECT '质量部', 70
    UNION ALL SELECT '专家办', 80
    UNION ALL SELECT '研发部', 90
    UNION ALL SELECT '制造部', 100
) t
WHERE NOT EXISTS (
    SELECT 1 FROM system_dept d
    WHERE d.name = t.name
      AND d.parent_id = @ROOT_DEPT_ID

      AND d.deleted = b'0'
);

SET @STAT_DEPT_ID = (SELECT id FROM system_dept WHERE name = '统计部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @FINANCE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '财务部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @HR_DEPT_ID = (SELECT id FROM system_dept WHERE name = '人力资源部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @SUPPLY_DEPT_ID = (SELECT id FROM system_dept WHERE name = '供应链部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @PROCESS_DEPT_ID = (SELECT id FROM system_dept WHERE name = '工艺部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @MARKETING_DEPT_ID = (SELECT id FROM system_dept WHERE name = '市场营销部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @QUALITY_DEPT_ID = (SELECT id FROM system_dept WHERE name = '质量部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @EXPERT_DEPT_ID = (SELECT id FROM system_dept WHERE name = '专家办' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_DEPT_ID = (SELECT id FROM system_dept WHERE name = '研发部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @MANUFACTURE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '制造部' AND parent_id = @ROOT_DEPT_ID AND deleted = b'0' LIMIT 1);

-- ========== 2. 研发部二级部门 ==========
INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT t.name, @RND_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT '系统部' AS name, 10 AS sort
    UNION ALL SELECT '软件部', 20
    UNION ALL SELECT '硬件部', 30
) t
WHERE @RND_DEPT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_dept d
      WHERE d.name = t.name
        AND d.parent_id = @RND_DEPT_ID

        AND d.deleted = b'0'
  );

SET @RND_SYSTEM_DEPT_ID = (SELECT id FROM system_dept WHERE name = '系统部' AND parent_id = @RND_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_SOFTWARE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '软件部' AND parent_id = @RND_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_HARDWARE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '硬件部' AND parent_id = @RND_DEPT_ID AND deleted = b'0' LIMIT 1);

-- ========== 3. 系统部三级部门 ==========
INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT t.name, @RND_SYSTEM_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT '调测组' AS name, 10 AS sort
    UNION ALL SELECT '测试组', 20
    UNION ALL SELECT '射频组', 30
    UNION ALL SELECT '结构组', 40
) t
WHERE @RND_SYSTEM_DEPT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_dept d
      WHERE d.name = t.name
        AND d.parent_id = @RND_SYSTEM_DEPT_ID

        AND d.deleted = b'0'
  );

SET @RND_TIAOCE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '调测组' AND parent_id = @RND_SYSTEM_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_TEST_DEPT_ID = (SELECT id FROM system_dept WHERE name = '测试组' AND parent_id = @RND_SYSTEM_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_RF_DEPT_ID = (SELECT id FROM system_dept WHERE name = '射频组' AND parent_id = @RND_SYSTEM_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @RND_STRUCTURE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '结构组' AND parent_id = @RND_SYSTEM_DEPT_ID AND deleted = b'0' LIMIT 1);

-- ========== 4. 制造部二级部门 ==========
INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT t.name, @MANUFACTURE_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0'
FROM (
    SELECT 'SMT组' AS name, 10 AS sort
    UNION ALL SELECT '电装组', 20
    UNION ALL SELECT '粘接1组', 30
    UNION ALL SELECT '粘接2组', 40
    UNION ALL SELECT '纤焊组', 50
    UNION ALL SELECT '键合组', 60
    UNION ALL SELECT '激光封焊组', 70
) t
WHERE @MANUFACTURE_DEPT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_dept d
      WHERE d.name = t.name
        AND d.parent_id = @MANUFACTURE_DEPT_ID

        AND d.deleted = b'0'
  );

SET @SMT_DEPT_ID = (SELECT id FROM system_dept WHERE name = 'SMT组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @ASSEMBLY_DEPT_ID = (SELECT id FROM system_dept WHERE name = '电装组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @BOND1_DEPT_ID = (SELECT id FROM system_dept WHERE name = '粘接1组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @BOND2_DEPT_ID = (SELECT id FROM system_dept WHERE name = '粘接2组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @SOLDER_DEPT_ID = (SELECT id FROM system_dept WHERE name = '纤焊组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @WIRE_DEPT_ID = (SELECT id FROM system_dept WHERE name = '键合组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);
SET @LASER_DEPT_ID = (SELECT id FROM system_dept WHERE name = '激光封焊组' AND parent_id = @MANUFACTURE_DEPT_ID AND deleted = b'0' LIMIT 1);

-- ========== 5. 岗位模板 ==========
INSERT INTO tmp_post_template (code, dept_id, post_name, staff_quota, sort, level, key_position, allow_part_time, remark) VALUES
('stat_project_assistant', @STAT_DEPT_ID, '项目助理', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('stat_project_management', @STAT_DEPT_ID, '项目管理', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),

('finance_accountant', @FINANCE_DEPT_ID, '会计', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('finance_cashier', @FINANCE_DEPT_ID, '出纳', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),

('hr_it', @HR_DEPT_ID, 'IT', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('hr_admin', @HR_DEPT_ID, '行政', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('hr_training', @HR_DEPT_ID, '培训', 1, 30, '未分级', b'0', b'0', '来源：最新图片导入'),
('hr_recruitment', @HR_DEPT_ID, '招聘', 1, 40, '未分级', b'0', b'0', '来源：最新图片导入'),
('hr_confidentiality', @HR_DEPT_ID, '保密专员', 1, 50, '未分级', b'0', b'0', '来源：最新图片导入'),

('supply_purchase_engineer', @SUPPLY_DEPT_ID, '采购工程师', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('supply_warehouse_specialist', @SUPPLY_DEPT_ID, '库房专员', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),

('process_engineer', @PROCESS_DEPT_ID, '工艺工程师', 3, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('marketing_customer_manager', @MARKETING_DEPT_ID, '客户经理', 2, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('marketing_sales_assistant', @MARKETING_DEPT_ID, '销售助理', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('marketing_sales_director', @MARKETING_DEPT_ID, '销售总监', 2, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('quality_system_engineer', @QUALITY_DEPT_ID, '体系工程师', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('quality_assistant', @QUALITY_DEPT_ID, '质量助理', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('quality_inspector', @QUALITY_DEPT_ID, '检验员', 5, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('expert_presales_support', @EXPERT_DEPT_ID, '售前技术支持', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('tiaoce_test_technician', @RND_TIAOCE_DEPT_ID, '测试技术员', 3, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('tiaoce_micro_assembly_test_engineer', @RND_TIAOCE_DEPT_ID, '微组装调测试工程师', 6, 20, '未分级', b'0', b'0', '来源：最新图片导入'),

('test_product_debug_engineer', @RND_TEST_DEPT_ID, '产品调试工程师', 3, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('test_software_engineer', @RND_TEST_DEPT_ID, '软件测试工程师', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('test_machine_engineer', @RND_TEST_DEPT_ID, '整机测试工程师', 1, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('rf_engineer', @RND_RF_DEPT_ID, '射频工程师', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('structure_engineer', @RND_STRUCTURE_DEPT_ID, '结构工程师', 2, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('software_cpp_engineer', @RND_SOFTWARE_DEPT_ID, 'C++研发工程师', 2, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('software_embedded_engineer', @RND_SOFTWARE_DEPT_ID, '嵌入式工程师', 7, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('software_embedded_intern', @RND_SOFTWARE_DEPT_ID, '嵌入式实习生', 1, 30, '未分级', b'0', b'0', '来源：最新图片导入'),
('software_information_engineer', @RND_SOFTWARE_DEPT_ID, '信息化工程师', 1, 40, '未分级', b'0', b'0', '来源：最新图片导入'),
('software_fpga_engineer', @RND_SOFTWARE_DEPT_ID, 'FPGA工程师', 2, 50, '未分级', b'0', b'0', '来源：最新图片导入'),
('software_pcb_engineer', @RND_SOFTWARE_DEPT_ID, 'PCB工程师', 1, 60, '未分级', b'0', b'0', '来源：最新图片导入'),

('hardware_component_engineer', @RND_HARDWARE_DEPT_ID, '器件工程师', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('hardware_rd_assistant', @RND_HARDWARE_DEPT_ID, '研发助理', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('hardware_engineer', @RND_HARDWARE_DEPT_ID, '硬件工程师', 6, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('manufacture_production_planner', @MANUFACTURE_DEPT_ID, '生产计划员', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('manufacture_logistics_support', @MANUFACTURE_DEPT_ID, '后勤员', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('manufacture_logistics_operator', @MANUFACTURE_DEPT_ID, '物流员', 1, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('smt_operator', @SMT_DEPT_ID, 'SMT', 3, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('assembly_operator', @ASSEMBLY_DEPT_ID, '电装', 8, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('bond1_chip_operator', @BOND1_DEPT_ID, '粘片操作员', 4, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('bond2_chip_operator', @BOND2_DEPT_ID, '粘片操作员', 4, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('solder_pack_helper', @SOLDER_DEPT_ID, '辅助操作员（包装）', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入'),
('solder_clean_helper', @SOLDER_DEPT_ID, '辅助操作员（清洗）', 1, 20, '未分级', b'0', b'0', '来源：最新图片导入'),
('solder_operator', @SOLDER_DEPT_ID, '纤焊操作员', 1, 30, '未分级', b'0', b'0', '来源：最新图片导入'),

('wire_bond_operator', @WIRE_DEPT_ID, '键合操作员', 4, 10, '未分级', b'0', b'0', '来源：最新图片导入'),

('laser_sealing_operator', @LASER_DEPT_ID, '激光封焊', 1, 10, '未分级', b'0', b'0', '来源：最新图片导入');

UPDATE system_post p
JOIN tmp_post_template t
  ON p.dept_id = t.dept_id
 AND p.name = t.post_name

 AND p.deleted = b'0'
SET p.code = t.code,
    p.level = t.level,
    p.staff_quota = t.staff_quota,
    p.key_position = t.key_position,
    p.allow_part_time = t.allow_part_time,
    p.sort = t.sort,
    p.status = 0,
    p.remark = t.remark,
    p.updater = 'admin',
    p.update_time = NOW();

INSERT INTO system_post
(`code`, `name`, `level`, `dept_id`, `staff_quota`, `key_position`, `allow_part_time`,
 `sort`, `status`, `job_description`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
    t.code, t.post_name, t.level, t.dept_id, t.staff_quota, t.key_position, t.allow_part_time,
    t.sort, 0, NULL, t.remark,
    'admin', NOW(), 'admin', NOW(), b'0'
FROM tmp_post_template t
WHERE t.dept_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_post p
      WHERE p.dept_id = t.dept_id
        AND p.name = t.post_name

        AND p.deleted = b'0'
  );

SET @LEVEL_HIGH = CONVERT(0xE9AB98E7BAA7 USING utf8mb4);
SET @LEVEL_MIDDLE = CONVERT(0xE4B8ADE7BAA7 USING utf8mb4);
SET @LEVEL_PRIMARY = CONVERT(0xE5889DE7BAA7 USING utf8mb4);

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

DROP TEMPORARY TABLE IF EXISTS tmp_post_template;

COMMIT;
