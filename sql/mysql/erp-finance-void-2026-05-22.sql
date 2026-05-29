-- 添加"已作废"字典数据到erp_audit_status
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (1493, 50, '已作废', '50', 'erp_audit_status', 0, 'info', '', '', '1', NOW(), '1', NOW(), b'0');
