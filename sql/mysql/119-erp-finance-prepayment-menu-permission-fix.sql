/*
 Target: restore prepayment menu and permissions under finance
 Schema: ruoyi-vue-pro
 Date: 2026-05-26
 Scope:
   1. Restore /finance -> prepayment page menu if missing
   2. Restore prepayment permission menus if missing
   3. Grant admin and finance test roles these menus
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
        OR name = '财务管理'
        OR component_name IN ('ProjectFinanceRoot', 'FormalFinanceRoot')
      )
    ORDER BY id
    LIMIT 1
);

SET @prepayment_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/prepayment/index'
        OR component_name IN ('ErpFinancePrepayment', 'FormalFinancePrepayment')
        OR (parent_id = @finance_root_id AND path = 'prepayment')
      )
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
    COALESCE((SELECT MAX(id) + 1 FROM system_menu), 1),
    '预付款', '', 2, 115, @finance_root_id, 'prepayment', 'ep:wallet-filled', 'erp/finance/prepayment/index', 'ErpFinancePrepayment',
    0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @prepayment_page_id IS NULL;

SET @prepayment_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/prepayment/index'
        OR component_name IN ('ErpFinancePrepayment', 'FormalFinancePrepayment')
        OR (parent_id = @finance_root_id AND path = 'prepayment')
      )
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT base.next_id + t.offset_id - 1, t.name, t.permission, 3, t.sort_no, @prepayment_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM (
    SELECT 1 AS offset_id, 1 AS sort_no, '预付款查询' AS name, 'erp:finance-prepayment:query' AS permission
    UNION ALL SELECT 2, 2, '预付款创建', 'erp:finance-prepayment:create'
    UNION ALL SELECT 3, 3, '预付款更新', 'erp:finance-prepayment:update'
    UNION ALL SELECT 4, 4, '预付款删除', 'erp:finance-prepayment:delete'
    UNION ALL SELECT 5, 5, '预付款审批', 'erp:finance-prepayment:update-status'
) t
JOIN (
    SELECT COALESCE(MAX(id), 0) + 1 AS next_id
    FROM system_menu
) base
WHERE @prepayment_page_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM system_menu sm
    WHERE sm.deleted = b'0'
      AND sm.permission = t.permission
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT DISTINCT role_ids.role_id, menu.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT 1 AS role_id
    UNION
    SELECT id
    FROM system_role
    WHERE deleted = b'0'
      AND code IN ('erp_finance_clerk', 'erp_finance_manager', 'erp_finance_purchase_collab')
) role_ids
JOIN system_menu menu
  ON menu.deleted = b'0'
 AND (
      menu.id = @finance_root_id
      OR menu.id = @prepayment_page_id
      OR menu.parent_id = @prepayment_page_id
     )
WHERE NOT EXISTS (
    SELECT 1
    FROM system_role_menu rm
    WHERE rm.role_id = role_ids.role_id
      AND rm.menu_id = menu.id
      AND rm.tenant_id = @tenant_id
      AND rm.deleted = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
