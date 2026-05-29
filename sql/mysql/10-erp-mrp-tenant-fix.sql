/*
 Target: ERP MRP tenant compatibility fix
 Schema: ruoyi-vue-pro
 Date: 2026-04-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `erp_bom`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_bom_tenant_id` (`tenant_id`);
UPDATE `erp_bom` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_bom_item`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_bom_item_tenant_id` (`tenant_id`);
UPDATE `erp_bom_item` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_material_plan_rule`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;
UPDATE `erp_material_plan_rule` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
ALTER TABLE `erp_material_plan_rule`
  DROP INDEX `uk_erp_material_plan_rule_product_id`,
  ADD UNIQUE KEY `uk_erp_material_plan_rule_product_id` (`tenant_id`, `product_id`);

ALTER TABLE `erp_mrp_plan`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;
UPDATE `erp_mrp_plan` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
ALTER TABLE `erp_mrp_plan`
  DROP INDEX `uk_erp_mrp_plan_plan_no`,
  ADD UNIQUE KEY `uk_erp_mrp_plan_plan_no` (`tenant_id`, `plan_no`);

ALTER TABLE `erp_mrp_demand`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_mrp_demand_tenant_id` (`tenant_id`);
UPDATE `erp_mrp_demand` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_mrp_result`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_mrp_result_tenant_id` (`tenant_id`);
UPDATE `erp_mrp_result` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_mrp_shortage`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_mrp_shortage_tenant_id` (`tenant_id`);
UPDATE `erp_mrp_shortage` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_purchase_suggest`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_purchase_suggest_tenant_id` (`tenant_id`);
UPDATE `erp_purchase_suggest` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_production_suggest`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`,
  ADD KEY `idx_erp_production_suggest_tenant_id` (`tenant_id`);
UPDATE `erp_production_suggest` SET `tenant_id` = 1 WHERE `tenant_id` = 0;

ALTER TABLE `erp_production_order`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;
UPDATE `erp_production_order` SET `tenant_id` = 1 WHERE `tenant_id` = 0;
ALTER TABLE `erp_production_order`
  DROP INDEX `uk_erp_production_order_order_no`,
  ADD UNIQUE KEY `uk_erp_production_order_order_no` (`tenant_id`, `order_no`);

SET FOREIGN_KEY_CHECKS = 1;
