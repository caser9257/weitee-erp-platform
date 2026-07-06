-- 年末财务决算一体化补强方案 DDL 变更
-- 包含：成本中心、库存成本追踪、无形资产类型隔离、租赁费用、盘点高级功能

-- ============================================================
-- 1. 成本中心：部门体系升级
-- ============================================================
-- 1.1 system_dept 新增 cost_type 字段
ALTER TABLE `system_dept`
    ADD COLUMN `cost_type` TINYINT NULL COMMENT '成本类型（0=不归集, 1=制造费用, 2=管理费用, 3=销售费用, 4=研发支出）' AFTER `status`;

-- ============================================================
-- 2. 库存成本追踪：移动加权平均法
-- ============================================================
-- 2.1 erp_stock 新增成本字段
ALTER TABLE `erp_stock`
    ADD COLUMN `average_cost` DECIMAL(20,6) NULL COMMENT '加权平均单价' AFTER `count`,
    ADD COLUMN `total_cost` DECIMAL(20,2) NULL COMMENT '库存总金额' AFTER `average_cost`;

-- 2.2 erp_stock_record 新增成本字段
ALTER TABLE `erp_stock_record`
    ADD COLUMN `price` DECIMAL(20,6) NULL COMMENT '单价' AFTER `biz_no`,
    ADD COLUMN `amount` DECIMAL(20,2) NULL COMMENT '金额' AFTER `price`;

-- ============================================================
-- 3. 无形资产类型隔离
-- ============================================================
-- 3.1 erp_finance_asset 新增资产类型字段
ALTER TABLE `erp_finance_asset`
    ADD COLUMN `asset_type` TINYINT NOT NULL DEFAULT 0 COMMENT '资产类型（0=固定资产, 1=无形资产）' AFTER `remark`,
    ADD COLUMN `sub_category` VARCHAR(64) NULL COMMENT '子分类（专利权、软件著作权等）' AFTER `asset_type`;

-- ============================================================
-- 4. 租赁费用轻量化接入
-- ============================================================
-- 4.1 erp_finance_expense 新增租赁合同编号字段
ALTER TABLE `erp_finance_expense`
    ADD COLUMN `lease_contract_no` VARCHAR(128) NULL COMMENT '租赁合同编号' AFTER `remark`;

-- ============================================================
-- 5. 盘点高级功能：快照、冻结、盲盘、初盘/复盘
-- ============================================================
-- 5.1 erp_warehouse 新增冻结字段
ALTER TABLE `erp_warehouse`
    ADD COLUMN `frozen` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否冻结（盘点期间使用）' AFTER `default_status`;

-- 5.2 erp_stock_check 新增快照时间和盲盘字段
ALTER TABLE `erp_stock_check`
    ADD COLUMN `snapshot_time` DATETIME NULL COMMENT '快照时间（盘点开始时的库存快照时间）' AFTER `file_url`,
    ADD COLUMN `blind_count` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否盲盘模式' AFTER `snapshot_time`;

-- 5.3 erp_stock_check_item 新增初盘/复盘字段
ALTER TABLE `erp_stock_check_item`
    ADD COLUMN `first_count` DECIMAL(20,6) NULL COMMENT '初盘数量' AFTER `remark`,
    ADD COLUMN `recount` DECIMAL(20,6) NULL COMMENT '复盘数量' AFTER `first_count`,
    ADD COLUMN `recount_diff` DECIMAL(20,6) NULL COMMENT '复盘差异' AFTER `recount`;

-- ============================================================
-- 6. 生产成本条目：来源单据关联
-- ============================================================
-- 6.1 erp_production_cost_entry 新增来源单据字段（用于盘亏结转关联盘点单）
ALTER TABLE `erp_production_cost_entry`
    ADD COLUMN `source_id` BIGINT NULL COMMENT '来源单据编号（如盘点单ID）' AFTER `remark`,
    ADD COLUMN `source_no` VARCHAR(64) NULL COMMENT '来源单据号（如盘点单号）' AFTER `source_id`;

-- ============================================================
-- 7. 历史数据初始化
-- ============================================================

-- 7.1 现有部门的 cost_type 配置（基于实际组织架构）

-- 制造费用 (1)：制造部及其下属组
UPDATE `system_dept` SET `cost_type` = 1
WHERE `name` IN ('制造部', 'SMT组', '电装组', '粘接1组', '粘接2组', '纤焊组', '键合组', '激光封焊组')
   OR `name` LIKE '%生产%';

-- 管理费用 (2)：财务部、人力资源部、供应链部、质量部、统计部、专家办
UPDATE `system_dept` SET `cost_type` = 2
WHERE `name` IN ('财务部', '人力资源部', '供应链部', '质量部', '统计部', '专家办')
   OR `name` LIKE '%行政%' OR `name` LIKE '%综合%';

-- 销售费用 (3)：市场营销部
UPDATE `system_dept` SET `cost_type` = 3
WHERE `name` IN ('市场营销部')
   OR `name` LIKE '%销售%';

-- 研发支出 (4)：研发部及其下属部门（系统部、软件部、硬件部）
UPDATE `system_dept` SET `cost_type` = 4
WHERE `name` IN ('研发部', '系统部', '软件部', '硬件部')
   OR `name` LIKE '%研发%';

-- 工艺部归入制造费用
UPDATE `system_dept` SET `cost_type` = 1
WHERE `name` = '工艺部';

-- 7.2 现有库存的 average_cost 初始化（按最近采购价填充）
-- 注意：如果无法确定采购价，average_cost 保持 NULL，表示"启用前数据"
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
WHERE s.`count` > 0;

-- 7.3 现有资产的 asset_type 初始化（全部标记为固定资产）
UPDATE `erp_finance_asset` SET `asset_type` = 0 WHERE `asset_type` IS NULL;

-- 7.4 费用类型字典数据：新增"仪器租赁费"类型
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `biz_attributes`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1500, 100, '仪器租赁费', '100', 'erp_expense_type', 0, '', '', '仪器设备租赁费用',
        '{"core":false,"costCenterRequired":true,"leaseContractRequired":true,"category":"LEASE"}',
        '1', NOW(), '1', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE `biz_attributes` = VALUES(`biz_attributes`);

-- ============================================================
-- 8. 凭证模板配置
-- ============================================================

-- 8.1 无形资产摊销凭证模板
-- 借：管理费用/研发支出-无形资产摊销，贷：累计摊销
INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (200, 1, 90, '无形资产摊销凭证模板', 0, b'1', '无形资产摊销', '无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 8.2 盘亏结转凭证模板
-- 借：管理费用/营业外支出/其他应收款，贷：待处理财产损溢
INSERT INTO `erp_finance_voucher_template` (`id`, `ledger_id`, `biz_type`, `name`, `status`, `auto_generate`, `default_summary`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (201, 1, 91, '盘亏结转凭证模板', 0, b'1', '盘亏结转', '盘点盘亏自动生成凭证', '1', NOW(), '1', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 8.3 确认会计科目表中"1702 累计摊销"科目已创建
INSERT INTO `erp_finance_subject` (`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1702, 1, NULL, '1702', '累计摊销', 1, 2, b'1', 0, 0, '无形资产摊销备抵科目', '1', NOW(), '1', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- 8.4 确认"1901 待处理财产损溢"科目已创建
INSERT INTO `erp_finance_subject` (`id`, `ledger_id`, `parent_id`, `subject_code`, `subject_name`, `subject_type`, `balance_direction`, `leaf`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1901, 1, NULL, '1901', '待处理财产损溢', 1, 1, b'1', 0, 0, '盘盈盘亏待处理科目', '1', NOW(), '1', NOW(), b'0', 0)
ON DUPLICATE KEY UPDATE `subject_name` = VALUES(`subject_name`);

-- ============================================================
-- 9. 生产工单扩展：机器工时字段
-- ============================================================
-- 9.1 erp_production_order 新增机器工时字段（用于机器工时分摊基准）
ALTER TABLE `erp_production_order`
    ADD COLUMN `machine_hour` DECIMAL(10,2) NULL DEFAULT 0 COMMENT '机器工时' AFTER `remark`;
