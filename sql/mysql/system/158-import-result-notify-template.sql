-- 导入完成站内信通知模板（产品/研发BOM/合同/用户/部门）
INSERT INTO `system_notify_template`
    (`name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `creator`, `updater`, `deleted`)
SELECT t.name, t.code, '系统', t.content, 2, t.params, 0, '1', '1', b'0'
FROM (
    SELECT '产品导入完成通知' AS name, 'erp_import_result_product' AS code,
           '{scene}导入完成：共 {totalCount} 行，成功 {successCount} 条，失败 {failCount} 条。{failSample}' AS content,
           '["scene","totalCount","successCount","failCount","failSample","detailUrl"]' AS params
    UNION ALL SELECT '研发BOM导入完成通知', 'erp_import_result_rd_bom',
           '{scene}导入完成：共 {totalCount} 行，成功 {successCount} 条，失败 {failCount} 条。{failSample}',
           '["scene","totalCount","successCount","failCount","failSample","detailUrl"]'
    UNION ALL SELECT '合同导入完成通知', 'erp_import_result_contract',
           '{scene}导入完成：共 {totalCount} 行，成功 {successCount} 条，失败 {failCount} 条。{failSample}',
           '["scene","totalCount","successCount","failCount","failSample","detailUrl"]'
    UNION ALL SELECT '用户导入完成通知', 'system_import_result_user',
           '{scene}导入完成：共 {totalCount} 行，成功 {successCount} 条，失败 {failCount} 条。{failSample}',
           '["scene","totalCount","successCount","failCount","failSample","detailUrl"]'
    UNION ALL SELECT '部门导入完成通知', 'system_import_result_dept',
           '{scene}导入完成：共 {totalCount} 行，成功 {successCount} 条，失败 {failCount} 条。{failSample}',
           '["scene","totalCount","successCount","failCount","failSample","detailUrl"]'
) t
WHERE NOT EXISTS (SELECT 1 FROM `system_notify_template` WHERE `code` = t.code AND `deleted` = b'0');
