/*
  研发 BOM「作废」按钮权限种子：erp:rd-bom:void
  挂载在研发BOM菜单（component='erp/rd/rd-bom/index'）之下，type=3 按钮。
  幂等：按 permission 判重；父菜单缺失时跳过（不猜测挂载点）。
  注意：不指定 USE，跟随执行时连接的数据库（如：mysql ... weitee-erp < 本文件）
*/
SET NAMES utf8mb4;

SET @rd_bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/rd/rd-bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '研发BOM作废', 'erp:rd-bom:void',
       3, 9, @rd_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @rd_bom_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:rd-bom:void' AND `deleted`=b'0');
