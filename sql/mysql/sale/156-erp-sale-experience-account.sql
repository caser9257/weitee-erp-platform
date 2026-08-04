-- 销售模块体验账号（sale01）授权脚本
-- 幂等写法：动态解析 /sales 根节点 + WITH RECURSIVE 收集整棵销售菜单树
-- 通过权限点白名单兜底，确保销售退货（旧 2659 / 新 931312 两种 ID）都不会悬空
-- 日期: 2026-08-04
-- 执行时机: 必须在 155-erp-sale-return-customer-menu.sql 之后执行

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

-- 与 finance01 / scm01 / approver01 共用同一演示密码（123456）
SET @demo_password = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

-- ========== 1. 创建 / 更新销售体验账号 ==========
-- nickname: 销售模块体验账号
-- remark:   用于销售模块主流程和异常流程体验
INSERT INTO system_users (username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, creator, create_time, updater, update_time, deleted)
SELECT 'sale01', @demo_password,
       _utf8mb4 0xE99480E594AEE6A8A1E59D97E4BD93E9AA8CE8B4A6E58FB7,
       _utf8mb4 0xE794A8E4BA8EE99480E594AEE6A8A1E59D97E4B8BBE6B581E7A88BE5928CE5BC82E5B8B8E6B581E7A88BE4BD93E9AA8C,
       910100, '[]', '', '', 0, '', 0, 'tester', NOW(), 'tester', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_users WHERE username = 'sale01' AND deleted = b'0');

UPDATE system_users
SET password = @demo_password,
    nickname = _utf8mb4 0xE99480E594AEE6A8A1E59D97E4BD93E9AA8CE8B4A6E58FB7,
    remark = _utf8mb4 0xE794A8E4BA8EE99480E594AEE6A8A1E59D97E4B8BBE6B581E7A88BE5928CE5BC82E5B8B8E6B581E7A88BE4BD93E9AA8C,
    dept_id = 910100,
    post_ids = '[]',
    status = 0,
    updater = 'tester',
    update_time = NOW()
WHERE username = 'sale01' AND deleted = b'0';

-- ========== 2. 动态解析 /sales 一级根节点 ==========
SET @sales_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0' AND parent_id = 0 AND path = '/sales'
  ORDER BY id
  LIMIT 1
);

-- ========== 3. 收集销售体验允许的菜单 ID（整棵 /sales 树 + 权限点白名单兜底） ==========
DROP TEMPORARY TABLE IF EXISTS tmp_sale_experience_menu_ids;
CREATE TEMPORARY TABLE tmp_sale_experience_menu_ids (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;

-- 3.1 从 /sales 根递归收集整棵子树（覆盖新旧菜单 ID）
INSERT INTO tmp_sale_experience_menu_ids (menu_id)
WITH RECURSIVE sale_menu_tree AS (
    SELECT id
    FROM system_menu
    WHERE parent_id = @sales_root_id AND deleted = b'0'
    UNION ALL
    SELECT child.id
    FROM system_menu child
    INNER JOIN sale_menu_tree parent ON parent.id = child.parent_id
    WHERE child.deleted = b'0'
)
SELECT id FROM sale_menu_tree;

-- 3.2 权限点白名单兜底（按权限标识收集，兼容任何菜单 ID）
INSERT IGNORE INTO tmp_sale_experience_menu_ids (menu_id)
SELECT id
FROM system_menu
WHERE deleted = b'0'
  AND permission IN (
      'erp:sale-order:query', 'erp:sale-order:create', 'erp:sale-order:update',
      'erp:sale-order:update-status', 'erp:sale-order:delete', 'erp:sale-order:export',
      'erp:sale-order:submit',
      'erp:sale-out:query', 'erp:sale-out:create', 'erp:sale-out:update',
      'erp:sale-out:update-status', 'erp:sale-out:delete', 'erp:sale-out:export',
      'erp:sale-return:query', 'erp:sale-return:create', 'erp:sale-return:update',
      'erp:sale-return:delete', 'erp:sale-return:export', 'erp:sale-return:update-status',
      'erp:customer:query', 'erp:customer:create', 'erp:customer:update',
      'erp:customer:delete', 'erp:customer:export',
      'crm:contract:query', 'crm:contract:audit',
      'erp:shipment-release:query', 'erp:shipment-release:check',
      'erp:shipment-release:submit', 'erp:shipment-release:approve', 'erp:shipment-release:reject',
      'erp:market-ledger:query', 'erp:market-ledger:export',
      'erp:market-alert:query', 'erp:market-alert:update', 'erp:market-alert:check',
      'erp:market-alert:handle'
  );

-- 3.3 兜底：销售根节点自身也要保留，让菜单树能渲染
INSERT IGNORE INTO tmp_sale_experience_menu_ids (menu_id)
SELECT @sales_root_id
WHERE @sales_root_id IS NOT NULL;

-- ========== 4. 为页面/按钮权限补全祖先节点（菜单树渲染必需） ==========
DROP TEMPORARY TABLE IF EXISTS tmp_sale_experience_seed_ids;
CREATE TEMPORARY TABLE tmp_sale_experience_seed_ids (
    menu_id BIGINT NOT NULL PRIMARY KEY
) ENGINE = MEMORY;
INSERT INTO tmp_sale_experience_seed_ids (menu_id)
SELECT menu_id FROM tmp_sale_experience_menu_ids;

INSERT IGNORE INTO tmp_sale_experience_menu_ids (menu_id)
WITH RECURSIVE menu_ancestors AS (
    SELECT menu.id, menu.parent_id
    FROM system_menu menu
    INNER JOIN tmp_sale_experience_seed_ids seed ON seed.menu_id = menu.id
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

DROP TEMPORARY TABLE IF EXISTS tmp_sale_experience_seed_ids;

-- ========== 5. 建立/复用销售体验角色 ==========
-- name: 销售模块体验角色
-- remark: 用于销售模块体验
INSERT INTO system_role (name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, create_time, updater, update_time, deleted)
SELECT _utf8mb4 0xE99480E594AEE6A8A1E59D97E4BD93E9AA8CE8A792E889B2,
       'erp_sale_experience', 120, 1, '', 0, 2,
       _utf8mb4 0xE794A8E4BA8EE99480E594AEE6A8A1E59D97E4BD93E9AA8C,
       'tester', NOW(), 'tester', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_role WHERE code = 'erp_sale_experience' AND deleted = b'0');

SET @experience_sale_role_id := (
    SELECT id
    FROM system_role
    WHERE code = 'erp_sale_experience' AND deleted = b'0'
    ORDER BY id
    LIMIT 1
);

-- ========== 6. 清洗 + 重建角色菜单授权（只保留销售树内菜单） ==========
DELETE role_menu
FROM system_role_menu role_menu
LEFT JOIN tmp_sale_experience_menu_ids allowed ON allowed.menu_id = role_menu.menu_id
WHERE role_menu.role_id = @experience_sale_role_id
  AND role_menu.deleted = b'0'
  AND allowed.menu_id IS NULL;

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT @experience_sale_role_id, allowed.menu_id, 'tester', NOW(), 'tester', NOW(), b'0'
FROM tmp_sale_experience_menu_ids allowed
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu role_menu
    WHERE role_menu.role_id = @experience_sale_role_id
      AND role_menu.menu_id = allowed.menu_id
      AND role_menu.deleted = b'0'
);

DROP TEMPORARY TABLE IF EXISTS tmp_sale_experience_menu_ids;

-- ========== 7. 绑定 sale01 -> 销售体验角色 ==========
INSERT INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
SELECT u.id, @experience_sale_role_id, 'tester', NOW(), 'tester', NOW(), b'0'
FROM system_users u
WHERE u.username = 'sale01'
  AND u.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM system_user_role ur
      WHERE ur.user_id = u.id AND ur.role_id = @experience_sale_role_id AND ur.deleted = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;