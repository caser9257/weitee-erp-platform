-- 站内信模板参数列扩容：导入失败摘要（failSample 含前 10 条明细）以 JSON 存入
-- template_params，原 varchar(255) 被超长失败明细打爆（Data too long），
-- 导致导入失败站内信落库失败被吞、用户收不到通知。与 template_content(1024) 对齐。
ALTER TABLE `system_notify_message`
    MODIFY COLUMN `template_params` varchar(1024) DEFAULT NULL COMMENT '模版参数';
