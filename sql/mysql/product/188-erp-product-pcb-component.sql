/*
 * PCB 元器件标识：仅标识为 PCB 元器件的物料进入 Cadence ODBC 视图，
 * 并在研发 BOM 导入时强制校验原理图库符号与 PCB 封装。
 */
SET NAMES utf8mb4;

ALTER TABLE `erp_product`
  ADD COLUMN `is_pcb_component` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否PCB元器件' AFTER `asset_flag`;

/* 已维护完整 Cadence 数据的存量物料自动归类为 PCB 元器件。 */
UPDATE `erp_product` p
INNER JOIN `erp_product_cadence` cadence
  ON cadence.`product_id` = p.`id` AND cadence.`deleted` = b'0'
SET p.`is_pcb_component` = b'1'
WHERE p.`deleted` = b'0'
  AND NULLIF(TRIM(cadence.`schematic_part`), '') IS NOT NULL
  AND NULLIF(TRIM(cadence.`pcb_footprint`), '') IS NOT NULL;

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
