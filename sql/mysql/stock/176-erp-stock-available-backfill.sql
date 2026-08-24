/*
  可用库存存量对齐：available_count 历史无维护链路（恒 0 或与 count 不一致），
  自本版本起：过检入库增加 available、生产发料扣减 available。
  本脚本一次性将存量对齐为 available_count = count，此后由业务链路增量维护。
  幂等：等值更新，重复执行无副作用；仅处理未删除记录
*/
SET NAMES utf8mb4;
USE `ruoyi-vue-pro`;

UPDATE `erp_stock`
SET `available_count` = `count`
WHERE `deleted` = b'0'
  AND `available_count` <> `count`;
