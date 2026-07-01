/*
 Target: Normalize SCM menu boundary
 Schema: current connection database
 Date: 2026-07-01
 Rule:
 1. /scm is the supply-chain navigation aggregate.
 2. /scm has three real database-backed menu groups: demand-plan, procurement, inventory-warehouse.
 3. MRP remains a planning capability under SCM, not a standalone root menu.
 4. Manufacturing BOM belongs to /process, not the SCM planning menu.
 5. Stock reservation remains an embedded capability opened from MRP calculation evidence.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930140,'供应链管理','',1,350,0,'/scm','ep:shopping-cart-full','','FormalScmRoot',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 930160,'工艺管理','',1,370,0,'/process','ep:connection','','FormalProcessRoot',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
);

SET @scm_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @process_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931620,'需求与计划','',1,10,@scm_root_id,'demand-plan','ep:histogram','','FormalScmDemandPlanGroup',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @scm_root_id AND `path` = 'demand-plan' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931621,'采购执行','',1,20,@scm_root_id,'procurement','ep:shopping-cart-full','','FormalScmProcurementGroup',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @scm_root_id AND `path` = 'procurement' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931622,'库存与仓储','',1,30,@scm_root_id,'inventory-warehouse','ep:box','','FormalScmInventoryWarehouseGroup',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @scm_root_id AND `path` = 'inventory-warehouse' AND `deleted` = b'0'
  );

SET @scm_demand_group_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @scm_root_id AND `path` = 'demand-plan' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @scm_procurement_group_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @scm_root_id AND `path` = 'procurement' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @scm_inventory_group_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = @scm_root_id AND `path` = 'inventory-warehouse' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '需求与计划',
    `type` = 1,
    `sort` = 10,
    `parent_id` = @scm_root_id,
    `path` = 'demand-plan',
    `icon` = 'ep:histogram',
    `component` = '',
    `component_name` = 'FormalScmDemandPlanGroup',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @scm_demand_group_id;

UPDATE `system_menu`
SET `name` = '采购执行',
    `type` = 1,
    `sort` = 20,
    `parent_id` = @scm_root_id,
    `path` = 'procurement',
    `icon` = 'ep:shopping-cart-full',
    `component` = '',
    `component_name` = 'FormalScmProcurementGroup',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @scm_procurement_group_id;

UPDATE `system_menu`
SET `name` = '库存与仓储',
    `type` = 1,
    `sort` = 30,
    `parent_id` = @scm_root_id,
    `path` = 'inventory-warehouse',
    `icon` = 'ep:box',
    `component` = '',
    `component_name` = 'FormalScmInventoryWarehouseGroup',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @scm_inventory_group_id;

SET @plan_rule_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
  ORDER BY CASE WHEN `id` = 5049 THEN 0 WHEN `id` = 930131 THEN 1 ELSE 2 END, `id`
  LIMIT 1
);

SET @plan_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY CASE WHEN `id` = 5068 THEN 0 WHEN `id` = 930132 THEN 1 ELSE 2 END, `id`
  LIMIT 1
);

SET @suggest_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0' AND `path` <> 'purchase-request'
  ORDER BY CASE WHEN `id` = 3276 THEN 0 WHEN `id` = 930134 THEN 1 ELSE 2 END, `id`
  LIMIT 1
);

SET @netting_policy_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/netting-policy/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @substitute_material_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/substitute/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @purchase_order_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/purchase/order/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @inbound_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/purchase/in/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @outsource_inbound_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/outsource-inbound/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @purchase_return_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/purchase/return/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_in_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/in/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_out_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/out/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_move_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/move/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_check_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/check/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @assemble_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/assemble/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @warehouse_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/warehouse/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @warehouse_category_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/warehouse-category/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/stock/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_record_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/record/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @stock_analysis_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/stock/analysis/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @manufacture_bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY CASE WHEN `id` = 920524 THEN 0 WHEN `id` = 930133 THEN 1 ELSE 2 END, `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931610,'计划参数','',2,10,@scm_demand_group_id,'plan-rule','ep:setting','erp/mrp/plan-rule/index','FormalScmPlanRule',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @plan_rule_menu_id IS NULL AND @scm_demand_group_id IS NOT NULL;

SET @plan_rule_menu_id := COALESCE(@plan_rule_menu_id, 931610);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931611,'MRP 计划','',2,20,@scm_demand_group_id,'plan','ep:calendar','erp/mrp/plan/index','FormalScmPlan',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @plan_menu_id IS NULL AND @scm_demand_group_id IS NOT NULL;

SET @plan_menu_id := COALESCE(@plan_menu_id, 931611);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931612,'MRP 运算','',2,30,@scm_demand_group_id,'suggest','ep:histogram','erp/mrp/suggest/index','FormalScmSuggest',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @suggest_menu_id IS NULL AND @scm_demand_group_id IS NOT NULL;

SET @suggest_menu_id := COALESCE(@suggest_menu_id, 931612);

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 931613,'制造 BOM','',2,10,@process_root_id,'bom','ep:collection','erp/mrp/bom/index','FormalProcessManufactureBom',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @manufacture_bom_menu_id IS NULL AND @process_root_id IS NOT NULL;

SET @manufacture_bom_menu_id := COALESCE(@manufacture_bom_menu_id, 931613);

UPDATE `system_menu`
SET `name` = '计划参数',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @scm_demand_group_id,
    `path` = 'plan-rule',
    `icon` = 'ep:setting',
    `component` = 'erp/mrp/plan-rule/index',
    `component_name` = 'FormalScmPlanRule',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_rule_menu_id;

UPDATE `system_menu`
SET `name` = 'MRP 计划',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @scm_demand_group_id,
    `path` = 'plan',
    `icon` = 'ep:calendar',
    `component` = 'erp/mrp/plan/index',
    `component_name` = 'FormalScmPlan',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_menu_id;

UPDATE `system_menu`
SET `name` = 'MRP 运算',
    `type` = 2,
    `sort` = 30,
    `parent_id` = @scm_demand_group_id,
    `path` = 'suggest',
    `icon` = 'ep:histogram',
    `component` = 'erp/mrp/suggest/index',
    `component_name` = 'FormalScmSuggest',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @suggest_menu_id;

UPDATE `system_menu`
SET `name` = '净需求策略',
    `type` = 2,
    `sort` = 50,
    `parent_id` = @scm_demand_group_id,
    `path` = 'netting-policy',
    `icon` = 'ep:operation',
    `component` = 'erp/mrp/netting-policy/index',
    `component_name` = 'FormalScmNettingPolicy',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @netting_policy_menu_id;

UPDATE `system_menu`
SET `name` = '替代料台账',
    `type` = 2,
    `sort` = 60,
    `parent_id` = @scm_demand_group_id,
    `path` = 'substitute-material',
    `icon` = 'ep:connection',
    `component` = 'erp/mrp/substitute/index',
    `component_name` = 'ProjectScmSubstitute',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @substitute_material_menu_id;

UPDATE `system_menu`
SET `name` = '采购订单台账',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @scm_procurement_group_id,
    `path` = 'purchase-order',
    `icon` = 'ep:shopping-trolley',
    `component` = 'erp/purchase/order/index',
    `component_name` = 'FormalScmPurchaseOrder',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @purchase_order_menu_id;

UPDATE `system_menu`
SET `name` = '收货入库',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @scm_procurement_group_id,
    `path` = 'inbound',
    `icon` = 'ep:box',
    `component` = 'erp/purchase/in/index',
    `component_name` = 'FormalScmInbound',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @inbound_menu_id;

UPDATE `system_menu`
SET `name` = '委外入库',
    `type` = 2,
    `sort` = 30,
    `parent_id` = @scm_procurement_group_id,
    `path` = 'outsource-inbound',
    `icon` = 'ep:box',
    `component` = 'erp/mrp/outsource-inbound/index',
    `component_name` = 'ErpMrpOutsourceInboundPage',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @outsource_inbound_menu_id;

UPDATE `system_menu`
SET `name` = '采购退货',
    `type` = 2,
    `sort` = 40,
    `parent_id` = @scm_procurement_group_id,
    `path` = 'return',
    `icon` = 'ep:minus',
    `component` = 'erp/purchase/return/index',
    `component_name` = 'FormalScmPurchaseReturn',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @purchase_return_menu_id;

UPDATE `system_menu`
SET `name` = '其他入库',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-in',
    `icon` = 'ep:zoom-in',
    `component` = 'erp/stock/in/index',
    `component_name` = 'FormalScmStockIn',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_in_menu_id;

UPDATE `system_menu`
SET `name` = '其他出库',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-out',
    `icon` = 'ep:zoom-out',
    `component` = 'erp/stock/out/index',
    `component_name` = 'FormalScmStockOut',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_out_menu_id;

UPDATE `system_menu`
SET `name` = '库存调拨',
    `type` = 2,
    `sort` = 30,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-move',
    `icon` = 'ep:folder-remove',
    `component` = 'erp/stock/move/index',
    `component_name` = 'FormalScmStockMove',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_move_menu_id;

UPDATE `system_menu`
SET `name` = '库存盘点',
    `type` = 2,
    `sort` = 40,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-check',
    `icon` = 'ep:circle-check-filled',
    `component` = 'erp/stock/check/index',
    `component_name` = 'FormalScmStockCheck',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_check_menu_id;

UPDATE `system_menu`
SET `name` = '组装与拆卸',
    `type` = 2,
    `sort` = 50,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'assemble',
    `icon` = 'ep:set-up',
    `component` = 'erp/stock/assemble/index',
    `component_name` = 'FormalScmAssemble',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @assemble_menu_id;

UPDATE `system_menu`
SET `name` = '仓库信息',
    `type` = 2,
    `sort` = 60,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'warehouse',
    `icon` = 'ep:house',
    `component` = 'erp/stock/warehouse/index',
    `component_name` = 'ProjectScmWarehouse',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @warehouse_menu_id;

UPDATE `system_menu`
SET `name` = '仓库分类管理',
    `type` = 2,
    `sort` = 70,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'warehouse-category',
    `icon` = 'ep:folder',
    `component` = 'erp/stock/warehouse-category/index',
    `component_name` = 'ErpWarehouseCategory',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @warehouse_category_menu_id;

UPDATE `system_menu`
SET `name` = '即时库存查询',
    `type` = 2,
    `sort` = 80,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock',
    `icon` = 'ep:coffee',
    `component` = 'erp/stock/stock/index',
    `component_name` = 'ProjectScmStock',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_menu_id;

UPDATE `system_menu`
SET `name` = '库存明细账',
    `type` = 2,
    `sort` = 90,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-record',
    `icon` = 'fa-solid:blog',
    `component` = 'erp/stock/record/index',
    `component_name` = 'ProjectScmStockRecord',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_record_menu_id;

UPDATE `system_menu`
SET `name` = '库存分析',
    `type` = 2,
    `sort` = 100,
    `parent_id` = @scm_inventory_group_id,
    `path` = 'stock-analysis',
    `icon` = 'ep:data-analysis',
    `component` = 'erp/stock/analysis/index',
    `component_name` = 'ProjectScmStockAnalysis',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @stock_analysis_menu_id;

UPDATE `system_menu`
SET `name` = '制造 BOM',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @process_root_id,
    `path` = 'bom',
    `icon` = 'ep:collection',
    `component` = 'erp/mrp/bom/index',
    `component_name` = 'FormalProcessManufactureBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @manufacture_bom_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND (
    (`type` = 1 AND `path` IN ('/mrp', 'mrp'))
    OR (`type` = 2 AND `component` = 'erp/mrp/plan-rule/index' AND `id` <> @plan_rule_menu_id)
    OR (`type` = 2 AND `component` = 'erp/mrp/plan/index' AND `id` <> @plan_menu_id)
    OR (`type` = 2 AND `component` = 'erp/mrp/suggest/index' AND `id` <> @suggest_menu_id)
    OR (`type` = 2 AND `component` = 'erp/mrp/bom/index' AND `id` <> @manufacture_bom_menu_id)
    OR (`type` = 2 AND `component` = 'erp/mrp/stock-reservation/index')
    OR (`type` = 2 AND `path` = 'purchase-request' AND `component` = 'erp/mrp/suggest/index')
  );

UPDATE `system_menu` root
LEFT JOIN `system_menu` child
  ON child.`parent_id` = root.`id`
 AND child.`deleted` = b'0'
 AND child.`status` = 0
 AND child.`visible` = b'1'
SET root.`status` = 1,
    root.`visible` = b'0',
    root.`updater` = '1',
    root.`update_time` = NOW()
WHERE root.`deleted` = b'0'
  AND root.`type` = 1
  AND root.`path` = '/pmo'
  AND child.`id` IS NULL;

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, ancestor.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
JOIN (
  SELECT @scm_root_id AS `menu_id`, @plan_rule_menu_id AS `child_id`
  UNION ALL SELECT @scm_root_id, @plan_menu_id
  UNION ALL SELECT @scm_root_id, @suggest_menu_id
  UNION ALL SELECT @scm_root_id, @netting_policy_menu_id
  UNION ALL SELECT @scm_root_id, @substitute_material_menu_id
  UNION ALL SELECT @scm_root_id, @purchase_order_menu_id
  UNION ALL SELECT @scm_root_id, @inbound_menu_id
  UNION ALL SELECT @scm_root_id, @outsource_inbound_menu_id
  UNION ALL SELECT @scm_root_id, @purchase_return_menu_id
  UNION ALL SELECT @scm_root_id, @stock_in_menu_id
  UNION ALL SELECT @scm_root_id, @stock_out_menu_id
  UNION ALL SELECT @scm_root_id, @stock_move_menu_id
  UNION ALL SELECT @scm_root_id, @stock_check_menu_id
  UNION ALL SELECT @scm_root_id, @assemble_menu_id
  UNION ALL SELECT @scm_root_id, @warehouse_menu_id
  UNION ALL SELECT @scm_root_id, @warehouse_category_menu_id
  UNION ALL SELECT @scm_root_id, @stock_menu_id
  UNION ALL SELECT @scm_root_id, @stock_record_menu_id
  UNION ALL SELECT @scm_root_id, @stock_analysis_menu_id
  UNION ALL SELECT @scm_demand_group_id, @plan_rule_menu_id
  UNION ALL SELECT @scm_demand_group_id, @plan_menu_id
  UNION ALL SELECT @scm_demand_group_id, @suggest_menu_id
  UNION ALL SELECT @scm_demand_group_id, @netting_policy_menu_id
  UNION ALL SELECT @scm_demand_group_id, @substitute_material_menu_id
  UNION ALL SELECT @scm_procurement_group_id, @purchase_order_menu_id
  UNION ALL SELECT @scm_procurement_group_id, @inbound_menu_id
  UNION ALL SELECT @scm_procurement_group_id, @outsource_inbound_menu_id
  UNION ALL SELECT @scm_procurement_group_id, @purchase_return_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_in_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_out_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_move_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_check_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @assemble_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @warehouse_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @warehouse_category_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_record_menu_id
  UNION ALL SELECT @scm_inventory_group_id, @stock_analysis_menu_id
  UNION ALL SELECT @process_root_id, @manufacture_bom_menu_id
) ancestor ON ancestor.`child_id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND ancestor.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = ancestor.`menu_id`
      AND exists_rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @scm_root_id AS `menu_id`
  UNION SELECT @scm_demand_group_id
  UNION SELECT @scm_procurement_group_id
  UNION SELECT @scm_inventory_group_id
  UNION SELECT @plan_rule_menu_id
  UNION SELECT @plan_menu_id
  UNION SELECT @suggest_menu_id
  UNION SELECT @netting_policy_menu_id
  UNION SELECT @substitute_material_menu_id
  UNION SELECT @purchase_order_menu_id
  UNION SELECT @inbound_menu_id
  UNION SELECT @outsource_inbound_menu_id
  UNION SELECT @purchase_return_menu_id
  UNION SELECT @stock_in_menu_id
  UNION SELECT @stock_out_menu_id
  UNION SELECT @stock_move_menu_id
  UNION SELECT @stock_check_menu_id
  UNION SELECT @assemble_menu_id
  UNION SELECT @warehouse_menu_id
  UNION SELECT @warehouse_category_menu_id
  UNION SELECT @stock_menu_id
  UNION SELECT @stock_record_menu_id
  UNION SELECT @stock_analysis_menu_id
  UNION SELECT @process_root_id
  UNION SELECT @manufacture_bom_menu_id
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`deleted` = b'0'
  );

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
