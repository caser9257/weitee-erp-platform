/*
   存量数据修复：研发/制造 BOM 明细的 reference_designator 混入「物料位置」描述文本。
   背景：两类原始 Excel（FM-RR-RD004 模块物料清单=仅物料位置列；研发物料清单=仅元件位号列）
   在 position 列诞生（180 号迁移）之前，位置文本只能整列落进 reference_designator。
   规则：位号字段含中文 → 整段搬入 position（原值为空时），reference_designator 置空；
        纯 ASCII 位号形态（R1,C2,U3 / R101-105）保持不动。
   幂等：搬移后 reference_designator 为 NULL，重复执行不产生变化。
 */
SET NAMES utf8mb4;

UPDATE `erp_rd_bom_item`
SET `position` = `reference_designator`,
    `reference_designator` = NULL
WHERE `position` IS NULL
  AND `reference_designator` IS NOT NULL
  AND `reference_designator` REGEXP '[一-龥]';

UPDATE `erp_bom_item`
SET `position` = `reference_designator`,
    `reference_designator` = NULL
WHERE `position` IS NULL
  AND `reference_designator` IS NOT NULL
  AND `reference_designator` REGEXP '[一-龥]';
