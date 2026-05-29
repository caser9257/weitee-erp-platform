/*
 Target: restore finance voucher template menu and permissions under finance
 Schema: ruoyi-vue-pro
 Date: 2026-05-26
 Scope:
   1. Restore /finance -> voucher-template page menu if missing
   2. Restore voucher-template permission menus if missing
   3. Grant admin and finance roles these menus
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;

SET @erp_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (id = 2563 OR path = '/erp' OR name = 'ERP 系统')
    ORDER BY id
    LIMIT 1
);

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
        OR component_name IN ('ProjectFinanceRoot', 'FormalFinanceRoot')
        OR (parent_id = @erp_root_id AND path = 'finance')
        OR name = '财务管理'
      )
    ORDER BY
      CASE
        WHEN path = '/finance' THEN 0
        WHEN component_name = 'FormalFinanceRoot' THEN 1
        ELSE 2
      END,
      id
    LIMIT 1
);

SET @voucher_template_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/voucher-template/index'
        OR component_name IN ('ErpFinanceVoucherTemplate', 'FormalFinanceVoucherTemplate')
        OR (parent_id = @finance_root_id AND path = 'voucher-template')
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
    '凭证模板', '', 2, 95, @finance_root_id, 'voucher-template', 'ep:files',
    'erp/finance/voucher-template/index', 'ErpFinanceVoucherTemplate',
    0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @voucher_template_page_id IS NULL;

SET @voucher_template_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/voucher-template/index'
        OR component_name IN ('ErpFinanceVoucherTemplate', 'FormalFinanceVoucherTemplate')
        OR (parent_id = @finance_root_id AND path = 'voucher-template')
      )
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT base.next_id + t.offset_id - 1, t.name, t.permission, 3, t.sort_no, @voucher_template_page_id, '', '', '', NULL,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM (
    SELECT 1 AS offset_id, 1 AS sort_no, '凭证模板查询' AS name, 'erp:finance-voucher-template:query' AS permission
    UNION ALL SELECT 2, 2, '凭证模板创建', 'erp:finance-voucher-template:create'
    UNION ALL SELECT 3, 3, '凭证模板更新', 'erp:finance-voucher-template:update'
    UNION ALL SELECT 4, 4, '凭证模板删除', 'erp:finance-voucher-template:delete'
) t
JOIN (
    SELECT COALESCE(MAX(id), 0) + 1 AS next_id
    FROM system_menu
) base
WHERE @voucher_template_page_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM system_menu sm
    WHERE sm.deleted = b'0'
      AND sm.permission = t.permission
  );

UPDATE system_menu
SET parent_id = @voucher_template_page_id,
    status = 0,
    visible = b'1',
    updater = '1',
    update_time = NOW()
WHERE deleted = b'0'
  AND permission IN (
    'erp:finance-voucher-template:query',
    'erp:finance-voucher-template:create',
    'erp:finance-voucher-template:update',
    'erp:finance-voucher-template:delete'
  )
  AND @voucher_template_page_id IS NOT NULL;

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT DISTINCT role_ids.role_id, menu.id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
    SELECT 1 AS role_id
    UNION
    SELECT id
    FROM system_role
    WHERE deleted = b'0'
      AND code IN ('super_admin', 'admin', 'erp_finance_clerk', 'erp_finance_manager', 'erp_finance_purchase_collab')
) role_ids
JOIN system_menu menu
  ON menu.deleted = b'0'
 AND (
      menu.id = @erp_root_id
      OR menu.id = @finance_root_id
      OR menu.id = @voucher_template_page_id
      OR menu.parent_id = @voucher_template_page_id
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
