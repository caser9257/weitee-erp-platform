/*
 Target: ERP 正式菜单体系兼容重建
 Schema: ruoyi-vue-pro
 Date: 2026-04-15
 Strategy:
 1. 新建正式一级目录
 2. 旧业务菜单优先按 component 原地挂接
 3. 旧按钮 permission 编码不改
 4. 目录授权按已有后代菜单自动补齐
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;


INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930100,'项目中心','',1,310,0,'/project','ep:files','','FormalProjectRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/project' AND `deleted`=b'0');
SET @project_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/project' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @project_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/project/project/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='项目台账',`type`=2,`sort`=10,`parent_id`=@project_root_id,`path`='project',`icon`='ep:document',`component`='erp/project/project/index',`component_name`='FormalProjectCenter',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@project_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930101,'项目台账','',2,10,@project_root_id,'project','ep:document','erp/project/project/index','FormalProjectCenter',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @project_menu_id IS NULL;
SET @project_menu_id := COALESCE(@project_menu_id, 930101);
SET @project_warning_menu_id := (SELECT `id` FROM `system_menu` WHERE `id` = 930107 AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu`
SET `name`='项目预警',
    `type`=2,
    `sort`=70,
    `parent_id`=@project_root_id,
    `path`='warning',
    `icon`='ep:warning',
    `component`='pmo/project/warning/index',
    `component_name`='FormalPmoWarning',
    `status`=0,
    `visible`=b'1',
    `keep_alive`=b'1',
    `always_show`=b'1',
    `updater`='1',
    `update_time`=NOW()
WHERE `id`=@project_warning_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930107,'项目预警','',2,70,@project_root_id,'warning','ep:warning','pmo/project/warning/index','FormalPmoWarning',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @project_warning_menu_id IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu`
    WHERE `parent_id` = @project_root_id
      AND `path` = 'warning'
      AND `deleted` = b'0'
  );
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930102,'全生命周期大盘','',2,20,@project_root_id,'lifecycle','ep:office-building','pmo/project/index','FormalProjectLifecycle',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='pmo/project/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930103,'经营指标看板','',2,30,@project_root_id,'kpi','ep:trend-charts','pmo/kpi/index','FormalProjectKpi',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='pmo/kpi/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930104,'组织架构视图','',2,40,@project_root_id,'org-chart','ep:share','pmo/org-chart/index','FormalProjectOrgChart',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='pmo/org-chart/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930110,'销售管理','',1,320,0,'/sales','ep:sell','','FormalSalesRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/sales' AND `deleted`=b'0');
SET @sales_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/sales' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @sales_contract_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='crm/contract/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='客户与合同评审',`type`=2,`sort`=10,`parent_id`=@sales_root_id,`path`='contract',`icon`='ep:document-checked',`component`='crm/contract/index',`component_name`='FormalSalesContract',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@sales_contract_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930111,'客户与合同评审','',2,10,@sales_root_id,'contract','ep:document-checked','crm/contract/index','FormalSalesContract',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @sales_contract_menu_id IS NULL;
SET @sales_order_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/sale/order/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='销售订单台账',`type`=2,`sort`=20,`parent_id`=@sales_root_id,`path`='order',`icon`='ep:list',`component`='erp/sale/order/index',`component_name`='FormalSalesOrder',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@sales_order_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930112,'销售订单台账','',2,20,@sales_root_id,'order','ep:list','erp/sale/order/index','FormalSalesOrder',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @sales_order_menu_id IS NULL;
SET @sales_delivery_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/sale/out/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='发货通知与签收',`type`=2,`sort`=30,`parent_id`=@sales_root_id,`path`='delivery',`icon`='ep:van',`component`='erp/sale/out/index',`component_name`='FormalSalesDelivery',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@sales_delivery_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930113,'发货通知与签收','',2,30,@sales_root_id,'delivery','ep:van','erp/sale/out/index','FormalSalesDelivery',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @sales_delivery_menu_id IS NULL;

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930120,'研发管理','',1,330,0,'/rd','ep:cpu','','FormalRdRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/rd' AND `deleted`=b'0');
SET @rd_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/rd' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @rd_rd_bom_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/rd/rd-bom/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='研发 BOM',`type`=2,`sort`=10,`parent_id`=@rd_root_id,`path`='rd-bom',`icon`='ep:collection-tag',`component`='erp/rd/rd-bom/index',`component_name`='FormalRdBom',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@rd_rd_bom_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930121,'研发 BOM','',2,10,@rd_root_id,'rd-bom','ep:collection-tag','erp/rd/rd-bom/index','FormalRdBom',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @rd_rd_bom_menu_id IS NULL;
SET @rd_bom_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/rd/bom/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='标准 BOM',`type`=2,`sort`=20,`parent_id`=@rd_root_id,`path`='bom',`icon`='ep:collection',`component`='erp/rd/bom/index',`component_name`='FormalStandardBom',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@rd_bom_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930122,'标准 BOM','',2,20,@rd_root_id,'bom','ep:collection','erp/rd/bom/index','FormalStandardBom',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @rd_bom_menu_id IS NULL;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930123,'研发文档与图纸库','',2,30,@rd_root_id,'document','ep:folder-opened','erp/rd/document/index','FormalRdDocument',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/rd/document/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930124,'工艺路线与 SOP','',2,40,@rd_root_id,'routing','ep:connection','erp/route/index','FormalRdRouting',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/route/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930130,'综合计划管理','',1,340,0,'/pmo','ep:data-board','','FormalPmoRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/pmo' AND `deleted`=b'0');
SET @pmo_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/pmo' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @pmo_plan_rule_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/mrp/plan-rule/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='计划参数',`type`=2,`sort`=10,`parent_id`=@pmo_root_id,`path`='plan-rule',`icon`='ep:setting',`component`='erp/mrp/plan-rule/index',`component_name`='FormalPmoPlanRule',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@pmo_plan_rule_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930131,'计划参数','',2,10,@pmo_root_id,'plan-rule','ep:setting','erp/mrp/plan-rule/index','FormalPmoPlanRule',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @pmo_plan_rule_menu_id IS NULL;
SET @pmo_plan_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/mrp/plan/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='MRP 计划',`type`=2,`sort`=20,`parent_id`=@pmo_root_id,`path`='plan',`icon`='ep:calendar',`component`='erp/mrp/plan/index',`component_name`='FormalPmoPlan',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@pmo_plan_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930132,'MRP 计划','',2,20,@pmo_root_id,'plan','ep:calendar','erp/mrp/plan/index','FormalPmoPlan',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @pmo_plan_menu_id IS NULL;
SET @pmo_bom_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/mrp/bom/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='制造 BOM',`type`=2,`sort`=30,`parent_id`=@pmo_root_id,`path`='bom',`icon`='ep:collection',`component`='erp/mrp/bom/index',`component_name`='FormalPmoManufactureBom',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@pmo_bom_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930133,'制造 BOM','',2,30,@pmo_root_id,'bom','ep:collection','erp/mrp/bom/index','FormalPmoManufactureBom',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @pmo_bom_menu_id IS NULL;
SET @pmo_suggest_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/mrp/suggest/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='MRP 运算',`type`=2,`sort`=40,`parent_id`=@pmo_root_id,`path`='suggest',`icon`='ep:histogram',`component`='erp/mrp/suggest/index',`component_name`='FormalPmoSuggest',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@pmo_suggest_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930134,'MRP 运算','',2,40,@pmo_root_id,'suggest','ep:histogram','erp/mrp/suggest/index','FormalPmoSuggest',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @pmo_suggest_menu_id IS NULL;

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930140,'供应链管理','',1,350,0,'/scm','ep:shopping-cart-full','','FormalScmRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/scm' AND `deleted`=b'0');
SET @scm_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/scm' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @scm_purchase_order_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/purchase/order/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='采购订单台账',`type`=2,`sort`=10,`parent_id`=@scm_root_id,`path`='purchase-order',`icon`='ep:shopping-trolley',`component`='erp/purchase/order/index',`component_name`='FormalScmPurchaseOrder',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@scm_purchase_order_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930141,'采购订单台账','',2,10,@scm_root_id,'purchase-order','ep:shopping-trolley','erp/purchase/order/index','FormalScmPurchaseOrder',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_purchase_order_menu_id IS NULL;
SET @scm_inbound_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/purchase/in/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='收货入库',`type`=2,`sort`=20,`parent_id`=@scm_root_id,`path`='inbound',`icon`='ep:box',`component`='erp/purchase/in/index',`component_name`='FormalScmInbound',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@scm_inbound_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930142,'收货入库','',2,20,@scm_root_id,'inbound','ep:box','erp/purchase/in/index','FormalScmInbound',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_inbound_menu_id IS NULL;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930143,'组装与拆卸','',2,30,@scm_root_id,'assemble','ep:set-up','erp/stock/assemble/index','FormalScmAssemble',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/stock/assemble/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930144,'采购退货','',2,40,@scm_root_id,'return','ep:minus','erp/purchase/return/index','FormalScmPurchaseReturn',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/purchase/return/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930145,'其它入库','',2,50,@scm_root_id,'stock-in','ep:zoom-in','erp/stock/in/index','FormalScmStockIn',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/stock/in/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930146,'其它出库','',2,60,@scm_root_id,'stock-out','ep:zoom-out','erp/stock/out/index','FormalScmStockOut',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/stock/out/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930147,'库存盘点','',2,70,@scm_root_id,'stock-check','ep:circle-check-filled','erp/stock/check/index','FormalScmStockCheck',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/stock/check/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930148,'库存调拨','',2,80,@scm_root_id,'stock-move','ep:folder-remove','erp/stock/move/index','FormalScmStockMove',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/stock/move/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930150,'质量管理','',1,360,0,'/qms','ep:medal','','FormalQmsRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/qms' AND `deleted`=b'0');
SET @qms_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/qms' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @qms_iqc_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` IN ('qms/iqc/IqcEntry','erp/purchase/in-quality/index') AND `deleted`=b'0' ORDER BY FIELD(`component`,'qms/iqc/IqcEntry','erp/purchase/in-quality/index'),`id` LIMIT 1);
UPDATE `system_menu` SET `name`='来料检验',`type`=2,`sort`=10,`parent_id`=@qms_root_id,`path`='iqc',`icon`='ep:finished',`component`='qms/iqc/IqcEntry',`component_name`='FormalQmsIqc',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@qms_iqc_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930151,'来料检验','',2,10,@qms_root_id,'iqc','ep:finished','qms/iqc/IqcEntry','FormalQmsIqc',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @qms_iqc_menu_id IS NULL;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930152,'过程检验','',2,20,@qms_root_id,'ipqc','ep:aim','qms/ipqc/index','FormalQmsIpqc',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='qms/ipqc/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930153,'完工与出货检验','',2,30,@qms_root_id,'oqc','ep:circle-check','qms/oqc/index','FormalQmsOqc',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='qms/oqc/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930160,'工艺管理','',1,370,0,'/process','ep:connection','','FormalProcessRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/process' AND `deleted`=b'0');
SET @process_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/process' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @process_route_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/process-route/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='工艺路线',`type`=2,`sort`=10,`parent_id`=@process_root_id,`path`='route',`icon`='ep:share',`component`='erp/manufacturing/process-route/index',`component_name`='FormalProcessRoute',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@process_route_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930161,'工艺路线','',2,10,@process_root_id,'route','ep:share','erp/manufacturing/process-route/index','FormalProcessRoute',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @process_route_menu_id IS NULL;
SET @process_center_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/work-center/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='工作中心',`type`=2,`sort`=20,`parent_id`=@process_root_id,`path`='work-center',`icon`='ep:office-building',`component`='erp/manufacturing/work-center/index',`component_name`='FormalProcessWorkCenter',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@process_center_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930162,'工作中心','',2,20,@process_root_id,'work-center','ep:office-building','erp/manufacturing/work-center/index','FormalProcessWorkCenter',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @process_center_menu_id IS NULL;

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930170,'制造执行管理','',1,380,0,'/mes','ep:operation','','FormalMesRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/mes' AND `deleted`=b'0');
SET @mes_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/mes' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
SET @mes_device_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/device/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='设备台账',`type`=2,`sort`=10,`parent_id`=@mes_root_id,`path`='device',`icon`='ep:cpu',`component`='erp/manufacturing/device/index',`component_name`='FormalMesDevice',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@mes_device_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930171,'设备台账','',2,10,@mes_root_id,'device','ep:cpu','erp/manufacturing/device/index','FormalMesDevice',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @mes_device_menu_id IS NULL;
SET @mes_report_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/production-report/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='生产报工',`type`=2,`sort`=20,`parent_id`=@mes_root_id,`path`='production-report',`icon`='ep:histogram',`component`='erp/manufacturing/production-report/index',`component_name`='FormalMesProductionReport',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@mes_report_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930172,'生产报工','',2,20,@mes_root_id,'production-report','ep:histogram','erp/manufacturing/production-report/index','FormalMesProductionReport',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @mes_report_menu_id IS NULL;
SET @mes_issue_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/material-issue/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='生产领料',`type`=2,`sort`=30,`parent_id`=@mes_root_id,`path`='material-issue',`icon`='ep:box',`component`='erp/manufacturing/material-issue/index',`component_name`='FormalMesMaterialIssue',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@mes_issue_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930173,'生产领料','',2,30,@mes_root_id,'material-issue','ep:box','erp/manufacturing/material-issue/index','FormalMesMaterialIssue',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @mes_issue_menu_id IS NULL;
SET @mes_return_menu_id := (SELECT `id` FROM `system_menu` WHERE `component`='erp/manufacturing/material-return/index' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
UPDATE `system_menu` SET `name`='生产退料',`type`=2,`sort`=40,`parent_id`=@mes_root_id,`path`='material-return',`icon`='ep:refresh-left',`component`='erp/manufacturing/material-return/index',`component_name`='FormalMesMaterialReturn',`status`=0,`visible`=b'1',`keep_alive`=b'1',`always_show`=b'1',`updater`='1',`update_time`=NOW() WHERE `id`=@mes_return_menu_id;
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930174,'生产退料','',2,40,@mes_root_id,'material-return','ep:refresh-left','erp/manufacturing/material-return/index','FormalMesMaterialReturn',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @mes_return_menu_id IS NULL;

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930180,'财务管理','',1,390,0,'/finance','ep:money','','FormalFinanceRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/finance' AND `deleted`=b'0');
SET @finance_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/finance' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930181,'财务账户','',2,10,@finance_root_id,'account','ep:wallet','erp/finance/account/index','FormalFinanceAccount',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/account/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930182,'应收应付','',2,20,@finance_root_id,'apar','ep:credit-card','erp/finance/apar/index','FormalFinanceApar',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/apar/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930183,'资产管理','',2,30,@finance_root_id,'assets','ep:office-building','erp/finance/assets/index','FormalFinanceAssets',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/assets/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930184,'成本分析','',2,40,@finance_root_id,'cost','ep:pie-chart','erp/finance/cost/index','FormalFinanceCost',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/cost/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930185,'收款管理','',2,50,@finance_root_id,'receipt','ep:coin','erp/finance/receipt/index','FormalFinanceReceipt',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/receipt/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930186,'付款管理','',2,60,@finance_root_id,'payment','ep:money','erp/finance/payment/index','FormalFinancePayment',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='erp/finance/payment/index' AND `deleted`=b'0');

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930190,'人事管理','',1,400,0,'/hr','ep:user','','FormalHrRoot',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `parent_id`=0 AND `path`='/hr' AND `deleted`=b'0');
SET @hr_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id`=0 AND `path`='/hr' AND `deleted`=b'0' ORDER BY `id` LIMIT 1);
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930191,'员工档案','',2,10,@hr_root_id,'archive','ep:files','hr/archive/index','FormalHrArchive',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='hr/archive/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930192,'绩效考核','',2,20,@hr_root_id,'appraisal','ep:data-analysis','hr/appraisal/index','FormalHrAppraisal',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='hr/appraisal/index' AND `deleted`=b'0');
INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930193,'员工台账','',2,30,@hr_root_id,'employee','ep:user-filled','hr/employee/index','FormalHrEmployee',0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='hr/employee/index' AND `deleted`=b'0');

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 1, sm.`id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 930100 AS `menu_id` UNION ALL SELECT 930101 UNION ALL SELECT 930102 UNION ALL SELECT 930103 UNION ALL
  SELECT 930104 UNION ALL SELECT 930107 UNION ALL SELECT 930110 UNION ALL SELECT 930111 UNION ALL SELECT 930112 UNION ALL
  SELECT 930113 UNION ALL SELECT 930120 UNION ALL SELECT 930121 UNION ALL SELECT 930122 UNION ALL
  SELECT 930123 UNION ALL SELECT 930124 UNION ALL SELECT 930130 UNION ALL SELECT 930131 UNION ALL
  SELECT 930132 UNION ALL SELECT 930133 UNION ALL SELECT 930134 UNION ALL SELECT 930140 UNION ALL
  SELECT 930141 UNION ALL SELECT 930142 UNION ALL SELECT 930143 UNION ALL SELECT 930144 UNION ALL
  SELECT 930145 UNION ALL SELECT 930146 UNION ALL SELECT 930147 UNION ALL SELECT 930148 UNION ALL
  SELECT 930150 UNION ALL
  SELECT 930151 UNION ALL SELECT 930152 UNION ALL SELECT 930153 UNION ALL SELECT 930160 UNION ALL
  SELECT 930161 UNION ALL SELECT 930162 UNION ALL SELECT 930170 UNION ALL SELECT 930171 UNION ALL
  SELECT 930172 UNION ALL SELECT 930173 UNION ALL SELECT 930174 UNION ALL SELECT 930180 UNION ALL
  SELECT 930181 UNION ALL SELECT 930182 UNION ALL SELECT 930183 UNION ALL SELECT 930184 UNION ALL
  SELECT 930185 UNION ALL SELECT 930186 UNION ALL SELECT 930190 UNION ALL SELECT 930191 UNION ALL
  SELECT 930192 UNION ALL SELECT 930193
) target
JOIN `system_menu` sm ON sm.`id` = target.`menu_id` AND sm.`deleted` = b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = sm.`id` AND rm.`deleted` = b'0'
);

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, root_map.`root_id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id` AND sm.`deleted` = b'0'
JOIN (
  SELECT @project_root_id AS `root_id` UNION ALL SELECT @sales_root_id UNION ALL SELECT @rd_root_id UNION ALL
  SELECT @pmo_root_id UNION ALL SELECT @scm_root_id UNION ALL SELECT @qms_root_id UNION ALL
  SELECT @process_root_id UNION ALL SELECT @mes_root_id UNION ALL SELECT @finance_root_id UNION ALL SELECT @hr_root_id
) root_map
  ON sm.`parent_id` = root_map.`root_id`
  OR sm.`parent_id` IN (SELECT child.`id` FROM `system_menu` child WHERE child.`parent_id` = root_map.`root_id` AND child.`deleted` = b'0')
WHERE rm.`deleted` = b'0'

  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = root_map.`root_id`
      AND exists_rm.`deleted` = b'0'
  );

UPDATE `system_menu`
SET `status` = 1, `visible` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `path` = '/manufacturing' AND `deleted` = b'0';

UPDATE `system_menu`
SET `status` = 1, `visible` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `path` IN ('/mrp', 'mrp')
  AND `type` = 1
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `status` = 1, `visible` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `path` = '/erp'
  AND `type` = 1
  AND `deleted` = b'0';

SET @legacy_erp_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `path` = '/erp' AND `type` = 1 AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status` = 1, `visible` = b'0', `updater` = '1', `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 1
  AND (
    (`parent_id` = @legacy_erp_root_id AND `path` IN ('project', 'sale', 'purchase', 'stock', 'finance', 'rd', 'mrp', 'manufacturing'))
    OR (`parent_id` = 0 AND `path` IN ('/sale', '/purchase', '/stock', '/rd', '/mrp', '/manufacturing'))
  );

SET FOREIGN_KEY_CHECKS = 1;
