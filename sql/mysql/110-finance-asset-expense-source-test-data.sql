-- =====================================================
-- 固定资产“费用报销来源”测试数据
-- 用途：
-- 1. 为固定资产台账补一条 source_type = 20（费用报销）的来源数据
-- 2. 让“查看来源”可以验证费用报销来源显隐与跳转
-- 3. 仅写入测试数据，不修改接口契约与业务逻辑
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 统一租户与基础账号
SET @tenant_id := COALESCE((SELECT tenant_id FROM system_users WHERE username = '财务主管' AND deleted = b'0' LIMIT 1), 1);
SET @finance_user_id := COALESCE((SELECT id FROM system_users WHERE username = '财务主管' AND deleted = b'0' LIMIT 1), 1);
SET @dept_id := COALESCE((SELECT dept_id FROM system_users WHERE id = @finance_user_id LIMIT 1), 1);
SET @account_id := (SELECT id FROM erp_account WHERE deleted = b'0' AND tenant_id = @tenant_id ORDER BY id LIMIT 1);

-- 如果当前租户没有财务账户，则补一个最小测试账户
INSERT INTO erp_account
(`id`, `name`, `no`, `remark`, `status`, `sort`, `default_status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 99591, '固定资产来源测试账户', 'ACC-TEST-ASSET-001', '固定资产费用来源测试数据自动补齐账户', 0, 91, b'0',
       'tester', NOW(), 'tester', NOW(), b'0', @tenant_id
WHERE @account_id IS NULL;

SET @account_id := COALESCE(
  @account_id,
  (SELECT id FROM erp_account WHERE no = 'ACC-TEST-ASSET-001' AND deleted = b'0' AND tenant_id = @tenant_id LIMIT 1)
);

-- 固定测试主键
SET @expense_id := 107901;
SET @expense_item_id := 107911;
SET @candidate_id := 108901;
SET @asset_id := 108911;

-- 测试单号
SET @expense_no := 'EXP-TEST-ASSET-001';
SET @asset_no := 'FA-TEST-EXP-001';

-- 1) 确保费用报销明细支持固定资产候选标记
SET @expense_item_asset_flag_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_finance_expense_item'
    AND COLUMN_NAME = 'asset_candidate_flag'
);
SET @expense_item_asset_flag_sql := IF(
  @expense_item_asset_flag_exists > 0,
  'SELECT ''erp_finance_expense_item.asset_candidate_flag already exists''',
  'ALTER TABLE `erp_finance_expense_item` ADD COLUMN `asset_candidate_flag` BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''是否转固定资产候选'' AFTER `remark`'
);
PREPARE expense_item_asset_flag_stmt FROM @expense_item_asset_flag_sql;
EXECUTE expense_item_asset_flag_stmt;
DEALLOCATE PREPARE expense_item_asset_flag_stmt;

-- 2) 费用报销主单：expense_type = 80（零星采购），满足转固定资产候选的业务前提
INSERT INTO erp_finance_expense
(`id`, `no`, `status`, `expense_time`, `expense_type`, `dept_id`, `project_id`, `supplier_id`,
 `finance_user_id`, `account_id`, `expense_price`, `paid_price`, `remain_price`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@expense_id, @expense_no, 20, '2026-05-21 10:00:00', 80, @dept_id, NULL, NULL,
 @finance_user_id, @account_id, 6888.00, 6888.00, 0.00, '固定资产来源测试-零星采购电脑',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`no` = VALUES(`no`),
`status` = VALUES(`status`),
`expense_time` = VALUES(`expense_time`),
`expense_type` = VALUES(`expense_type`),
`dept_id` = VALUES(`dept_id`),
`project_id` = VALUES(`project_id`),
`supplier_id` = VALUES(`supplier_id`),
`finance_user_id` = VALUES(`finance_user_id`),
`account_id` = VALUES(`account_id`),
`expense_price` = VALUES(`expense_price`),
`paid_price` = VALUES(`paid_price`),
`remain_price` = VALUES(`remain_price`),
`remark` = VALUES(`remark`),
`updater` = 'tester',
`update_time` = NOW(),
`deleted` = b'0',
`tenant_id` = VALUES(`tenant_id`);

-- 3) 费用报销明细：asset_candidate_flag = 1，且明细名称带“电脑”，便于追溯展示
INSERT INTO erp_finance_expense_item
(`id`, `expense_id`, `item_name`, `amount`, `remark`, `asset_candidate_flag`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@expense_item_id, @expense_id, '办公电脑', 6888.00, '用于固定资产来源跳转测试', b'1',
 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`expense_id` = VALUES(`expense_id`),
`item_name` = VALUES(`item_name`),
`amount` = VALUES(`amount`),
`remark` = VALUES(`remark`),
`asset_candidate_flag` = VALUES(`asset_candidate_flag`),
`updater` = 'tester',
`update_time` = NOW(),
`deleted` = b'0',
`tenant_id` = VALUES(`tenant_id`);

-- 4) 固定资产候选：source_type = 20（费用报销），source_item_id 对应上面的报销明细
INSERT INTO erp_finance_asset_candidate
(`id`, `source_type`, `source_biz_id`, `source_biz_no`, `source_item_id`, `product_id`,
 `asset_name`, `category_name`, `amount`, `purchase_date`, `dept_id`, `responsible_user_id`,
 `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@candidate_id, 20, @expense_id, @expense_no, @expense_item_id, NULL,
 '办公电脑', '零星采购', 6888.00, '2026-05-21', @dept_id, @finance_user_id,
 10, '固定资产来源测试候选-由费用报销生成', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`source_type` = VALUES(`source_type`),
`source_biz_id` = VALUES(`source_biz_id`),
`source_biz_no` = VALUES(`source_biz_no`),
`source_item_id` = VALUES(`source_item_id`),
`product_id` = VALUES(`product_id`),
`asset_name` = VALUES(`asset_name`),
`category_name` = VALUES(`category_name`),
`amount` = VALUES(`amount`),
`purchase_date` = VALUES(`purchase_date`),
`dept_id` = VALUES(`dept_id`),
`responsible_user_id` = VALUES(`responsible_user_id`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`updater` = 'tester',
`update_time` = NOW(),
`deleted` = b'0',
`tenant_id` = VALUES(`tenant_id`);

-- 5) 固定资产主数据：直接做成已确认且使用中的资产，确保在固定资产台账页可见
INSERT INTO erp_finance_asset
(`id`, `no`, `name`, `category_name`, `candidate_id`, `source_type`, `source_biz_id`, `source_biz_no`, `source_item_id`,
 `dept_id`, `responsible_user_id`, `purchase_date`, `start_use_date`, `original_amount`,
 `salvage_rate`, `salvage_amount`, `depreciation_method`, `depreciation_period_months`,
 `depreciation_start_period`, `depreciated_amount`, `current_amount`, `status`,
 `last_depreciation_period`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@asset_id, @asset_no, '办公电脑', '零星采购', @candidate_id, 20, @expense_id, @expense_no, @expense_item_id,
 @dept_id, @finance_user_id, '2026-05-21', '2026-05-21', 6888.00,
 0.05, 344.40, '年限平均法', 36,
 '2026-05', 0.00, 6888.00, 10,
 NULL, '固定资产来源测试-费用报销跳转验证', 'tester', NOW(), 'tester', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
`no` = VALUES(`no`),
`name` = VALUES(`name`),
`category_name` = VALUES(`category_name`),
`candidate_id` = VALUES(`candidate_id`),
`source_type` = VALUES(`source_type`),
`source_biz_id` = VALUES(`source_biz_id`),
`source_biz_no` = VALUES(`source_biz_no`),
`source_item_id` = VALUES(`source_item_id`),
`dept_id` = VALUES(`dept_id`),
`responsible_user_id` = VALUES(`responsible_user_id`),
`purchase_date` = VALUES(`purchase_date`),
`start_use_date` = VALUES(`start_use_date`),
`original_amount` = VALUES(`original_amount`),
`salvage_rate` = VALUES(`salvage_rate`),
`salvage_amount` = VALUES(`salvage_amount`),
`depreciation_method` = VALUES(`depreciation_method`),
`depreciation_period_months` = VALUES(`depreciation_period_months`),
`depreciation_start_period` = VALUES(`depreciation_start_period`),
`depreciated_amount` = VALUES(`depreciated_amount`),
`current_amount` = VALUES(`current_amount`),
`status` = VALUES(`status`),
`last_depreciation_period` = VALUES(`last_depreciation_period`),
`remark` = VALUES(`remark`),
`updater` = 'tester',
`update_time` = NOW(),
`deleted` = b'0',
`tenant_id` = VALUES(`tenant_id`);

-- 6) 查询结果，便于执行后马上核对
SELECT
  a.id,
  a.no,
  a.name,
  a.source_type,
  a.source_biz_id,
  a.source_biz_no,
  a.source_item_id,
  c.id AS candidate_id,
  c.status AS candidate_status,
  e.id AS expense_id,
  e.no AS expense_no,
  i.id AS expense_item_id,
  i.item_name,
  i.asset_candidate_flag
FROM erp_finance_asset a
LEFT JOIN erp_finance_asset_candidate c
  ON c.id = a.candidate_id AND c.deleted = b'0'
LEFT JOIN erp_finance_expense e
  ON e.id = a.source_biz_id AND e.deleted = b'0'
LEFT JOIN erp_finance_expense_item i
  ON i.id = a.source_item_id AND i.deleted = b'0'
WHERE a.id = @asset_id
  AND a.deleted = b'0';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 可选回滚 SQL（按需手工执行）
-- DELETE FROM erp_finance_asset WHERE id = 108911;
-- DELETE FROM erp_finance_asset_candidate WHERE id = 108901;
-- DELETE FROM erp_finance_expense_item WHERE id = 107911;
-- DELETE FROM erp_finance_expense WHERE id = 107901;
-- =====================================================
