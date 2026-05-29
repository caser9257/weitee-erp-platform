/*
 Target: ERP sale-order detail dialog layout demo data
 Schema: ruoyi-vue-pro
 Date: 2026-05-19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := COALESCE((
  SELECT tenant_id
  FROM system_users
  WHERE deleted = b'0'
  ORDER BY id ASC
  LIMIT 1
), 1);

SET @sale_user_id := COALESCE((
  SELECT id
  FROM system_users
  WHERE deleted = b'0' AND username IN ('admin', 'Administrator')
  ORDER BY id ASC
  LIMIT 1
), 1);

SET @sale_creator := CAST(@sale_user_id AS CHAR);

INSERT INTO erp_product_category
  (id, parent_id, name, code, sort, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (99090, 0, '详情弹窗压测分类', 'LAYOUT-DEMO-CATEGORY', 90, 0, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  code = VALUES(code),
  sort = VALUES(sort),
  status = VALUES(status),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_product_unit
  (id, name, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (99091, '件', 0, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99092, '块', 0, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99093, '卷', 0, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99094, '套', 0, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_product
  (id, name, material_code, bar_code, category_id, unit_id, status, standard, remark, expiry_day,
   batch_control_flag, inspection_required_flag, weight, purchase_price, sale_price, min_price,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (99181, '端子线束总成 20cm 灰黑双色耐弯折版', 'LAYOUT-MAT-001', 'BC-LAYOUT-TERM-20CM-GRAY-BLACK-001',
   99090, 99091, 0, '20cm / 灰黑双色 / 镀锡铜芯', '用于详情弹窗长名称与长条码布局压测', 365,
   b'1', b'0', 0.020000, 0.320000, 0.560000, 0.500000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99182, '热缩管 Φ3 黑色阻燃套管', 'LAYOUT-MAT-002', 'BC-LAYOUT-HSRINK-PHI3-BLACK-FR-002',
   99090, 99091, 0, 'Φ3 / 阻燃 / 黑色', '用于详情弹窗中等长度商品摘要压测', 365,
   b'0', b'0', 0.005000, 0.030000, 0.080000, 0.060000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99183, '扎带 100mm 白色自锁款', 'LAYOUT-MAT-003', 'BC-LAYOUT-TIE-100MM-WHITE-003',
   99090, 99091, 0, '100mm / 白色 / 自锁', '用于详情弹窗短名称与大数量压测', 365,
   b'0', b'0', 0.002000, 0.010000, 0.030000, 0.020000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99184, '工业控制主板 V3.2 多接口扩展版（含散热片）', 'LAYOUT-MAT-004', 'BC-LAYOUT-CONTROL-BOARD-V32-MULTI-PORT-004',
   99090, 99092, 0, 'V3.2 / 多接口 / 含散热片', '用于详情弹窗高单价商品布局压测', 365,
   b'1', b'1', 0.180000, 210.000000, 268.000000, 250.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99185, '户外防水波纹护线管 25mm 加厚抗压款', 'LAYOUT-MAT-005', 'BC-LAYOUT-CORRUGATED-TUBE-25MM-HEAVY-005',
   99090, 99091, 0, '25mm / 户外 / 加厚抗压', '用于详情弹窗备注与金额列宽压测', 365,
   b'0', b'0', 0.060000, 12.000000, 18.750000, 16.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99186, '定制包装标签卷（双排二维码 + 批次追溯信息）', 'LAYOUT-MAT-006', 'BC-LAYOUT-LABEL-ROLL-DOUBLE-QR-BATCH-TRACE-006',
   99090, 99093, 0, '双排二维码 / 批次追溯', '用于详情弹窗长备注与卷类单位压测', 365,
   b'1', b'0', 0.030000, 7.500000, 12.160000, 10.500000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99187, '伺服驱动器 CN3 编码器延长线 3m 屏蔽款', 'LAYOUT-MAT-007', 'BC-LAYOUT-SERVO-CN3-EXT-3M-007',
   99090, 99091, 0, '3m / 屏蔽双绞 / 黑色拖链级', '更贴近日常工控配线销售台账', 365,
   b'0', b'0', 0.120000, 58.000000, 86.500000, 79.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99188, '24V 开关电源 120W 导轨安装型', 'LAYOUT-MAT-008', 'BC-LAYOUT-PSU-24V120W-DIN-008',
   99090, 99092, 0, '24V / 5A / 导轨安装', '用于模拟控制柜常规电气件销售', 365,
   b'1', b'1', 0.280000, 72.000000, 98.000000, 92.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99189, 'RJ45 工业以太网跳线 Cat6A 蓝色 2m', 'LAYOUT-MAT-009', 'BC-LAYOUT-RJ45-CAT6A-2M-009',
   99090, 99091, 0, 'Cat6A / 2m / 蓝色', '用于模拟网络辅料类销售明细', 365,
   b'0', b'0', 0.050000, 9.600000, 16.800000, 15.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99190, '铝合金控制箱门锁组件 MS813-1', 'LAYOUT-MAT-010', 'BC-LAYOUT-LOCK-MS8131-010',
   99090, 99094, 0, 'MS813-1 / 铝合金 / 含钥匙', '用于模拟标准配套件销售', 365,
   b'0', b'0', 0.180000, 15.000000, 23.500000, 21.500000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99191, '冷压端头套装 E0508-E2512 混装盒', 'LAYOUT-MAT-011', 'BC-LAYOUT-TERMINAL-KIT-MIX-011',
   99090, 99094, 0, 'E0508~E2512 / 混装盒', '用于模拟耗材打包销售', 365,
   b'0', b'0', 0.220000, 31.000000, 45.000000, 42.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99192, '设备铭牌贴纸 PET 哑银耐酒精 100x60mm', 'LAYOUT-MAT-012', 'BC-LAYOUT-NAMEPLATE-PET-100X60-012',
   99090, 99093, 0, '100x60mm / 哑银 / 耐酒精', '用于模拟短名称与短备注混排', 365,
   b'1', b'0', 0.010000, 0.860000, 1.280000, 1.100000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  material_code = VALUES(material_code),
  bar_code = VALUES(bar_code),
  category_id = VALUES(category_id),
  unit_id = VALUES(unit_id),
  status = VALUES(status),
  standard = VALUES(standard),
  remark = VALUES(remark),
  expiry_day = VALUES(expiry_day),
  batch_control_flag = VALUES(batch_control_flag),
  inspection_required_flag = VALUES(inspection_required_flag),
  weight = VALUES(weight),
  purchase_price = VALUES(purchase_price),
  sale_price = VALUES(sale_price),
  min_price = VALUES(min_price),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_customer
  (id, name, contact, mobile, telephone, email, fax, remark, status, sort, tax_no, tax_percent,
   bank_name, bank_account, bank_address, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (99481, '销售详情弹窗压测客户（华东项目集成中心）', '布局验证联系人', '13900019981', '021-77009981',
   'layout-customer@test.local', NULL, '用于销售订单详情弹窗布局压测的测试客户', 0, 81, '91310000LAYOUT9981',
   0.130000, '中国银行上海张江支行', '6222020000009981', '上海市浦东新区张江高科技园区测试路 81 号',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99482, '苏州智控成套设备有限公司', '周工', '13800029982', '0512-66009982',
   'zhou.gong@test.local', NULL, '模拟更贴近真实销售台账的工控客户', 0, 82, '91320500LAYOUT9982',
   0.130000, '招商银行苏州工业园区支行', '6225880000009982', '江苏省苏州市工业园区金鸡湖大道 288 号',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  contact = VALUES(contact),
  mobile = VALUES(mobile),
  telephone = VALUES(telephone),
  email = VALUES(email),
  fax = VALUES(fax),
  remark = VALUES(remark),
  status = VALUES(status),
  sort = VALUES(sort),
  tax_no = VALUES(tax_no),
  tax_percent = VALUES(tax_percent),
  bank_name = VALUES(bank_name),
  bank_account = VALUES(bank_account),
  bank_address = VALUES(bank_address),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_account
  (id, name, no, remark, status, sort, default_status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (99581, '销售详情弹窗压测结算户', 'ACC-LAYOUT-001', '用于销售订单详情弹窗布局压测', 0, 81, b'0',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (99582, '苏州工控项目回款户', 'ACC-LAYOUT-002', '用于模拟日常销售台账回款账户', 0, 82, b'0',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  no = VALUES(no),
  remark = VALUES(remark),
  status = VALUES(status),
  sort = VALUES(sort),
  default_status = VALUES(default_status),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order
  (id, no, status, process_instance_id, customer_id, project_id, business_type, source_project_id, settlement_type,
   source_product_id, account_id, sale_user_id, order_time, delivery_date, total_count, total_price,
   total_product_price, total_tax_price, discount_percent, discount_price, deposit_price, last_reject_reason,
   last_reject_time, last_reject_user_id, file_url, remark, out_count, return_count, delivery_ready_status,
   creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (110901, 'SO-LAYOUT-202605-001', 20, 'PI-SO-LAYOUT-202605-001', 99481, NULL, 'SELF_RESEARCH', NULL, 'PRODUCT_SALE',
   99181, 99581, @sale_user_id, '2026-05-19 10:30:00', '2026-05-28',
   3678.000000, 7219.580000, 6486.300000, 843.220000, 1.500000, 109.940000, 600.000000,
   '首次审批时要求把标签卷备注补充到批次追溯级别，并明确线束总成的护套颜色与二维码打印规则。',
   '2026-05-18 16:20:00', @sale_user_id,
   'https://static.example.com/demo/sale-order/layout-check/SO-LAYOUT-202605-001/%E9%94%80%E5%94%AE%E8%AE%A2%E5%8D%95%E9%99%84%E4%BB%B6-%E8%AF%A6%E6%83%85%E5%BC%B9%E7%AA%97%E5%B8%83%E5%B1%80%E5%8E%8B%E6%B5%8B.pdf',
   '用于销售订单详情弹窗布局压测：包含长商品名称、长条码、长附件链接、长驳回原因、长备注、多商品行、不同单位与金额量级，重点检查商品摘要列换行、备注列宽度、价税列对齐以及审批轨迹滚动表现。',
   1480.000000, 126.000000, 'PART_READY',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110902, 'SO-LAYOUT-202605-002', 20, 'PI-SO-LAYOUT-202605-002', 99482, NULL, 'CUSTOMER_SUPPLIED', NULL, 'PRODUCT_SALE',
   99188, 99582, @sale_user_id, '2026-05-20 14:18:00', '2026-05-30',
   424.000000, 4388.940000, 3954.000000, 514.020000, 2.000000, 79.080000, 1200.000000,
   '客户首次提交缺少柜体位号对应表，要求补齐后再走审批。', '2026-05-20 15:10:00', @sale_user_id, '',
   '模拟更贴近日常销售台账的一张常规工控订单：商品名称中长适中、备注长短混排、附件为空、部分明细无备注，重点观察正常业务单在详情弹窗中的密度与可读性。',
   128.000000, 6.000000, 'READY_TO_SHIP',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110903, 'SO-LAYOUT-202605-003', 20, 'PI-SO-LAYOUT-202605-003', 99482, NULL, 'CUSTOMER_SUPPLIED', NULL, 'PRODUCT_SALE',
   99189, 99582, @sale_user_id, '2026-04-12 09:26:00', '2026-04-20',
   91.000000, 1044.35, 949.500000, 123.440000, 3.000000, 28.590000, 300.000000,
   '历史订单补录时仅保留了简短说明，部分商品信息依赖主数据回填。', '2026-04-12 11:05:00', @sale_user_id, NULL,
   '模拟历史补录单：附件为空、备注较短、明细中混有空备注与简写备注，用来验证详情弹窗在“信息不完整但不是脏到不可用”的旧单场景下是否仍然可读。',
   36.000000, 1.000000, 'PART_READY',
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  no = VALUES(no),
  status = VALUES(status),
  process_instance_id = VALUES(process_instance_id),
  customer_id = VALUES(customer_id),
  project_id = VALUES(project_id),
  business_type = VALUES(business_type),
  source_project_id = VALUES(source_project_id),
  settlement_type = VALUES(settlement_type),
  source_product_id = VALUES(source_product_id),
  account_id = VALUES(account_id),
  sale_user_id = VALUES(sale_user_id),
  order_time = VALUES(order_time),
  delivery_date = VALUES(delivery_date),
  total_count = VALUES(total_count),
  total_price = VALUES(total_price),
  total_product_price = VALUES(total_product_price),
  total_tax_price = VALUES(total_tax_price),
  discount_percent = VALUES(discount_percent),
  discount_price = VALUES(discount_price),
  deposit_price = VALUES(deposit_price),
  last_reject_reason = VALUES(last_reject_reason),
  last_reject_time = VALUES(last_reject_time),
  last_reject_user_id = VALUES(last_reject_user_id),
  file_url = VALUES(file_url),
  remark = VALUES(remark),
  out_count = VALUES(out_count),
  return_count = VALUES(return_count),
  delivery_ready_status = VALUES(delivery_ready_status),
  creator = VALUES(creator),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_items
  (id, order_id, product_id, product_unit_id, product_price, count, total_price, tax_percent, tax_price, remark,
   out_count, return_count, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (110911, 110901, 99181, 99091, 0.560000, 1000.000000, 560.000000, 13.000000, 72.800000,
   '线束两端均需加贴批次追溯码，灰黑双色比例按 1:1 备料，备注故意写长一点用于测试弹窗备注列能否稳定承载。',
   420.000000, 36.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110912, 110901, 99182, 99091, 0.080000, 1000.000000, 80.000000, 13.000000, 10.400000,
   '阻燃等级需在随货标签中单独标注，备注长度用于测试中等文本密度。', 300.000000, 12.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110913, 110901, 99183, 99091, 0.030000, 1500.000000, 45.000000, 13.000000, 5.850000,
   '扎带用于箱内辅料固定，测试大数量与短名称并存的排版。', 560.000000, 18.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110914, 110901, 99184, 99092, 268.000000, 12.000000, 3216.000000, 13.000000, 418.080000,
   '控制主板需要在出货前写入 V3.2 固件，并核对扩展接口贴标顺序；这条备注用于测试高单价行的换行与金额列对齐。',
   8.000000, 2.000000, @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110915, 110901, 99185, 99091, 18.750000, 86.000000, 1612.500000, 13.000000, 209.630000,
   '护线管外箱需增加防水向上标识，备注中故意加入较长业务说明以观察详情弹窗是否出现遮挡。', 72.000000, 28.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110916, 110901, 99186, 99093, 12.160000, 80.000000, 972.800000, 13.000000, 126.460000,
   '标签卷包含双排二维码、批次号、客户项目简称与箱序号，设计为长备注测试滚动与列宽行为。', 120.000000, 30.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110921, 110902, 99187, 99091, 86.500000, 6.000000, 519.000000, 13.000000, 67.470000,
   '用于两台伺服驱动器配套延长，线号需与客户柜体图纸一致。', 2.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110922, 110902, 99188, 99092, 98.000000, 12.000000, 1176.000000, 13.000000, 152.880000,
   '导轨电源分三批发货，首批先出 6 块。', 6.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110923, 110902, 99189, 99091, 16.800000, 24.000000, 403.200000, 13.000000, 52.420000,
   '', 8.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110924, 110902, 99190, 99094, 23.500000, 18.000000, 423.000000, 13.000000, 54.990000,
   '门锁组件按左开门和右开门各半配货。', 6.000000, 2.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110925, 110902, 99191, 99094, 45.000000, 10.000000, 450.000000, 13.000000, 58.500000,
   '车间预装耗材，客户要求独立外箱并附装箱清单。', 4.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110926, 110902, 99186, 99093, 12.160000, 30.000000, 364.800000, 13.000000, 47.420000,
   '铭牌、二维码和箱号标签合并下单，备注长度保持日常水平。', 18.000000, 1.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110927, 110902, 99181, 99091, 0.560000, 300.000000, 168.000000, 13.000000, 21.840000,
   '线束按 50 件/包分装。', 72.000000, 3.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110928, 110902, 99185, 99091, 18.750000, 24.000000, 450.000000, 13.000000, 58.500000,
   NULL, 12.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110931, 110903, 99189, 99091, 16.800000, 15.000000, 252.000000, 13.000000, 32.760000,
   '车间联网调试用', 6.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110932, 110903, 99192, 99093, 1.280000, 40.000000, 51.200000, 13.000000, 6.660000,
   '', 12.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110933, 110903, 99190, 99094, 23.500000, 8.000000, 188.000000, 13.000000, 24.440000,
   NULL, 4.000000, 1.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110934, 110903, 99187, 99091, 86.500000, 3.000000, 259.500000, 13.000000, 33.740000,
   '老单补录，仅保留主用途', 2.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id),
  (110935, 110903, 99188, 99092, 98.000000, 2.000000, 196.000000, 13.000000, 25.840000,
   '补 2 台备用电源', 1.000000, 0.000000,
   @sale_creator, NOW(), @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  product_id = VALUES(product_id),
  product_unit_id = VALUES(product_unit_id),
  product_price = VALUES(product_price),
  count = VALUES(count),
  total_price = VALUES(total_price),
  tax_percent = VALUES(tax_percent),
  tax_price = VALUES(tax_price),
  remark = VALUES(remark),
  out_count = VALUES(out_count),
  return_count = VALUES(return_count),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_audit_log
  (id, order_id, action_type, before_status, after_status, reason, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (110941, 110901, 'REJECT', 10, 30,
   '首次审批退回：请补充标签卷的二维码编码规则，并确认线束护套颜色描述与附件文件名一致。',
   @sale_creator, '2026-05-18 16:20:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110942, 110901, 'RESUBMIT', 30, 10,
   '补充附件与备注后重新提交审批。',
   @sale_creator, '2026-05-19 09:10:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110943, 110901, 'APPROVE', 10, 20,
   '审批通过，允许按部分就绪状态推进发货准备。',
   @sale_creator, '2026-05-19 09:45:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110944, 110902, 'REJECT', 10, 30,
   '客户首次提交缺少柜体位号对应表，要求补齐后再走审批。',
   @sale_creator, '2026-05-20 15:10:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110945, 110902, 'RESUBMIT', 30, 10,
   '客户补齐柜体位号表后重新提交流程。',
   @sale_creator, '2026-05-21 09:12:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110946, 110902, 'APPROVE', 10, 20,
   '审批通过，允许直接按客户周交付计划分批出货。',
   @sale_creator, '2026-05-21 10:05:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110947, 110903, 'REJECT', 10, 30,
   '历史补录单要求把用途说明补全后再审批。',
   @sale_creator, '2026-04-12 11:05:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110948, 110903, 'RESUBMIT', 30, 10,
   '补充用途说明并重新提交。',
   @sale_creator, '2026-04-13 09:20:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110949, 110903, 'APPROVE', 10, 20,
   '审批通过，按历史补录单据归档处理。',
   @sale_creator, '2026-05-21 10:05:00', @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  action_type = VALUES(action_type),
  before_status = VALUES(before_status),
  after_status = VALUES(after_status),
  reason = VALUES(reason),
  creator = VALUES(creator),
  create_time = VALUES(create_time),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

INSERT INTO erp_sale_order_reject_log
  (id, order_id, reason, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
  (110951, 110901,
   '首次审批时要求把标签卷备注补充到批次追溯级别，并明确线束总成的护套颜色与二维码打印规则。',
   @sale_creator, '2026-05-18 16:20:00', @sale_creator, NOW(), b'0', @tenant_id),
  (110952, 110902,
   '客户首次提交缺少柜体位号对应表，要求补齐后再走审批。',
   @sale_creator, '2026-05-20 15:10:00', @sale_creator, NOW(), b'0', @tenant_id)
  ,(110953, 110903,
   '历史补录单要求把用途说明补全后再审批。',
   @sale_creator, '2026-04-12 11:05:00', @sale_creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  order_id = VALUES(order_id),
  reason = VALUES(reason),
  creator = VALUES(creator),
  create_time = VALUES(create_time),
  updater = VALUES(updater),
  update_time = NOW(),
  deleted = VALUES(deleted),
  tenant_id = VALUES(tenant_id);

SET FOREIGN_KEY_CHECKS = 1;
