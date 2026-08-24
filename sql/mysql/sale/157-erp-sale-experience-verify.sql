-- 上机后快速验证脚本：核对 sale01 账号与销售授权状态
-- 使用方法: mysql -u root -p weitee-erp < 157-erp-sale-experience-verify.sql
-- 预期：四条查询各返回 1~N 行，数值说明详见注释

USE `weitee-erp`;

-- 1. sale01 账号是否存在且启用（预期返回 1 行, status=0）
SELECT id, username, nickname, dept_id, status, deleted
FROM system_users
WHERE username = 'sale01';

-- 2. sale01 是否绑定销售体验角色（预期返回 1 行）
SELECT u.username, r.code, r.name
FROM system_user_role ur
JOIN system_users u ON u.id = ur.user_id AND u.deleted = b'0'
JOIN system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE u.username = 'sale01' AND ur.deleted = b'0';

-- 3. /sales 根节点（预期返回 1 行）
SELECT id, name, path
FROM system_menu
WHERE parent_id = 0 AND path = '/sales' AND deleted = b'0';

-- 4. sale01 角色授权的菜单数量（预期 > 10）
SELECT COUNT(*) AS granted_menu_count
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
WHERE r.code = 'erp_sale_experience' AND rm.deleted = b'0';

-- 5. 销售退货页面菜单及其按钮是否被授权（预期 sale-return 查询/创建/更新/更新状态/删除/导出 各一条）
SELECT m.id, m.name, m.permission
FROM system_menu m
WHERE m.deleted = b'0'
  AND m.permission LIKE 'erp:sale-return:%'
ORDER BY m.id;

-- 6. 检查是否存在悬空授权（角色被授权了已删除/不存在的菜单，预期 count=0）
SELECT COUNT(*) AS dangling_permissions
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
LEFT JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE r.code = 'erp_sale_experience'
  AND rm.deleted = b'0'
  AND (m.id IS NULL OR m.deleted = b'1');