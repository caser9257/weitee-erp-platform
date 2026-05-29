/*
 Target: Weitai final menu information architecture
 Schema: ruoyi-vue-pro
 Date: 2026-04-20
 Note:
   1. Strictly align the final menu tree to the approved spec
   2. Reuse real Chinese pages when available
   3. Redirect legacy scaffold pages to the unified Chinese placeholder page
*/

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;
SET @placeholder_component = 'common/menu-placeholder/index';

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_roots`;
CREATE TEMPORARY TABLE `tmp_weitai_roots` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `path` VARCHAR(255) NOT NULL,
  `legacy_path` VARCHAR(255) NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  `match_component` VARCHAR(255) NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_roots`
(`fixed_id`,`actual_id`,`path`,`legacy_path`,`name`,`sort_no`,`icon`,`component_name`,`match_component`)
VALUES
  (930100,NULL,'/project',NULL,'椤圭洰涓績',310,'ep:files','FormalProjectRoot','erp/project/project/index'),
  (930105,NULL,'/master-data',NULL,'浜у搧涓庣墿鏂欎腑蹇?,320,'ep:box','FormalMasterDataRoot','erp/product/product/index'),
  (930110,NULL,'/sales',NULL,'閿€鍞鐞?,330,'ep:sell','FormalSalesRoot','erp/sale/order/index'),
  (930130,NULL,'/pmo',NULL,'缁煎悎璁″垝绠＄悊',340,'ep:data-board','FormalPmoRoot','system/post-level/overview/index'),
  (930120,NULL,'/rd','/research','鐮斿彂绠＄悊',350,'ep:cpu','FormalRdRoot','erp/rd/design/index'),
  (930140,NULL,'/scm',NULL,'渚涘簲閾剧鐞?,360,'ep:shopping-cart-full','FormalScmRoot','erp/mrp/plan-rule/index'),
  (930160,NULL,'/process',NULL,'宸ヨ壓绠＄悊',370,'ep:connection','FormalProcessRoot','erp/mrp/bom/index'),
  (930170,NULL,'/mes',NULL,'鍒堕€犳墽琛岀鐞?,380,'ep:operation','FormalMesRoot','mes/work-order/index'),
  (930150,NULL,'/qms',NULL,'璐ㄩ噺绠＄悊',390,'ep:medal','FormalQmsRoot','qms/iqc/IqcEntry'),
  (930180,NULL,'/finance',NULL,'璐㈠姟绠＄悊',400,'ep:money','FormalFinanceRoot','erp/finance/receipt/index'),
  (930190,NULL,'/hr',NULL,'浜轰簨绠＄悊',410,'ep:user','FormalHrRoot','hr/shift/index'),
  (930210,NULL,'/system',NULL,'绯荤粺绠＄悊',420,'ep:tools','FormalSystemRoot','system/user/index');

UPDATE `tmp_weitai_roots` t
LEFT JOIN (
  SELECT `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = 0
  GROUP BY `path`
) root_path_menu ON root_path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
LEFT JOIN (
  SELECT `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `parent_id` = 0
  GROUP BY `path`
) legacy_root_menu ON legacy_root_menu.`path` COLLATE utf8mb4_unicode_ci = t.`legacy_path`
LEFT JOIN (
  SELECT `component`, MIN(`parent_id`) AS `parent_id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `component` IS NOT NULL AND `component` <> ''
  GROUP BY `component`
) matched_component_menu ON matched_component_menu.`component` COLLATE utf8mb4_unicode_ci = t.`match_component`
SET t.`actual_id` = COALESCE(root_path_menu.`id`, legacy_root_menu.`id`, matched_component_menu.`parent_id`);

UPDATE `system_menu` sm
JOIN `tmp_weitai_roots` t ON t.`actual_id` = sm.`id`
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
FROM `tmp_weitai_roots` t
WHERE t.`actual_id` IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `deleted` = b'0' AND `parent_id` = 0 AND `path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_roots` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = 0
    AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_pmo_groups`;
CREATE TEMPORARY TABLE `tmp_weitai_pmo_groups` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `actual_parent_id` BIGINT NULL,
  `root_path` VARCHAR(255) NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_pmo_groups`
(`fixed_id`,`actual_id`,`actual_parent_id`,`root_path`,`path`,`name`,`sort_no`,`icon`,`component_name`)
VALUES
  (931410,NULL,NULL,'/pmo','project','椤圭洰绠＄悊',10,'ep:calendar','FormalPmoProjectGroup'),
  (931411,NULL,NULL,'/pmo','organization','缁勭粐涓庡矖浣?,20,'fa:address-card','FormalPmoOrganizationGroup'),
  (931412,NULL,NULL,'/pmo','process','娴佺▼鍗忓悓',30,'ep:connection','FormalPmoProcessGroup'),
  (931413,NULL,NULL,'/pmo','analysis','缁忚惀鍒嗘瀽',40,'ep:trend-charts','FormalPmoAnalysisGroup');

UPDATE `tmp_weitai_pmo_groups` t
JOIN `tmp_weitai_roots` r ON r.`path` COLLATE utf8mb4_unicode_ci = t.`root_path`
SET t.`actual_parent_id` = r.`actual_id`;

UPDATE `tmp_weitai_pmo_groups` t
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) path_menu ON path_menu.`parent_id` = t.`actual_parent_id` AND path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET t.`actual_id` = path_menu.`id`;

UPDATE `system_menu` sm
JOIN `tmp_weitai_pmo_groups` t
  ON sm.`parent_id` = t.`actual_parent_id`
 AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET sm.`name` = t.`name`,
    sm.`type` = 1,
    sm.`sort` = t.`sort_no`,
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

UPDATE `system_menu` sm
JOIN `tmp_weitai_pmo_groups` t ON t.`actual_id` = sm.`id`
SET sm.`name` = t.`name`,
    sm.`type` = 1,
    sm.`sort` = t.`sort_no`,
    sm.`parent_id` = t.`actual_parent_id`,
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
SELECT t.`fixed_id`, t.`name`, '', 1, t.`sort_no`, t.`actual_parent_id`, t.`path`, t.`icon`, '', t.`component_name`,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_pmo_groups` t
WHERE t.`actual_id` IS NULL
  AND t.`actual_parent_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
      AND `parent_id` = t.`actual_parent_id`
      AND `path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_pmo_groups` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = t.`actual_parent_id`
    AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_pmo_targets`;
CREATE TEMPORARY TABLE `tmp_weitai_pmo_targets` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `root_path` VARCHAR(255) NOT NULL,
  `group_path` VARCHAR(255) NOT NULL,
  `actual_parent_id` BIGINT NULL,
  `legacy_path` VARCHAR(255) NOT NULL,
  `path` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component` VARCHAR(255) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  `lookup_component` VARCHAR(255) NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_weitai_pmo_targets`
(`fixed_id`,`actual_id`,`root_path`,`group_path`,`actual_parent_id`,`legacy_path`,`path`,`name`,`sort_no`,`icon`,`component`,`component_name`,`lookup_component`)
VALUES
  (990002,NULL,'/pmo','project',NULL,'master-plan','master-plan','椤圭洰涓昏鍒?,10,'ep:calendar','pmo/project/index','FormalPmoMasterPlan','pmo/project/index'),
  (931402,NULL,'/pmo','project',NULL,'follow-up','follow-up','椤圭洰璺熻繘',20,'ep:checked',@placeholder_component,'FormalPmoFollowUp',NULL),
  (931403,NULL,'/pmo','project',NULL,'warning','warning','椤圭洰棰勮',30,'ep:warning',@placeholder_component,'FormalPmoWarning',NULL),
  (931404,NULL,'/pmo','analysis',NULL,'kpi','kpi','缁忚惀鎸囨爣鐪嬫澘',40,'ep:trend-charts','pmo/kpi/index','FormalPmoKpi','pmo/kpi/index'),
  (931405,NULL,'/pmo','organization',NULL,'org-chart','org-chart','缁勭粐鏋舵瀯',50,'ep:share','pmo/org-chart/index','FormalPmoOrgChart','pmo/org-chart/index'),
  (931406,NULL,'/pmo','organization',NULL,'post-level-overview','post-level-overview','宀椾綅灞傜骇',60,'ep:histogram','system/post-level/overview/index','FormalPmoPostLevelOverview','system/post-level/overview/index'),
  (931407,NULL,'/pmo','process',NULL,'process-monitor','process-monitor','娴佺▼鐩戞帶',70,'ep:monitor','bpm/processInstance/report/index','FormalPmoProcessMonitor','bpm/processInstance/report/index');

UPDATE `tmp_weitai_pmo_targets` t
JOIN `tmp_weitai_pmo_groups` g ON g.`path` COLLATE utf8mb4_unicode_ci = t.`group_path`
SET t.`actual_parent_id` = g.`actual_id`;

UPDATE `system_menu` sm
JOIN `tmp_weitai_pmo_targets` t
  ON sm.`parent_id` = t.`actual_parent_id`
 AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET sm.`name` = t.`name`,
    sm.`type` = 2,
    sm.`sort` = t.`sort_no`,
    sm.`icon` = t.`icon`,
    sm.`component` = t.`component`,
    sm.`component_name` = t.`component_name`,
    sm.`status` = 0,
    sm.`visible` = b'1',
    sm.`keep_alive` = b'1',
    sm.`always_show` = b'1',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0';

UPDATE `tmp_weitai_pmo_targets` t
LEFT JOIN (
  SELECT `component`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `component` IS NOT NULL AND `component` <> ''
  GROUP BY `component`
) component_menu ON component_menu.`component` COLLATE utf8mb4_unicode_ci = t.`lookup_component`
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) path_menu ON path_menu.`parent_id` = t.`actual_parent_id` AND path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
LEFT JOIN `tmp_weitai_roots` root_menu ON root_menu.`path` COLLATE utf8mb4_unicode_ci = t.`root_path`
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) legacy_path_menu ON legacy_path_menu.`parent_id` = root_menu.`actual_id` AND legacy_path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`legacy_path`
SET t.`actual_id` = COALESCE(component_menu.`id`, path_menu.`id`, legacy_path_menu.`id`);

UPDATE `system_menu` sm
JOIN `tmp_weitai_pmo_targets` t ON t.`actual_id` = sm.`id`
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
WHERE sm.`deleted` = b'0';

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT t.`name`, '', 2, t.`sort_no`, t.`actual_parent_id`, t.`path`, t.`icon`, t.`component`,
       t.`component_name`, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_pmo_targets` t
WHERE t.`actual_id` IS NULL
  AND t.`actual_parent_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
  WHERE `deleted` = b'0'
      AND `parent_id` = t.`actual_parent_id`
      AND `path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_pmo_targets` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = t.`actual_parent_id`
    AND sm.`path` = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_menu_targets`;
CREATE TEMPORARY TABLE `tmp_weitai_menu_targets` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `root_path` VARCHAR(255) NOT NULL,
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

INSERT INTO `tmp_weitai_menu_targets`
(`fixed_id`,`actual_id`,`root_path`,`actual_parent_id`,`path`,`name`,`sort_no`,`icon`,`component`,`component_name`,`lookup_component`)
VALUES
  (931101,NULL,'/project',NULL,'project','椤圭洰鍙拌处',10,'ep:document','erp/project/project/index','FormalProjectCenter','erp/project/project/index'),
  (931102,NULL,'/project',NULL,'lifecycle','椤圭洰鐢熷懡鍛ㄦ湡',20,'ep:office-building',@placeholder_component,'FormalProjectLifecycle','pmo/project/index'),
  (931103,NULL,'/project',NULL,'progress','椤圭洰杩涘害瑙嗗浘',30,'ep:histogram',@placeholder_component,'FormalProjectProgress',NULL),
  (931104,NULL,'/project',NULL,'archive','椤圭洰璧勬枡褰掓。',40,'ep:folder-opened',@placeholder_component,'FormalProjectArchive',NULL),
  (931105,NULL,'/project',NULL,'activity','椤圭洰鍔ㄦ€佽褰?,50,'ep:chat-dot-round',@placeholder_component,'FormalProjectActivity',NULL),
  (931106,NULL,'/project',NULL,'cost','椤圭洰鎴愭湰瑙嗗浘',60,'ep:money',@placeholder_component,'FormalProjectCostView',NULL),

  (931201,NULL,'/master-data',NULL,'product','浜у搧涓绘。',10,'fa-solid:apple-alt','erp/product/product/index','FormalMasterProduct','erp/product/product/index'),
  (931202,NULL,'/master-data',NULL,'material','鐗╂枡涓绘。',20,'ep:box',@placeholder_component,'FormalMasterMaterial',NULL),
  (931203,NULL,'/master-data',NULL,'product-category','浜у搧鍒嗙被',30,'fa:certificate','erp/product/category/index','FormalMasterProductCategory','erp/product/category/index'),
  (931204,NULL,'/master-data',NULL,'material-category','鐗╂枡鍒嗙被',40,'ep:collection',@placeholder_component,'FormalMasterMaterialCategory',NULL),
  (931205,NULL,'/master-data',NULL,'unit','鍗曚綅涓庢崲绠?,50,'ep:opportunity','erp/product/unit/index','FormalMasterUnit','erp/product/unit/index'),
  (931206,NULL,'/master-data',NULL,'coding-rule','缂栫爜瑙勫垯',60,'ep:key',@placeholder_component,'FormalMasterCodingRule',NULL),

  (931301,NULL,'/sales',NULL,'contract','瀹㈡埛涓庡悎鍚岃瘎瀹?,10,'ep:document-checked','crm/contract/index','FormalSalesContract','crm/contract/index'),
  (931302,NULL,'/sales',NULL,'project-initiation','閿€鍞」鐩珛椤?,20,'ep:flag',@placeholder_component,'FormalSalesProjectInitiation',NULL),
  (931303,NULL,'/sales',NULL,'order','閿€鍞鍗曞彴璐?,30,'ep:list','erp/sale/order/index','FormalSalesOrder','erp/sale/order/index'),
  (931304,NULL,'/sales',NULL,'delivery','鍙戣揣閫氱煡涓庣鏀?,40,'ep:van','erp/sale/out/index','FormalSalesDelivery','erp/sale/out/index'),
  (931305,NULL,'/sales',NULL,'customer-supplied-material','瀹緵鏂欎笟鍔¤窡韪?,50,'ep:box',@placeholder_component,'FormalSalesCustomerSuppliedMaterial',NULL),
  (931306,NULL,'/sales',NULL,'incoming-processing','鏉ユ枡鍔犲伐涓氬姟璺熻釜',60,'ep:setting',@placeholder_component,'FormalSalesIncomingProcessing',NULL),

  (931501,NULL,'/rd',NULL,'requirement-review','闇€姹備笌绔嬮」璇勫',10,'ep:document',@placeholder_component,'FormalRdRequirementReview',NULL),
  (931502,NULL,'/rd',NULL,'design','璁捐涓績',20,'ep:edit-pen','erp/rd/design/index','FormalRdDesign','erp/rd/design/index'),
  (931503,NULL,'/rd',NULL,'rd-bom','鐮斿彂 BOM',30,'ep:collection-tag','erp/rd/rd-bom/index','FormalRdBom','erp/rd/rd-bom/index'),
  (931504,NULL,'/rd',NULL,'bom','鏍囧噯 BOM',40,'ep:collection','erp/rd/bom/index','FormalStandardBom','erp/rd/bom/index'),
  (931505,NULL,'/rd',NULL,'design-change','璁捐鍙樻洿',50,'ep:refresh',@placeholder_component,'FormalRdDesignChange',NULL),
  (931506,NULL,'/rd',NULL,'document','鐮斿彂鏂囨。',60,'ep:folder-opened',@placeholder_component,'FormalRdDocument','erp/rd/document/index'),
  (931507,NULL,'/rd',NULL,'follow-up','鐮斿彂璺熻繘',70,'ep:checked',@placeholder_component,'FormalRdFollowUp',NULL),
  (931508,NULL,'/rd',NULL,'warning','鐮斿彂棰勮',80,'ep:warning',@placeholder_component,'FormalRdWarning',NULL),

  (931601,NULL,'/scm',NULL,'plan-rule','璁″垝鍙傛暟',10,'ep:setting','erp/mrp/plan-rule/index','FormalScmPlanRule','erp/mrp/plan-rule/index'),
  (931602,NULL,'/scm',NULL,'plan','MRP 璁″垝',20,'ep:calendar','erp/mrp/plan/index','FormalScmPlan','erp/mrp/plan/index'),
  (931603,NULL,'/scm',NULL,'suggest','MRP 杩愮畻',30,'ep:histogram','erp/mrp/suggest/index','FormalScmSuggest','erp/mrp/suggest/index'),
  (931604,NULL,'/scm',NULL,'purchase-request','閲囪喘闇€姹?,40,'ep:tickets',@placeholder_component,'FormalScmPurchaseRequest',NULL),
  (931605,NULL,'/scm',NULL,'purchase-order','閲囪喘璁㈠崟鍙拌处',50,'ep:shopping-trolley','erp/purchase/order/index','FormalScmPurchaseOrder','erp/purchase/order/index'),
  (931606,NULL,'/scm',NULL,'inbound','鏀惰揣鍏ュ簱',60,'ep:box','erp/purchase/in/index','FormalScmInbound','erp/purchase/in/index'),
  (931607,NULL,'/scm',NULL,'stock-occupancy','搴撳瓨鍗犵敤璺熻釜',70,'ep:goods','erp/mrp/stock-reservation/index','FormalScmStockOccupancy','erp/mrp/stock-reservation/index'),
  (931608,NULL,'/scm',NULL,'substitute-material','鏇夸唬鏂欑鐞?,80,'ep:switch','erp/mrp/substitute/index','FormalScmSubstituteMaterial','erp/mrp/substitute/index'),

  (931701,NULL,'/process',NULL,'bom','鍒堕€?BOM',10,'ep:collection','erp/mrp/bom/index','FormalProcessManufactureBom','erp/mrp/bom/index'),
  (931702,NULL,'/process',NULL,'process-spec','宸ヨ壓瑙勮寖',20,'ep:document-copy',@placeholder_component,'FormalProcessSpec',NULL),
  (931703,NULL,'/process',NULL,'work-instruction','浣滀笟鎸囧涔?,30,'ep:reading',@placeholder_component,'FormalProcessInstruction',NULL),
  (931704,NULL,'/process',NULL,'route','宸ヨ壓璺嚎',40,'ep:share',@placeholder_component,'FormalProcessRoute','erp/manufacturing/process-route/index'),
  (931705,NULL,'/process',NULL,'flow-card','宸ヨ壓娴佽浆鍗?,50,'ep:postcard',@placeholder_component,'FormalProcessFlowCard',NULL),
  (931706,NULL,'/process',NULL,'review','宸ヨ壓璇勫',60,'ep:checked',@placeholder_component,'FormalProcessReview',NULL),
  (931707,NULL,'/process',NULL,'validation','宸ヨ壓楠岃瘉椤圭洰',70,'ep:select',@placeholder_component,'FormalProcessValidation',NULL),
  (931708,NULL,'/process',NULL,'work-center','宸ヤ綔涓績',80,'ep:office-building',@placeholder_component,'FormalProcessWorkCenter','erp/manufacturing/work-center/index'),

  (931801,NULL,'/mes',NULL,'plan-execution','鐢熶骇璁″垝鎵ц',10,'ep:calendar',@placeholder_component,'FormalMesPlanExecution',NULL),
  (931802,NULL,'/mes',NULL,'work-order','宸ュ崟涓庡垎鍗?,20,'ep:tickets',@placeholder_component,'FormalMesWorkOrder','mes/work-order/index'),
  (931803,NULL,'/mes',NULL,'report','鎶ュ伐绠＄悊',30,'ep:histogram',@placeholder_component,'FormalMesReport','erp/manufacturing/production-report/index'),
  (931804,NULL,'/mes',NULL,'material-movement','棰嗘枡閫€鏂?,40,'ep:box',@placeholder_component,'FormalMesMaterialMovement','erp/manufacturing/material-issue/index'),
  (931805,NULL,'/mes',NULL,'barcode-trace','鏉＄爜杩芥函',50,'ep:price-tag',@placeholder_component,'FormalMesBarcodeTrace',NULL),
  (931806,NULL,'/mes',NULL,'equipment','璁惧缁存姢',60,'ep:cpu',@placeholder_component,'FormalMesEquipment','erp/manufacturing/device/index'),
  (931807,NULL,'/mes',NULL,'outsource-process','濮斿宸ュ簭绠＄悊',70,'ep:connection',@placeholder_component,'FormalMesOutsourceProcess',NULL),
  (931808,NULL,'/mes',NULL,'abnormal-rework','鐢熶骇寮傚父涓庤繑淇?,80,'ep:warning-filled',@placeholder_component,'FormalMesAbnormalRework',NULL),

  (931901,NULL,'/qms',NULL,'iqc','鏉ユ枡妫€楠?,10,'ep:finished','qms/iqc/IqcEntry','FormalQmsIqc','qms/iqc/IqcEntry'),
  (931902,NULL,'/qms',NULL,'ipqc','杩囩▼妫€楠?,20,'ep:aim',@placeholder_component,'FormalQmsIpqc','qms/ipqc/index'),
  (931903,NULL,'/qms',NULL,'oqc','瀹屽伐涓庡嚭璐ф楠?,30,'ep:circle-check',@placeholder_component,'FormalQmsOqc','qms/oqc/index'),
  (931904,NULL,'/qms',NULL,'report-approval','妫€楠屾姤鍛婂鎵?,40,'ep:document-checked',@placeholder_component,'FormalQmsReportApproval',NULL),
  (931905,NULL,'/qms',NULL,'quality-issue','璐ㄩ噺寮傚父',50,'ep:warn-triangle-filled',@placeholder_component,'FormalQmsIssue',NULL),
  (931906,NULL,'/qms',NULL,'certificate','璐ㄩ噺璇佹槑鏂囦欢',60,'ep:medal',@placeholder_component,'FormalQmsCertificate',NULL),

  (932001,NULL,'/finance',NULL,'project-cost','椤圭洰鎴愭湰',10,'ep:money',@placeholder_component,'FormalFinanceProjectCost',NULL),
  (932002,NULL,'/finance',NULL,'production-cost','鐢熶骇鎴愭湰',20,'ep:coin',@placeholder_component,'FormalFinanceProductionCost',NULL),
  (932003,NULL,'/finance',NULL,'apar','搴旀敹搴斾粯',30,'ep:credit-card',@placeholder_component,'FormalFinanceApar','erp/finance/apar/index'),
  (932004,NULL,'/finance',NULL,'assets','鍥哄畾璧勪骇',40,'ep:office-building',@placeholder_component,'FormalFinanceAssets','erp/finance/assets/index'),
  (932005,NULL,'/finance',NULL,'statement','璐㈠姟鎶ヨ〃',50,'ep:document',@placeholder_component,'FormalFinanceStatement',NULL),
  (932006,NULL,'/finance',NULL,'analysis','缁忚惀鍒嗘瀽',60,'ep:pie-chart',@placeholder_component,'FormalFinanceAnalysis','erp/finance/cost/index'),

  (932101,NULL,'/hr',NULL,'employee-archive','鍛樺伐妗ｆ',10,'ep:files',@placeholder_component,'FormalHrArchive','hr/archive/index'),
  (932102,NULL,'/hr',NULL,'onboarding-offboarding','鍏ョ鑱屾祦绋?,20,'ep:switch-button',@placeholder_component,'FormalHrOnboardingOffboarding',NULL),
  (932103,NULL,'/hr',NULL,'appraisal','缁╂晥鑰冩牳',30,'ep:data-analysis',@placeholder_component,'FormalHrAppraisal','hr/appraisal/index'),
  (932104,NULL,'/hr',NULL,'shift','鑰冨嫟鐝',40,'ep:calendar','hr/shift/index','FormalHrShift','hr/shift/index'),
  (932105,NULL,'/hr',NULL,'salary','钖叕绠＄悊',50,'ep:wallet','hr/salary/index','FormalHrSalary','hr/salary/index'),
  (932106,NULL,'/hr',NULL,'recruitment','鎷涜仒绠＄悊',60,'ep:user-filled',@placeholder_component,'FormalHrRecruitment',NULL),

  (932201,NULL,'/system',NULL,'org','缁勭粐鏋舵瀯缁存姢',10,'fa:address-card','system/dept/index','FormalSystemOrg','system/dept/index'),
  (932202,NULL,'/system',NULL,'post','宀椾綅缁存姢',20,'ep:briefcase','system/post/index','FormalSystemPost','system/post/index'),
  (932203,NULL,'/system',NULL,'user-role','鐢ㄦ埛涓庤鑹?,30,'ep:user','system/user/index','FormalSystemUserRole','system/user/index'),
  (932204,NULL,'/system',NULL,'menu-permission','鑿滃崟涓庢潈闄?,40,'ep:menu','system/menu/index','FormalSystemMenuPermission','system/menu/index'),
  (932205,NULL,'/system',NULL,'dictionary-config','瀛楀吀涓庡弬鏁?,50,'ep:collection','system/dict/index','FormalSystemDictConfig','system/dict/index'),
  (932206,NULL,'/system',NULL,'workflow-config','娴佺▼閰嶇疆',60,'fa:medium','bpm/model/index','FormalSystemWorkflowConfig','bpm/model/index'),
  (932207,NULL,'/system',NULL,'audit-log','瀹¤鏃ュ織',70,'ep:document-copy','system/operatelog/index','FormalSystemAuditLog','system/operatelog/index');

UPDATE `tmp_weitai_menu_targets` t
JOIN `tmp_weitai_roots` r ON r.`path` COLLATE utf8mb4_unicode_ci = t.`root_path`
SET t.`actual_parent_id` = r.`actual_id`;

UPDATE `tmp_weitai_menu_targets` t
LEFT JOIN (
  SELECT `component`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0' AND `component` IS NOT NULL AND `component` <> ''
  GROUP BY `component`
) component_menu ON component_menu.`component` COLLATE utf8mb4_unicode_ci = t.`lookup_component`
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) path_menu ON path_menu.`parent_id` = t.`actual_parent_id` AND path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET t.`actual_id` = COALESCE(component_menu.`id`, path_menu.`id`);

UPDATE `system_menu` sm
JOIN `tmp_weitai_menu_targets` t ON t.`actual_id` = sm.`id`
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
WHERE sm.`deleted` = b'0';

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT t.`fixed_id`, t.`name`, '', 2, t.`sort_no`, t.`actual_parent_id`, t.`path`, t.`icon`, t.`component`,
       t.`component_name`, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_weitai_menu_targets` t
WHERE t.`actual_id` IS NULL
  AND t.`actual_parent_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `deleted` = b'0'
      AND `parent_id` = t.`actual_parent_id`
      AND `path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `tmp_weitai_menu_targets` t
SET t.`actual_id` = (
  SELECT sm.`id`
  FROM `system_menu` sm
  WHERE sm.`deleted` = b'0'
    AND sm.`parent_id` = t.`actual_parent_id`
    AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  ORDER BY CASE WHEN sm.`id` = t.`fixed_id` THEN 0 ELSE 1 END, sm.`id`
  LIMIT 1
);

UPDATE `system_menu` sm
LEFT JOIN `tmp_weitai_roots` t ON t.`actual_id` = sm.`id`
SET sm.`status` = 1,
    sm.`visible` = b'0',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0'
  AND sm.`parent_id` = 0
  AND t.`actual_id` IS NULL
  AND sm.`path` IN (
    '/infra',
    '/project', '/master-data', '/sales', '/pmo', '/rd', '/research', '/scm', '/process',
    '/mes', '/qms', '/finance', '/hr', '/system', '/erp', '/mrp', '/manufacturing',
    '/pay', '/report', '/bpm', '/member', '/mall', '/mp', '/crm', '/ai', '/iot'
  );

UPDATE `system_menu` sm
LEFT JOIN `tmp_weitai_menu_targets` t ON t.`actual_id` = sm.`id`
JOIN `system_menu` root_parent
  ON root_parent.`id` = sm.`parent_id`
 AND root_parent.`deleted` = b'0'
 AND root_parent.`parent_id` = 0
 AND root_parent.`path` IN (
   '/project', '/master-data', '/sales', '/pmo', '/rd', '/scm',
   '/process', '/mes', '/qms', '/finance', '/hr', '/system'
 )
SET sm.`status` = 1,
    sm.`visible` = b'0',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0'
  AND t.`actual_id` IS NULL
  AND NOT (
    root_parent.`path` = '/pmo'
    AND sm.`path` IN ('project', 'organization', 'process', 'analysis')
  );

UPDATE `system_menu` sm
JOIN `tmp_weitai_menu_targets` t ON t.`lookup_component` IS NOT NULL AND sm.`component` COLLATE utf8mb4_unicode_ci = t.`lookup_component`
SET sm.`status` = 1,
    sm.`visible` = b'0',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0'
  AND sm.`type` = 2
  AND sm.`id` <> t.`actual_id`;

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, sm.`id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `system_menu` sm
LEFT JOIN `system_menu` self_root
  ON self_root.`id` = sm.`id`
 AND self_root.`deleted` = b'0'
 AND self_root.`parent_id` = 0
 AND self_root.`path` IN (
   '/project', '/master-data', '/sales', '/pmo', '/rd', '/scm',
   '/process', '/mes', '/qms', '/finance', '/hr', '/system'
 )
LEFT JOIN `system_menu` parent_root
  ON parent_root.`id` = sm.`parent_id`
 AND parent_root.`deleted` = b'0'
 AND parent_root.`parent_id` = 0
 AND parent_root.`path` IN (
   '/project', '/master-data', '/sales', '/pmo', '/rd', '/scm',
   '/process', '/mes', '/qms', '/finance', '/hr', '/system'
 )
WHERE sm.`deleted` = b'0'
  AND sm.`status` = 0
  AND (self_root.`id` IS NOT NULL OR parent_root.`id` IS NOT NULL)
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = sm.`id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT DISTINCT rm.`role_id`, r.`actual_id`, '1', NOW(), '1', NOW(), b'0', rm.`tenant_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
JOIN `tmp_weitai_roots` r ON sm.`parent_id` = r.`actual_id`
WHERE rm.`deleted` = b'0'
  AND sm.`deleted` = b'0'
  AND sm.`status` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` existing
    WHERE existing.`deleted` = b'0'
      AND existing.`role_id` = rm.`role_id`
      AND existing.`tenant_id` = rm.`tenant_id`
      AND existing.`menu_id` = r.`actual_id`
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_menu_targets`;
DROP TEMPORARY TABLE IF EXISTS `tmp_weitai_roots`;

SET FOREIGN_KEY_CHECKS = 1;
