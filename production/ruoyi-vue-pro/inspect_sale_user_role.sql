SELECT id, user_id, role_id, tenant_id, deleted
FROM `ruoyi-vue-pro`.system_user_role
WHERE id IN (910301, 910302, 910303)
   OR user_id IN (910201, 910202, 910203)
ORDER BY id, user_id, role_id;
