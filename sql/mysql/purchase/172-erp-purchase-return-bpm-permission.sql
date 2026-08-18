-- 采购退货：提交审批 / 撤回审批 权限点规范化
-- 背景：历史上前端/后端 submit、cancel-approval 均兼容走 erp:purchase-return:update-status；
--       本脚本补齐正式权限点，后续可移除兼容逻辑。
-- 幂等：以 permission 判重，重复执行安全。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `weitee-erp`;

SET @purchase_return_menu_id = (
    SELECT parent_id FROM system_menu
    WHERE permission = 'erp:purchase-return:update-status' AND deleted = b'0'
    ORDER BY id LIMIT 1
);

-- 采购退货：提交审批
INSERT INTO system_menu
    (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
     status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 2687, '采购退货提交审批', 'erp:purchase-return:submit', 3, 7, @purchase_return_menu_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @purchase_return_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:purchase-return:submit' AND deleted = b'0'
  );

-- 采购退货：撤回审批
INSERT INTO system_menu
    (id, name, permission, type, sort, parent_id, path, icon, component, component_name,
     status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 2688, '采购退货撤回审批', 'erp:purchase-return:cancel-approval', 3, 8, @purchase_return_menu_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @purchase_return_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM system_menu
      WHERE permission = 'erp:purchase-return:cancel-approval' AND deleted = b'0'
  );
