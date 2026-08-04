-- =====================================================================
-- 159-erp-sale-experience-permission-fix.sql
-- 用途：补齐销售体验账号（sale01）在退货创建时缺失的仓库/库存/账户查询权限
-- 背景：
--   sale01 角色 = erp_sale_experience（role id 940005），原授权仅含销售树内 40 个
--   权限点；「新增退货」弹窗会加载 仓库 / 结算账户 下拉，并查询库存（
--   erp:warehouse:query、erp:stock:query、erp:account:query），因缺权限导致
--   4 个「服务器错误,请联系管理员!」（Access Denied）。
-- 说明：
--   仅补授权，不新增侧边栏菜单（这三个权限点 type=3 按钮，不会生成菜单项）。
-- 执行时机：在任何销售退货「新增」操作之前。幂等，可重复执行。
-- =====================================================================
USE `weitee-erp`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ========== 1. 补齐 940005（erp_sale_experience）的仓库/库存/账户查询权限 ==========
-- 权限点 menu_id 与权限码对照：
--   2585  erp:warehouse:query（仓库查询）
--   2591  erp:stock:query（库存查询）
--   2647  erp:account:query（结算账户查询）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 940005, m.id, 'tester', NOW(), 'tester', NOW(), b'0'
FROM system_menu m
WHERE m.id IN (2585, 2591, 2647)
  AND m.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1 FROM system_role_menu rm
      WHERE rm.role_id = 940005
        AND rm.menu_id = m.id
        AND rm.deleted = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;

COMMIT;