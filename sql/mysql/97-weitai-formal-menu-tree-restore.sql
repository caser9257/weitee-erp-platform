/*
 Target: Restore Weitai formal menu tree with real page routing where available
 Schema: ruoyi-vue-pro
 Date: 2026-05-07
 Scope:
   1. Keep the 12 formal top-level menus as the visible navigation contract
   2. Restore second-level and third-level menus under the formal roots
   3. Prefer real frontend page components over placeholder components
   4. Hide legacy top-level roots such as /erp, /iot, /mrp, /manufacturing
*/

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;
SET @placeholder_component = 'common/menu-placeholder/index';

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_roots`;
CREATE TEMPORARY TABLE `tmp_weitai_restore_roots` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `path` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_restore_roots`
(`fixed_id`,`actual_id`,`path`,`name`,`sort_no`,`icon`,`component_name`)
VALUES
  (930100,NULL,'/project','项目中心',310,'ep:files','FormalProjectRoot'),
  (930105,NULL,'/master-data','产品与物料中心',320,'ep:box','FormalMasterDataRoot'),
  (930110,NULL,'/sales','销售管理',330,'ep:sell','FormalSalesRoot'),
  (930130,NULL,'/pmo','综合计划管理',340,'ep:data-board','FormalPmoRoot'),
  (930120,NULL,'/rd','研发管理',350,'ep:cpu','FormalRdRoot'),
  (930140,NULL,'/scm','供应链管理',360,'ep:shopping-cart-full','FormalScmRoot'),
  (930160,NULL,'/process','工艺管理',370,'ep:connection','FormalProcessRoot'),
  (930170,NULL,'/mes','制造执行管理',380,'ep:operation','FormalMesRoot'),
  (930150,NULL,'/qms','质量管理',390,'ep:medal','FormalQmsRoot'),
  (930180,NULL,'/finance','财务管理',400,'ep:money','FormalFinanceRoot'),
  (930190,NULL,'/hr','人事管理',410,'ep:user','FormalHrRoot'),
  (930210,NULL,'/system','系统管理',420,'ep:tools','FormalSystemRoot');

UPDATE `tmp_weitai_restore_roots` t
LEFT JOIN (
  SELECT `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = 0
  GROUP BY `path`
) existing_root
  ON existing_root.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET t.`actual_id` = existing_root.`id`;

UPDATE `system_menu` sm
JOIN `tmp_weitai_restore_roots` t ON t.`actual_id` = sm.`id`
SET sm.`name` = t.`name`,
    sm.`type` = 1,
    sm.`sort` = t.`sort_no`,
    sm.`parent_id` = 0,
    sm.`path` = t.`path`,
    sm.`icon` = t.`icon`,
    sm.`component` = '',
    sm.`component_name` = t.`component_name`,
    sm.`status` = 0,
    sm.`visible` = b'1',
    sm.`keep_alive` = b'1',
    sm.`always_show` = b'1',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0';

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT t.`fixed_id`, t.`name`, '', 1, t.`sort_no`, 0, t.`path`, t.`icon`, '', t.`component_name`,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_restore_roots` t
WHERE t.`actual_id` IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu` sm
    WHERE sm.`deleted` = b'0'
      AND sm.`parent_id` = 0
      AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_restore_roots` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = 0
    AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_groups`;
CREATE TEMPORARY TABLE `tmp_weitai_restore_groups` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `root_path` VARCHAR(255) NOT NULL,
  `actual_parent_id` BIGINT NULL,
  `path` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_restore_groups`
(`fixed_id`,`actual_id`,`root_path`,`actual_parent_id`,`path`,`name`,`sort_no`,`icon`,`component_name`)
VALUES
  (931410,NULL,'/pmo',NULL,'project','项目管理',10,'ep:calendar','FormalPmoProjectGroup'),
  (931411,NULL,'/pmo',NULL,'organization','组织与岗位',20,'fa:address-card','FormalPmoOrganizationGroup'),
  (931412,NULL,'/pmo',NULL,'process','流程协同',30,'ep:connection','FormalPmoProcessGroup'),
  (931413,NULL,'/pmo',NULL,'analysis','经营分析',40,'ep:trend-charts','FormalPmoAnalysisGroup');

UPDATE `tmp_weitai_restore_groups` g
JOIN `tmp_weitai_restore_roots` r
  ON r.`path` COLLATE utf8mb4_unicode_ci = g.`root_path`
SET g.`actual_parent_id` = r.`actual_id`;

UPDATE `tmp_weitai_restore_groups` g
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) existing_group
  ON existing_group.`parent_id` = g.`actual_parent_id`
 AND existing_group.`path` COLLATE utf8mb4_unicode_ci = g.`path`
SET g.`actual_id` = existing_group.`id`;

UPDATE `system_menu` sm
JOIN `tmp_weitai_restore_groups` g ON g.`actual_id` = sm.`id`
SET sm.`name` = g.`name`,
    sm.`type` = 1,
    sm.`sort` = g.`sort_no`,
    sm.`parent_id` = g.`actual_parent_id`,
    sm.`path` = g.`path`,
    sm.`icon` = g.`icon`,
    sm.`component` = '',
    sm.`component_name` = g.`component_name`,
    sm.`status` = 0,
    sm.`visible` = b'1',
    sm.`keep_alive` = b'1',
    sm.`always_show` = b'1',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0';

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT g.`fixed_id`, g.`name`, '', 1, g.`sort_no`, g.`actual_parent_id`, g.`path`, g.`icon`, '', g.`component_name`,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_restore_groups` g
WHERE g.`actual_id` IS NULL
  AND g.`actual_parent_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu` sm
    WHERE sm.`deleted` = b'0'
      AND sm.`parent_id` = g.`actual_parent_id`
      AND sm.`path` COLLATE utf8mb4_unicode_ci = g.`path`
  );

UPDATE `tmp_weitai_restore_groups` g
SET g.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = g.`actual_parent_id`
    AND sm.`path` COLLATE utf8mb4_unicode_ci = g.`path`
  ORDER BY CASE WHEN sm.`id` = g.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_targets`;
CREATE TEMPORARY TABLE `tmp_weitai_restore_targets` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `root_path` VARCHAR(255) NOT NULL,
  `group_path` VARCHAR(255) NULL,
  `actual_parent_id` BIGINT NULL,
  `path` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component` VARCHAR(255) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  `lookup_component` VARCHAR(255) NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_restore_targets`
(`fixed_id`,`actual_id`,`root_path`,`group_path`,`actual_parent_id`,`path`,`name`,`sort_no`,`icon`,`component`,`component_name`,`lookup_component`)
VALUES
  (931101,NULL,'/project',NULL,NULL,'project','项目台账',10,'ep:document','erp/project/project/index','FormalProjectCenter','erp/project/project/index'),
  (931102,NULL,'/project',NULL,NULL,'lifecycle','项目生命周期',20,'ep:office-building',@placeholder_component,'FormalProjectLifecycle',NULL),
  (931103,NULL,'/project',NULL,NULL,'progress','项目进度视图',30,'ep:histogram',@placeholder_component,'FormalProjectProgress',NULL),
  (931104,NULL,'/project',NULL,NULL,'archive','项目资料归档',40,'ep:folder-opened',@placeholder_component,'FormalProjectArchive',NULL),
  (931105,NULL,'/project',NULL,NULL,'activity','项目动态记录',50,'ep:chat-dot-round',@placeholder_component,'FormalProjectActivity',NULL),
  (931106,NULL,'/project',NULL,NULL,'cost','项目成本视图',60,'ep:money',@placeholder_component,'FormalProjectCostView',NULL),

  (931201,NULL,'/master-data',NULL,NULL,'product','产品主档',10,'fa-solid:apple-alt','erp/product/product/index','FormalMasterProduct','erp/product/product/index'),
  (931202,NULL,'/master-data',NULL,NULL,'material','物料主档',20,'ep:box',@placeholder_component,'FormalMasterMaterial',NULL),
  (931203,NULL,'/master-data',NULL,NULL,'product-category','产品分类',30,'fa:certificate','erp/product/category/index','FormalMasterProductCategory','erp/product/category/index'),
  (931204,NULL,'/master-data',NULL,NULL,'material-category','物料分类',40,'ep:collection',@placeholder_component,'FormalMasterMaterialCategory',NULL),
  (931205,NULL,'/master-data',NULL,NULL,'unit','单位与换算',50,'ep:opportunity','erp/product/unit/index','FormalMasterUnit','erp/product/unit/index'),
  (931206,NULL,'/master-data',NULL,NULL,'coding-rule','编码规则',60,'ep:key',@placeholder_component,'FormalMasterCodingRule',NULL),

  (931301,NULL,'/sales',NULL,NULL,'contract','客户与合同评审',10,'ep:document-checked','crm/contract/index','FormalSalesContract','crm/contract/index'),
  (931302,NULL,'/sales',NULL,NULL,'project-initiation','销售项目立项',20,'ep:flag',@placeholder_component,'FormalSalesProjectInitiation',NULL),
  (931303,NULL,'/sales',NULL,NULL,'order','销售订单台账',30,'ep:list','erp/sale/order/index','FormalSalesOrder','erp/sale/order/index'),
  (931307,NULL,'/sales',NULL,NULL,'closure-workbench','销售闭环工作台',35,'ep:data-analysis','erp/sale/order/closure-workbench','FormalSalesClosureWorkbench','erp/sale/order/closure-workbench'),
  (931304,NULL,'/sales',NULL,NULL,'delivery','发货通知与签收',40,'ep:van','erp/sale/out/index','FormalSalesDelivery','erp/sale/out/index'),
  (931305,NULL,'/sales',NULL,NULL,'customer-supplied-material','客供料业务跟踪',50,'ep:box',@placeholder_component,'FormalSalesCustomerSuppliedMaterial',NULL),
  (931306,NULL,'/sales',NULL,NULL,'incoming-processing','来料加工业务跟踪',60,'ep:setting',@placeholder_component,'FormalSalesIncomingProcessing',NULL),

  (931401,NULL,'/pmo','project',NULL,'master-plan','项目主计划',10,'ep:calendar','pmo/project/index','FormalPmoMasterPlan','pmo/project/index'),
  (931402,NULL,'/pmo','project',NULL,'follow-up','项目跟进',20,'ep:checked','pmo/project/follow-up/index','FormalPmoFollowUp','pmo/project/follow-up/index'),
  (931403,NULL,'/pmo','project',NULL,'warning','项目预警',30,'ep:warning','pmo/project/warning/index','FormalPmoWarning','pmo/project/warning/index'),
  (931404,NULL,'/pmo','analysis',NULL,'kpi','经营指标看板',40,'ep:trend-charts','pmo/kpi/index','FormalPmoKpi','pmo/kpi/index'),
  (931405,NULL,'/pmo','organization',NULL,'org-chart','组织架构',50,'ep:share','pmo/org-chart/index','FormalPmoOrgChart','pmo/org-chart/index'),
  (931406,NULL,'/pmo','organization',NULL,'post-level-overview','岗位层级',60,'ep:histogram','system/post-level/overview/index','FormalPmoPostLevelOverview','system/post-level/overview/index'),
  (931407,NULL,'/pmo','process',NULL,'process-monitor','流程监控',70,'ep:monitor','bpm/processInstance/report/index','FormalPmoProcessMonitor','bpm/processInstance/report/index'),

  (931501,NULL,'/rd',NULL,NULL,'requirement-review','需求与立项评审',10,'ep:document',@placeholder_component,'FormalRdRequirementReview',NULL),
  (931502,NULL,'/rd',NULL,NULL,'design','设计中心',20,'ep:edit-pen','erp/rd/design/index','FormalRdDesign','erp/rd/design/index'),
  (931503,NULL,'/rd',NULL,NULL,'rd-bom','研发 BOM',30,'ep:collection-tag','erp/rd/rd-bom/index','FormalRdBom','erp/rd/rd-bom/index'),
  (931504,NULL,'/rd',NULL,NULL,'bom','标准 BOM',40,'ep:collection','erp/rd/bom/index','FormalStandardBom','erp/rd/bom/index'),
  (931505,NULL,'/rd',NULL,NULL,'design-change','设计变更',50,'ep:refresh',@placeholder_component,'FormalRdDesignChange',NULL),
  (931506,NULL,'/rd',NULL,NULL,'document','研发文档',60,'ep:folder-opened','erp/rd/document/index','FormalRdDocument','erp/rd/document/index'),
  (931507,NULL,'/rd',NULL,NULL,'follow-up','研发跟进',70,'ep:checked',@placeholder_component,'FormalRdFollowUp',NULL),
  (931508,NULL,'/rd',NULL,NULL,'warning','研发预警',80,'ep:warning',@placeholder_component,'FormalRdWarning',NULL),

  (931601,NULL,'/scm',NULL,NULL,'plan-rule','计划参数',10,'ep:setting','erp/mrp/plan-rule/index','FormalScmPlanRule','erp/mrp/plan-rule/index'),
  (931602,NULL,'/scm',NULL,NULL,'plan','MRP 计划',20,'ep:calendar','erp/mrp/plan/index','FormalScmPlan','erp/mrp/plan/index'),
  (931603,NULL,'/scm',NULL,NULL,'suggest','MRP 运算',30,'ep:histogram','erp/mrp/suggest/index','FormalScmSuggest','erp/mrp/suggest/index'),
  (931604,NULL,'/scm',NULL,NULL,'purchase-request','采购需求',40,'ep:tickets','erp/mrp/suggest/index','FormalScmPurchaseRequest',NULL),
  (931605,NULL,'/scm',NULL,NULL,'purchase-order','采购订单台账',50,'ep:shopping-trolley','erp/purchase/order/index','FormalScmPurchaseOrder','erp/purchase/order/index'),
  (931606,NULL,'/scm',NULL,NULL,'inbound','收货入库',60,'ep:box','erp/purchase/in/index','FormalScmInbound','erp/purchase/in/index'),
  (931607,NULL,'/scm',NULL,NULL,'stock-occupancy','库存占用追溯',70,'ep:goods','erp/mrp/stock-reservation/index','FormalScmStockOccupancy','erp/mrp/stock-reservation/index'),
  (931608,NULL,'/scm',NULL,NULL,'netting-policy','净需求策略',80,'ep:operation','erp/mrp/netting-policy/index','FormalScmNettingPolicy','erp/mrp/netting-policy/index'),
  (931609,NULL,'/scm',NULL,NULL,'substitute-material','替代料管理',90,'ep:switch','erp/mrp/substitute/index','FormalScmSubstituteMaterial','erp/mrp/substitute/index'),
  (931610,NULL,'/scm',NULL,NULL,'manufacture-bom','制造 BOM',100,'ep:collection','erp/mrp/bom/index','FormalScmManufactureBom','erp/mrp/bom/index'),
  (931611,NULL,'/scm',NULL,NULL,'warehouse','仓库信息',110,'ep:house','erp/stock/warehouse/index','FormalScmWarehouse','erp/stock/warehouse/index'),
  (931612,NULL,'/scm',NULL,NULL,'stock','产品库存',120,'ep:coffee','erp/stock/stock/index','FormalScmStock','erp/stock/stock/index'),
  (931613,NULL,'/scm',NULL,NULL,'stock-record','库存明细',130,'fa-solid:blog','erp/stock/record/index','FormalScmStockRecord','erp/stock/record/index'),
  (931614,NULL,'/scm',NULL,NULL,'stock-in','其它入库',140,'ep:zoom-in','erp/stock/in/index','FormalScmStockIn','erp/stock/in/index'),
  (931615,NULL,'/scm',NULL,NULL,'stock-out','其它出库',150,'ep:zoom-out','erp/stock/out/index','FormalScmStockOut','erp/stock/out/index'),
  (931616,NULL,'/scm',NULL,NULL,'stock-move','库存调拨',160,'ep:folder-remove','erp/stock/move/index','FormalScmStockMove','erp/stock/move/index'),
  (931617,NULL,'/scm',NULL,NULL,'stock-check','库存盘点',170,'ep:circle-check-filled','erp/stock/check/index','FormalScmStockCheck','erp/stock/check/index'),
  (931618,NULL,'/scm',NULL,NULL,'assemble','组装与拆卸',180,'ep:set-up','erp/stock/assemble/index','FormalScmAssemble','erp/stock/assemble/index'),

  (931701,NULL,'/process',NULL,NULL,'bom','制造 BOM',10,'ep:collection','erp/mrp/bom/index','FormalProcessManufactureBom','erp/mrp/bom/index'),
  (931702,NULL,'/process',NULL,NULL,'process-spec','工艺规范',20,'ep:document-copy',@placeholder_component,'FormalProcessSpec',NULL),
  (931703,NULL,'/process',NULL,NULL,'work-instruction','作业指导书',30,'ep:reading',@placeholder_component,'FormalProcessInstruction',NULL),
  (931704,NULL,'/process',NULL,NULL,'route','工艺路线',40,'ep:share','erp/manufacturing/process-route/index','FormalProcessRoute','erp/manufacturing/process-route/index'),
  (931705,NULL,'/process',NULL,NULL,'flow-card','工艺流转卡',50,'ep:postcard',@placeholder_component,'FormalProcessFlowCard',NULL),
  (931706,NULL,'/process',NULL,NULL,'review','工艺评审',60,'ep:checked',@placeholder_component,'FormalProcessReview',NULL),
  (931707,NULL,'/process',NULL,NULL,'validation','工艺验证项目',70,'ep:select',@placeholder_component,'FormalProcessValidation',NULL),
  (931708,NULL,'/process',NULL,NULL,'work-center','工作中心',80,'ep:office-building','erp/manufacturing/work-center/index','FormalProcessWorkCenter','erp/manufacturing/work-center/index'),

  (931801,NULL,'/mes',NULL,NULL,'plan-execution','生产计划执行',10,'ep:calendar',@placeholder_component,'FormalMesPlanExecution',NULL),
  (931802,NULL,'/mes',NULL,NULL,'work-order','工单与分单',20,'ep:tickets','mes/work-order/index','FormalMesWorkOrder','mes/work-order/index'),
  (931803,NULL,'/mes',NULL,NULL,'pda-receive','PDA 扫码领退料',30,'ep:cellphone','mes/pda-execute/receive','FormalMesPdaReceive','mes/pda-execute/receive'),
  (931804,NULL,'/mes',NULL,NULL,'pda-report','PDA 扫码报工',40,'ep:iphone','mes/pda-execute/report','FormalMesPdaReport','mes/pda-execute/report'),
  (931805,NULL,'/mes',NULL,NULL,'equipment','设备台账',50,'ep:cpu','mes/equipment/index','FormalMesEquipment','mes/equipment/index'),
  (931806,NULL,'/mes',NULL,NULL,'outsource-process','委外工序管理',60,'ep:connection',@placeholder_component,'FormalMesOutsourceProcess',NULL),
  (931807,NULL,'/mes',NULL,NULL,'abnormal-rework','生产异常与返修',70,'ep:warning-filled',@placeholder_component,'FormalMesAbnormalRework',NULL),

  (931901,NULL,'/qms',NULL,NULL,'iqc','来料检验',10,'ep:finished','qms/iqc/IqcEntry','FormalQmsIqc','qms/iqc/IqcEntry'),
  (931902,NULL,'/qms',NULL,NULL,'ipqc','过程检验',20,'ep:aim','qms/ipqc/index','FormalQmsIpqc','qms/ipqc/index'),
  (931903,NULL,'/qms',NULL,NULL,'oqc','完工与出货检验',30,'ep:circle-check','qms/oqc/index','FormalQmsOqc','qms/oqc/index'),
  (931904,NULL,'/qms',NULL,NULL,'report-approval','检验报告审批',40,'ep:document-checked',@placeholder_component,'FormalQmsReportApproval',NULL),
  (931905,NULL,'/qms',NULL,NULL,'quality-issue','质量异常',50,'ep:warn-triangle-filled',@placeholder_component,'FormalQmsIssue',NULL),
  (931906,NULL,'/qms',NULL,NULL,'certificate','质量证明文件',60,'ep:medal',@placeholder_component,'FormalQmsCertificate',NULL),

  (932001,NULL,'/finance',NULL,NULL,'cost','项目成本分析',10,'ep:pie-chart','erp/finance/cost/index','FormalFinanceCost','erp/finance/cost/index'),
  (932002,NULL,'/finance',NULL,NULL,'apar','应收应付账款池',20,'ep:wallet','erp/finance/apar/index','FormalFinanceApar','erp/finance/apar/index'),
  (932003,NULL,'/finance',NULL,NULL,'ap-estimate','采购暂估入库',30,'ep:clock','erp/finance/ap-estimate/index','FormalFinanceApEstimate','erp/finance/ap-estimate/index'),
  (932004,NULL,'/finance',NULL,NULL,'expense','研发报销 / 零星采购',35,'ep:document',@placeholder_component,'FormalFinanceExpense',NULL),
  (932005,NULL,'/finance',NULL,NULL,'ledger','财务账簿',40,'ep:collection','erp/finance/ledger/index','FormalFinanceLedger','erp/finance/ledger/index'),
  (932017,NULL,'/finance',NULL,NULL,'dual-ledger-config','双账套账簿映射',45,'ep:connection','erp/finance/dual-ledger-config/index','FormalFinanceDualLedgerConfig','erp/finance/dual-ledger-config/index'),
  (932018,NULL,'/finance',NULL,NULL,'dual-ledger-diff-config','双账套口径配置',46,'ep:operation','erp/finance/dual-ledger-diff-config/index','FormalFinanceDualLedgerDiffConfig','erp/finance/dual-ledger-diff-config/index'),
  (932006,NULL,'/finance',NULL,NULL,'period','会计期间',50,'ep:calendar','erp/finance/period/index','FormalFinancePeriod','erp/finance/period/index'),
  (932007,NULL,'/finance',NULL,NULL,'subject','财务科目',60,'ep:document','erp/finance/subject/index','FormalFinanceSubject','erp/finance/subject/index'),
  (932008,NULL,'/finance',NULL,NULL,'report-item','报表项目',70,'ep:list','erp/finance/report-item/index','FormalFinanceReportItem','erp/finance/report-item/index'),
  (932009,NULL,'/finance',NULL,NULL,'reports','财务报表',80,'ep:data-analysis','erp/finance/reports/index','FormalFinanceReports','erp/finance/reports/index'),
  (932010,NULL,'/finance',NULL,NULL,'voucher','财务凭证',90,'ep:tickets','erp/finance/voucher/index','FormalFinanceVoucher','erp/finance/voucher/index'),
  (932019,NULL,'/finance',NULL,NULL,'voucher-template','凭证模板',95,'ep:files','erp/finance/voucher-template/index','FormalFinanceVoucherTemplate','erp/finance/voucher-template/index'),
  (932015,NULL,'/finance',NULL,NULL,'general-ledger','总账',100,'ep:collection-tag','erp/finance/general-ledger/index','FormalFinanceGeneralLedger','erp/finance/general-ledger/index'),
  (932011,NULL,'/finance',NULL,NULL,'receipt','项目收款管理',100,'ep:expand','erp/finance/receipt/index','FormalFinanceReceipt','erp/finance/receipt/index'),
  (932012,NULL,'/finance',NULL,NULL,'payment','项目付款管理',110,'ep:caret-right','erp/finance/payment/index','FormalFinancePayment','erp/finance/payment/index'),
  (932016,NULL,'/finance',NULL,NULL,'prepayment','预付款',115,'ep:wallet-filled','erp/finance/prepayment/index','FormalFinancePrepayment','erp/finance/prepayment/index'),
  (932013,NULL,'/finance',NULL,NULL,'account','结算账户',120,'fa:universal-access','erp/finance/account/index','FormalFinanceAccount','erp/finance/account/index'),
  (932014,NULL,'/finance',NULL,NULL,'assets','固定资产台账',130,'ep:coin','erp/finance/assets/index','FormalFinanceAssets','erp/finance/assets/index'),

  (932101,NULL,'/hr',NULL,NULL,'employee-archive','员工档案',10,'ep:files','hr/archive/index','FormalHrArchive','hr/archive/index'),
  (932102,NULL,'/hr',NULL,NULL,'onboarding-offboarding','入离职流程',20,'ep:switch-button',@placeholder_component,'FormalHrOnboardingOffboarding',NULL),
  (932103,NULL,'/hr',NULL,NULL,'appraisal','绩效考核',30,'ep:data-analysis','hr/appraisal/index','FormalHrAppraisal','hr/appraisal/index'),
  (932104,NULL,'/hr',NULL,NULL,'shift','考勤班次',40,'ep:calendar','hr/shift/index','FormalHrShift','hr/shift/index'),
  (932105,NULL,'/hr',NULL,NULL,'salary','薪酬管理',50,'ep:wallet','hr/salary/index','FormalHrSalary','hr/salary/index'),
  (932106,NULL,'/hr',NULL,NULL,'recruitment','招聘管理',60,'ep:user-filled',@placeholder_component,'FormalHrRecruitment',NULL),

  (932201,NULL,'/system',NULL,NULL,'org','组织架构维护',10,'fa:address-card','system/dept/index','FormalSystemOrg','system/dept/index'),
  (932202,NULL,'/system',NULL,NULL,'post','岗位维护',20,'ep:briefcase','system/post/index','FormalSystemPost','system/post/index'),
  (932203,NULL,'/system',NULL,NULL,'user-role','用户与角色',30,'ep:user','system/user/index','FormalSystemUserRole','system/user/index'),
  (932204,NULL,'/system',NULL,NULL,'menu-permission','菜单与权限',40,'ep:menu','system/menu/index','FormalSystemMenuPermission','system/menu/index'),
  (932205,NULL,'/system',NULL,NULL,'dictionary-config','字典与参数',50,'ep:collection','system/dict/index','FormalSystemDictConfig','system/dict/index'),
  (932206,NULL,'/system',NULL,NULL,'workflow-config','流程配置',60,'fa:medium','bpm/model/index','FormalSystemWorkflowConfig','bpm/model/index'),
  (932207,NULL,'/system',NULL,NULL,'audit-log','审计日志',70,'ep:document-copy','system/operatelog/index','FormalSystemAuditLog','system/operatelog/index');

UPDATE `tmp_weitai_restore_targets` t
JOIN `tmp_weitai_restore_roots` r
  ON r.`path` COLLATE utf8mb4_unicode_ci = t.`root_path`
LEFT JOIN `tmp_weitai_restore_groups` g
  ON g.`root_path` COLLATE utf8mb4_unicode_ci = t.`root_path`
 AND g.`path` COLLATE utf8mb4_unicode_ci = t.`group_path`
SET t.`actual_parent_id` = COALESCE(g.`actual_id`, r.`actual_id`);

UPDATE `tmp_weitai_restore_targets` t
LEFT JOIN (
  SELECT `component`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `component` IS NOT NULL
    AND `component` <> ''
  GROUP BY `component`
) component_menu
  ON component_menu.`component` COLLATE utf8mb4_unicode_ci = t.`lookup_component`
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) path_menu
  ON path_menu.`parent_id` = t.`actual_parent_id`
 AND path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET t.`actual_id` = COALESCE(component_menu.`id`, path_menu.`id`);

UPDATE `system_menu` sm
JOIN `tmp_weitai_restore_targets` t ON t.`actual_id` = sm.`id`
SET sm.`name` = t.`name`,
    sm.`type` = 2,
    sm.`sort` = t.`sort_no`,
    sm.`parent_id` = t.`actual_parent_id`,
    sm.`path` = t.`path`,
    sm.`icon` = t.`icon`,
    sm.`component` = t.`component`,
    sm.`component_name` = t.`component_name`,
    sm.`status` = 0,
    sm.`visible` = b'1',
    sm.`keep_alive` = b'1',
    sm.`always_show` = b'1',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0'
  AND t.`actual_parent_id` IS NOT NULL;

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT t.`fixed_id`, t.`name`, '', 2, t.`sort_no`, t.`actual_parent_id`, t.`path`, t.`icon`, t.`component`,
       t.`component_name`, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_restore_targets` t
WHERE t.`actual_id` IS NULL
  AND t.`actual_parent_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu` sm
    WHERE sm.`deleted` = b'0'
      AND sm.`parent_id` = t.`actual_parent_id`
      AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_restore_targets` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = t.`actual_parent_id`
    AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `parent_id` = (
    SELECT `actual_id`
    FROM `tmp_weitai_restore_roots`
    WHERE `path` = '/finance'
    LIMIT 1
  )
  AND `type` = 2
  AND `path` IN ('project-cost', 'production-cost', 'statement', 'analysis')
  AND `component` = @placeholder_component;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `parent_id` = 0
  AND `path` IN (
    '/infra', '/erp', '/iot', '/mrp', '/manufacturing', '/research',
    '/pay', '/report', '/bpm', '/member', '/mall', '/mp', '/crm', '/ai'
  );

SET @legacy_erp_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `parent_id` = 0
    AND `path` = '/erp'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND @legacy_erp_root_id IS NOT NULL
  AND (`id` = @legacy_erp_root_id OR `parent_id` = @legacy_erp_root_id);

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, candidate.`menu_id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
  SELECT `actual_id` AS `menu_id` FROM `tmp_weitai_restore_roots` WHERE `actual_id` IS NOT NULL
  UNION
  SELECT `actual_id` AS `menu_id` FROM `tmp_weitai_restore_groups` WHERE `actual_id` IS NOT NULL
  UNION
  SELECT `actual_id` AS `menu_id` FROM `tmp_weitai_restore_targets` WHERE `actual_id` IS NOT NULL
) candidate
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1
    AND rm.`menu_id` = candidate.`menu_id`
    AND rm.`tenant_id` = @tenant_id
    AND rm.`deleted` = b'0'
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_root_descendants`;
CREATE TEMPORARY TABLE `tmp_weitai_restore_root_descendants` (
  `root_menu_id` BIGINT NOT NULL,
  `descendant_menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`root_menu_id`, `descendant_menu_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_restore_root_descendants` (`root_menu_id`,`descendant_menu_id`)
SELECT r.`actual_id`, g.`actual_id`
FROM `tmp_weitai_restore_groups` g
JOIN `tmp_weitai_restore_roots` r
  ON r.`path` COLLATE utf8mb4_unicode_ci = g.`root_path`
WHERE r.`actual_id` IS NOT NULL
  AND g.`actual_id` IS NOT NULL;

INSERT IGNORE INTO `tmp_weitai_restore_root_descendants` (`root_menu_id`,`descendant_menu_id`)
SELECT r.`actual_id`, t.`actual_id`
FROM `tmp_weitai_restore_targets` t
JOIN `tmp_weitai_restore_roots` r
  ON r.`path` COLLATE utf8mb4_unicode_ci = t.`root_path`
WHERE r.`actual_id` IS NOT NULL
  AND t.`actual_id` IS NOT NULL;

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_group_descendants`;
CREATE TEMPORARY TABLE `tmp_weitai_restore_group_descendants` (
  `group_menu_id` BIGINT NOT NULL,
  `descendant_menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`group_menu_id`, `descendant_menu_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_restore_group_descendants` (`group_menu_id`,`descendant_menu_id`)
SELECT g.`actual_id`, t.`actual_id`
FROM `tmp_weitai_restore_targets` t
JOIN `tmp_weitai_restore_groups` g
  ON g.`root_path` COLLATE utf8mb4_unicode_ci = t.`root_path`
 AND g.`path` COLLATE utf8mb4_unicode_ci = t.`group_path`
WHERE g.`actual_id` IS NOT NULL
  AND t.`actual_id` IS NOT NULL
  AND t.`group_path` IS NOT NULL;

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT DISTINCT rm.`role_id`, root_menu.`actual_id`, '1', NOW(), '1', NOW(), b'0', rm.`tenant_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm
  ON sm.`id` = rm.`menu_id`
 AND sm.`deleted` = b'0'
JOIN `tmp_weitai_restore_root_descendants` root_desc
  ON root_desc.`descendant_menu_id` = sm.`id`
JOIN `tmp_weitai_restore_roots` root_menu
  ON root_menu.`actual_id` = root_desc.`root_menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`status` = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` existing
    WHERE existing.`deleted` = b'0'
      AND existing.`role_id` = rm.`role_id`
      AND existing.`tenant_id` = rm.`tenant_id`
      AND existing.`menu_id` = root_menu.`actual_id`
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT DISTINCT rm.`role_id`, grp.`actual_id`, '1', NOW(), '1', NOW(), b'0', rm.`tenant_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm
  ON sm.`id` = rm.`menu_id`
 AND sm.`deleted` = b'0'
JOIN `tmp_weitai_restore_group_descendants` grp_desc
  ON grp_desc.`descendant_menu_id` = sm.`id`
JOIN `tmp_weitai_restore_groups` grp
  ON grp.`actual_id` = grp_desc.`group_menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`status` = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` existing
    WHERE existing.`deleted` = b'0'
      AND existing.`role_id` = rm.`role_id`
      AND existing.`tenant_id` = rm.`tenant_id`
      AND existing.`menu_id` = grp.`actual_id`
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_group_descendants`;
DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_root_descendants`;
DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_targets`;
DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_groups`;
DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_restore_roots`;

SET FOREIGN_KEY_CHECKS = 1;
