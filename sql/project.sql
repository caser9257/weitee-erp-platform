-- 项目表
CREATE TABLE IF NOT EXISTS `project_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    `owner_user_id` BIGINT NOT NULL COMMENT '创建人/负责人',
    `personal` BIT(1) DEFAULT 0 COMMENT '是否个人项目',
    `archive_method` VARCHAR(32) DEFAULT NULL COMMENT '自动归档方式',
    `archive_days` INT DEFAULT NULL COMMENT '自动归档天数',
    `archived_at` DATETIME DEFAULT NULL COMMENT '归档时间',
    `archived_user_id` BIGINT DEFAULT NULL COMMENT '归档操作人',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_owner_user_id` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS `project_project_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `owner` BIT(1) DEFAULT 0 COMMENT '是否负责人',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `top_at` DATETIME DEFAULT NULL COMMENT '置顶时间',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 项目列表表
CREATE TABLE IF NOT EXISTS `project_column` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(64) NOT NULL COMMENT '列表名称',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目列表表';

-- 项目标签表
CREATE TABLE IF NOT EXISTS `project_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(16) DEFAULT NULL COMMENT '标签颜色',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目标签表';

-- 项目日志表
CREATE TABLE IF NOT EXISTS `project_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `column_id` BIGINT DEFAULT NULL COMMENT '列表ID',
    `task_id` BIGINT DEFAULT NULL COMMENT '任务ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `detail` VARCHAR(500) DEFAULT NULL COMMENT '日志详情',
    `record` JSON DEFAULT NULL COMMENT '变更记录',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目日志表';

-- 项目邀请表
CREATE TABLE IF NOT EXISTS `project_invite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '邀请人ID',
    `invite_user_id` BIGINT NOT NULL COMMENT '被邀请人ID',
    `status` TINYINT DEFAULT NULL COMMENT '状态',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_invite_user_id` (`invite_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目邀请表';

-- 任务表
CREATE TABLE IF NOT EXISTS `project_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT DEFAULT 0 COMMENT '父任务ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `column_id` BIGINT DEFAULT NULL COMMENT '列表ID',
    `flow_item_id` BIGINT DEFAULT NULL COMMENT '工作流状态ID',
    `flow_item_name` VARCHAR(128) DEFAULT NULL COMMENT '工作流状态名称',
    `name` VARCHAR(255) NOT NULL COMMENT '任务名称',
    `description` TEXT DEFAULT NULL COMMENT '任务描述',
    `color` VARCHAR(16) DEFAULT NULL COMMENT '颜色',
    `start_at` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_at` DATETIME DEFAULT NULL COMMENT '截止时间',
    `complete_at` DATETIME DEFAULT NULL COMMENT '完成时间',
    `archived_at` DATETIME DEFAULT NULL COMMENT '归档时间',
    `archived_user_id` BIGINT DEFAULT NULL COMMENT '归档操作人',
    `archived_follow` BIT(1) DEFAULT 0 COMMENT '归档后是否关注',
    `visibility` TINYINT DEFAULT 1 COMMENT '可见性: 1项目人员 2任务人员 3指定成员',
    `priority_level` INT DEFAULT NULL COMMENT '优先级等级',
    `priority_name` VARCHAR(32) DEFAULT NULL COMMENT '优先级名称',
    `priority_color` VARCHAR(16) DEFAULT NULL COMMENT '优先级颜色',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `loop_rule` VARCHAR(64) DEFAULT NULL COMMENT '循环规则',
    `loop_at` DATETIME DEFAULT NULL COMMENT '下次循环时间',
    `deleted_user_id` BIGINT DEFAULT NULL COMMENT '删除人ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_column_id` (`column_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_flow_item_id` (`flow_item_id`),
    INDEX `idx_complete_at` (`complete_at`),
    INDEX `idx_archived_at` (`archived_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务负责人表
CREATE TABLE IF NOT EXISTS `project_task_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `task_pid` BIGINT DEFAULT NULL COMMENT '父任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `owner` BIT(1) DEFAULT 0 COMMENT '是否负责人',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`, `user_id`),
    INDEX `idx_project_id` (`project_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务负责人表';

-- 任务内容表
CREATE TABLE IF NOT EXISTS `project_task_content` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `content` JSON DEFAULT NULL COMMENT '内容JSON',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务内容表';

-- 任务附件表
CREATE TABLE IF NOT EXISTS `project_task_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务附件表';

-- 任务标签表
CREATE TABLE IF NOT EXISTS `project_task_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(16) DEFAULT NULL COMMENT '标签颜色',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务标签表';

-- 任务关联表
CREATE TABLE IF NOT EXISTS `project_task_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `related_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
    `relation_type` VARCHAR(32) NOT NULL COMMENT '关联类型',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务关联表';

-- 任务可见性表
CREATE TABLE IF NOT EXISTS `project_task_visibility_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务可见性表';

-- 任务模板表
CREATE TABLE IF NOT EXISTS `project_task_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `content` TEXT DEFAULT NULL COMMENT '模板内容',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

-- 工作流表
CREATE TABLE IF NOT EXISTS `project_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(64) NOT NULL COMMENT '工作流名称',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表';

-- 工作流状态表
CREATE TABLE IF NOT EXISTS `project_flow_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `flow_id` BIGINT NOT NULL COMMENT '工作流ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(64) NOT NULL COMMENT '状态名称',
    `status` VARCHAR(32) NOT NULL COMMENT '状态类型: start/progress/test/end',
    `color` VARCHAR(16) DEFAULT NULL COMMENT '颜色',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `turns` JSON DEFAULT NULL COMMENT '流转配置',
    `user_ids` JSON DEFAULT NULL COMMENT '用户ID列表',
    `user_type` VARCHAR(32) DEFAULT NULL COMMENT '用户类型: add/replace/merge',
    `user_limit` BIT(1) DEFAULT 0 COMMENT '是否限制用户',
    `column_id` BIGINT DEFAULT NULL COMMENT '列表ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流状态表';

-- 工作流变更记录表
CREATE TABLE IF NOT EXISTS `project_task_flow_change` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `before_flow_item_id` BIGINT DEFAULT NULL COMMENT '变更前状态ID',
    `before_flow_item_name` VARCHAR(128) DEFAULT NULL COMMENT '变更前状态名称',
    `after_flow_item_id` BIGINT DEFAULT NULL COMMENT '变更后状态ID',
    `after_flow_item_name` VARCHAR(128) DEFAULT NULL COMMENT '变更后状态名称',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流变更记录表';

-- 文件表
CREATE TABLE IF NOT EXISTS `project_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '文件名称',
    `ext` VARCHAR(32) DEFAULT NULL COMMENT '文件扩展名',
    `size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `type` TINYINT DEFAULT NULL COMMENT '文件类型',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建人',
    `tenant_id` BIGINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 文件内容表
CREATE TABLE IF NOT EXISTS `project_file_content` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `content` LONGBLOB DEFAULT NULL COMMENT '文件内容',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件内容表';

-- 文件关联表
CREATE TABLE IF NOT EXISTS `project_file_link` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `source_type` VARCHAR(32) NOT NULL COMMENT '关联来源类型',
    `source_id` BIGINT NOT NULL COMMENT '关联来源ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_source` (`source_type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件关联表';

-- 文件权限表
CREATE TABLE IF NOT EXISTS `project_file_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `permission` VARCHAR(32) DEFAULT NULL COMMENT '权限类型',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件权限表';

-- 日报表
CREATE TABLE IF NOT EXISTS `project_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(32) DEFAULT NULL COMMENT '日报类型',
    `content` TEXT DEFAULT NULL COMMENT '日报内容',
    `sign` VARCHAR(32) DEFAULT NULL COMMENT '标识',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报表';

-- 日报接收人表
CREATE TABLE IF NOT EXISTS `project_report_receive` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `report_id` BIGINT NOT NULL COMMENT '日报ID',
    `user_id` BIGINT NOT NULL COMMENT '接收人ID',
    `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报接收人表';

-- 日报关联表
CREATE TABLE IF NOT EXISTS `project_report_link` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `report_id` BIGINT NOT NULL COMMENT '日报ID',
    `source_type` VARCHAR(32) NOT NULL COMMENT '关联来源类型',
    `source_id` BIGINT NOT NULL COMMENT '关联来源ID',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报关联表';

-- 会议表
CREATE TABLE IF NOT EXISTS `project_meeting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '会议名称',
    `description` TEXT DEFAULT NULL COMMENT '会议描述',
    `start_at` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_at` DATETIME DEFAULT NULL COMMENT '结束时间',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议表';

-- 会议消息表
CREATE TABLE IF NOT EXISTS `project_meeting_msg` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议消息表';

-- 审批流程实例表
CREATE TABLE IF NOT EXISTS `project_approve_proc` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '审批名称',
    `status` TINYINT DEFAULT NULL COMMENT '审批状态',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程实例表';

-- 审批消息表
CREATE TABLE IF NOT EXISTS `project_approve_msg` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `approve_id` BIGINT NOT NULL COMMENT '审批ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `action` VARCHAR(32) DEFAULT NULL COMMENT '操作类型',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `tenant_id` BIGINT DEFAULT 0,
    `creator` VARCHAR(64) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_approve_id` (`approve_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批消息表';
