-- 资产折旧/摊销凭证模板配置
-- 业务类型：70=资产折旧, 71=无形资产摊销

SET NAMES utf8mb4;

-- 1. 资产折旧凭证模板
-- 借：制造费用-折旧费 / 管理费用-折旧费
-- 贷：累计折旧
INSERT INTO erp_finance_voucher_template (id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted)
VALUES (203, 1, 70, '固定资产折旧凭证模板', 0, b'1', '固定资产折旧', '固定资产折旧自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2. 无形资产摊销凭证模板
-- 借：管理费用-无形资产摊销 / 研发支出-无形资产摊销
-- 贷：累计摊销
INSERT INTO erp_finance_voucher_template (id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted)
VALUES (204, 1, 71, '无形资产摊销凭证模板', 0, b'1', '无形资产摊销', '无形资产摊销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. 确认会计科目表中"1602 累计折旧"科目已创建
INSERT INTO erp_finance_subject (id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark, creator, create_time, updater, update_time, deleted)
VALUES (1602, 1, NULL, '1602', '累计折旧', 1, 2, b'1', 0, 0, '固定资产折旧备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name);

-- 4. 确认"1702 累计摊销"科目已创建（如果之前未创建）
INSERT INTO erp_finance_subject (id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark, creator, create_time, updater, update_time, deleted)
VALUES (1702, 1, NULL, '1702', '累计摊销', 1, 2, b'1', 0, 0, '无形资产摊销备抵科目', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name);

-- 5. 添加资产表的成本中心字段（如果不存在）
ALTER TABLE `erp_finance_asset`
    ADD COLUMN IF NOT EXISTS `cost_center_id` BIGINT NULL COMMENT '成本中心ID（部门ID）' AFTER `sub_category`;

-- 6. 添加折旧记录表的凭证ID字段（如果不存在）
ALTER TABLE `erp_finance_asset_depreciation`
    ADD COLUMN IF NOT EXISTS `voucher_id` BIGINT NULL COMMENT '生成的凭证ID' AFTER `status`;
