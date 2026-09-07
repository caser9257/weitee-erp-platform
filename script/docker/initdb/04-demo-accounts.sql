-- Demo accounts for finance and supply-chain experience.
-- This file is imported only when the MySQL data volume is empty.

SET NAMES utf8mb4;

SET @demo_password = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

-- Hide two obsolete supply-chain posts from the experience deployment.
UPDATE system_post
SET deleted = b'1',
    updater = 'codex',
    update_time = NOW()
WHERE code IN ('supply_fanxiangwen', 'supply_zahongsai')
  AND deleted = b'0';

INSERT INTO system_users (username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, creator, create_time, updater, update_time, deleted)
SELECT 'finance01', @demo_password, _utf8mb4 0xE8B4A2E58AA1E6A8A1E59D97E4BD93E9AA8CE8B4A6E58FB7, _utf8mb4 0xE794A8E4BA8EE8B4A2E58AA1E6A8A1E59D97E6ADA3E5B8B8E4BDBFE794A8E9AA8CE8AF81, 940100, '[]', '', '', 0, '', 0, 'codex', NOW(), 'codex', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_users WHERE username = 'finance01' AND deleted = b'0');

UPDATE system_users
SET password = @demo_password,
    nickname = _utf8mb4 0xE8B4A2E58AA1E6A8A1E59D97E4BD93E9AA8CE8B4A6E58FB7,
    remark = _utf8mb4 0xE794A8E4BA8EE8B4A2E58AA1E6A8A1E59D97E6ADA3E5B8B8E4BDBFE794A8E9AA8CE8AF81,
    dept_id = 940100,
    post_ids = '[]',
    status = 0,
    updater = 'codex',
    update_time = NOW()
WHERE username = 'finance01' AND deleted = b'0';

INSERT INTO system_users (username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, creator, create_time, updater, update_time, deleted)
SELECT 'scm01', @demo_password, _utf8mb4 0xE4BE9BE5BA94E993BEE4BD93E9AA8CE8B4A6E58FB7, _utf8mb4 0xE794A8E4BA8EE4BE9BE5BA94E993BEE6A8A1E59D97E58A9FE883BDE4BD93E9AA8CE9AA8CE8AF81, 920100, '[]', '', '', 0, '', 0, 'codex', NOW(), 'codex', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_users WHERE username = 'scm01' AND deleted = b'0');

UPDATE system_users
SET password = @demo_password,
    nickname = _utf8mb4 0xE4BE9BE5BA94E993BEE4BD93E9AA8CE8B4A6E58FB7,
    remark = _utf8mb4 0xE794A8E4BA8EE4BE9BE5BA94E993BEE6A8A1E59D97E58A9FE883BDE4BD93E9AA8CE9AA8CE8AF81,
    dept_id = 920100,
    post_ids = '[]',
    status = 0,
    updater = 'codex',
    update_time = NOW()
WHERE username = 'scm01' AND deleted = b'0';

INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
SELECT u.id, r.role_id, 'codex', NOW(), 'codex', NOW(), b'0'
FROM system_users u
JOIN (
    SELECT 940001 AS role_id UNION ALL
    SELECT 940002 UNION ALL
    SELECT 940003
) r
WHERE u.username = 'finance01'
  AND u.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM system_user_role ur
      WHERE ur.user_id = u.id AND ur.role_id = r.role_id AND ur.deleted = b'0'
  );

-- The finance supervisor needs the voucher state-transition actions used by
-- the acceptance flow. Keep this grant separate from the finance accountant
-- and all supply-chain roles.
SET @experience_finance_voucher_menu_id := (
    SELECT page.id
    FROM system_menu page
    WHERE page.component = 'erp/finance/voucher/index'
      AND page.deleted = b'0'
      AND EXISTS (
          SELECT 1
          FROM system_menu button
          WHERE button.parent_id = page.id
            AND button.permission = 'erp:finance-voucher:query'
            AND button.deleted = b'0'
      )
    ORDER BY page.id DESC
    LIMIT 1
);

SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE587ADE8AF81E69BB4E696B0,
       'erp:finance-voucher:update', 3, 2, @experience_finance_voucher_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_finance_voucher_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:finance-voucher:update' AND deleted = b'0'
  );

SET @experience_finance_voucher_update_menu_id := (
    SELECT id
    FROM system_menu
    WHERE permission = 'erp:finance-voucher:update' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 940002, @experience_finance_voucher_update_menu_id, 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_finance_voucher_update_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu
      WHERE role_id = 940002
        AND menu_id = @experience_finance_voucher_update_menu_id
        AND deleted = b'0'
  );

-- Ensure the MES production-order page and its backend permission points exist
-- in a clean experience database. The backup may predate the MES menu seed.
SET @experience_next_menu_id := (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu);
SET @experience_mes_root_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = 0 AND path = '/mes' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE588B6E980A0E689A7E8A18CE7AEA1E79086, '', 1, 380, 0, '/mes', 'ep:operation', '',
       'ProjectMesRoot', 0, b'1', b'1', b'1', 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_mes_root_id IS NULL;

SET @experience_mes_root_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = 0 AND path = '/mes' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @experience_work_order_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'mes/work-order/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE7949FE4BAA7E5B7A5E58D95, '', 2, 10, @experience_mes_root_id, 'work-order',
       'ep:calendar', 'mes/work-order/index', 'ProjectMesWorkOrder', 0, b'1', b'1', b'1', 'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_mes_root_id IS NOT NULL
  AND @experience_work_order_menu_id IS NULL;

SET @experience_work_order_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'mes/work-order/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E5B7A5E58D95E69FA5E8AFA2,
       'erp:production-order:query', 3, 1, @experience_work_order_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-order:query' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E5B7A5E58D95E5889BE5BBBA,
       'erp:production-order:create', 3, 2, @experience_work_order_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-order:create' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E5B7A5E58D95E69BB4E696B0,
       'erp:production-order:update', 3, 3, @experience_work_order_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_work_order_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-order:update' AND deleted = b'0');

SET @experience_issue_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/manufacturing/material-issue/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE7949FE4BAA7E9A286E69699, '', 2, 30, @experience_mes_root_id, 'material-issue',
       'ep:box', 'erp/manufacturing/material-issue/index', 'FormalMesMaterialIssue', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_mes_root_id IS NOT NULL
  AND @experience_issue_menu_id IS NULL;

SET @experience_issue_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/manufacturing/material-issue/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

SET @experience_return_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/manufacturing/material-return/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE7949FE4BAA7E98080E69699, '', 2, 40, @experience_mes_root_id, 'material-return',
       'ep:refresh-left', 'erp/manufacturing/material-return/index', 'FormalMesMaterialReturn', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_mes_root_id IS NOT NULL
  AND @experience_return_menu_id IS NULL;

SET @experience_return_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/manufacturing/material-return/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E9A286E69699E69FA5E8AFA2,
       'erp:production-material-issue:query', 3, 1, @experience_issue_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_issue_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-material-issue:query' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E9A286E69699E68EA8E88D90,
       'erp:production-material-issue:recommend', 3, 2, @experience_issue_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_issue_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-material-issue:recommend' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E9A286E69699E5889BE5BBBA,
       'erp:production-material-issue:create', 3, 3, @experience_issue_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_issue_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-material-issue:create' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E9A286E69699E5AFBCE587BA,
       'erp:production-material-issue:export', 3, 4, @experience_issue_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_issue_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-material-issue:export' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE7949FE4BAA7E98080E69699E5889BE5BBBA,
       'erp:production-material-return:create', 3, 1, @experience_return_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-material-return:create' AND deleted = b'0');

SET @experience_scm_root_id := (
    SELECT id
    FROM system_menu
    WHERE parent_id = 0 AND path = '/scm' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_inbound_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/mrp/production-inbound/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE887AAE588B6E585A5E5BA93, '', 2, 85, @experience_scm_root_id, 'production-inbound',
       'ep:box', 'erp/mrp/production-inbound/index', 'ErpProductionInboundPage', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_scm_root_id IS NOT NULL
  AND @experience_inbound_menu_id IS NULL;

SET @experience_inbound_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/mrp/production-inbound/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE887AAE588B6E585A5E5BA93E69FA5E8AFA2,
       'erp:production-inbound:query', 3, 1, @experience_inbound_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-inbound:query' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE887AAE588B6E585A5E5BA93E69BB4E696B0,
       'erp:production-inbound:update', 3, 2, @experience_inbound_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:production-inbound:update' AND deleted = b'0');

-- The formal stock-assemble menu can be absent from an older backup. Create
-- the page and its controller permissions together so the experience role
-- never receives a menu entry without executable actions.
SET @experience_assemble_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/stock/assemble/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE7BB84E8A385E4B88EE68B86E58DB8, '', 2, 90, @experience_scm_root_id,
       'assemble', 'ep:set-up', 'erp/stock/assemble/index', 'ErpStockAssemblePage', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_scm_root_id IS NOT NULL
  AND @experience_assemble_menu_id IS NULL;

SET @experience_assemble_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/stock/assemble/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE69FA5E8AFA2,
       'erp:stock-assemble:query', 3, 1, @experience_assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-assemble:query' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE696B0E5A29E,
       'erp:stock-assemble:create', 3, 2, @experience_assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-assemble:create' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE4BFAEE694B9,
       'erp:stock-assemble:update', 3, 3, @experience_assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-assemble:update' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE5AEA1E6A0B8,
       'erp:stock-assemble:update-status', 3, 4, @experience_assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-assemble:update-status' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE588A0E999A4,
       'erp:stock-assemble:delete', 3, 5, @experience_assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-assemble:delete' AND deleted = b'0');

-- BOM is needed by the assembly workflow. Keep its buttons under the
-- manufacturing page instead of inheriting the research (/rd) menu tree.
SET @experience_bom_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/mrp/bom/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0x42E4BEBFE7AEA1E79086, '', 2, 20, @experience_mes_root_id,
       'bom', 'ep:document-copy', 'erp/mrp/bom/index', 'ErpBomPage', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_mes_root_id IS NOT NULL
  AND @experience_bom_menu_id IS NULL;

SET @experience_bom_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'erp/mrp/bom/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE69FA5E8AFA2,
       'erp:bom:query', 3, 1, @experience_bom_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:bom:query' AND parent_id = @experience_bom_menu_id AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE696B0E5A29E,
       'erp:bom:create', 3, 2, @experience_bom_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:bom:create' AND parent_id = @experience_bom_menu_id AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE4BFAEE694B9,
       'erp:bom:update', 3, 3, @experience_bom_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:bom:update' AND parent_id = @experience_bom_menu_id AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE588A0E999A4,
       'erp:bom:delete', 3, 4, @experience_bom_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:bom:delete' AND parent_id = @experience_bom_menu_id AND deleted = b'0');

-- The stock BPM endpoints are executable experience workflows. Older backups
-- contain the page buttons for direct status updates only, so add the missing
-- submit and withdraw permissions under their existing pages.
SET @experience_stock_in_menu_id := (
    SELECT id FROM system_menu
    WHERE component = 'erp/stock/in/index' AND deleted = b'0'
    ORDER BY id LIMIT 1
);
SET @experience_stock_out_menu_id := (
    SELECT id FROM system_menu
    WHERE component = 'erp/stock/out/index' AND deleted = b'0'
    ORDER BY id LIMIT 1
);
SET @experience_iqc_menu_id := (
    SELECT id FROM system_menu
    WHERE component = 'qms/iqc/IqcEntry' AND deleted = b'0'
    ORDER BY id LIMIT 1
);

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE68F90E4BAA4E5AEA1E689B9,
       'erp:stock-in:submit', 3, 7, @experience_stock_in_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_stock_in_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-in:submit' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE692A4E59B9EE5AEA1E689B9,
       'erp:stock-in:cancel-approval', 3, 8, @experience_stock_in_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_stock_in_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-in:cancel-approval' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE68F90E4BAA4E5AEA1E689B9,
       'erp:stock-out:submit', 3, 7, @experience_stock_out_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_stock_out_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-out:submit' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE692A4E59B9EE5AEA1E689B9,
       'erp:stock-out:cancel-approval', 3, 8, @experience_stock_out_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_stock_out_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:stock-out:cancel-approval' AND deleted = b'0');

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id := @experience_next_menu_id + 1, _utf8mb4 0xE8B4A8E6A380E98080E8B4A7,
       'erp:purchase-in-quality:update', 3, 7, @experience_iqc_menu_id, '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_iqc_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'erp:purchase-in-quality:update' AND deleted = b'0');

-- The supply-chain experience account receives the core business trees and a
-- small manufacturing action allowlist. Finance, system administration,
-- user/permission management, and research/engineering trees are excluded.
DROP TEMPORARY TABLE IF EXISTS tmp_experience_scm_menu_ids;
CREATE TEMPORARY TABLE tmp_experience_scm_menu_ids (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;

INSERT INTO tmp_experience_scm_menu_ids (menu_id)
WITH RECURSIVE core_menu_tree AS (
    SELECT id
    FROM system_menu
    WHERE id IN (2564, 2583, 2602, 2617) AND deleted = b'0'
    UNION ALL
    SELECT child.id
    FROM system_menu child
    INNER JOIN core_menu_tree parent ON parent.id = child.parent_id
    WHERE child.deleted = b'0'
)
SELECT id FROM core_menu_tree;

INSERT IGNORE INTO tmp_experience_scm_menu_ids (menu_id)
SELECT id
FROM system_menu
WHERE deleted = b'0'
  AND permission IN (
      'erp:mrp-plan:query', 'erp:mrp-plan:create', 'erp:mrp-plan:run',
      'erp:mrp-plan-rule:query', 'erp:mrp-plan-rule:create', 'erp:mrp-plan-rule:update',
      'erp:mrp-suggest:query', 'erp:mrp-suggest:approve', 'erp:mrp-suggest:reject',
      'erp:mrp-suggest:convert-purchase', 'erp:mrp-suggest:convert-production',
      'erp:production-order:query', 'erp:production-order:create', 'erp:production-order:update',
      'erp:production-report:query', 'erp:production-report:create', 'erp:production-report:update',
      'erp:production-material-issue:query', 'erp:production-material-issue:recommend',
      'erp:production-material-issue:create', 'erp:production-material-issue:export',
      'erp:production-material-return:query', 'erp:production-material-return:create',
      'erp:production-inbound:query', 'erp:production-inbound:update',
      'erp:purchase-in-quality:query', 'erp:purchase-in-quality:create',
      'erp:purchase-in-quality:first-check', 'erp:purchase-in-quality:start-recheck',
      'erp:purchase-in-quality:recheck', 'erp:purchase-in-quality:assign-checker',
      'erp:purchase-in-quality:update',
      'erp:stock-assemble:query', 'erp:stock-assemble:create',
      'erp:stock-assemble:update', 'erp:stock-assemble:update-status',
      'erp:stock-assemble:delete',
      'erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete'
  );

-- The legacy research tree contains duplicate BOM permission strings. Keep
-- only the buttons attached to the manufacturing BOM page for scm01.
DELETE allowed
FROM tmp_experience_scm_menu_ids allowed
INNER JOIN system_menu menu ON menu.id = allowed.menu_id
WHERE menu.deleted = b'0'
  AND menu.permission IN ('erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete')
  AND menu.parent_id <> @experience_bom_menu_id;

-- Keep the page entries for the explicitly tested manufacturing workflows.
INSERT IGNORE INTO tmp_experience_scm_menu_ids (menu_id)
SELECT id
FROM system_menu
WHERE deleted = b'0'
  AND component IN (
      'mes/work-order/index',
      'erp/manufacturing/production-report/index',
      'erp/manufacturing/material-issue/index',
      'erp/manufacturing/material-return/index',
      'erp/mrp/plan-rule/index', 'erp/mrp/plan/index', 'erp/mrp/suggest/index',
      'erp/mrp/production-inbound/index', 'erp/mrp/finish-quality/index',
      'erp/stock/assemble/index'
  );

-- Page/button permissions need their ancestors for the menu tree to render.
DROP TEMPORARY TABLE IF EXISTS tmp_experience_scm_seed_ids;
CREATE TEMPORARY TABLE tmp_experience_scm_seed_ids (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;
INSERT INTO tmp_experience_scm_seed_ids (menu_id)
SELECT menu_id FROM tmp_experience_scm_menu_ids;

INSERT IGNORE INTO tmp_experience_scm_menu_ids (menu_id)
WITH RECURSIVE menu_ancestors AS (
    SELECT menu.id, menu.parent_id
    FROM system_menu menu
    INNER JOIN tmp_experience_scm_seed_ids seed ON seed.menu_id = menu.id
    WHERE menu.deleted = b'0'
    UNION ALL
    SELECT parent.id, parent.parent_id
    FROM system_menu parent
    INNER JOIN menu_ancestors child ON child.parent_id = parent.id
    WHERE parent.deleted = b'0'
)
SELECT id
FROM menu_ancestors
WHERE id IS NOT NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_experience_scm_seed_ids;

-- Remove previously granted permissions outside the approved allowlist.
DELETE role_menu
FROM system_role_menu role_menu
LEFT JOIN tmp_experience_scm_menu_ids allowed ON allowed.menu_id = role_menu.menu_id
WHERE role_menu.role_id = 910004
  AND role_menu.deleted = b'0'
  AND allowed.menu_id IS NULL;

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 910004, allowed.menu_id, 'codex', NOW(), 'codex', NOW(), b'0'
FROM tmp_experience_scm_menu_ids allowed
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu role_menu
    WHERE role_menu.role_id = 910004
      AND role_menu.menu_id = allowed.menu_id
      AND role_menu.deleted = b'0'
);

DROP TEMPORARY TABLE IF EXISTS tmp_experience_scm_menu_ids;

-- The experience account must not inherit the broad operational roles: they
-- expose unrelated research and finance roots through their existing menus.
UPDATE system_user_role ur
INNER JOIN system_users u ON u.id = ur.user_id AND u.deleted = b'0'
SET ur.deleted = b'1',
    ur.updater = 'codex',
    ur.update_time = NOW()
WHERE u.username = 'scm01'
  AND ur.role_id IN (920001, 920004, 920701)
  AND ur.deleted = b'0';

INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
SELECT u.id, r.role_id, 'codex', NOW(), 'codex', NOW(), b'0'
FROM system_users u
JOIN (
    SELECT 910004 AS role_id
) r
WHERE u.username = 'scm01'
  AND u.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM system_user_role ur
      WHERE ur.user_id = u.id AND ur.role_id = r.role_id AND ur.deleted = b'0'
  );

-- Auto-generated vouchers use the default ledger or both ledgers from an
-- enabled dual-ledger configuration. Seed an open period for the current
-- month for every such ledger so a newly started experience environment can
-- complete supply-chain to finance voucher flows immediately.
INSERT INTO erp_finance_period (ledger_id, period_code, period_year, period_month, period_sort,
                                start_date, end_date, status, creator, create_time, updater, update_time, deleted)
SELECT required.ledger_id,
       DATE_FORMAT(CURDATE(), '%Y-%m'),
       YEAR(CURDATE()),
       MONTH(CURDATE()),
       YEAR(CURDATE()) * 100 + MONTH(CURDATE()),
       DATE_SUB(CURDATE(), INTERVAL DAYOFMONTH(CURDATE()) - 1 DAY),
       LAST_DAY(CURDATE()),
       10,
       'codex', NOW(), 'codex', NOW(), b'0'
FROM (
    SELECT id AS ledger_id
    FROM erp_finance_ledger
    WHERE default_status = b'1' AND status = 0 AND deleted = b'0'
    UNION
    SELECT external_ledger_id
    FROM erp_finance_dual_ledger_config
    WHERE status = 0 AND deleted = b'0'
    UNION
    SELECT internal_ledger_id
    FROM erp_finance_dual_ledger_config
    WHERE status = 0 AND deleted = b'0'
) required
INNER JOIN erp_finance_ledger ledger
        ON ledger.id = required.ledger_id
       AND ledger.status = 0
       AND ledger.deleted = b'0'
LEFT JOIN erp_finance_period period
       ON period.ledger_id = required.ledger_id
      AND period.period_sort = YEAR(CURDATE()) * 100 + MONTH(CURDATE())
      AND period.deleted = b'0'
WHERE period.id IS NULL;

-- Supply-chain approvers need only their own workflow inbox and task action.
-- Keep this separate from the scm01 operator role so an applicant cannot
-- approve its own stock and manufacturing documents.
SET @experience_bpm_todo_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'bpm/task/todo/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := (SELECT IFNULL(MAX(id), 0) + 1 FROM system_menu);
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE5BE85E58AA1E5AEA1, 'bpm:task:query', 2, 10, 3000,
       'task-todo', 'ep:checked', 'bpm/task/todo/index', 'BpmTaskTodo', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bpm_todo_menu_id IS NULL
  AND EXISTS (SELECT 1 FROM system_menu WHERE id = 3000 AND deleted = b'0');

SET @experience_bpm_todo_menu_id := (
    SELECT id
    FROM system_menu
    WHERE component = 'bpm/task/todo/index' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);
SET @experience_next_menu_id := @experience_next_menu_id + 1;
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
                         status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT @experience_next_menu_id, _utf8mb4 0xE5AEA1E689B9, 'bpm:task:update', 3, 1, @experience_bpm_todo_menu_id,
       '', '', '', '', 0, b'1', b'1', b'1',
       'codex', NOW(), 'codex', NOW(), b'0'
WHERE @experience_bpm_todo_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission = 'bpm:task:update' AND deleted = b'0');

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 920003, menu.id, 'codex', NOW(), 'codex', NOW(), b'0'
FROM system_menu menu
WHERE menu.permission IN ('bpm:task:query', 'bpm:task:update')
  AND menu.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu role_menu
      WHERE role_menu.role_id = 920003
        AND role_menu.menu_id = menu.id
        AND role_menu.deleted = b'0'
  );
