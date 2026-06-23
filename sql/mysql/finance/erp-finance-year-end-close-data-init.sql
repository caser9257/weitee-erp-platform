-- 年末财务决算补强方案 — 数据初始化（去租户化版本）
-- 说明：
-- 1. 本脚本第 1 节已完整覆盖历史脚本 erp-dept-cost-type-init.sql
-- 2. 如仅需初始化部门 cost_type，也应优先复用本脚本对应片段，避免维护重复脚本

-- ============================================================
-- 1. 部门成本类型初始化
-- ============================================================

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

-- ============================================================
-- 2. 库存成本初始化（按最近采购价填充）
-- ============================================================
UPDATE `erp_stock` s
SET `average_cost` = (
    SELECT pi.`price` FROM `erp_purchase_in_item` pi
    INNER JOIN `erp_purchase_in` p ON pi.`in_id` = p.`id`
    WHERE pi.`product_id` = s.`product_id` AND p.`status` = 20
    ORDER BY p.`create_time` DESC LIMIT 1
),
`total_cost` = s.`count` * COALESCE((
    SELECT pi.`price` FROM `erp_purchase_in_item` pi
    INNER JOIN `erp_purchase_in` p ON pi.`in_id` = p.`id`
    WHERE pi.`product_id` = s.`product_id` AND p.`status` = 20
    ORDER BY p.`create_time` DESC LIMIT 1
), 0)
WHERE s.`count` > 0 AND (s.`average_cost` IS NULL OR s.`average_cost` = 0);

-- ============================================================
-- 3. 资产类型初始化（全部标记为固定资产）
-- ============================================================
UPDATE `erp_finance_asset` SET `asset_type` = 0 WHERE `asset_type` IS NULL;

-- ============================================================
-- 4. 费用类型字典：新增"仪器租赁费"
-- ============================================================
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `biz_attributes`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1500, 100, '仪器租赁费', '100', 'erp_expense_type', 0, '', '', '仪器设备租赁费用',
        '{"core":false,"costCenterRequired":true,"leaseContractRequired":true,"category":"LEASE"}',
        '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `biz_attributes` = VALUES(`biz_attributes`);

-- ============================================================
-- 5. 凭证模板配置
-- ============================================================

-- 无形资产摊销凭证模板
INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (200, 1, 90, '无形资产摊销凭证模板', 0, b'1', '无形资产摊销', '无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 盘亏结转凭证模板
INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (201, 1, 91, '盘亏结转凭证模板', 0, b'1', '盘亏结转', '盘点盘亏自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 会计科目：1702 累计摊销
INSERT INTO `erp_finance_subject` (`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1702, 1, NULL, '1702', '累计摊销', 1, 2, b'1', 0, 0, '无形资产摊销备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 会计科目：1901 待处理财产损溢
INSERT INTO `erp_finance_subject` (`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1901, 1, NULL, '1901', '待处理财产损溢', 1, 1, b'1', 0, 0, '盘盈盘亏待处理科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- ============================================================
-- 6. 验证：部门成本类型配置结果
-- ============================================================
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
