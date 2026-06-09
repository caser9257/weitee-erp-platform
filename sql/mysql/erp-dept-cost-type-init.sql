-- 部门成本类型初始化（精简版）
-- 只执行部门 cost_type 配置，其他表的初始化暂跳过

-- 制造费用 (1)
UPDATE `system_dept` SET `cost_type` = 1
WHERE `name` IN ('制造部', 'SMT组', '电装组', '粘接1组', '粘接2组', '纤焊组', '键合组', '激光封焊组', '工艺部');

-- 管理费用 (2)
UPDATE `system_dept` SET `cost_type` = 2
WHERE `name` IN ('财务部', '人力资源部', '供应链部', '质量部', '统计部', '专家办');

-- 销售费用 (3)
UPDATE `system_dept` SET `cost_type` = 3
WHERE `name` IN ('市场营销部');

-- 研发支出 (4)
UPDATE `system_dept` SET `cost_type` = 4
WHERE `name` IN ('研发部', '系统部', '软件部', '硬件部');

-- 验证结果
SELECT id, name,
  CASE cost_type
    WHEN 1 THEN '制造费用'
    WHEN 2 THEN '管理费用'
    WHEN 3 THEN '销售费用'
    WHEN 4 THEN '研发支出'
    ELSE '未配置'
  END AS cost_type_name
FROM `system_dept`
WHERE `deleted` = b'0'
ORDER BY `parent_id`, `sort`;
