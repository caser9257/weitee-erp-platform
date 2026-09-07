-- 研发 BOM 导入站内信需保留完整告警与重复编码明细，template_params 不再适合使用 varchar(1024)。
-- 仅扩展存储类型，不修改既有站内信数据、索引或模板内容。
ALTER TABLE `system_notify_message`
    MODIFY COLUMN `template_params` MEDIUMTEXT DEFAULT NULL COMMENT '模版参数';
