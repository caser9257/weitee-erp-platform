-- Keep the finance experience account able to repair and verify generated vouchers.
-- This migration is idempotent and only adds missing menu permissions/grants.

SET NAMES utf8mb4;

SET @erp_finance_voucher_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
          component = 'erp/finance/voucher/index'
          OR component_name IN ('ErpFinanceVoucher', 'FormalFinanceVoucher')
      )
    ORDER BY id
    LIMIT 1
);
SET @erp_finance_permission_next_id := (SELECT COALESCE(MAX(id), 0) + 1 FROM system_menu);

INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @erp_finance_permission_next_id, 'Voucher create', 'erp:finance-voucher:create', 3, 2,
       @erp_finance_voucher_page_id, '', '', '', '', 0, b'1', b'1', b'0', 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @erp_finance_voucher_page_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher:create'
  );

SET @erp_finance_permission_next_id := @erp_finance_permission_next_id + 1;
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @erp_finance_permission_next_id, 'Voucher update', 'erp:finance-voucher:update', 3, 3,
       @erp_finance_voucher_page_id, '', '', '', '', 0, b'1', b'1', b'0', 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @erp_finance_voucher_page_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu WHERE deleted = b'0' AND permission = 'erp:finance-voucher:update'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT role.id, menu.id, 'codex', NOW(), 'codex', NOW(), b'0'
FROM system_role role
JOIN system_menu menu
  ON menu.deleted = b'0'
 AND menu.permission IN ('erp:finance-voucher:create', 'erp:finance-voucher:update')
WHERE role.deleted = b'0'
  AND role.code = 'erp_finance_manager'
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu existing
      WHERE existing.role_id = role.id
        AND existing.menu_id = menu.id
        AND existing.deleted = b'0'
  );
