-- 修复销售订单审批场景（erp.sale.order.submit）被软删除的问题
-- 背景：weitee-erp_backup_20260709.sql 中该场景 deleted=1，导致
--       /erp/sale-order/submit 回落到 FAILED 而不是创建 Flowable 流程实例。
--       post-import-fixes.sql 已恢复采购场景(id=4)但遗漏销售场景(id=3)，
--       本脚本补齐同类修复。
-- 执行时机：在任何销售订单 submit 之前执行。幂等。
USE `weitee-erp`;

-- 1. 恢复销售审批场景
UPDATE bpm_approval_scene
SET active_scheme_id = 4,
    status = 1,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 3
  AND scene_code = 'erp.sale.order.submit';

-- 2. 关联销售方案（v2，id=4）与场景
UPDATE bpm_approval_scheme
SET scene_id = 3,
    active_version_id = 4,
    latest_version_id = 4,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 4
  AND code = 'erp.sale.order.submit.scheme.v2';

-- 3. 激活销售方案 v2 版本
UPDATE bpm_approval_scheme_version
SET status = 30,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 4
  AND scheme_id = 4;

-- 4. 销售审批规则指向正式流程 erp_sale_order
UPDATE bpm_approval_rule
SET process_json = 'erp_sale_order',
    enabled = b'1',
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 3
  AND scheme_version_id = 4;

-- 5. 清理销售流程的租户 ID（Flowable 租户机制不需要）
UPDATE ACT_RE_DEPLOYMENT AS deployment
INNER JOIN ACT_RE_PROCDEF AS procdef
  ON procdef.DEPLOYMENT_ID_ = deployment.ID_
SET deployment.TENANT_ID_ = ''
WHERE procdef.KEY_ = 'erp_sale_order';

UPDATE ACT_RE_PROCDEF
SET TENANT_ID_ = ''
WHERE KEY_ = 'erp_sale_order';

-- 6. 补审批权限点：bpm:task:update（id=1222，标准库存在但备份库缺失）
--    审批人（部门领导/总经理）完成审批动作依赖该权限，缺失时审批中心报 Access Denied
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
VALUES (1222, '流程任务的更新', 'bpm:task:update', 3, 2, 1207, '', '', '', NULL,
        0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE deleted = b'0';

-- 7. 授权审批权限给体验角色（幂等，仅补充缺失项）
--    部门领导审批人角色：erp_sale_order_manager_approver（910203）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, m.id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN system_menu m ON m.id IN (3000, 3001, 3002, 3003, 1222) AND m.deleted = b'0'
WHERE r.code = 'erp_sale_order_manager_approver'
  AND r.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM system_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = b'0');

--    统一审批角色：erp_approval_experience（approver01/970207）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, m.id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN system_menu m ON m.id IN (3000, 3001, 3002, 3003, 1222) AND m.deleted = b'0'
WHERE r.code = 'erp_approval_experience'
  AND r.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM system_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = b'0');