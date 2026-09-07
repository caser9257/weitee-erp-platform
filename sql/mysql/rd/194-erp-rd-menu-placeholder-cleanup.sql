/*
 * 研发管理菜单补充清理
 *
 * 193 脚本执行后继续收敛历史遗留入口：
 * 1. 隐藏旧“标准 BOM”，避免与“研发BOM”重复；
 * 2. 隐藏研发目录下所有占位页，避免未交付功能进入正式菜单。
 *
 * 保留菜单由 193 脚本统一维护：研发物料、研发BOM、文档与图纸。
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
SET `status`=1, `visible`=b'0', `updater`='1', `update_time`=NOW()
WHERE `deleted`=b'0'
  AND `parent_id`=@rd_root_id
  AND (
    `component`='erp/rd/bom/index'
    OR `component`='common/menu-placeholder/index'
  );
