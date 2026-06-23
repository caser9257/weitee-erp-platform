-- Department person tree migration (legacy MySQL compatible)

SET @db_name = DATABASE();

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'level'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN level VARCHAR(16) NULL COMMENT ''post level'' AFTER code'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'dept_id'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN dept_id BIGINT NULL COMMENT ''department id'' AFTER level'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'staff_quota'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN staff_quota INT NOT NULL DEFAULT 1 COMMENT ''headcount quota'' AFTER dept_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'key_position'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN key_position BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''key post'' AFTER staff_quota'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'allow_part_time'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN allow_part_time BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''allow part-time'' AFTER key_position'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_post' AND COLUMN_NAME = 'job_description'
    ),
    'SELECT 1',
    'ALTER TABLE system_post ADD COLUMN job_description VARCHAR(500) NULL COMMENT ''job description'' AFTER allow_part_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_user_post' AND COLUMN_NAME = 'is_primary'
    ),
    'SELECT 1',
    'ALTER TABLE system_user_post ADD COLUMN is_primary BIT(1) NOT NULL DEFAULT b''0'' COMMENT ''primary post'' AFTER post_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_user_post' AND COLUMN_NAME = 'start_date'
    ),
    'SELECT 1',
    'ALTER TABLE system_user_post ADD COLUMN start_date DATETIME NULL COMMENT ''start date'' AFTER is_primary'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_user_post' AND COLUMN_NAME = 'end_date'
    ),
    'SELECT 1',
    'ALTER TABLE system_user_post ADD COLUMN end_date DATETIME NULL COMMENT ''end date'' AFTER start_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF (
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'system_user_post' AND COLUMN_NAME = 'remark'
    ),
    'SELECT 1',
    'ALTER TABLE system_user_post ADD COLUMN remark VARCHAR(255) NULL COMMENT ''remark'' AFTER end_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @HR_ROOT_ID = COALESCE(
    (SELECT `id` FROM system_menu WHERE `path` = '/hr' AND `deleted` = b'0' ORDER BY `id` LIMIT 1),
    (SELECT `id` FROM system_menu WHERE `parent_id` = 0
        AND HEX(CONVERT(`name` USING utf8mb4)) IN (
            'E4BABAE4BA8BE7AEA1E79086',
            'E4BABAE58A9BE8B584E6BA90'
        )
        AND `deleted` = b'0' ORDER BY `id` LIMIT 1)
);
SET @PMO_ROOT_ID = COALESCE(
    (SELECT `id` FROM system_menu WHERE `path` = '/pmo' AND `deleted` = b'0' ORDER BY `id` LIMIT 1),
    (SELECT `id` FROM system_menu WHERE `parent_id` = 0
        AND HEX(CONVERT(`name` USING utf8mb4)) = 'E7BBBCE59088E8AEA1E58892E7AEA1E79086'
        AND `deleted` = b'0' ORDER BY `id` LIMIT 1)
);

UPDATE system_menu
SET `name` = CONVERT(0xE7BB84E7BB87E5B297E4BD8DE7AEA1E79086 USING utf8mb4),
    `sort` = 40,
    `parent_id` = COALESCE(@HR_ROOT_ID, `parent_id`, 1),
    `path` = 'post',
    `icon` = 'fa:address-book-o',
    `component` = 'system/post/index',
    `component_name` = 'SystemPost',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = 'admin',
    `update_time` = NOW(),
    `deleted` = b'0'
WHERE `id` = 104;

INSERT INTO system_menu
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`,
 `updater`, `update_time`, `deleted`)
SELECT 104,
       CONVERT(0xE7BB84E7BB87E5B297E4BD8DE7AEA1E79086 USING utf8mb4),
       '',
       2,
       40,
       COALESCE(@HR_ROOT_ID, 1),
       'post',
       'fa:address-book-o',
       'system/post/index',
       'SystemPost',
       0,
       b'1',
       b'1',
       b'1',
       'admin',
       NOW(),
       'admin',
       NOW(),
       b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 104);

UPDATE system_menu
SET `name` = CONVERT(0xE5B297E4BD8DE5B182E7BAA7E58FAFE8A786E58C96 USING utf8mb4),
    `sort` = 50,
    `parent_id` = COALESCE(@PMO_ROOT_ID, `parent_id`, 1),
    `path` = 'post-level-overview',
    `icon` = 'ep:histogram',
    `component` = 'system/post-level/overview/index',
    `component_name` = 'SystemPostLevelOverview',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = 'admin',
    `update_time` = NOW(),
    `deleted` = b'0'
WHERE `id` = 3041;

INSERT INTO system_menu
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`,
 `updater`, `update_time`, `deleted`)
SELECT 3041,
       CONVERT(0xE5B297E4BD8DE5B182E7BAA7E58FAFE8A786E58C96 USING utf8mb4),
       '',
       2,
       50,
       COALESCE(@PMO_ROOT_ID, 1),
       'post-level-overview',
       'ep:histogram',
       'system/post-level/overview/index',
       'SystemPostLevelOverview',
       0,
       b'1',
       b'1',
       b'1',
       'admin',
       NOW(),
       'admin',
       NOW(),
       b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 3041);

DELETE FROM system_menu
WHERE `id` = 3042;
