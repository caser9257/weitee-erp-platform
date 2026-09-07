/*
 * Cadence 3D 模型库字段：ERP 内部以中文“3D模型”维护，
 * ODBC 视图以 Cadence 约定英文列名 3D_Lib 输出。
 */
SET NAMES utf8mb4;

ALTER TABLE `erp_product_cadence`
  ADD COLUMN IF NOT EXISTS `three_d_lib` varchar(1000) DEFAULT NULL COMMENT '3D模型' AFTER `dimension`;

CREATE OR REPLACE VIEW `v_cadence_component` AS
SELECT
  p.`material_code` AS `Part_Number`,
  category.`name` AS `Part_Type`,
  cadence.`schematic_part` AS `Schematic_Part`,
  p.`standard` AS `Value`,
  cadence.`pcb_footprint` AS `PCB_Footprint`,
  cadence.`cadence_description` AS `Description`,
  p.`packaging` AS `Package`,
  cadence.`manufacturer_part_number` AS `Part_Name`,
  p.`brand_manufacturer` AS `Manufacturer`,
  cadence.`dimension` AS `Dimension`,
  cadence.`three_d_lib` AS `3D_Lib`,
  cadence.`datasheet` AS `Datasheet`,
  cadence.`lifecycle` AS `Lifecycle`,
  cadence.`preferred_part` AS `Preferred_Part`,
  p.`quality_grade` AS `Grade`,
  cadence.`operating_temperature` AS `Operating_Temperature`,
  cadence.`mounting_type` AS `Mounting_Type`,
  cadence.`dnp` AS `DNP`,
  cadence.`imported_or_replacement` AS `Imported_or_Replacement`,
  cadence.`second_description` AS `Second_Description`,
  cadence.`third_description` AS `Third_Description`,
  cadence.`fourth_description` AS `Fourth_Description`
FROM `erp_product` p
INNER JOIN `erp_product_cadence` cadence
  ON cadence.`product_id` = p.`id` AND cadence.`deleted` = b'0'
LEFT JOIN `erp_product_category` category
  ON category.`id` = p.`category_id` AND category.`deleted` = b'0'
WHERE p.`deleted` = b'0'
  AND p.`status` = 0
  AND p.`audit_status` = 20
  AND p.`is_pcb_component` = b'1'
  AND NULLIF(TRIM(cadence.`schematic_part`), '') IS NOT NULL
  AND NULLIF(TRIM(cadence.`pcb_footprint`), '') IS NOT NULL;
