-- 修复财务审计验收账号的错误昵称数据。
-- 只替换明确存成问号的昵称，不覆盖已经正常的自定义昵称。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `weitee-erp`;

UPDATE system_users
SET nickname = CONVERT(0xE8B4A2E58AA1E5AEA1E8AEA1E4BD93E9AA8CE8B4A6E58FB7 USING utf8mb4),
    updater = 'codex',
    update_time = NOW()
WHERE username = 'financeaud'
  AND deleted = b'0'
  AND nickname = '????';

SELECT username, nickname, HEX(nickname)
FROM system_users
WHERE username = 'financeaud'
  AND deleted = b'0';
