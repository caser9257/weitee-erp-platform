-- 项目管理通知模板
INSERT INTO `system_notify_template` (`name`, `code`, `type`, `nickname`, `content`, `params`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
('项目评论@提及通知', 'project_comment_mention', 1, '系统通知', '【{userName}】在任务【{taskName}】中提到了你', '["userName", "taskName", "projectId", "taskId"]', 0, '评论@提及通知', 'admin', NOW(), 'admin', NOW(), b'0'),
('项目评论回复通知', 'project_comment_reply', 1, '系统通知', '【{userName}】回复了你在任务【{taskName}】中的评论', '["userName", "taskName", "projectId", "taskId"]', 0, '评论回复通知', 'admin', NOW(), 'admin', NOW(), b'0'),
('任务分配通知', 'project_task_assigned', 1, '系统通知', '【{userName}】将任务【{taskName}】分配给你', '["userName", "taskName", "projectId", "taskId"]', 0, '任务分配通知', 'admin', NOW(), 'admin', NOW(), b'0'),
('任务完成通知', 'project_task_completed', 1, '系统通知', '【{userName}】完成了任务【{taskName}】', '["userName", "taskName", "projectId", "taskId"]', 0, '任务完成通知', 'admin', NOW(), 'admin', NOW(), b'0');
