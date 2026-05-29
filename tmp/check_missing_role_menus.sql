SELECT 'super_admin_missing_active_menus' AS label, COUNT(*) AS value
FROM `ruoyi-vue-pro`.system_menu m
WHERE m.deleted = b'0'
  AND m.status = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `ruoyi-vue-pro`.system_role_menu rm
    JOIN `ruoyi-vue-pro`.system_role r ON r.id = rm.role_id
    WHERE r.code = 'super_admin'
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND r.deleted = b'0'
  );

SELECT 'supply_chain_manager_missing_active_menus' AS label, COUNT(*) AS value
FROM `ruoyi-vue-pro`.system_menu m
WHERE m.deleted = b'0'
  AND m.status = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `ruoyi-vue-pro`.system_role_menu rm
    JOIN `ruoyi-vue-pro`.system_role r ON r.id = rm.role_id
    WHERE r.code = 'supply_chain_manager'
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND r.deleted = b'0'
  );

SELECT m.id, m.name, m.path, m.component, m.parent_id, m.sort
FROM `ruoyi-vue-pro`.system_menu m
WHERE m.deleted = b'0'
  AND m.status = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `ruoyi-vue-pro`.system_role_menu rm
    JOIN `ruoyi-vue-pro`.system_role r ON r.id = rm.role_id
    WHERE r.code = 'super_admin'
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND r.deleted = b'0'
  )
ORDER BY m.parent_id, m.sort, m.id
LIMIT 60;

SELECT m.id, m.name, m.path, m.component, m.parent_id, m.sort
FROM `ruoyi-vue-pro`.system_menu m
WHERE m.deleted = b'0'
  AND m.status = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `ruoyi-vue-pro`.system_role_menu rm
    JOIN `ruoyi-vue-pro`.system_role r ON r.id = rm.role_id
    WHERE r.code = 'supply_chain_manager'
      AND rm.menu_id = m.id
      AND rm.deleted = b'0'
      AND r.deleted = b'0'
  )
ORDER BY m.parent_id, m.sort, m.id
LIMIT 60;
