-- post level menu parent finalization for existing databases
-- purpose:
-- 1. unify hr root menu display name
-- 2. move hr / pmo post menus under the correct parent roots
-- 3. rename child menus to match actual business functions

SET NAMES utf8mb4;

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
SET `name` = CONVERT(0xE4BABAE4BA8BE7AEA1E79086 USING utf8mb4),
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `id` = @HR_ROOT_ID;

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
