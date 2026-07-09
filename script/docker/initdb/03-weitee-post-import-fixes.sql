-- Post-import fixes for the production backup used by the local Docker stack.
-- This file is imported only when the MySQL data volume is empty.

SET NAMES utf8mb4;

UPDATE system_menu AS menu
INNER JOIN system_menu AS finance_root
  ON finance_root.id = menu.parent_id
SET menu.name = _utf8mb4 0xE7A094E58F91E68AA5E99480202F20E99BB6E6989FE98787E8B4AD,
    menu.component = 'erp/finance/expense/index',
    menu.component_name = 'FormalFinanceExpense',
    menu.status = 0,
    menu.visible = b'1',
    menu.keep_alive = b'1',
    menu.always_show = b'1',
    menu.updater = '1',
    menu.update_time = NOW()
WHERE finance_root.path = '/finance'
  AND finance_root.deleted = b'0'
  AND menu.path = 'expense'
  AND menu.deleted = b'0'
  AND menu.component = 'common/menu-placeholder/index';

-- Restore the purchase-order approval scene shipped in the production backup.
-- The backup keeps the scene logically deleted and its default rule still points
-- to an old test process key, which makes /erp/purchase-order/submit fall back
-- to FAILED instead of creating a Flowable instance.
UPDATE bpm_approval_scene
SET active_scheme_id = 5,
    status = 1,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 4
  AND scene_code = 'erp.purchase.order.submit';

UPDATE bpm_approval_scheme
SET scene_id = 4,
    active_version_id = 5,
    latest_version_id = 5,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 5
  AND code = 'erp.purchase.order.submit.scheme';

UPDATE bpm_approval_scheme_version
SET status = 30,
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 5
  AND scheme_id = 5;

UPDATE bpm_approval_rule
SET process_json = 'erp_purchase_order',
    enabled = b'1',
    deleted = b'0',
    updater = '1',
    update_time = NOW()
WHERE id = 4
  AND scheme_version_id = 5;

UPDATE ACT_RE_DEPLOYMENT AS deployment
INNER JOIN ACT_RE_PROCDEF AS procdef
  ON procdef.DEPLOYMENT_ID_ = deployment.ID_
SET deployment.TENANT_ID_ = ''
WHERE procdef.KEY_ = 'erp_purchase_order';

UPDATE ACT_RE_PROCDEF
SET TENANT_ID_ = ''
WHERE KEY_ = 'erp_purchase_order';
