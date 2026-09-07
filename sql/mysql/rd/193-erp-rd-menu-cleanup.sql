/*
 * 研发管理菜单精简
 *
 * 保留并排序：研发物料、研发BOM、文档与图纸。
 * 移除展示：设计中心、研发跟进、研发预警（当前为占位页面）；
 * 工艺路线与 SOP 已由独立“工艺管理”目录承载，不在研发目录重复展示。
 * 历史 /research、/erp/rd 根目录仅保留正式 /rd，避免同一业务出现多个入口。
 * 仅调整菜单状态，不删除页面、接口或按钮权限。
 */

SET NAMES utf8mb4;

-- 先建立唯一的正式根目录，避免模块迁移顺序不同导致保留菜单无父节点。
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.`id`), 0) + 1 FROM `system_menu` t),
       '研发管理', '', 1, 330, 0, '/rd', 'ep:cpu', '', 'FormalRdRoot',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu`
  WHERE `parent_id`=0 AND `path`='/rd' AND `type`=1 AND `deleted`=b'0'
);

SET @rd_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id`=0 AND `path`='/rd' AND `type`=1 AND `deleted`=b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name`='研发管理', `sort`=330, `icon`='ep:cpu',
    `status`=0, `visible`=b'1', `always_show`=b'1',
    `updater`='1', `update_time`=NOW()
WHERE `id`=@rd_root_id;

-- 隐藏历史重复的研发根目录；正式 /rd 不受影响。
UPDATE `system_menu`
SET `status`=1, `visible`=b'0', `updater`='1', `update_time`=NOW()
WHERE `deleted`=b'0'
  AND `type`=1
  AND ((`parent_id`=0 AND `path`='/research')
       OR (`parent_id`<>0 AND `path`='rd' AND `name`='研发管理'))
  AND (`id`<>@rd_root_id OR @rd_root_id IS NULL);

-- 隐藏研发目录下无实际功能或重复承载的入口。
UPDATE `system_menu`
SET `status`=1, `visible`=b'0', `updater`='1', `update_time`=NOW()
WHERE `deleted`=b'0'
  AND `parent_id`=@rd_root_id
  AND (
    `component`='erp/rd/design/index'
    OR `component`='erp/route/index'
    OR (`component`='common/menu-placeholder/index' AND `name` IN ('研发跟进','研发预警'))
  );

-- 保证正式保留项的父节点和展示状态，并按研发工作流排序；不改动其按钮权限。
UPDATE `system_menu`
SET `name`=CASE
      WHEN `permission`='erp:product:rd-cadence:query' THEN '研发物料'
      WHEN `component`='erp/rd/rd-bom/index' THEN '研发BOM'
      WHEN `component`='erp/rd/document/index' THEN '文档与图纸'
      ELSE `name`
    END,
    `sort`=CASE
      WHEN `permission`='erp:product:rd-cadence:query' THEN 10
      WHEN `component`='erp/rd/rd-bom/index' THEN 20
      WHEN `component`='erp/rd/document/index' THEN 30
      ELSE `sort`
    END,
    `parent_id`=@rd_root_id, `status`=0, `visible`=b'1',
    `updater`='1', `update_time`=NOW()
WHERE `deleted`=b'0'
  AND @rd_root_id IS NOT NULL
  AND (
    `component` IN ('erp/rd/rd-bom/index','erp/rd/document/index')
    OR `permission`='erp:product:rd-cadence:query'
  );
