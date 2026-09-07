/*
  测试物料：用于两段式变更/废除端到端验收
  背景：
    两段式变更/废除后端的完整链路（状态机、BPM、废除留痕）需要一条基线明确的已审批物料来走通。
    该物料纳入 seed 数据，仅供验收和新功能回归，不参与正常业务流转。
  幂等：按 material_code 判重，可重复执行。
*/
SET NAMES utf8mb4;

INSERT INTO `erp_product` (`id`, `name`, `material_code`, `bar_code`, `category_id`, `unit_id`, `status`, `audit_status`, `standard`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 999001, '两段式验收测试物料', 'MAT-TWOSTAGE-TEST-001', 'BAR-TWOSTAGE-001', 1, 1, 1, 20, 'A-V1', '两段式变更/废除端到端验收专用，请勿删除', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM `erp_product` WHERE `material_code` = 'MAT-TWOSTAGE-TEST-001' AND `deleted` = b'0');