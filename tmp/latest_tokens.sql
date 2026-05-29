SELECT id, access_token, refresh_token, user_id, expires_time, update_time
FROM system_oauth2_access_token
WHERE user_id = 145
ORDER BY id DESC
LIMIT 5;
