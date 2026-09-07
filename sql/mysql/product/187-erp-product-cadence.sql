/*
 * Cadence 器件库扩展：产品基础字段仍由 erp_product 维护，
 * CAD 专属字段保存在一对一扩展表，并通过只读视图供 ODBC 查询。
 */
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_product_cadence` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `schematic_part` varchar(255) DEFAULT NULL COMMENT '原理图库符号',
  `pcb_footprint` varchar(255) DEFAULT NULL COMMENT 'PCB封装',
  `cadence_description` varchar(1000) DEFAULT NULL COMMENT '关键参数描述',
  `manufacturer_part_number` varchar(255) DEFAULT NULL COMMENT '厂家型号',
  `dimension` varchar(255) DEFAULT NULL COMMENT '三维尺寸',
  `datasheet` varchar(1000) DEFAULT NULL COMMENT '数据手册地址或编号',
  `lifecycle` varchar(64) DEFAULT NULL COMMENT '生命周期',
  `preferred_part` bit(1) DEFAULT NULL COMMENT '是否优选',
  `operating_temperature` varchar(255) DEFAULT NULL COMMENT '工作温度',
  `mounting_type` varchar(64) DEFAULT NULL COMMENT '安装类型',
  `dnp` bit(1) DEFAULT NULL COMMENT '空置标志',
  `imported_or_replacement` varchar(64) DEFAULT NULL COMMENT '进口/替代物料',
  `second_description` varchar(1000) DEFAULT NULL COMMENT '参数描述2',
  `third_description` varchar(1000) DEFAULT NULL COMMENT '参数描述3',
  `fourth_description` varchar(1000) DEFAULT NULL COMMENT '参数描述4',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品Cadence扩展配置';

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
  AND NULLIF(TRIM(cadence.`schematic_part`), '') IS NOT NULL
  AND NULLIF(TRIM(cadence.`pcb_footprint`), '') IS NOT NULL;

-- ODBC 专用账号仅授予 SELECT(v_cadence_component)；账号名称和密码由部署环境单独配置。
