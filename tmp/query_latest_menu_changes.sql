SELECT id, name, path, component, parent_id, sort, create_time, update_time
FROM `ruoyi-vue-pro`.system_menu
ORDER BY create_time DESC, id DESC
LIMIT 30;

SELECT id, role_id, menu_id, tenant_id, create_time, update_time
FROM `ruoyi-vue-pro`.system_role_menu
ORDER BY create_time DESC, id DESC
LIMIT 30;

SELECT r.code, COUNT(*) AS menu_count
FROM `ruoyi-vue-pro`.system_role r
LEFT JOIN `ruoyi-vue-pro`.system_role_menu rm
  ON rm.role_id = r.id AND rm.deleted = b'0'
GROUP BY r.code
ORDER BY menu_count DESC, r.code;
