SELECT id, access_token, refresh_token, user_id, expires_time, create_time, update_time
FROM system_oauth2_access_token
WHERE user_id = 145
ORDER BY id DESC
LIMIT 10;

SELECT id, username, nickname, status, deleted, last_login_time
FROM system_users
WHERE username = 'superadmin';
