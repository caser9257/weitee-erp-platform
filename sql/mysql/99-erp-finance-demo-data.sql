/*
 * ERP 财务联调测试数据
 * 目标：
 * - 财务模块主数据、台账、期间、科目、报表项目
 * - 采购 -> 暂估 -> 应付 -> 付款
 * - 销售回款、预付款、费用报销、凭证
 * - 以较大样本量覆盖列表、详情、统计、联动页
 *
 * 说明：
 * - 幂等导入
 * - 不是独立建库脚本，建议优先执行 `scripts/erp/install-finance-demo-data.ps1`
 * - 仅写当前仓库内的测试数据
 * - 不修改接口契约
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
    SELECT id
    FROM system_tenant
    WHERE deleted = b'0'
    ORDER BY id
    LIMIT 1
), 1);

SET @password_hash := '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';
SET @finance_user_id := COALESCE((SELECT id FROM system_users WHERE username = '财务主管' AND deleted = b'0' LIMIT 1), 1);
SET @clerk_user_id := COALESCE((SELECT id FROM system_users WHERE username = '财务经办' AND deleted = b'0' LIMIT 1), @finance_user_id);

INSERT INTO erp_product_category (id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99001, 0, '测试成品', 'TEST-FIN-001', 1, 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99002, 99001, '五金类', 'TEST-FIN-001-01', 2, 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99003, 99001, '电子类', 'TEST-FIN-001-02', 3, 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99004, 99001, '辅料类', 'TEST-FIN-001-03', 4, 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), code = VALUES(code), sort = VALUES(sort), status = VALUES(status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_product_unit (id, name, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99001, '件', 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99002, '套', 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99003, '箱', 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99004, '台', 0, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_product (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark, expiry_day, batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99101, '不锈钢螺栓 M8', 'MAT-9901', 'BC-9901', 99002, 99001, 0, 'M8*30', '测试产品-五金', 365, b'1', b'0', 0.120000, 1.200000, 2.400000, 1.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99102, '工业控制板', 'MAT-9902', 'BC-9902', 99003, 99004, 0, 'V2.1', '测试产品-电子', 540, b'1', b'1', 0.850000, 58.000000, 88.000000, 55.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99103, '绝缘垫片', 'MAT-9903', 'BC-9903', 99004, 99003, 0, '标准件', '测试产品-辅料', 720, b'0', b'0', 0.010000, 0.180000, 0.500000, 0.150000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99104, '组装电机', 'MAT-9904', 'BC-9904', 99001, 99002, 0, '1.5KW', '测试产品-整机', 900, b'1', b'1', 12.500000, 320.000000, 520.000000, 300.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99105, '包装材料套装', 'MAT-9905', 'BC-9905', 99004, 99003, 0, '标准包装', '测试产品-包装', 365, b'0', b'0', 0.050000, 6.800000, 12.000000, 6.500000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), material_code = VALUES(material_code), bar_code = VALUES(bar_code), category_id = VALUES(category_id), unit_id = VALUES(unit_id), status = VALUES(status), standard = VALUES(standard), remark = VALUES(remark), expiry_day = VALUES(expiry_day), batch_control_flag = VALUES(batch_control_flag), inspection_required_flag = VALUES(inspection_required_flag), weight = VALUES(weight), purchase_price = VALUES(purchase_price), sale_price = VALUES(sale_price), min_price = VALUES(min_price), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_warehouse (id, name, address, sort, remark, principal, warehouse_price, truckage_price, status, default_status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99201, '原料仓', '深圳仓库 A 区', 1, '测试仓库', '张仓管', 0.500000, 0.200000, 0, b'1', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99202, '成品仓', '深圳仓库 B 区', 2, '测试仓库', '李仓管', 0.500000, 0.200000, 0, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99203, '在途仓', '深圳仓库 C 区', 3, '测试仓库', '王仓管', 0.300000, 0.100000, 0, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), sort = VALUES(sort), remark = VALUES(remark), principal = VALUES(principal), warehouse_price = VALUES(warehouse_price), truckage_price = VALUES(truckage_price), status = VALUES(status), default_status = VALUES(default_status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_supplier (id, name, contact, mobile, telephone, email, fax, remark, status, sort, tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99301, '华南五金供应商', '赵经理', '13800010001', '0755-88000001', 'supplier1@test.local', NULL, '五金采购测试', 0, 1, '91440300TEST001', 0.130000, '招商银行', '6214830000000001', '深圳南山', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99302, '深港电子供应商', '钱经理', '13800010002', '0755-88000002', 'supplier2@test.local', NULL, '电子采购测试', 0, 2, '91440300TEST002', 0.130000, '建设银行', '6214830000000002', '深圳福田', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99303, '北方辅料供应商', '孙经理', '13800010003', '010-88000003', 'supplier3@test.local', NULL, '辅料采购测试', 0, 3, '91110100TEST003', 0.090000, '工商银行', '6214830000000003', '北京朝阳', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99304, '长三角设备供应商', '李经理', '13800010004', '021-88000004', 'supplier4@test.local', NULL, '设备采购测试', 0, 4, '91310000TEST004', 0.130000, '农业银行', '6214830000000004', '上海浦东', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99305, '中部包装供应商', '周经理', '13800010005', '027-88000005', 'supplier5@test.local', NULL, '包装采购测试', 0, 5, '91420100TEST005', 0.090000, '交通银行', '6214830000000005', '武汉洪山', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), contact = VALUES(contact), mobile = VALUES(mobile), telephone = VALUES(telephone), email = VALUES(email), fax = VALUES(fax), remark = VALUES(remark), status = VALUES(status), sort = VALUES(sort), tax_no = VALUES(tax_no), tax_percent = VALUES(tax_percent), bank_name = VALUES(bank_name), bank_account = VALUES(bank_account), bank_address = VALUES(bank_address), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_customer (id, name, contact, mobile, telephone, email, fax, remark, status, sort, tax_no, tax_percent, bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99401, '华南制造客户', '陈经理', '13900010001', '0755-77000001', 'customer1@test.local', NULL, '收款测试客户', 0, 1, '91440300CUST001', 0.130000, '中国银行', '6222020000000001', '深圳南山', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99402, '华东贸易客户', '吴经理', '13900010002', '021-77000002', 'customer2@test.local', NULL, '收款测试客户', 0, 2, '91310000CUST002', 0.130000, '工商银行', '6222020000000002', '上海浦东', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99403, '华北零售客户', '郑经理', '13900010003', '010-77000003', 'customer3@test.local', NULL, '收款测试客户', 0, 3, '91110100CUST003', 0.090000, '建设银行', '6222020000000003', '北京朝阳', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), contact = VALUES(contact), mobile = VALUES(mobile), telephone = VALUES(telephone), email = VALUES(email), fax = VALUES(fax), remark = VALUES(remark), status = VALUES(status), sort = VALUES(sort), tax_no = VALUES(tax_no), tax_percent = VALUES(tax_percent), bank_name = VALUES(bank_name), bank_account = VALUES(bank_account), bank_address = VALUES(bank_address), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_account (id, name, no, remark, status, sort, default_status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99501, '招商银行结算户', 'ACC-001', '测试账户', 0, 1, b'1', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99502, '建设银行结算户', 'ACC-002', '测试账户', 0, 2, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99503, '工商银行收款户', 'ACC-003', '测试账户', 0, 3, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), no = VALUES(no), remark = VALUES(remark), status = VALUES(status), sort = VALUES(sort), default_status = VALUES(default_status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_ledger (id, no, name, status, sort, default_status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99601, 'LEDGER-FIN-001', '财务主账簿', 0, 1, b'1', '财务模块测试主账簿', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99602, 'LEDGER-FIN-002', '应付专项账簿', 0, 2, b'0', '应付联动测试账簿', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99603, 'LEDGER-EXT-001', '对外账（税务账）', 0, 3, b'0', '对外报送账簿，用于税务申报和外部审计', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99604, 'LEDGER-INT-001', '内部账（管理账）', 0, 4, b'0', '内部管理账簿，用于成本核算和经营分析', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE name = VALUES(name), status = VALUES(status), sort = VALUES(sort), default_status = VALUES(default_status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `external_ledger_id` BIGINT NOT NULL COMMENT '对外账账簿编号',
    `internal_ledger_id` BIGINT NOT NULL COMMENT '内部账账簿编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_ledger_config_biz_type` (`tenant_id`, `biz_type`, `deleted`),
    KEY `idx_dual_ledger_config_status` (`tenant_id`, `status`, `deleted`),
    KEY `idx_dual_ledger_config_external` (`tenant_id`, `external_ledger_id`, `deleted`),
    KEY `idx_dual_ledger_config_internal` (`tenant_id`, `internal_ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套账簿映射配置';

-- 双账套配置：采购入库业务启用双账套
INSERT INTO erp_finance_dual_ledger_config (id, biz_type, external_ledger_id, internal_ledger_id, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99651, 11, 99603, 99604, 0, '采购入库双账套配置', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99652, 40, 99603, 99604, 0, '费用报销双账套配置', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99653, 41, 99603, 99604, 0, '研发费用化双账套配置', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99654, 42, 99603, 99604, 0, '研发资本化双账套配置', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE biz_type = VALUES(biz_type), external_ledger_id = VALUES(external_ledger_id), internal_ledger_id = VALUES(internal_ledger_id), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_period (id, ledger_id, period_code, period_year, period_month, period_sort, start_date, end_date, status, close_time, close_user_id, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99611, 99601, '2026-01', 2026, 1, 202601, '2026-01-01', '2026-01-31', 10, NULL, NULL, '测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99612, 99601, '2026-02', 2026, 2, 202602, '2026-02-01', '2026-02-28', 10, NULL, NULL, '测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99613, 99601, '2026-03', 2026, 3, 202603, '2026-03-01', '2026-03-31', 10, NULL, NULL, '测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99614, 99601, '2026-04', 2026, 4, 202604, '2026-04-01', '2026-04-30', 10, NULL, NULL, '测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99615, 99601, '2026-05', 2026, 5, 202605, '2026-05-01', '2026-05-31', 10, NULL, NULL, '测试期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
-- 对外账期间
(99621, 99603, '2026-01', 2026, 1, 202601, '2026-01-01', '2026-01-31', 10, NULL, NULL, '对外账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99622, 99603, '2026-02', 2026, 2, 202602, '2026-02-01', '2026-02-28', 10, NULL, NULL, '对外账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99623, 99603, '2026-03', 2026, 3, 202603, '2026-03-01', '2026-03-31', 10, NULL, NULL, '对外账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99624, 99603, '2026-04', 2026, 4, 202604, '2026-04-01', '2026-04-30', 10, NULL, NULL, '对外账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99625, 99603, '2026-05', 2026, 5, 202605, '2026-05-01', '2026-05-31', 10, NULL, NULL, '对外账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
-- 内部账期间
(99631, 99604, '2026-01', 2026, 1, 202601, '2026-01-01', '2026-01-31', 10, NULL, NULL, '内部账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99632, 99604, '2026-02', 2026, 2, 202602, '2026-02-01', '2026-02-28', 10, NULL, NULL, '内部账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99633, 99604, '2026-03', 2026, 3, 202603, '2026-03-01', '2026-03-31', 10, NULL, NULL, '内部账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99634, 99604, '2026-04', 2026, 4, 202604, '2026-04-01', '2026-04-30', 10, NULL, NULL, '内部账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99635, 99604, '2026-05', 2026, 5, 202605, '2026-05-01', '2026-05-31', 10, NULL, NULL, '内部账期间', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE ledger_id = VALUES(ledger_id), period_code = VALUES(period_code), period_year = VALUES(period_year), period_month = VALUES(period_month), period_sort = VALUES(period_sort), start_date = VALUES(start_date), end_date = VALUES(end_date), status = VALUES(status), close_time = VALUES(close_time), close_user_id = VALUES(close_user_id), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_subject (id, ledger_id, parent_id, subject_code, subject_name, subject_type, balance_direction, leaf, status, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99701, 99601, NULL, '1001', '库存现金', 10, 10, b'1', 0, 10, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99702, 99601, NULL, '1002', '银行存款', 10, 10, b'1', 0, 20, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99703, 99601, NULL, '1122', '应收账款', 20, 10, b'1', 0, 30, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99704, 99601, NULL, '2202', '应付账款', 20, 20, b'1', 0, 40, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99705, 99601, NULL, '2221', '应交税费', 20, 20, b'1', 0, 50, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99706, 99601, NULL, '1403', '原材料', 10, 10, b'1', 0, 60, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99707, 99601, NULL, '1405', '库存商品', 10, 10, b'1', 0, 70, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99708, 99601, NULL, '5001', '生产成本', 40, 10, b'1', 0, 80, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99709, 99601, NULL, '6001', '主营业务收入', 40, 20, b'1', 0, 90, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99710, 99601, NULL, '6401', '主营业务成本', 50, 10, b'1', 0, 100, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99711, 99601, NULL, '6601', '销售费用', 50, 10, b'1', 0, 110, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99712, 99601, NULL, '6711', '财务费用', 50, 10, b'1', 0, 120, '测试科目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE ledger_id = VALUES(ledger_id), parent_id = VALUES(parent_id), subject_name = VALUES(subject_name), subject_type = VALUES(subject_type), balance_direction = VALUES(balance_direction), leaf = VALUES(leaf), status = VALUES(status), sort = VALUES(sort), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_report_item (id, ledger_id, report_type, item_category, item_code, item_name, status, sort, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99801, 99601, 10, 10, 'BS-CASH', '货币资金', 0, 10, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99802, 99601, 10, 20, 'BS-AR', '应收账款', 0, 20, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99803, 99601, 10, 30, 'BS-AP', '应付账款', 0, 30, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99804, 99601, 10, 40, 'BS-ADV', '预付款项', 0, 40, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99805, 99601, 20, 10, 'IS-REV', '营业收入', 0, 10, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99806, 99601, 20, 20, 'IS-COST', '营业成本', 0, 20, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99807, 99601, 20, 30, 'IS-FEE', '销售费用', 0, 30, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99808, 99601, 20, 40, 'IS-FIN', '财务费用', 0, 40, '测试报表项目', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE ledger_id = VALUES(ledger_id), report_type = VALUES(report_type), item_category = VALUES(item_category), item_name = VALUES(item_name), status = VALUES(status), sort = VALUES(sort), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_report_item_subject (id, item_id, subject_code, amount_rule, amount_sign, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99811, 99801, '1001', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99812, 99801, '1002', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99813, 99802, '1122', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99814, 99803, '2202', 60, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99815, 99804, '1121', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99816, 99805, '6001', 40, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99817, 99806, '6401', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99818, 99807, '6601', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99819, 99808, '6711', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
-- 生产成本映射
(99820, 99806, '5001', 30, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
-- 原材料映射（存货）
(99821, 99801, '1403', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
-- 库存商品映射（存货）
(99822, 99801, '1405', 50, 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE item_id = VALUES(item_id), subject_code = VALUES(subject_code), amount_rule = VALUES(amount_rule), amount_sign = VALUES(amount_sign), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_template (id, ledger_id, biz_type, name, status, auto_generate, default_summary, remark, research_category, research_template, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99821, 99601, 11, '采购入库凭证模板', 0, b'1', '采购入库自动生成凭证', '测试模板', NULL, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99822, 99601, 40, '费用报销凭证模板', 0, b'1', '费用报销自动生成凭证', '测试模板', NULL, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99823, 99601, 10, '采购订单凭证模板', 0, b'0', '采购订单生成凭证', '测试模板', NULL, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99824, 99601, 41, '研发费用化凭证模板', 0, b'1', '研发费用化自动生成凭证', '研发模板', 10, b'1', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99825, 99601, 42, '研发资本化凭证模板', 0, b'1', '研发资本化自动生成凭证', '研发模板', 20, b'1', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99826, 99601, 30, '委外加工费凭证模板', 0, b'1', '委外加工费自动生成凭证', '委外模板', NULL, b'0', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE ledger_id = VALUES(ledger_id), biz_type = VALUES(biz_type), name = VALUES(name), status = VALUES(status), auto_generate = VALUES(auto_generate), default_summary = VALUES(default_summary), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_template_item (id, template_id, entry_no, entry_direction, subject_code, subject_name, amount_source, amount_source_value, summary, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99831, 99821, 1, 10, '1405', '库存商品', 10, 0.000000, '确认采购入库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99832, 99821, 2, 20, '2202', '应付账款', 20, 0.000000, '确认采购入库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99833, 99822, 1, 10, '6601', '销售费用', 10, 0.000000, '费用报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99834, 99822, 2, 20, '1002', '银行存款', 20, 0.000000, '费用报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99835, 99823, 1, 10, '1403', '原材料', 10, 0.000000, '采购订单', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99836, 99823, 2, 20, '2202', '应付账款', 20, 0.000000, '采购订单', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99837, 99824, 1, 10, '6601', '销售费用', 10, 0.000000, '研发费用化', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99838, 99824, 2, 20, '2202', '应付账款', 20, 0.000000, '研发费用化', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99839, 99825, 1, 10, '1801', '长期待摊费用', 10, 0.000000, '研发资本化', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99840, 99825, 2, 20, '2202', '应付账款', 20, 0.000000, '研发资本化', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99841, 99826, 1, 10, '5001', '生产成本', 10, 0.000000, '委外加工费', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99842, 99826, 2, 20, '2202', '应付账款', 20, 0.000000, '委外加工费', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE template_id = VALUES(template_id), entry_no = VALUES(entry_no), entry_direction = VALUES(entry_direction), subject_code = VALUES(subject_code), subject_name = VALUES(subject_name), amount_source = VALUES(amount_source), amount_source_value = VALUES(amount_source_value), summary = VALUES(summary), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_subject_balance (id, ledger_id, period_id, period_sort, subject_code, subject_name, opening_debit_amount, opening_credit_amount, current_debit_amount, current_credit_amount, ending_debit_amount, ending_credit_amount, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(99901, 99601, 99611, 202601, '1001', '库存现金', 1000.000000, 0.000000, 1800.000000, 900.000000, 1900.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99902, 99601, 99611, 202601, '1002', '银行存款', 15000.000000, 0.000000, 62000.000000, 33000.000000, 44000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99903, 99601, 99611, 202601, '1122', '应收账款', 0.000000, 0.000000, 42000.000000, 12000.000000, 30000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99904, 99601, 99611, 202601, '2202', '应付账款', 0.000000, 0.000000, 8000.000000, 47000.000000, 0.000000, 39000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99905, 99601, 99611, 202601, '2221', '应交税费', 0.000000, 0.000000, 900.000000, 600.000000, 300.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99906, 99601, 99611, 202601, '6001', '主营业务收入', 0.000000, 0.000000, 0.000000, 48000.000000, 0.000000, 48000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99907, 99601, 99611, 202601, '6401', '主营业务成本', 0.000000, 0.000000, 28000.000000, 0.000000, 28000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99908, 99601, 99612, 202602, '1001', '库存现金', 1900.000000, 0.000000, 1200.000000, 600.000000, 2500.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99909, 99601, 99612, 202602, '1002', '银行存款', 44000.000000, 0.000000, 55000.000000, 43000.000000, 56000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99910, 99601, 99612, 202602, '1122', '应收账款', 30000.000000, 0.000000, 39000.000000, 18000.000000, 51000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99911, 99601, 99612, 202602, '2202', '应付账款', 0.000000, 39000.000000, 9000.000000, 38000.000000, 0.000000, 68000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99912, 99601, 99612, 202602, '6001', '主营业务收入', 0.000000, 48000.000000, 0.000000, 52000.000000, 0.000000, 100000.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(99913, 99601, 99612, 202602, '6401', '主营业务成本', 28000.000000, 0.000000, 21000.000000, 0.000000, 49000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE ledger_id = VALUES(ledger_id), period_id = VALUES(period_id), period_sort = VALUES(period_sort), subject_name = VALUES(subject_name), opening_debit_amount = VALUES(opening_debit_amount), opening_credit_amount = VALUES(opening_credit_amount), current_debit_amount = VALUES(current_debit_amount), current_credit_amount = VALUES(current_credit_amount), ending_debit_amount = VALUES(ending_debit_amount), ending_credit_amount = VALUES(ending_credit_amount), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order (id, no, status, process_instance_id, supplier_id, account_id, order_time, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(100101, 'PO-20260501-001', 20, NULL, 99301, 99501, '2026-01-05 09:15:00', 120.000000, 15600.000000, 13800.000000, 1800.000000, 0.020000, 300.000000, 2000.000000, NULL, '五金标准采购', NULL, NULL, NULL, 80.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100102, 'PO-20260501-002', 20, NULL, 99302, 99501, '2026-01-08 10:00:00', 60.000000, 9400.000000, 8400.000000, 1200.000000, 0.010000, 200.000000, 1000.000000, NULL, '电子控制板采购', NULL, NULL, NULL, 55.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100103, 'PO-20260501-003', 20, NULL, 99303, 99502, '2026-02-12 14:20:00', 500.000000, 7800.000000, 7200.000000, 900.000000, 0.000000, 0.000000, 0.000000, NULL, '辅料批量采购', NULL, NULL, NULL, 480.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100104, 'PO-20260501-004', 20, NULL, 99304, 99502, '2026-03-02 08:45:00', 18.000000, 21120.000000, 18800.000000, 2320.000000, 0.030000, 600.000000, 3000.000000, NULL, '设备采购', NULL, NULL, NULL, 12.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100105, 'PO-20260501-005', 20, NULL, 99305, 99503, '2026-04-10 11:05:00', 200.000000, 13200.000000, 12000.000000, 1400.000000, 0.015000, 200.000000, 500.000000, NULL, '包装材料采购', NULL, NULL, NULL, 190.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), process_instance_id = VALUES(process_instance_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), order_time = VALUES(order_time), total_count = VALUES(total_count), total_price = VALUES(total_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), deposit_price = VALUES(deposit_price), file_url = VALUES(file_url), remark = VALUES(remark), last_reject_reason = VALUES(last_reject_reason), last_reject_time = VALUES(last_reject_time), last_reject_user_id = VALUES(last_reject_user_id), in_count = VALUES(in_count), return_count = VALUES(return_count), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order_items (id, order_id, product_id, project_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version, count, total_price, tax_percent, tax_price, remark, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(100201, 100101, 99101, NULL, 99001, 1.100000, 0.000000, NULL, NULL, 800.000000, 880.000000, 0.130000, 114.400000, '螺栓', 500.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100202, 100101, 99103, NULL, 99003, 0.150000, 0.000000, NULL, NULL, 4000.000000, 600.000000, 0.130000, 78.000000, '垫片', 2000.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100203, 100102, 99102, NULL, 99004, 55.000000, 0.000000, NULL, NULL, 120.000000, 6600.000000, 0.130000, 858.000000, '控制板', 90.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100204, 100103, 99105, NULL, 99003, 6.000000, 0.000000, NULL, NULL, 1000.000000, 6000.000000, 0.090000, 540.000000, '包装套装', 950.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100205, 100104, 99104, NULL, 99002, 300.000000, 0.000000, NULL, NULL, 60.000000, 18000.000000, 0.130000, 2340.000000, '电机', 30.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100206, 100105, 99105, NULL, 99003, 6.200000, 0.000000, NULL, NULL, 2000.000000, 12400.000000, 0.080000, 992.000000, '包装材料', 1800.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), product_id = VALUES(product_id), project_id = VALUES(project_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), engineering_fee = VALUES(engineering_fee), pricing_bom_id = VALUES(pricing_bom_id), pricing_bom_version = VALUES(pricing_bom_version), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), in_count = VALUES(in_count), return_count = VALUES(return_count), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in (id, no, status, qa_status, process_instance_id, supplier_id, account_id, in_time, order_id, order_no, total_count, total_price, payment_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, qa_time, qa_user_id, qa_remark, qa_pass_count, qa_reject_count, stock_in_count, stock_in_status, stock_in_time, stock_in_user_id, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(101101, 'PI-20260501-001', 20, 20, NULL, 99301, 99501, '2026-01-06 09:00:00', 100101, 'PO-20260501-001', 800.000000, 995.000000, 995.000000, 880.000000, 115.000000, 0.000000, 0.000000, 0.000000, NULL, '五金首批入库', NULL, NULL, NULL, '2026-01-06 11:00:00', 1, '质检通过', 800.000000, 0.000000, 800.000000, 20, '2026-01-06 12:00:00', 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101102, 'PI-20260501-002', 20, 20, NULL, 99302, 99501, '2026-01-09 10:10:00', 100102, 'PO-20260501-002', 120.000000, 7458.000000, 7458.000000, 6600.000000, 858.000000, 0.000000, 0.000000, 0.000000, NULL, '电子首批入库', NULL, NULL, NULL, '2026-01-09 15:00:00', 1, '质检通过', 120.000000, 0.000000, 120.000000, 20, '2026-01-10 09:00:00', 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101103, 'PI-20260501-003', 20, 20, NULL, 99303, 99502, '2026-02-15 08:30:00', 100103, 'PO-20260501-003', 1000.000000, 6540.000000, 6540.000000, 6000.000000, 540.000000, 0.000000, 0.000000, 0.000000, NULL, '辅料入库', NULL, NULL, NULL, '2026-02-15 10:30:00', 1, '质检通过', 1000.000000, 0.000000, 1000.000000, 20, '2026-02-15 11:30:00', 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101104, 'PI-20260501-004', 20, 20, NULL, 99304, 99502, '2026-03-05 13:20:00', 100104, 'PO-20260501-004', 60.000000, 20340.000000, 20340.000000, 18000.000000, 2340.000000, 0.000000, 0.000000, 0.000000, NULL, '设备入库', NULL, NULL, NULL, '2026-03-05 15:00:00', 1, '质检通过', 60.000000, 0.000000, 60.000000, 20, '2026-03-05 16:00:00', 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101105, 'PI-20260501-005', 20, 20, NULL, 99305, 99503, '2026-04-12 09:30:00', 100105, 'PO-20260501-005', 2000.000000, 13392.000000, 13392.000000, 12400.000000, 992.000000, 0.000000, 0.000000, 0.000000, NULL, '包装材料入库', NULL, NULL, NULL, '2026-04-12 11:10:00', 1, '质检通过', 2000.000000, 0.000000, 2000.000000, 20, '2026-04-12 12:30:00', 1, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), qa_status = VALUES(qa_status), process_instance_id = VALUES(process_instance_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), in_time = VALUES(in_time), order_id = VALUES(order_id), order_no = VALUES(order_no), total_count = VALUES(total_count), total_price = VALUES(total_price), payment_price = VALUES(payment_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), other_price = VALUES(other_price), file_url = VALUES(file_url), remark = VALUES(remark), last_reject_reason = VALUES(last_reject_reason), last_reject_time = VALUES(last_reject_time), last_reject_user_id = VALUES(last_reject_user_id), qa_time = VALUES(qa_time), qa_user_id = VALUES(qa_user_id), qa_remark = VALUES(qa_remark), qa_pass_count = VALUES(qa_pass_count), qa_reject_count = VALUES(qa_reject_count), stock_in_count = VALUES(stock_in_count), stock_in_status = VALUES(stock_in_status), stock_in_time = VALUES(stock_in_time), stock_in_user_id = VALUES(stock_in_user_id), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_items (id, in_id, order_item_id, warehouse_id, product_id, purchase_source_batch_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version, count, total_price, tax_percent, tax_price, qa_pass_count, qa_reject_count, stock_in_count, remark, qa_remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(101201, 101101, 100201, 99201, 99101, 106001, 99001, 1.100000, 0.000000, NULL, NULL, 800.000000, 880.000000, 0.130000, 114.400000, 800.000000, 0.000000, 800.000000, '螺栓入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101202, 101101, 100202, 99201, 99103, NULL, 99003, 0.150000, 0.000000, NULL, NULL, 4000.000000, 600.000000, 0.130000, 78.000000, 4000.000000, 0.000000, 4000.000000, '垫片入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101203, 101102, 100203, 99202, 99102, 106002, 99004, 55.000000, 0.000000, NULL, NULL, 120.000000, 6600.000000, 0.130000, 858.000000, 120.000000, 0.000000, 120.000000, '控制板入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101204, 101103, 100204, 99201, 99105, NULL, 99003, 6.000000, 0.000000, NULL, NULL, 1000.000000, 6000.000000, 0.090000, 540.000000, 1000.000000, 0.000000, 1000.000000, '包装套装入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101205, 101104, 100205, 99202, 99104, 106003, 99002, 300.000000, 0.000000, NULL, NULL, 60.000000, 18000.000000, 0.130000, 2340.000000, 60.000000, 0.000000, 60.000000, '电机入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(101206, 101105, 100206, 99203, 99105, NULL, 99003, 6.200000, 0.000000, NULL, NULL, 2000.000000, 12400.000000, 0.080000, 992.000000, 2000.000000, 0.000000, 2000.000000, '包装材料入库', '通过', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE in_id = VALUES(in_id), order_item_id = VALUES(order_item_id), warehouse_id = VALUES(warehouse_id), product_id = VALUES(product_id), purchase_source_batch_id = VALUES(purchase_source_batch_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), engineering_fee = VALUES(engineering_fee), pricing_bom_id = VALUES(pricing_bom_id), pricing_bom_version = VALUES(pricing_bom_version), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), qa_pass_count = VALUES(qa_pass_count), qa_reject_count = VALUES(qa_reject_count), stock_in_count = VALUES(stock_in_count), remark = VALUES(remark), qa_remark = VALUES(qa_remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_source_batch (id, batch_no, product_id, purchase_order_id, purchase_order_item_id, supplier_id, status, biz_date, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(106001, 'PSB-202601-001', 99101, 100101, 100201, 99301, 20, '2026-01-06', '五金来源批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106002, 'PSB-202601-002', 99102, 100102, 100203, 99302, 20, '2026-01-10', '电子来源批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106003, 'PSB-202603-001', 99104, 100104, 100205, 99304, 20, '2026-03-05', '设备来源批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE batch_no = VALUES(batch_no), product_id = VALUES(product_id), purchase_order_id = VALUES(purchase_order_id), purchase_order_item_id = VALUES(purchase_order_item_id), supplier_id = VALUES(supplier_id), status = VALUES(status), biz_date = VALUES(biz_date), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_stock_execute (id, no, purchase_in_id, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107001, 'PISE-202601-001', 101101, 20, '五金分批入库执行', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107002, 'PISE-202601-002', 101102, 20, '电子分批入库执行', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107003, 'PISE-202603-001', 101104, 20, '设备分批入库执行', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE no = VALUES(no), purchase_in_id = VALUES(purchase_in_id), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_stock_execute_item (id, execute_id, purchase_in_id, purchase_in_item_id, product_id, warehouse_id, count, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(108001, 107001, 101101, 101201, 99101, 99201, 800.000000, '五金执行明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108002, 107002, 101102, 101203, 99102, 99202, 120.000000, '电子执行明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108003, 107003, 101104, 101205, 99104, 99202, 60.000000, '设备执行明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE execute_id = VALUES(execute_id), purchase_in_id = VALUES(purchase_in_id), purchase_in_item_id = VALUES(purchase_in_item_id), product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), count = VALUES(count), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_stock_batch (id, product_id, warehouse_id, batch_no, inbound_time, produce_date, expire_date, total_qty, available_qty, locked_qty, virtual_flag, source_biz_type, source_biz_id, source_biz_item_id, purchase_source_batch_id, source_biz_no, purchase_source_batch_no, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(109001, 99101, 99201, 'BATCH-202601-001', '2026-01-06 12:10:00', '2026-01-01', '2027-01-01', 800.000000, 800.000000, 0.000000, b'0', 'PURCHASE_IN', 101101, 101201, 106001, 'PI-20260501-001', 'PSB-202601-001', '五金库存批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109002, 99102, 99202, 'BATCH-202601-002', '2026-01-10 09:20:00', '2026-01-05', '2027-01-05', 120.000000, 120.000000, 0.000000, b'0', 'PURCHASE_IN', 101102, 101203, 106002, 'PI-20260501-002', 'PSB-202601-002', '电子库存批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109003, 99104, 99202, 'BATCH-202603-001', '2026-03-05 16:10:00', '2026-02-20', '2028-02-20', 60.000000, 60.000000, 0.000000, b'0', 'PURCHASE_IN', 101104, 101205, 106003, 'PI-20260501-004', 'PSB-202603-001', '设备库存批次', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), batch_no = VALUES(batch_no), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), total_qty = VALUES(total_qty), available_qty = VALUES(available_qty), locked_qty = VALUES(locked_qty), virtual_flag = VALUES(virtual_flag), source_biz_type = VALUES(source_biz_type), source_biz_id = VALUES(source_biz_id), source_biz_item_id = VALUES(source_biz_item_id), purchase_source_batch_id = VALUES(purchase_source_batch_id), source_biz_no = VALUES(source_biz_no), purchase_source_batch_no = VALUES(purchase_source_batch_no), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_in_stock_execute_item_batch (id, execute_item_id, purchase_in_item_id, stock_batch_id, purchase_source_batch_id, product_id, warehouse_id, batch_no, purchase_source_batch_no, count, inbound_time, produce_date, expire_date, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(110001, 108001, 101201, 109001, 106001, 99101, 99201, 'BATCH-202601-001', 'PSB-202601-001', 500.000000, '2026-01-06 12:10:00', '2026-01-01', '2027-01-01', '五金批次1', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110002, 108001, 101201, 109001, 106001, 99101, 99201, 'BATCH-202601-001', 'PSB-202601-001', 300.000000, '2026-01-06 12:15:00', '2026-01-01', '2027-01-01', '五金批次2', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110003, 108002, 101203, 109002, 106002, 99102, 99202, 'BATCH-202601-002', 'PSB-202601-002', 120.000000, '2026-01-10 09:20:00', '2026-01-05', '2027-01-05', '电子整批入库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110004, 108003, 101205, 109003, 106003, 99104, 99202, 'BATCH-202603-001', 'PSB-202603-001', 60.000000, '2026-03-05 16:10:00', '2026-02-20', '2028-02-20', '设备整批入库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE execute_item_id = VALUES(execute_item_id), purchase_in_item_id = VALUES(purchase_in_item_id), stock_batch_id = VALUES(stock_batch_id), purchase_source_batch_id = VALUES(purchase_source_batch_id), product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), batch_no = VALUES(batch_no), purchase_source_batch_no = VALUES(purchase_source_batch_no), count = VALUES(count), inbound_time = VALUES(inbound_time), produce_date = VALUES(produce_date), expire_date = VALUES(expire_date), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_estimate (id, estimate_no, estimate_month, source_biz_type, source_biz_id, source_biz_no, source_purchase_in_id, source_purchase_in_no, source_order_id, source_order_no, supplier_id, account_id, currency_code, source_amount, amount, status, confirm_user_id, confirm_time, reverse_user_id, reverse_time, reverse_type, reverse_source_id, reverse_source_no, reverse_remark, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(102001, 'APE-202601-001', '2026-01', 11, 101101, 'PI-20260501-001', 101101, 'PI-20260501-001', 100101, 'PO-20260501-001', 99301, 99501, 'CNY', 1000.000000, 995.000000, 20, 1, '2026-01-06 12:00:00', NULL, NULL, NULL, NULL, NULL, NULL, '五金暂估', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(102002, 'APE-202601-002', '2026-01', 11, 101102, 'PI-20260501-002', 101102, 'PI-20260501-002', 100102, 'PO-20260501-002', 99302, 99501, 'CNY', 7600.000000, 7458.000000, 20, 1, '2026-01-10 09:30:00', NULL, NULL, NULL, NULL, NULL, NULL, '电子暂估', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(102003, 'APE-202602-001', '2026-02', 11, 101103, 'PI-20260501-003', 101103, 'PI-20260501-003', 100103, 'PO-20260501-003', 99303, 99502, 'CNY', 6600.000000, 6540.000000, 20, 1, '2026-02-15 12:00:00', NULL, NULL, NULL, NULL, NULL, NULL, '辅料暂估', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(102004, 'APE-202603-001', '2026-03', 11, 101104, 'PI-20260501-004', 101104, 'PI-20260501-004', 100104, 'PO-20260501-004', 99304, 99502, 'CNY', 21000.000000, 20340.000000, 20, 1, '2026-03-05 17:00:00', NULL, NULL, NULL, NULL, NULL, NULL, '设备暂估', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(102005, 'APE-202604-001', '2026-04', 11, 101105, 'PI-20260501-005', 101105, 'PI-20260501-005', 100105, 'PO-20260501-005', 99305, 99503, 'CNY', 13400.000000, 13392.000000, 20, 1, '2026-04-12 13:00:00', NULL, NULL, NULL, NULL, NULL, NULL, '包装暂估', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE estimate_month = VALUES(estimate_month), source_biz_type = VALUES(source_biz_type), source_biz_id = VALUES(source_biz_id), source_biz_no = VALUES(source_biz_no), source_purchase_in_id = VALUES(source_purchase_in_id), source_purchase_in_no = VALUES(source_purchase_in_no), source_order_id = VALUES(source_order_id), source_order_no = VALUES(source_order_no), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), currency_code = VALUES(currency_code), source_amount = VALUES(source_amount), amount = VALUES(amount), status = VALUES(status), confirm_user_id = VALUES(confirm_user_id), confirm_time = VALUES(confirm_time), reverse_user_id = VALUES(reverse_user_id), reverse_time = VALUES(reverse_time), reverse_type = VALUES(reverse_type), reverse_source_id = VALUES(reverse_source_id), reverse_source_no = VALUES(reverse_source_no), reverse_remark = VALUES(reverse_remark), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement (id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount, paid_amount, remain_amount, currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(103001, 'AP-202601-001', 11, 101101, 'PI-20260501-001', 100101, 'PO-20260501-001', 99301, 99501, 995.000000, 0.000000, 995.000000, 'CNY', '2026-01-06 12:00:00', '2026-02-06 12:00:00', 0, NULL, NULL, 10, '五金应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103002, 'AP-202601-002', 11, 101102, 'PI-20260501-002', 100102, 'PO-20260501-002', 99302, 99501, 7458.000000, 2458.000000, 5000.000000, 'CNY', '2026-01-10 09:30:00', '2026-02-10 09:30:00', 1, 'INV-202601-001', 7458.000000, 10, '电子应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103003, 'AP-202602-001', 11, 101103, 'PI-20260501-003', 100103, 'PO-20260501-003', 99303, 99502, 6540.000000, 1540.000000, 5000.000000, 'CNY', '2026-02-15 12:00:00', '2026-03-15 12:00:00', 0, NULL, NULL, 10, '辅料应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103004, 'AP-202603-001', 11, 101104, 'PI-20260501-004', 100104, 'PO-20260501-004', 99304, 99502, 20340.000000, 5000.000000, 15340.000000, 'CNY', '2026-03-05 17:00:00', '2026-04-05 17:00:00', 1, 'INV-202603-001', 20340.000000, 10, '设备应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103005, 'AP-202604-001', 11, 101105, 'PI-20260501-005', 100105, 'PO-20260501-005', 99305, 99503, 13392.000000, 1392.000000, 12000.000000, 'CNY', '2026-04-12 13:00:00', '2026-05-12 13:00:00', 0, NULL, NULL, 10, '包装应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE statement_no = VALUES(statement_no), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), source_order_id = VALUES(source_order_id), source_order_no = VALUES(source_order_no), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), amount = VALUES(amount), paid_amount = VALUES(paid_amount), remain_amount = VALUES(remain_amount), currency_code = VALUES(currency_code), biz_date = VALUES(biz_date), due_date = VALUES(due_date), invoice_status = VALUES(invoice_status), invoice_no = VALUES(invoice_no), invoice_amount = VALUES(invoice_amount), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement_item (id, statement_id, item_type, ref_type, ref_id, ref_no, amount, after_paid_amount, after_remain_amount, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(103101, 103001, 10, 11, 101101, 'PI-20260501-001', 995.000000, 0.000000, 995.000000, '采购入库转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103102, 103002, 10, 11, 101102, 'PI-20260501-002', 7458.000000, 2458.000000, 5000.000000, '采购入库转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103103, 103003, 10, 11, 101103, 'PI-20260501-003', 6540.000000, 1540.000000, 5000.000000, '采购入库转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103104, 103004, 10, 11, 101104, 'PI-20260501-004', 20340.000000, 5000.000000, 15340.000000, '采购入库转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(103105, 103005, 10, 11, 101105, 'PI-20260501-005', 13392.000000, 1392.000000, 12000.000000, '采购入库转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE statement_id = VALUES(statement_id), item_type = VALUES(item_type), ref_type = VALUES(ref_type), ref_id = VALUES(ref_id), ref_no = VALUES(ref_no), amount = VALUES(amount), after_paid_amount = VALUES(after_paid_amount), after_remain_amount = VALUES(after_remain_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment (id, no, status, payment_time, finance_user_id, supplier_id, account_id, total_price, discount_price, payment_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104001, 'PAY-202601-001', 20, '2026-01-15 10:00:00', @finance_user_id, 99301, 99501, 995.000000, 0.000000, 995.000000, '首批付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104002, 'PAY-202601-002', 20, '2026-01-20 15:20:00', @finance_user_id, 99302, 99501, 2458.000000, 0.000000, 2458.000000, '部分付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104003, 'PAY-202602-001', 20, '2026-02-18 09:40:00', @finance_user_id, 99303, 99502, 1540.000000, 0.000000, 1540.000000, '部分付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104004, 'PAY-202603-001', 20, '2026-03-18 11:30:00', @finance_user_id, 99304, 99502, 5000.000000, 0.000000, 5000.000000, '部分付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104005, 'PAY-202604-001', 20, '2026-04-20 16:10:00', @finance_user_id, 99305, 99503, 1392.000000, 0.000000, 1392.000000, '部分付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), payment_time = VALUES(payment_time), finance_user_id = VALUES(finance_user_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), total_price = VALUES(total_price), discount_price = VALUES(discount_price), payment_price = VALUES(payment_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_item (id, payment_id, ap_statement_id, biz_type, biz_id, biz_no, total_price, paid_price, payment_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104101, 104001, 103001, 11, 101101, 'PI-20260501-001', 995.000000, 0.000000, 995.000000, '应付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104102, 104002, 103002, 11, 101102, 'PI-20260501-002', 7458.000000, 0.000000, 2458.000000, '应付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104103, 104003, 103003, 11, 101103, 'PI-20260501-003', 6540.000000, 0.000000, 1540.000000, '应付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104104, 104004, 103004, 11, 101104, 'PI-20260501-004', 20340.000000, 0.000000, 5000.000000, '应付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104105, 104005, 103005, 11, 101105, 'PI-20260501-005', 13392.000000, 0.000000, 1392.000000, '应付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE payment_id = VALUES(payment_id), ap_statement_id = VALUES(ap_statement_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), total_price = VALUES(total_price), paid_price = VALUES(paid_price), payment_price = VALUES(payment_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_allocate (id, payment_id, payment_item_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104201, 104001, 104101, 103001, 995.000000, 99301, 11, 101101, 'PI-20260501-001', 20, '核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104202, 104002, 104102, 103002, 2458.000000, 99302, 11, 101102, 'PI-20260501-002', 20, '核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104203, 104003, 104103, 103003, 1540.000000, 99303, 11, 101103, 'PI-20260501-003', 20, '核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104204, 104004, 104104, 103004, 5000.000000, 99304, 11, 101104, 'PI-20260501-004', 20, '核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104205, 104005, 104105, 103005, 1392.000000, 99305, 11, 101105, 'PI-20260501-005', 20, '核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE payment_id = VALUES(payment_id), payment_item_id = VALUES(payment_item_id), ap_statement_id = VALUES(ap_statement_id), allocate_amount = VALUES(allocate_amount), supplier_id = VALUES(supplier_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_return (id, no, status, supplier_id, account_id, return_time, order_id, order_no, total_count, total_price, refund_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107001, 'CGTH-20260513-001', 20, 99301, 99501, '2026-05-13 09:20:00', 100101, 'PO-20260501-001', 20.000000, 16.224000, 0.000000, 14.400000, 1.824000, 0.000000, 0.000000, 0.000000, NULL, '螺栓与垫片混合退货，已审核未退款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107002, 'CGTH-20260513-002', 20, 99302, 99501, '2026-05-13 10:05:00', 100102, 'PO-20260501-002', 10.000000, 621.500000, 120.000000, 550.000000, 71.500000, 0.000000, 0.000000, 0.000000, NULL, '电子控制板部分退货，已审核且已退款一部分', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107003, 'CGTH-20260513-003', 20, 99305, 99503, '2026-05-13 10:40:00', 100105, 'PO-20260501-005', 20.000000, 133.920000, 133.920000, 124.000000, 9.920000, 0.000000, 0.000000, 0.000000, 'https://www.iocoder.cn/demo/purchase-return/CGTH-20260513-003-附件A.pdf,https://www.iocoder.cn/demo/purchase-return/CGTH-20260513-003-附件B.pdf', '包装材料全额退货，已审核且已退款完成，带两份来源附件', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107004, 'CGTH-20260513-004', 10, 99304, 99502, '2026-05-13 11:10:00', 100104, 'PO-20260501-004', 2.000000, 678.000000, 0.000000, 600.000000, 78.000000, 0.000000, 0.000000, 0.000000, NULL, '设备采购退货草稿，待审批', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), return_time = VALUES(return_time), order_id = VALUES(order_id), order_no = VALUES(order_no), total_count = VALUES(total_count), total_price = VALUES(total_price), refund_price = VALUES(refund_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), other_price = VALUES(other_price), file_url = VALUES(file_url), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_return_items (id, return_id, order_item_id, warehouse_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107101, 107001, 100201, 99201, 99101, 99001, 1.100000, 12.000000, 13.200000, 0.130000, 1.716000, '螺栓外观不良退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107102, 107001, 100202, 99201, 99103, 99003, 0.150000, 8.000000, 1.200000, 0.090000, 0.108000, '垫片尺寸偏差退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107103, 107002, 100203, 99202, 99102, 99004, 55.000000, 10.000000, 550.000000, 0.130000, 71.500000, '电子控制板批次退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107104, 107003, 100206, 99203, 99105, 99003, 6.200000, 20.000000, 124.000000, 0.080000, 9.920000, '包装材料全额退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107105, 107004, 100205, 99202, 99104, 99002, 300.000000, 2.000000, 600.000000, 0.130000, 78.000000, '设备采购退货草稿明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_id = VALUES(return_id), order_item_id = VALUES(order_item_id), warehouse_id = VALUES(warehouse_id), product_id = VALUES(product_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement (id, statement_no, biz_type, biz_id, biz_no, source_order_id, source_order_no, supplier_id, account_id, amount, paid_amount, remain_amount, currency_code, biz_date, due_date, invoice_status, invoice_no, invoice_amount, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107301, 'AP-12-CGTH-20260513-001', 12, 107001, 'CGTH-20260513-001', 100101, 'PO-20260501-001', 99301, 99501, -16.224000, 0.000000, -16.224000, 'CNY', '2026-05-13 09:20:00', '2026-05-13 09:20:00', 0, NULL, NULL, 10, '采购退货应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107302, 'AP-12-CGTH-20260513-002', 12, 107002, 'CGTH-20260513-002', 100102, 'PO-20260501-002', 99302, 99501, -621.500000, -120.000000, -501.500000, 'CNY', '2026-05-13 10:05:00', '2026-05-13 10:05:00', 0, NULL, NULL, 20, '采购退货应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107303, 'AP-12-CGTH-20260513-003', 12, 107003, 'CGTH-20260513-003', 100105, 'PO-20260501-005', 99305, 99503, -133.920000, -133.920000, 0.000000, 'CNY', '2026-05-13 10:40:00', '2026-05-13 10:40:00', 0, NULL, NULL, 30, '采购退货应付台账', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE statement_no = VALUES(statement_no), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), source_order_id = VALUES(source_order_id), source_order_no = VALUES(source_order_no), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), amount = VALUES(amount), paid_amount = VALUES(paid_amount), remain_amount = VALUES(remain_amount), currency_code = VALUES(currency_code), biz_date = VALUES(biz_date), due_date = VALUES(due_date), invoice_status = VALUES(invoice_status), invoice_no = VALUES(invoice_no), invoice_amount = VALUES(invoice_amount), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_statement_item (id, statement_id, item_type, ref_type, ref_id, ref_no, amount, after_paid_amount, after_remain_amount, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107401, 107301, 10, 12, 107001, 'CGTH-20260513-001', -16.224000, 0.000000, -16.224000, '采购退货转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107402, 107302, 10, 12, 107002, 'CGTH-20260513-002', -621.500000, -120.000000, -501.500000, '采购退货转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107403, 107303, 10, 12, 107003, 'CGTH-20260513-003', -133.920000, -133.920000, 0.000000, '采购退货转应付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE statement_id = VALUES(statement_id), item_type = VALUES(item_type), ref_type = VALUES(ref_type), ref_id = VALUES(ref_id), ref_no = VALUES(ref_no), amount = VALUES(amount), after_paid_amount = VALUES(after_paid_amount), after_remain_amount = VALUES(after_remain_amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_stock_record (id, product_id, warehouse_id, count, total_count, biz_type, biz_id, biz_item_id, biz_no, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107501, 99101, 99201, -12.000000, 788.000000, 80, 107001, 107101, 'CGTH-20260513-001', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107502, 99103, 99201, -8.000000, 3992.000000, 80, 107001, 107102, 'CGTH-20260513-001', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107503, 99102, 99202, -10.000000, 110.000000, 80, 107002, 107103, 'CGTH-20260513-002', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107504, 99105, 99203, -20.000000, 1980.000000, 80, 107003, 107104, 'CGTH-20260513-003', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE product_id = VALUES(product_id), warehouse_id = VALUES(warehouse_id), count = VALUES(count), total_count = VALUES(total_count), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_item_id = VALUES(biz_item_id), biz_no = VALUES(biz_no), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order (id, no, status, process_instance_id, supplier_id, account_id, order_time, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, file_url, remark, last_reject_reason, last_reject_time, last_reject_user_id, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(100101, 'PO-20260501-001', 20, NULL, 99301, 99501, '2026-01-05 09:15:00', 120.000000, 15600.000000, 13800.000000, 1800.000000, 0.020000, 300.000000, 2000.000000, NULL, '五金标准采购', NULL, NULL, NULL, 80.000000, 20.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100102, 'PO-20260501-002', 20, NULL, 99302, 99501, '2026-01-08 10:00:00', 60.000000, 9400.000000, 8400.000000, 1200.000000, 0.010000, 200.000000, 1000.000000, NULL, '电子控制板采购', NULL, NULL, NULL, 55.000000, 10.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100104, 'PO-20260501-004', 20, NULL, 99304, 99502, '2026-03-02 08:45:00', 18.000000, 21120.000000, 18800.000000, 2320.000000, 0.030000, 600.000000, 3000.000000, NULL, '设备采购', NULL, NULL, NULL, 12.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100105, 'PO-20260501-005', 20, NULL, 99305, 99503, '2026-04-10 11:05:00', 200.000000, 13200.000000, 12000.000000, 1400.000000, 0.015000, 200.000000, 500.000000, NULL, '包装材料采购', NULL, NULL, NULL, 190.000000, 20.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), process_instance_id = VALUES(process_instance_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), order_time = VALUES(order_time), total_count = VALUES(total_count), total_price = VALUES(total_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), deposit_price = VALUES(deposit_price), file_url = VALUES(file_url), remark = VALUES(remark), last_reject_reason = VALUES(last_reject_reason), last_reject_time = VALUES(last_reject_time), last_reject_user_id = VALUES(last_reject_user_id), in_count = VALUES(in_count), return_count = VALUES(return_count), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_purchase_order_items (id, order_id, product_id, project_id, product_unit_id, product_price, engineering_fee, pricing_bom_id, pricing_bom_version, count, total_price, tax_percent, tax_price, remark, in_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(100201, 100101, 99101, NULL, 99001, 1.100000, 0.000000, NULL, NULL, 800.000000, 880.000000, 0.130000, 114.400000, '螺栓', 500.000000, 12.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100202, 100101, 99103, NULL, 99003, 0.150000, 0.000000, NULL, NULL, 4000.000000, 600.000000, 0.130000, 78.000000, '垫片', 2000.000000, 8.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100203, 100102, 99102, NULL, 99004, 55.000000, 0.000000, NULL, NULL, 120.000000, 6600.000000, 0.130000, 858.000000, '控制板', 90.000000, 10.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100205, 100104, 99104, NULL, 99002, 300.000000, 0.000000, NULL, NULL, 60.000000, 18000.000000, 0.130000, 2340.000000, '电机', 30.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(100206, 100105, 99105, NULL, 99003, 6.200000, 0.000000, NULL, NULL, 2000.000000, 12400.000000, 0.080000, 992.000000, '包装材料', 1800.000000, 20.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), product_id = VALUES(product_id), project_id = VALUES(project_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), engineering_fee = VALUES(engineering_fee), pricing_bom_id = VALUES(pricing_bom_id), pricing_bom_version = VALUES(pricing_bom_version), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), in_count = VALUES(in_count), return_count = VALUES(return_count), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment (id, no, status, prepayment_time, finance_user_id, supplier_id, account_id, prepayment_price, allocated_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(105001, 'PRE-202601-001', 20, '2026-01-12 09:00:00', @finance_user_id, 99301, 99501, 3000.000000, 1000.000000, 2000.000000, '预付五金', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105002, 'PRE-202602-001', 20, '2026-02-11 09:30:00', @finance_user_id, 99302, 99501, 5000.000000, 2000.000000, 3000.000000, '预付电子', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105003, 'PRE-202603-001', 20, '2026-03-11 09:30:00', @finance_user_id, 99303, 99502, 4000.000000, 500.000000, 3500.000000, '预付辅料', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105004, 'PRE-202604-001', 20, '2026-04-11 09:30:00', @finance_user_id, 99304, 99502, 10000.000000, 3000.000000, 7000.000000, '预付设备', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105005, 'PRE-202605-001', 20, '2026-05-11 09:30:00', @finance_user_id, 99305, 99503, 2600.000000, 600.000000, 2000.000000, '预付包装', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), prepayment_time = VALUES(prepayment_time), finance_user_id = VALUES(finance_user_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), prepayment_price = VALUES(prepayment_price), allocated_price = VALUES(allocated_price), remain_price = VALUES(remain_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment_allocate (id, prepayment_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(105101, 105001, 103001, 1000.000000, 99301, 11, 101101, 'PI-20260501-001', 20, '预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105102, 105002, 103002, 2000.000000, 99302, 11, 101102, 'PI-20260501-002', 20, '预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105103, 105003, 103003, 500.000000, 99303, 11, 101103, 'PI-20260501-003', 20, '预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105104, 105004, 103004, 3000.000000, 99304, 11, 101104, 'PI-20260501-004', 20, '预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105105, 105005, 103005, 600.000000, 99305, 11, 101105, 'PI-20260501-005', 20, '预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE prepayment_id = VALUES(prepayment_id), ap_statement_id = VALUES(ap_statement_id), allocate_amount = VALUES(allocate_amount), supplier_id = VALUES(supplier_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt (id, no, status, receipt_time, finance_user_id, customer_id, account_id, total_price, discount_price, receipt_price, remark, file_url, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(106001, 'RCPT-202601-001', 20, '2026-01-16 10:00:00', @finance_user_id, 99401, 99503, 5000.000000, 0.000000, 5000.000000, '首批回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106002, 'RCPT-202602-001', 20, '2026-02-18 15:00:00', @finance_user_id, 99402, 99503, 12000.000000, 0.000000, 12000.000000, '回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106003, 'RCPT-202603-001', 20, '2026-03-20 11:30:00', @finance_user_id, 99403, 99502, 8000.000000, 0.000000, 8000.000000, '回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), receipt_time = VALUES(receipt_time), finance_user_id = VALUES(finance_user_id), customer_id = VALUES(customer_id), account_id = VALUES(account_id), total_price = VALUES(total_price), discount_price = VALUES(discount_price), receipt_price = VALUES(receipt_price), remark = VALUES(remark), file_url = VALUES(file_url), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt_item (id, receipt_id, biz_type, biz_id, biz_no, total_price, receipted_price, receipt_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(106101, 106001, 21, 201001, 'SO-202601-001', 5000.000000, 0.000000, 5000.000000, '销售回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106102, 106002, 21, 201002, 'SO-202602-001', 12000.000000, 0.000000, 12000.000000, '销售回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106103, 106003, 21, 201003, 'SO-202603-001', 8000.000000, 0.000000, 8000.000000, '销售回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE receipt_id = VALUES(receipt_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), total_price = VALUES(total_price), receipted_price = VALUES(receipted_price), receipt_price = VALUES(receipt_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_expense (id, no, status, expense_time, expense_type, dept_id, project_id, supplier_id, finance_user_id, account_id, expense_price, paid_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107001, 'EXP-202601-001', 20, '2026-01-22 14:00:00', 10, 106, NULL, NULL, @finance_user_id, 99501, 860.000000, 860.000000, 0.000000, '差旅报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107002, 'EXP-202602-001', 20, '2026-02-22 14:00:00', 20, 106, NULL, NULL, @finance_user_id, 99501, 1290.000000, 1290.000000, 0.000000, '办公采购报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107003, 'EXP-202603-001', 20, '2026-03-22 14:00:00', 30, 106, NULL, NULL, @finance_user_id, 99502, 980.000000, 980.000000, 0.000000, '招待费报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107004, 'EXP-202604-001', 20, '2026-04-22 14:00:00', 10, 106, NULL, NULL, @finance_user_id, 99502, 1750.000000, 1750.000000, 0.000000, '差旅报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107005, 'EXP-202605-001', 20, '2026-05-22 14:00:00', 20, 106, NULL, NULL, @finance_user_id, 99503, 640.000000, 640.000000, 0.000000, '办公采购报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), expense_time = VALUES(expense_time), expense_type = VALUES(expense_type), dept_id = VALUES(dept_id), project_id = VALUES(project_id), supplier_id = VALUES(supplier_id), finance_user_id = VALUES(finance_user_id), account_id = VALUES(account_id), expense_price = VALUES(expense_price), paid_price = VALUES(paid_price), remain_price = VALUES(remain_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_expense_item (id, expense_id, item_name, amount, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107101, 107001, '差旅费', 860.000000, '交通与住宿', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107102, 107002, '办公用品', 1290.000000, '电脑耗材', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107103, 107003, '招待费', 980.000000, '客户招待', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107104, 107004, '差旅费', 1750.000000, '异地出差', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107105, 107005, '办公用品', 640.000000, '纸张耗材', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE expense_id = VALUES(expense_id), item_name = VALUES(item_name), amount = VALUES(amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher (id, voucher_no, ledger_id, period_id, template_id, biz_type, biz_id, biz_no, voucher_time, status, total_debit_amount, total_credit_amount, approve_user_id, approve_time, post_user_id, post_time, reverse_user_id, reverse_time, reverse_voucher_id, reverse_from_voucher_id, reverse_remark, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(108001, 'VCH-202601-001', 99601, 99611, 99821, 11, 101101, 'PI-20260501-001', '2026-01-06 12:30:00', 20, 995.000000, 995.000000, @finance_user_id, '2026-01-06 13:00:00', @finance_user_id, '2026-01-06 13:10:00', NULL, NULL, NULL, NULL, NULL, '采购入库凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108002, 'VCH-202601-002', 99601, 99611, 99821, 11, 101102, 'PI-20260501-002', '2026-01-10 09:50:00', 20, 7458.000000, 7458.000000, @finance_user_id, '2026-01-10 10:00:00', @finance_user_id, '2026-01-10 10:10:00', NULL, NULL, NULL, NULL, NULL, '采购入库凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108003, 'VCH-202602-001', 99601, 99612, 99821, 11, 101103, 'PI-20260501-003', '2026-02-15 12:30:00', 20, 6540.000000, 6540.000000, @finance_user_id, '2026-02-15 13:00:00', @finance_user_id, '2026-02-15 13:10:00', NULL, NULL, NULL, NULL, NULL, '采购入库凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108004, 'VCH-202603-001', 99601, 99613, 99821, 11, 101104, 'PI-20260501-004', '2026-03-05 17:30:00', 20, 20340.000000, 20340.000000, @finance_user_id, '2026-03-05 18:00:00', @finance_user_id, '2026-03-05 18:10:00', NULL, NULL, NULL, NULL, NULL, '采购入库凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108005, 'VCH-202604-001', 99601, 99614, 99821, 11, 101105, 'PI-20260501-005', '2026-04-12 13:30:00', 20, 13392.000000, 13392.000000, @finance_user_id, '2026-04-12 14:00:00', @finance_user_id, '2026-04-12 14:10:00', NULL, NULL, NULL, NULL, NULL, '采购入库凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108006, 'VCH-202601-EXP-001', 99601, 99611, 99822, 40, 107001, 'EXP-202601-001', '2026-01-22 16:00:00', 20, 860.000000, 860.000000, @finance_user_id, '2026-01-22 16:20:00', @finance_user_id, '2026-01-22 16:30:00', NULL, NULL, NULL, NULL, NULL, '费用报销凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108007, 'VCH-202602-EXP-001', 99601, 99612, 99822, 40, 107002, 'EXP-202602-001', '2026-02-22 16:00:00', 20, 1290.000000, 1290.000000, @finance_user_id, '2026-02-22 16:20:00', @finance_user_id, '2026-02-22 16:30:00', NULL, NULL, NULL, NULL, NULL, '费用报销凭证', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE voucher_no = VALUES(voucher_no), ledger_id = VALUES(ledger_id), period_id = VALUES(period_id), template_id = VALUES(template_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), voucher_time = VALUES(voucher_time), status = VALUES(status), total_debit_amount = VALUES(total_debit_amount), total_credit_amount = VALUES(total_credit_amount), approve_user_id = VALUES(approve_user_id), approve_time = VALUES(approve_time), post_user_id = VALUES(post_user_id), post_time = VALUES(post_time), reverse_user_id = VALUES(reverse_user_id), reverse_time = VALUES(reverse_time), reverse_voucher_id = VALUES(reverse_voucher_id), reverse_from_voucher_id = VALUES(reverse_from_voucher_id), reverse_remark = VALUES(reverse_remark), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_voucher_entry (id, voucher_id, entry_no, summary, subject_code, subject_name, debit_amount, credit_amount, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(108101, 108001, 1, '确认采购入库', '1405', '库存商品', 995.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108102, 108001, 2, '确认采购入库', '2202', '应付账款', 0.000000, 995.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108103, 108002, 1, '确认采购入库', '1405', '库存商品', 6600.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108104, 108002, 2, '确认采购入库', '2202', '应付账款', 0.000000, 7458.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108105, 108003, 1, '确认采购入库', '1405', '库存商品', 6540.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108106, 108003, 2, '确认采购入库', '2202', '应付账款', 0.000000, 6540.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108107, 108004, 1, '确认采购入库', '1405', '库存商品', 20340.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108108, 108004, 2, '确认采购入库', '2202', '应付账款', 0.000000, 20340.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108109, 108005, 1, '确认采购入库', '1405', '库存商品', 13392.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108110, 108005, 2, '确认采购入库', '2202', '应付账款', 0.000000, 13392.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108111, 108006, 1, '费用报销', '6601', '销售费用', 860.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108112, 108006, 2, '费用报销', '1002', '银行存款', 0.000000, 860.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108113, 108007, 1, '费用报销', '6601', '销售费用', 1290.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(108114, 108007, 2, '费用报销', '1002', '银行存款', 0.000000, 1290.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE voucher_id = VALUES(voucher_id), entry_no = VALUES(entry_no), summary = VALUES(summary), subject_code = VALUES(subject_code), subject_name = VALUES(subject_name), debit_amount = VALUES(debit_amount), credit_amount = VALUES(credit_amount), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_invoice (id, supplier_id, invoice_no, invoice_date, invoice_type, total_count, matched_count, total_amount, matched_amount, unmatched_amount, tolerance_amount, difference_amount, match_status, difference_reason, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(109001, 99301, 'INV-202601-001', '2026-01-08 00:00:00', 10, 1.000000, 1.000000, 995.000000, 995.000000, 0.000000, 5.000000, 0.000000, 20, NULL, '五金发票', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109002, 99302, 'INV-202601-002', '2026-01-11 00:00:00', 10, 1.000000, 1.000000, 7458.000000, 7458.000000, 0.000000, 5.000000, 0.000000, 20, NULL, '电子发票', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109003, 99303, 'INV-202602-001', '2026-02-16 00:00:00', 10, 1.000000, 1.000000, 6540.000000, 6540.000000, 0.000000, 5.000000, 0.000000, 20, NULL, '辅料发票', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109004, 99304, 'INV-202603-001', '2026-03-06 00:00:00', 10, 1.000000, 1.000000, 20340.000000, 20340.000000, 0.000000, 10.000000, 0.000000, 20, NULL, '设备发票', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109005, 99305, 'INV-202604-001', '2026-04-13 00:00:00', 10, 1.000000, 1.000000, 13392.000000, 13392.000000, 0.000000, 10.000000, 0.000000, 20, NULL, '包装发票', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE supplier_id = VALUES(supplier_id), invoice_no = VALUES(invoice_no), invoice_date = VALUES(invoice_date), invoice_type = VALUES(invoice_type), total_count = VALUES(total_count), matched_count = VALUES(matched_count), total_amount = VALUES(total_amount), matched_amount = VALUES(matched_amount), unmatched_amount = VALUES(unmatched_amount), tolerance_amount = VALUES(tolerance_amount), difference_amount = VALUES(difference_amount), match_status = VALUES(match_status), difference_reason = VALUES(difference_reason), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_ap_invoice_match_item (id, invoice_id, ap_statement_id, source_order_id, source_order_no, source_purchase_in_id, source_purchase_in_no, source_purchase_in_item_id, product_id, supplier_id, match_count, match_amount, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(109101, 109001, 103001, 100101, 'PO-20260501-001', 101101, 'PI-20260501-001', 101201, 99101, 99301, 800.000000, 995.000000, 20, '匹配完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109102, 109002, 103002, 100102, 'PO-20260501-002', 101102, 'PI-20260501-002', 101203, 99102, 99302, 120.000000, 7458.000000, 20, '匹配完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109103, 109003, 103003, 100103, 'PO-20260501-003', 101103, 'PI-20260501-003', 101204, 99105, 99303, 1000.000000, 6540.000000, 20, '匹配完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109104, 109004, 103004, 100104, 'PO-20260501-004', 101104, 'PI-20260501-004', 101205, 99104, 99304, 60.000000, 20340.000000, 20, '匹配完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(109105, 109005, 103005, 100105, 'PO-20260501-005', 101105, 'PI-20260501-005', 101206, 99105, 99305, 2000.000000, 13392.000000, 20, '匹配完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE invoice_id = VALUES(invoice_id), ap_statement_id = VALUES(ap_statement_id), source_order_id = VALUES(source_order_id), source_order_no = VALUES(source_order_no), source_purchase_in_id = VALUES(source_purchase_in_id), source_purchase_in_no = VALUES(source_purchase_in_no), source_purchase_in_item_id = VALUES(source_purchase_in_item_id), product_id = VALUES(product_id), supplier_id = VALUES(supplier_id), match_count = VALUES(match_count), match_amount = VALUES(match_amount), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order (id, no, status, process_instance_id, customer_id, project_id, business_type, source_project_id, settlement_type, source_product_id, account_id, sale_user_id, order_time, delivery_date, total_count, total_price, total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, last_reject_reason, last_reject_time, last_reject_user_id, file_url, remark, out_count, return_count, delivery_ready_status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(110001, 'SO-202601-001', 20, NULL, 99401, NULL, '标准销售', NULL, '现款', 99101, 99503, @finance_user_id, '2026-01-02 09:00:00', '2026-01-08', 100.000000, 5800.000000, 5300.000000, 690.000000, 0.000000, 190.000000, 1000.000000, NULL, NULL, NULL, NULL, '首批销售订单', 100.000000, 0.000000, 'READY_TO_SHIP', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110002, 'SO-202601-002', 20, NULL, 99402, NULL, '标准销售', NULL, '月结', 99102, 99503, @finance_user_id, '2026-01-10 10:00:00', '2026-01-18', 60.000000, 9200.000000, 8400.000000, 1092.000000, 0.010000, 292.000000, 1500.000000, NULL, NULL, NULL, NULL, '电子订单', 60.000000, 0.000000, 'READY_TO_SHIP', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110003, 'SO-202602-001', 20, NULL, 99403, NULL, '标准销售', NULL, '现款', 99103, 99502, @finance_user_id, '2026-02-05 11:15:00', '2026-02-12', 300.000000, 1500.000000, 1380.000000, 120.000000, 0.000000, 0.000000, 0.000000, NULL, NULL, NULL, NULL, '辅料订单', 300.000000, 0.000000, 'READY_TO_SHIP', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110004, 'SO-202603-001', 20, NULL, 99401, NULL, '标准销售', NULL, '现款', 99104, 99501, @finance_user_id, '2026-03-03 14:30:00', '2026-03-12', 20.000000, 10800.000000, 9600.000000, 1200.000000, 0.020000, 0.000000, 2000.000000, NULL, NULL, NULL, NULL, '设备订单', 18.000000, 2.000000, 'PART_READY', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110005, 'SO-202604-001', 20, NULL, 99402, NULL, '标准销售', NULL, '月结', 99105, 99503, @finance_user_id, '2026-04-08 16:00:00', '2026-04-18', 1000.000000, 12500.000000, 11500.000000, 1000.000000, 0.000000, 0.000000, 2000.000000, NULL, NULL, NULL, NULL, '包装订单', 980.000000, 0.000000, 'READY_TO_SHIP', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110006, 'SO-202605-001', 20, NULL, 99403, NULL, '标准销售', NULL, '现款', 99101, 99502, @finance_user_id, '2026-05-06 09:20:00', '2026-05-16', 220.000000, 6600.000000, 6000.000000, 600.000000, 0.000000, 0.000000, 800.000000, NULL, NULL, NULL, NULL, '追加订单', 180.000000, 40.000000, 'PART_READY', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), process_instance_id = VALUES(process_instance_id), customer_id = VALUES(customer_id), project_id = VALUES(project_id), business_type = VALUES(business_type), source_project_id = VALUES(source_project_id), settlement_type = VALUES(settlement_type), source_product_id = VALUES(source_product_id), account_id = VALUES(account_id), sale_user_id = VALUES(sale_user_id), order_time = VALUES(order_time), delivery_date = VALUES(delivery_date), total_count = VALUES(total_count), total_price = VALUES(total_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), deposit_price = VALUES(deposit_price), last_reject_reason = VALUES(last_reject_reason), last_reject_time = VALUES(last_reject_time), last_reject_user_id = VALUES(last_reject_user_id), file_url = VALUES(file_url), remark = VALUES(remark), out_count = VALUES(out_count), return_count = VALUES(return_count), delivery_ready_status = VALUES(delivery_ready_status), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_items (id, order_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark, out_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(110101, 110001, 99101, 99001, 53.000000, 100.000000, 5300.000000, 0.130000, 689.000000, '螺栓销售', 100.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110102, 110002, 99102, 99004, 140.000000, 60.000000, 8400.000000, 0.130000, 1092.000000, '控制板销售', 60.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110103, 110003, 99103, 99003, 4.600000, 300.000000, 1380.000000, 0.090000, 124.200000, '垫片销售', 300.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110104, 110004, 99104, 99002, 480.000000, 20.000000, 9600.000000, 0.125000, 1200.000000, '电机销售', 18.000000, 2.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110105, 110005, 99105, 99003, 11.500000, 1000.000000, 11500.000000, 0.087000, 1000.500000, '包装材料销售', 980.000000, 0.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(110106, 110006, 99101, 99001, 30.000000, 220.000000, 6000.000000, 0.100000, 600.000000, '补单销售', 180.000000, 40.000000, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), product_id = VALUES(product_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), out_count = VALUES(out_count), return_count = VALUES(return_count), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_out (id, no, status, customer_id, account_id, sale_user_id, out_time, order_id, order_no, total_count, total_price, receipt_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(111001, 'SOOUT-202601-001', 20, 99401, 99503, @finance_user_id, '2026-01-09 10:00:00', 110001, 'SO-202601-001', 100.000000, 5679.000000, 5000.000000, 5300.000000, 689.000000, 0.000000, 310.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111002, 'SOOUT-202601-002', 20, 99402, 99503, @finance_user_id, '2026-01-18 15:10:00', 110002, 'SO-202601-002', 60.000000, 9492.000000, 12000.000000, 8400.000000, 1092.000000, 0.010000, 0.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111003, 'SOOUT-202602-001', 20, 99403, 99502, @finance_user_id, '2026-02-12 10:20:00', 110003, 'SO-202602-001', 300.000000, 1504.200000, 8000.000000, 1380.000000, 124.200000, 0.000000, 0.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111004, 'SOOUT-202603-001', 20, 99401, 99501, @finance_user_id, '2026-03-12 13:30:00', 110004, 'SO-202603-001', 20.000000, 10800.000000, 0.000000, 9600.000000, 1200.000000, 0.020000, 0.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111005, 'SOOUT-202604-001', 20, 99402, 99503, @finance_user_id, '2026-04-18 16:10:00', 110005, 'SO-202604-001', 1000.000000, 12500.000000, 9000.000000, 11500.000000, 1000.000000, 0.000000, 0.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111006, 'SOOUT-202605-001', 20, 99403, 99502, @finance_user_id, '2026-05-16 14:40:00', 110006, 'SO-202605-001', 220.000000, 6600.000000, 2600.000000, 6000.000000, 600.000000, 0.000000, 0.000000, 0.000000, NULL, '销售出库', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), customer_id = VALUES(customer_id), account_id = VALUES(account_id), sale_user_id = VALUES(sale_user_id), out_time = VALUES(out_time), order_id = VALUES(order_id), order_no = VALUES(order_no), total_count = VALUES(total_count), total_price = VALUES(total_price), receipt_price = VALUES(receipt_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), other_price = VALUES(other_price), file_url = VALUES(file_url), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_out_items (id, out_id, order_item_id, warehouse_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(111101, 111001, 110101, 99202, 99101, 99001, 53.000000, 100.000000, 5300.000000, 0.130000, 689.000000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111102, 111002, 110102, 99202, 99102, 99004, 140.000000, 60.000000, 8400.000000, 0.130000, 1092.000000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111103, 111003, 110103, 99201, 99103, 99003, 4.600000, 300.000000, 1380.000000, 0.090000, 124.200000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111104, 111004, 110104, 99202, 99104, 99002, 480.000000, 20.000000, 9600.000000, 0.125000, 1200.000000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111105, 111005, 110105, 99203, 99105, 99003, 11.500000, 1000.000000, 11500.000000, 0.087000, 1000.500000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(111106, 111006, 110106, 99202, 99101, 99001, 30.000000, 220.000000, 6000.000000, 0.100000, 600.000000, '销售出库明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE out_id = VALUES(out_id), order_item_id = VALUES(order_item_id), warehouse_id = VALUES(warehouse_id), product_id = VALUES(product_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_return (id, no, status, customer_id, account_id, sale_user_id, return_time, order_id, order_no, total_count, total_price, refund_price, total_product_price, total_tax_price, discount_percent, discount_price, other_price, file_url, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(112001, 'SORET-202602-001', 20, 99401, 99501, @finance_user_id, '2026-02-20 11:00:00', 110004, 'SO-202603-001', 2.000000, 1188.000000, 1188.000000, 960.000000, 120.000000, 0.000000, 0.000000, 108.000000, NULL, '销售退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(112002, 'SORET-202605-001', 20, 99403, 99502, @finance_user_id, '2026-05-20 09:40:00', 110006, 'SO-202605-001', 40.000000, 1360.000000, 1360.000000, 1200.000000, 120.000000, 0.000000, 0.000000, 40.000000, NULL, '销售退货', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), customer_id = VALUES(customer_id), account_id = VALUES(account_id), sale_user_id = VALUES(sale_user_id), return_time = VALUES(return_time), order_id = VALUES(order_id), order_no = VALUES(order_no), total_count = VALUES(total_count), total_price = VALUES(total_price), refund_price = VALUES(refund_price), total_product_price = VALUES(total_product_price), total_tax_price = VALUES(total_tax_price), discount_percent = VALUES(discount_percent), discount_price = VALUES(discount_price), other_price = VALUES(other_price), file_url = VALUES(file_url), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_return_items (id, return_id, order_item_id, warehouse_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(112101, 112001, 110104, 99202, 99104, 99002, 480.000000, 2.000000, 960.000000, 0.125000, 120.000000, '退货明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(112102, 112002, 110106, 99203, 99101, 99001, 30.000000, 40.000000, 1200.000000, 0.100000, 120.000000, '退货明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE return_id = VALUES(return_id), order_item_id = VALUES(order_item_id), warehouse_id = VALUES(warehouse_id), product_id = VALUES(product_id), product_unit_id = VALUES(product_unit_id), product_price = VALUES(product_price), count = VALUES(count), total_price = VALUES(total_price), tax_percent = VALUES(tax_percent), tax_price = VALUES(tax_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

UPDATE erp_finance_receipt
SET remark = CONCAT(remark, '；关联销售回款')
WHERE id IN (106001, 106002, 106003);

UPDATE erp_finance_payment
SET remark = CONCAT(remark, '；关联应付结算')
WHERE id IN (104001, 104002, 104003, 104004, 104005);

UPDATE erp_finance_voucher
SET status = 30
WHERE id IN (108001, 108002, 108003, 108004, 108005, 108006, 108007);

INSERT INTO erp_finance_payment (id, no, status, payment_time, finance_user_id, supplier_id, account_id, total_price, discount_price, payment_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104006, 'PAY-202605-002', 20, '2026-05-21 10:00:00', @finance_user_id, 99301, 99502, 600.000000, 0.000000, 600.000000, '追加付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104007, 'PAY-202605-003', 20, '2026-05-22 10:00:00', @finance_user_id, 99302, 99502, 1000.000000, 0.000000, 1000.000000, '追加付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104008, 'PAY-202605-004', 20, '2026-05-23 10:00:00', @finance_user_id, 99303, 99503, 1500.000000, 0.000000, 1500.000000, '追加付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104009, 'PAY-202605-005', 20, '2026-05-24 10:00:00', @finance_user_id, 99304, 99503, 2000.000000, 0.000000, 2000.000000, '追加付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104010, 'PAY-202605-006', 20, '2026-05-25 10:00:00', @finance_user_id, 99305, 99501, 800.000000, 0.000000, 800.000000, '追加付款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), payment_time = VALUES(payment_time), finance_user_id = VALUES(finance_user_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), total_price = VALUES(total_price), discount_price = VALUES(discount_price), payment_price = VALUES(payment_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment (id, no, status, prepayment_time, finance_user_id, supplier_id, account_id, prepayment_price, allocated_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(105006, 'PRE-202605-002', 20, '2026-05-21 09:30:00', @finance_user_id, 99301, 99502, 1200.000000, 200.000000, 1000.000000, '追加预付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105007, 'PRE-202605-003', 20, '2026-05-22 09:30:00', @finance_user_id, 99302, 99502, 2200.000000, 700.000000, 1500.000000, '追加预付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105008, 'PRE-202605-004', 20, '2026-05-23 09:30:00', @finance_user_id, 99303, 99503, 3000.000000, 900.000000, 2100.000000, '追加预付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105009, 'PRE-202605-005', 20, '2026-05-24 09:30:00', @finance_user_id, 99304, 99503, 4000.000000, 1500.000000, 2500.000000, '追加预付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105010, 'PRE-202605-006', 20, '2026-05-25 09:30:00', @finance_user_id, 99305, 99501, 1800.000000, 300.000000, 1500.000000, '追加预付', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), prepayment_time = VALUES(prepayment_time), finance_user_id = VALUES(finance_user_id), supplier_id = VALUES(supplier_id), account_id = VALUES(account_id), prepayment_price = VALUES(prepayment_price), allocated_price = VALUES(allocated_price), remain_price = VALUES(remain_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt (id, no, status, receipt_time, finance_user_id, customer_id, account_id, total_price, discount_price, receipt_price, remark, file_url, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(106004, 'RCPT-202604-001', 20, '2026-04-18 10:00:00', @finance_user_id, 99401, 99502, 6000.000000, 0.000000, 6000.000000, '追加回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106005, 'RCPT-202605-001', 20, '2026-05-19 10:00:00', @finance_user_id, 99402, 99503, 10000.000000, 0.000000, 10000.000000, '追加回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106006, 'RCPT-202605-002', 20, '2026-05-20 14:00:00', @finance_user_id, 99403, 99501, 4000.000000, 0.000000, 4000.000000, '追加回款', NULL, 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), receipt_time = VALUES(receipt_time), finance_user_id = VALUES(finance_user_id), customer_id = VALUES(customer_id), account_id = VALUES(account_id), total_price = VALUES(total_price), discount_price = VALUES(discount_price), receipt_price = VALUES(receipt_price), remark = VALUES(remark), file_url = VALUES(file_url), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_receipt_item (id, receipt_id, biz_type, biz_id, biz_no, total_price, receipted_price, receipt_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(106104, 106004, 21, 201004, 'SO-202604-001', 6000.000000, 0.000000, 6000.000000, '追加回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106105, 106005, 21, 201005, 'SO-202605-001', 10000.000000, 0.000000, 10000.000000, '追加回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(106106, 106006, 21, 201006, 'SO-202605-002', 4000.000000, 0.000000, 4000.000000, '追加回款', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE receipt_id = VALUES(receipt_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), total_price = VALUES(total_price), receipted_price = VALUES(receipted_price), receipt_price = VALUES(receipt_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_expense (id, no, status, expense_time, expense_type, dept_id, project_id, supplier_id, finance_user_id, account_id, expense_price, paid_price, remain_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107006, 'EXP-202605-002', 20, '2026-05-26 14:00:00', 30, 106, NULL, NULL, @finance_user_id, 99502, 760.000000, 760.000000, 0.000000, '追加报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107007, 'EXP-202605-003', 20, '2026-05-27 14:00:00', 10, 106, NULL, NULL, @finance_user_id, 99503, 980.000000, 980.000000, 0.000000, '追加报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107008, 'EXP-202605-004', 20, '2026-05-28 14:00:00', 20, 106, NULL, NULL, @finance_user_id, 99501, 1500.000000, 1500.000000, 0.000000, '追加报销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE status = VALUES(status), expense_time = VALUES(expense_time), expense_type = VALUES(expense_type), dept_id = VALUES(dept_id), project_id = VALUES(project_id), supplier_id = VALUES(supplier_id), finance_user_id = VALUES(finance_user_id), account_id = VALUES(account_id), expense_price = VALUES(expense_price), paid_price = VALUES(paid_price), remain_price = VALUES(remain_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_expense_item (id, expense_id, item_name, amount, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(107106, 107006, '招待费', 760.000000, '追加报销明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107107, 107007, '差旅费', 980.000000, '追加报销明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(107108, 107008, '办公用品', 1500.000000, '追加报销明细', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE expense_id = VALUES(expense_id), item_name = VALUES(item_name), amount = VALUES(amount), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_item (id, payment_id, ap_statement_id, biz_type, biz_id, biz_no, total_price, paid_price, payment_price, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104106, 104006, 103001, 11, 101101, 'PI-20260501-001', 995.000000, 995.000000, 600.000000, '追加核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104107, 104007, 103002, 11, 101102, 'PI-20260501-002', 7458.000000, 2458.000000, 1000.000000, '追加核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104108, 104008, 103003, 11, 101103, 'PI-20260501-003', 6540.000000, 1540.000000, 1500.000000, '追加核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104109, 104009, 103004, 11, 101104, 'PI-20260501-004', 20340.000000, 5000.000000, 2000.000000, '追加核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104110, 104010, 103005, 11, 101105, 'PI-20260501-005', 13392.000000, 1392.000000, 800.000000, '追加核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE payment_id = VALUES(payment_id), ap_statement_id = VALUES(ap_statement_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), total_price = VALUES(total_price), paid_price = VALUES(paid_price), payment_price = VALUES(payment_price), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_payment_allocate (id, payment_id, payment_item_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(104206, 104006, 104106, 103001, 600.000000, 99301, 11, 101101, 'PI-20260501-001', 20, '追加核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104207, 104007, 104107, 103002, 1000.000000, 99302, 11, 101102, 'PI-20260501-002', 20, '追加核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104208, 104008, 104108, 103003, 1500.000000, 99303, 11, 101103, 'PI-20260501-003', 20, '追加核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104209, 104009, 104109, 103004, 2000.000000, 99304, 11, 101104, 'PI-20260501-004', 20, '追加核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(104210, 104010, 104110, 103005, 800.000000, 99305, 11, 101105, 'PI-20260501-005', 20, '追加核销完成', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE payment_id = VALUES(payment_id), payment_item_id = VALUES(payment_item_id), ap_statement_id = VALUES(ap_statement_id), allocate_amount = VALUES(allocate_amount), supplier_id = VALUES(supplier_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

INSERT INTO erp_finance_prepayment_allocate (id, prepayment_id, ap_statement_id, allocate_amount, supplier_id, biz_type, biz_id, biz_no, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
(105106, 105006, 103001, 200.000000, 99301, 11, 101101, 'PI-20260501-001', 20, '追加预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105107, 105007, 103002, 700.000000, 99302, 11, 101102, 'PI-20260501-002', 20, '追加预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105108, 105008, 103003, 900.000000, 99303, 11, 101103, 'PI-20260501-003', 20, '追加预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105109, 105009, 103004, 1500.000000, 99304, 11, 101104, 'PI-20260501-004', 20, '追加预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id),
(105110, 105010, 103005, 300.000000, 99305, 11, 101105, 'PI-20260501-005', 20, '追加预付核销', 'admin', NOW(), 'admin', NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE prepayment_id = VALUES(prepayment_id), ap_statement_id = VALUES(ap_statement_id), allocate_amount = VALUES(allocate_amount), supplier_id = VALUES(supplier_id), biz_type = VALUES(biz_type), biz_id = VALUES(biz_id), biz_no = VALUES(biz_no), status = VALUES(status), remark = VALUES(remark), updater = VALUES(updater), update_time = NOW(), deleted = VALUES(deleted), tenant_id = VALUES(tenant_id);

UPDATE erp_finance_subject_balance
SET current_debit_amount = current_debit_amount + 1000.000000,
    current_credit_amount = current_credit_amount + 1000.000000,
    ending_debit_amount = ending_debit_amount + 1000.000000,
    ending_credit_amount = ending_credit_amount + 1000.000000
WHERE id IN (99901, 99902, 99903, 99904, 99905, 99906, 99907, 99908, 99909, 99910, 99911, 99912, 99913);

SET FOREIGN_KEY_CHECKS = 1;
