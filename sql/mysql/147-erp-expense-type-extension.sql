-- 报销费用集成 - 费用类型扩展 + 凭证模板配置

SET NAMES utf8mb4;

-- 1. 费用类型字典数据
INSERT INTO system_dict_data (dict_type, label, value, sort, status, remark, creator, create_time, updater, update_time, deleted)
VALUES 
('erp_expense_type', '房租费', '100', 100, 0, '行政房租费用', '1', NOW(), '1', NOW(), b'0'),
('erp_expense_type', '水电费', '110', 110, 0, '行政水电费用', '1', NOW(), '1', NOW(), b'0'),
('erp_expense_type', '仪器租赁费', '120', 120, 0, '仪器设备租赁费用', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE label = VALUES(label), remark = VALUES(remark);

-- 2. 房租水电凭证模板
-- 借：管理费用-水电费/房租费
-- 贷：银行存款/应付账款
INSERT INTO erp_finance_voucher_template (id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted)
VALUES (210, 1, 40, '房租水电凭证模板', 0, b'1', '房租水电报销', '房租水电费用报销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2.1 房租水电凭证模板明细
INSERT INTO erp_finance_voucher_template_item (id, template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary, creator, create_time, updater, update_time, deleted)
VALUES 
(2101, 210, 1, 1, '6602', '管理费用', 10, NULL, '房租水电报销', '1', NOW(), '1', NOW(), b'0'),
(2102, 210, 2, 2, '1002', '银行存款', 10, NULL, '房租水电报销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE subject_code = VALUES(subject_code), subject_name = VALUES(subject_name);

-- 3. 仪器租赁凭证模板
-- 借：制造费用-设备租赁费 / 研发支出-设备租赁费
-- 贷：应付账款
INSERT INTO erp_finance_voucher_template (id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, creator, create_time, updater, update_time, deleted)
VALUES (211, 1, 40, '仪器租赁凭证模板', 0, b'1', '仪器租赁报销', '仪器租赁费用报销自动生成凭证', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3.1 仪器租赁凭证模板明细
INSERT INTO erp_finance_voucher_template_item (id, template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary, creator, create_time, updater, update_time, deleted)
VALUES 
(2111, 211, 1, 1, '5101', '制造费用', 10, NULL, '仪器租赁报销', '1', NOW(), '1', NOW(), b'0'),
(2112, 211, 2, 2, '2202', '应付账款', 10, NULL, '仪器租赁报销', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE subject_code = VALUES(subject_code), subject_name = VALUES(subject_name);
