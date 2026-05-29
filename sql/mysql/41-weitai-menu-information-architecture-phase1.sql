/*
 Target: Weitai menu information architecture phase 1
 Schema: ruoyi-vue-pro
 Date: 2026-04-17
 Note:
   1. Only migrate menus backed by real pages on tester/backend-frontend-fusion
   2. Promote existing business roots to top-level menus where possible
   3. Remove MRP as a first-level navigation root
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @erp_root_id := COALESCE(
  (SELECT `id` FROM `system_menu` WHERE `path` = '/erp' AND `deleted` = b'0' ORDER BY `id` LIMIT 1),
  2563
);

SET @legacy_product_root_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/product/product/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_id AND `path` = 'product' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/master-data' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

UPDATE `system_menu`
SET `name` = '产品与物料中心',
    `parent_id` = 0,
    `sort` = 36,
    `path` = '/master-data',
    `icon` = 'ep:box',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @legacy_product_root_id
  AND @legacy_product_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '产品与物料中心', '', 1, 36, 0, '/master-data',
       'ep:box', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @legacy_product_root_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/master-data' AND `deleted` = b'0'
  );

SET @product_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/master-data' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @legacy_sale_root_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/sale/order/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_id AND `path` = 'sale' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/sales' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

UPDATE `system_menu`
SET `name` = '销售管理',
    `parent_id` = 0,
    `sort` = 37,
    `path` = '/sales',
    `icon` = 'ep:sell',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @legacy_sale_root_id
  AND @legacy_sale_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '销售管理', '', 1, 37, 0, '/sales',
       'ep:sell', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @legacy_sale_root_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/sales' AND `deleted` = b'0'
  );

SET @sale_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/sales' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @legacy_supply_root_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/purchase/order/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/purchase/in/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_id AND `path` = 'purchase' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

UPDATE `system_menu`
SET `name` = '供应链管理',
    `parent_id` = 0,
    `sort` = 39,
    `path` = '/scm',
    `icon` = 'ep:shopping-cart-full',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @legacy_supply_root_id
  AND @legacy_supply_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '供应链管理', '', 1, 39, 0, '/scm',
       'ep:shopping-cart-full', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @legacy_supply_root_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  );

SET @supply_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @legacy_rd_root_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/rd/rd-bom/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/rd/design/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_id AND `path` = 'rd' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/research' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

UPDATE `system_menu`
SET `name` = '研发管理',
    `parent_id` = 0,
    `sort` = 38,
    `path` = '/research',
    `icon` = 'ep:edit-pen',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @legacy_rd_root_id
  AND @legacy_rd_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发管理', '', 1, 38, 0, '/research',
       'ep:edit-pen', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @legacy_rd_root_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/research' AND `deleted` = b'0'
  );

SET @rd_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/research' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @legacy_finance_root_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/finance/payment/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/finance/receipt/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = @erp_root_id AND `path` = 'finance' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

UPDATE `system_menu`
SET `name` = '财务管理',
    `parent_id` = 0,
    `sort` = 45,
    `path` = '/finance',
    `icon` = 'ep:money',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @legacy_finance_root_id
  AND @legacy_finance_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '财务管理', '', 1, 45, 0, '/finance',
       'ep:money', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @legacy_finance_root_id IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0'
  );

SET @finance_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目中心', '', 1, 35, 0, '/project',
       'ep:files', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/project' AND `deleted` = b'0'
);

SET @project_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/project' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '工艺管理', '', 1, 40, 0, '/process',
       'ep:connection', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
);

SET @process_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @project_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/project/project/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '项目台账',
    `parent_id` = @project_root_id,
    `type` = 2,
    `sort` = 1,
    `path` = 'project',
    `icon` = 'ep:document',
    `component_name` = 'ErpProjectCenter',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @project_menu_id
  AND @project_menu_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目台账', '', 2, 1, @project_root_id, 'project',
       'ep:document', 'erp/project/project/index', 'ErpProjectCenter',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NULL
  AND @project_root_id IS NOT NULL;

SET @project_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/project/project/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `parent_id` = @project_menu_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` IN (
  'erp:project:query',
  'erp:project:create',
  'erp:project:update',
  'erp:project:delete',
  'erp:project:export',
  'erp:project:pc-confirm',
  'erp:project:mc-confirm'
)
  AND `deleted` = b'0'
  AND @project_menu_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目查询', 'erp:project:query', 3, 1, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目创建', 'erp:project:create', 3, 2, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目更新', 'erp:project:update', 3, 3, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目删除', 'erp:project:delete', 3, 4, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:delete' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目导出', 'erp:project:export', 3, 5, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:export' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目PC确认', 'erp:project:pc-confirm', 3, 6, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:pc-confirm' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '项目MC确认', 'erp:project:mc-confirm', 3, 7, @project_menu_id,
       '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @project_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:project:mc-confirm' AND `deleted` = b'0'
  );

UPDATE `system_menu`
SET `name` = '产品主档',
    `parent_id` = @product_root_id,
    `sort` = 1,
    `path` = 'product',
    `icon` = 'fa-solid:apple-alt',
    `component_name` = 'ErpProduct',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/product/product/index'
  AND `deleted` = b'0'
  AND @product_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '产品分类',
    `parent_id` = @product_root_id,
    `sort` = 2,
    `path` = 'product-category',
    `icon` = 'fa:certificate',
    `component_name` = 'ErpProductCategory',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/product/category/index'
  AND `deleted` = b'0'
  AND @product_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '单位与换算',
    `parent_id` = @product_root_id,
    `sort` = 3,
    `path` = 'unit',
    `icon` = 'ep:operation',
    `component_name` = 'ErpProductUnit',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/product/unit/index'
  AND `deleted` = b'0'
  AND @product_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '客户信息',
    `parent_id` = @sale_root_id,
    `sort` = 1,
    `path` = 'customer',
    `icon` = 'ep:user',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/sale/customer/index'
  AND `deleted` = b'0'
  AND @sale_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '销售订单台账',
    `parent_id` = @sale_root_id,
    `sort` = 2,
    `path` = 'order',
    `icon` = 'ep:list',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/sale/order/index'
  AND `deleted` = b'0'
  AND @sale_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '销售出库',
    `parent_id` = @sale_root_id,
    `sort` = 3,
    `path` = 'delivery',
    `icon` = 'ep:van',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/sale/out/index'
  AND `deleted` = b'0'
  AND @sale_root_id IS NOT NULL;

SET @design_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/rd/design/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '设计中心',
    `parent_id` = @rd_root_id,
    `type` = 2,
    `sort` = 1,
    `path` = 'design',
    `icon` = 'ep:edit-pen',
    `component_name` = 'ErpDesign',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @design_menu_id
  AND @design_menu_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '设计中心', '', 2, 1, @rd_root_id, 'design',
       'ep:edit-pen', 'erp/rd/design/index', 'ErpDesign',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @design_menu_id IS NULL
  AND @rd_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '研发BOM',
    `parent_id` = @rd_root_id,
    `sort` = 2,
    `path` = 'rd-bom',
    `icon` = 'ep:document',
    `component_name` = 'ErpRdBom',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/rd/rd-bom/index'
  AND `deleted` = b'0'
  AND @rd_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '标准BOM',
    `parent_id` = @rd_root_id,
    `sort` = 3,
    `path` = 'standard-bom',
    `icon` = 'ep:files',
    `component_name` = 'ErpStandardBom',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/rd/bom/index'
  AND `deleted` = b'0'
  AND @rd_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '计划参数',
    `parent_id` = @supply_root_id,
    `sort` = 1,
    `path` = 'plan-rule',
    `icon` = 'ep:setting',
    `component_name` = 'ErpPlanRule',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/mrp/plan-rule/index'
  AND `deleted` = b'0'
  AND @supply_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = 'MRP计划',
    `parent_id` = @supply_root_id,
    `sort` = 2,
    `path` = 'plan',
    `icon` = 'ep:calendar',
    `component_name` = 'ErpMrpPlan',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/mrp/plan/index'
  AND `deleted` = b'0'
  AND @supply_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = 'MRP运算',
    `parent_id` = @supply_root_id,
    `sort` = 3,
    `path` = 'suggest',
    `icon` = 'ep:histogram',
    `component_name` = 'ErpMrpSuggest',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/mrp/suggest/index'
  AND `deleted` = b'0'
  AND @supply_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '采购订单台账',
    `parent_id` = @supply_root_id,
    `sort` = 4,
    `path` = 'purchase-order',
    `icon` = 'ep:shopping-trolley',
    `component_name` = 'ErpPurchaseOrder',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/purchase/order/index'
  AND `deleted` = b'0'
  AND @supply_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '收货入库',
    `parent_id` = @supply_root_id,
    `sort` = 5,
    `path` = 'inbound',
    `icon` = 'ep:box',
    `component_name` = 'ErpPurchaseIn',
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` = 'erp/purchase/in/index'
  AND `deleted` = b'0'
  AND @supply_root_id IS NOT NULL;

SET @manufacture_bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '制造BOM',
    `parent_id` = @process_root_id,
    `type` = 2,
    `sort` = 1,
    `path` = 'manufacture-bom',
    `icon` = 'ep:operation',
    `component_name` = 'ErpManufactureBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @manufacture_bom_menu_id
  AND @manufacture_bom_menu_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM', '', 2, 1, @process_root_id, 'manufacture-bom',
       'ep:operation', 'erp/mrp/bom/index', 'ErpManufactureBom',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NULL
  AND @process_root_id IS NOT NULL;

UPDATE `system_menu`
SET `parent_id` = @finance_root_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `component` IN (
  'erp/finance/account/index',
  'erp/finance/payment/index',
  'erp/finance/receipt/index'
)
  AND `deleted` = b'0'
  AND @finance_root_id IS NOT NULL;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND (
    (`parent_id` = @erp_root_id AND `path` = 'mrp')
    OR (`parent_id` = 0 AND `path` = '/mrp')
  );

SET FOREIGN_KEY_CHECKS = 1;
