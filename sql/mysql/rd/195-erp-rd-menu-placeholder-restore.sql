/*
 * 研发管理占位入口恢复
 *
 * 需求评审和设计变更属于研发核心生命周期，虽然当前页面仍为占位页，
 * 仍保留菜单入口用于承载后续功能；研发跟进、研发预警继续由项目中心/预警中心承载。
 */

SET NAMES utf8mb4;

SET @rd_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id`=0 AND `path`='/rd' AND `type`=1 AND `deleted`=b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status`=0,
    `visible`=b'1',
    `sort`=CASE
      WHEN `name`='需求与立项评审' THEN 40
      WHEN `name`='设计变更' THEN 50
      ELSE `sort`
    END,
    `updater`='1',
    `update_time`=NOW()
WHERE `deleted`=b'0'
  AND `parent_id`=@rd_root_id
  AND `component`='common/menu-placeholder/index'
  AND `name` IN ('需求与立项评审','设计变更');

-- 职责重叠入口继续隐藏。
UPDATE `system_menu`
SET `status`=1, `visible`=b'0', `updater`='1', `update_time`=NOW()
WHERE `deleted`=b'0'
  AND `parent_id`=@rd_root_id
  AND `component`='common/menu-placeholder/index'
  AND `name` IN ('研发跟进','研发预警');
