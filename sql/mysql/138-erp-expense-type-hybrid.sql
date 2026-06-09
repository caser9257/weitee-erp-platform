-- ============================================================
-- 费用类型混合方案 - 数据库脚本
-- 功能：支持核心枚举类型和字典扩展类型的混合管理
-- ============================================================

-- 1. 扩展字典数据表，支持业务属性
ALTER TABLE system_dict_data ADD COLUMN biz_attributes JSON COMMENT '业务属性JSON';

-- 2. 创建费用类型字典类型
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES 
('费用类型', 'erp_expense_type', 0, 'ERP费用报销类型配置', 'admin', NOW(), 'admin', NOW(), b'0');

-- 3. 初始化核心类型（从枚举迁移）
-- 说明：core=true 表示系统核心类型，保留枚举实现
INSERT INTO system_dict_data (dict_type, label, value, sort, status, color_type, css_class, remark, biz_attributes, creator, create_time, updater, update_time, deleted) VALUES
-- 研发费用（特殊：需要项目，自动生成凭证）
('erp_expense_type', '研发费用', '10', 10, 0, 'primary', '', '系统核心类型', '{"core": true, "projectRequired": true, "autoGenerateVoucher": true, "voucherBizType": 41, "category": "RD"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 差旅费
('erp_expense_type', '差旅费', '20', 20, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 材料费
('erp_expense_type', '材料费', '30', 30, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 测试费
('erp_expense_type', '测试费', '40', 40, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 招待费
('erp_expense_type', '招待费', '50', 50, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 服务费
('erp_expense_type', '服务费', '60', 60, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 人工费
('erp_expense_type', '人工费', '70', 70, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 零星采购（特殊：标记为资产候选）
('erp_expense_type', '零星采购', '80', 80, 0, 'warning', '', '系统核心类型', '{"core": true, "projectRequired": false, "assetCandidateFlag": true, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 其他
('erp_expense_type', '其他', '90', 90, 0, 'default', '', '系统核心类型', '{"core": true, "projectRequired": false, "category": "OTHER"}', 'admin', NOW(), 'admin', NOW(), b'0');

-- 4. 添加扩展类型（行政费用）
-- 说明：core=false 表示扩展类型，使用字典配置
INSERT INTO system_dict_data (dict_type, label, value, sort, status, color_type, css_class, remark, biz_attributes, creator, create_time, updater, update_time, deleted) VALUES
-- 房租费（需要成本中心）
('erp_expense_type', '房租费', '100', 100, 0, 'success', '', '行政费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "ADMIN"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 水电费（需要成本中心）
('erp_expense_type', '水电费', '110', 110, 0, 'success', '', '行政费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "ADMIN"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 物业费（需要成本中心）
('erp_expense_type', '物业费', '120', 120, 0, 'success', '', '行政费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "ADMIN"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 通讯费（需要成本中心）
('erp_expense_type', '通讯费', '130', 130, 0, 'success', '', '行政费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "ADMIN"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 办公费（需要成本中心）
('erp_expense_type', '办公费', '140', 140, 0, 'success', '', '行政费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "ADMIN"}', 'admin', NOW(), 'admin', NOW(), b'0');

-- 5. 添加扩展类型（租赁费用）
INSERT INTO system_dict_data (dict_type, label, value, sort, status, color_type, css_class, remark, biz_attributes, creator, create_time, updater, update_time, deleted) VALUES
-- 仪器租赁费（需要租赁合同）
('erp_expense_type', '仪器租赁费', '200', 200, 0, 'info', '', '租赁费用', '{"core": false, "projectRequired": false, "leaseContractRequired": true, "category": "LEASE"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 设备租赁费（需要租赁合同）
('erp_expense_type', '设备租赁费', '210', 210, 0, 'info', '', '租赁费用', '{"core": false, "projectRequired": false, "leaseContractRequired": true, "category": "LEASE"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 车辆租赁费（需要租赁合同）
('erp_expense_type', '车辆租赁费', '220', 220, 0, 'info', '', '租赁费用', '{"core": false, "projectRequired": false, "leaseContractRequired": true, "category": "LEASE"}', 'admin', NOW(), 'admin', NOW(), b'0'),

-- 场地租赁费（需要成本中心）
('erp_expense_type', '场地租赁费', '230', 230, 0, 'info', '', '租赁费用', '{"core": false, "projectRequired": false, "costCenterRequired": true, "category": "LEASE"}', 'admin', NOW(), 'admin', NOW(), b'0');

-- ============================================================
-- 说明：
-- 1. 核心类型（core=true）保留枚举实现，保证类型安全
-- 2. 扩展类型（core=false）使用字典配置，支持灵活扩展
-- 3. 业务属性字段说明：
--    - core: 是否为核心类型
--    - projectRequired: 是否需要选择项目
--    - costCenterRequired: 是否需要选择成本中心
--    - leaseContractRequired: 是否需要选择租赁合同
--    - assetCandidateFlag: 是否标记为资产候选
--    - category: 费用分类（ADMIN-行政费用，LEASE-租赁费用，RD-研发费用，OTHER-其他）
--    - autoGenerateVoucher: 是否自动生成凭证
--    - voucherBizType: 凭证业务类型
-- ============================================================
