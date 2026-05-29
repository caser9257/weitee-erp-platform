SELECT
    id,
    access_token,
    refresh_token,
    user_id,
    user_type,
    client_id,
    expires_time
FROM system_oauth2_access_token
WHERE user_type = 2
  AND expires_time > NOW()
ORDER BY id DESC
LIMIT 20;
