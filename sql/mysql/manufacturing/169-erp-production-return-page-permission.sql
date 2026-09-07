/*
  生产退料分页查询权限
  将历史种子的旧权限统一为生产退料权限，并为已有退料创建权限的角色补齐查询权限。
*/

SET NAMES utf8mb4;

SET @return_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/manufacturing/material-return/index'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

UPDATE system_menu
SET permission = 'erp:production-material-return:query',
    updater = 'codex',
    update_time = NOW()
WHERE parent_id = @return_menu_id
  AND permission = 'erp:material-return:query'
  AND deleted = b'0';

UPDATE system_menu
SET permission = 'erp:production-material-return:create',
    updater = 'codex',
    update_time = NOW()
WHERE parent_id = @return_menu_id
  AND permission = 'erp:material-return:create'
  AND deleted = b'0';

SET @return_query_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @return_menu_id
      AND permission = 'erp:production-material-return:query'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @next_menu_id := (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu);

INSERT INTO system_menu
(id, name, permission, type, sort, parent_id, path, icon, component, component_name,
 status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @next_menu_id, '退料查询', 'erp:production-material-return:query', 3, 1, @return_menu_id,
       '', '', '', '', 0, b'1', b'1', b'1', 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @return_menu_id IS NOT NULL
  AND @return_query_menu_id IS NULL;

SET @return_query_menu_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = @return_menu_id
      AND permission = 'erp:production-material-return:query'
      AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT IGNORE INTO system_role_menu (role_id, menu_id)
SELECT DISTINCT role_menu.role_id, @return_query_menu_id
FROM system_role_menu role_menu
INNER JOIN system_menu menu ON menu.id = role_menu.menu_id
WHERE role_menu.deleted = b'0'
  AND menu.deleted = b'0'
  AND menu.permission = 'erp:production-material-return:create'
  AND @return_query_menu_id IS NOT NULL;
