/*
  采购入库来源批次追溯查询权限
  仅补充查询按钮权限，并授权已有供应链经理角色。
  不新增菜单入口，不改变后端接口契约。
*/

SET NAMES utf8mb4;

SET @purchase_in_menu_id := (
  SELECT parent_id
  FROM system_menu
  WHERE permission = 'erp:purchase-in:query'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

SET @query_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'erp:purchase-source-batch:query'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu
(id, name, permission, type, sort, parent_id, path, icon, component, component_name,
 status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT COALESCE(@query_menu_id, 932090), '来源批次追溯查询', 'erp:purchase-source-batch:query', 3, 7, @purchase_in_menu_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @purchase_in_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  parent_id = VALUES(parent_id),
  status = VALUES(status),
  deleted = b'0',
  updater = '1',
  update_time = NOW();

SET @query_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'erp:purchase-source-batch:query'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

INSERT IGNORE INTO system_role_menu (role_id, menu_id)
SELECT 910004, @query_menu_id
WHERE @query_menu_id IS NOT NULL;
