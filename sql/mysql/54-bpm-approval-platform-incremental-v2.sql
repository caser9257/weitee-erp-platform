-- ERP 通用审批接入平台 - 增量数据库脚本（简化版）
-- 创建日期：2026-06-10
-- 说明：补充审批场景表、运行时快照表、审批任务表、审批记录表、审批模板表

-- 1. 创建审批场景表（如果不存在）
CREATE TABLE IF NOT EXISTS `bpm_approval_scene` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '场景ID',
    `scene_code` VARCHAR(100) NOT NULL COMMENT '场景编码',
    `name` VARCHAR(200) NOT NULL COMMENT '场景名称',
    `module_code` VARCHAR(50) NOT NULL COMMENT '模块编码',
    `biz_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
    `action_code` VARCHAR(50) NOT NULL COMMENT '动作编码',
    `active_scheme_id` BIGINT COMMENT '当前生效方案ID',
    `owner_user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '归属用户ID（配置管理员）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_scene_code` (`scene_code`),
    INDEX `idx_bpm_approval_scene_module_biz` (`module_code`, `biz_type`),
    INDEX `idx_bpm_approval_scene_owner` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批场景表';

-- 2. 创建运行时快照表
CREATE TABLE IF NOT EXISTS `bpm_approval_instance_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '快照ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批ID（业务唯一标识）',
    `scene_code` VARCHAR(100) NOT NULL COMMENT '场景编码',
    `biz_id` VARCHAR(100) NOT NULL COMMENT '业务单据ID',
    `scheme_id` BIGINT NOT NULL COMMENT '方案ID',
    `version_id` BIGINT NOT NULL COMMENT '版本ID',
    `rule_id` BIGINT NOT NULL COMMENT '规则ID',
    `process_instance_id` VARCHAR(64) COMMENT 'Flowable流程实例ID',
    `process_definition_key` VARCHAR(100) COMMENT '流程定义Key',
    `context_json` TEXT COMMENT '业务上下文JSON',
    `process_json` TEXT COMMENT '流程配置JSON',
    `notify_json` TEXT COMMENT '通知配置JSON',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（PENDING/APPROVED/REJECTED/WITHDRAWN）',
    `result_reason` VARCHAR(500) COMMENT '结果原因',
    `start_user_id` BIGINT NOT NULL COMMENT '发起人ID',
    `start_time` DATETIME NOT NULL COMMENT '发起时间',
    `end_time` DATETIME COMMENT '结束时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_snapshot_approval_id` (`approval_id`),
    INDEX `idx_bpm_approval_snapshot_scene` (`scene_code`),
    INDEX `idx_bpm_approval_snapshot_biz` (`biz_id`),
    INDEX `idx_bpm_approval_snapshot_process` (`process_instance_id`),
    INDEX `idx_bpm_approval_snapshot_user` (`start_user_id`),
    INDEX `idx_bpm_approval_snapshot_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批运行时快照表';

-- 3. 创建审批任务表
CREATE TABLE IF NOT EXISTS `bpm_approval_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT 'Flowable任务ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批ID',
    `node_id` VARCHAR(100) NOT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `assignee_user_id` BIGINT COMMENT '审批人ID',
    `status` VARCHAR(20) NOT NULL COMMENT '状态（PENDING/APPROVED/REJECTED/RETURNED/TRANSFERRED）',
    `comment` VARCHAR(500) COMMENT '审批意见',
    `complete_time` DATETIME COMMENT '完成时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_task_task_id` (`task_id`),
    INDEX `idx_bpm_approval_task_approval` (`approval_id`),
    INDEX `idx_bpm_approval_task_assignee` (`assignee_user_id`),
    INDEX `idx_bpm_approval_task_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 4. 创建审批记录表
CREATE TABLE IF NOT EXISTS `bpm_approval_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批ID',
    `task_id` VARCHAR(64) COMMENT '任务ID',
    `action` VARCHAR(20) NOT NULL COMMENT '操作（APPROVE/REJECT/RETURN/WITHDRAW/TRANSFER/URGE）',
    `operator_user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `comment` VARCHAR(500) COMMENT '操作意见',
    `target_user_id` BIGINT COMMENT '目标用户ID（转办时）',
    `target_node_id` VARCHAR(100) COMMENT '目标节点ID（驳回时）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_bpm_approval_record_approval` (`approval_id`),
    INDEX `idx_bpm_approval_record_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 5. 创建审批模板表
CREATE TABLE IF NOT EXISTS `bpm_approval_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `code` VARCHAR(100) NOT NULL COMMENT '模板编码',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `category` VARCHAR(50) COMMENT '模板分类',
    `icon` VARCHAR(200) COMMENT '模板图标',
    `description` VARCHAR(500) COMMENT '模板描述',
    `form_config` TEXT COMMENT '表单配置JSON',
    `flow_config` TEXT COMMENT '流程配置JSON',
    `notify_config` TEXT COMMENT '通知配置JSON',
    `use_count` BIGINT DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_template_code` (`code`),
    INDEX `idx_bpm_approval_template_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板表';

-- 6. 创建催办记录表
CREATE TABLE IF NOT EXISTS `bpm_approval_urge_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批ID',
    `task_id` VARCHAR(64) COMMENT '任务ID',
    `urge_user_id` BIGINT NOT NULL COMMENT '催办人ID',
    `urge_message` VARCHAR(500) COMMENT '催办消息',
    `urge_time` DATETIME NOT NULL COMMENT '催办时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_bpm_approval_urge_approval` (`approval_id`),
    INDEX `idx_bpm_approval_urge_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催办记录表';

-- 7. 创建审批委托配置表
CREATE TABLE IF NOT EXISTS `bpm_approval_delegation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '委托ID',
    `user_id` BIGINT NOT NULL COMMENT '委托人ID',
    `delegate_user_id` BIGINT NOT NULL COMMENT '代理人ID',
    `start_time` DATETIME NOT NULL COMMENT '委托开始时间',
    `end_time` DATETIME NOT NULL COMMENT '委托结束时间',
    `scene_code` VARCHAR(100) COMMENT '场景编码（为空表示所有场景）',
    `reason` VARCHAR(500) COMMENT '委托原因',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_bpm_approval_delegation_user` (`user_id`),
    INDEX `idx_bpm_approval_delegation_delegate` (`delegate_user_id`),
    INDEX `idx_bpm_approval_delegation_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批委托配置表';

-- 8. 插入预置模板数据（使用 INSERT IGNORE 避免重复插入）
INSERT IGNORE INTO `bpm_approval_template` (`code`, `name`, `category`, `icon`, `description`, `form_config`, `flow_config`, `notify_config`, `status`) VALUES
('LEAVE_APPROVAL', '请假审批', 'OA', 'ep:calendar', '适用于员工请假申请', 
 '{"fields":[{"field":"leaveType","label":"请假类型","type":"select","required":true,"options":[{"value":"ANNUAL","label":"年假"},{"value":"SICK","label":"病假"},{"value":"PERSONAL","label":"事假"}]},{"field":"startDate","label":"开始日期","type":"date","required":true},{"field":"endDate","label":"结束日期","type":"date","required":true},{"field":"reason","label":"请假原因","type":"textarea","required":true}]}',
 '{"nodes":[{"nodeId":"deptManager","name":"直属主管审批","type":"APPROVAL","approverType":"DEPT_LEADER","approveMethod":"ANY"},{"nodeId":"hr","name":"HR审批","type":"APPROVAL","approverType":"ROLE","roleCode":"hr","approveMethod":"ANY"}]}',
 '{"scenes":[{"sceneCode":"TASK_ASSIGNED","enabled":true},{"sceneCode":"PROCESS_APPROVE","enabled":true},{"sceneCode":"PROCESS_REJECT","enabled":true}]}',
 0),
('EXPENSE_APPROVAL', '报销审批', '财务', 'ep:money', '适用于费用报销申请',
 '{"fields":[{"field":"expenseType","label":"费用类型","type":"select","required":true,"options":[{"value":"TRAVEL","label":"差旅费"},{"value":"MEAL","label":"餐饮费"},{"value":"OFFICE","label":"办公费"}]},{"field":"amount","label":"金额","type":"number","required":true},{"field":"invoiceNo","label":"发票号","type":"text","required":false},{"field":"reason","label":"费用说明","type":"textarea","required":true}]}',
 '{"nodes":[{"nodeId":"deptManager","name":"部门经理审批","type":"APPROVAL","approverType":"DEPT_LEADER","approveMethod":"ANY"},{"nodeId":"finance","name":"财务审批","type":"APPROVAL","approverType":"ROLE","roleCode":"finance","approveMethod":"ANY"}]}',
 '{"scenes":[{"sceneCode":"TASK_ASSIGNED","enabled":true},{"sceneCode":"PROCESS_APPROVE","enabled":true},{"sceneCode":"PROCESS_REJECT","enabled":true}]}',
 0),
('PURCHASE_APPROVAL', '采购审批', '采购', 'ep:shopping-cart', '适用于采购订单申请',
 '{"fields":[{"field":"supplierName","label":"供应商","type":"text","required":true},{"field":"amount","label":"金额","type":"number","required":true},{"field":"items","label":"采购明细","type":"table","required":true}]}',
 '{"nodes":[{"nodeId":"deptManager","name":"部门经理审批","type":"APPROVAL","approverType":"DEPT_LEADER","approveMethod":"ANY"},{"nodeId":"purchase","name":"采购经理审批","type":"APPROVAL","approverType":"ROLE","roleCode":"purchase_manager","approveMethod":"ANY"},{"nodeId":"finance","name":"财务审批","type":"APPROVAL","approverType":"ROLE","roleCode":"finance","approveMethod":"ANY"}]}',
 '{"scenes":[{"sceneCode":"TASK_ASSIGNED","enabled":true},{"sceneCode":"PROCESS_APPROVE","enabled":true},{"sceneCode":"PROCESS_REJECT","enabled":true}]}',
 0),
('SALE_APPROVAL', '销售审批', '销售', 'ep:sell', '适用于销售订单申请',
 '{"fields":[{"field":"customerName","label":"客户","type":"text","required":true},{"field":"amount","label":"金额","type":"number","required":true},{"field":"items","label":"产品明细","type":"table","required":true}]}',
 '{"nodes":[{"nodeId":"salesManager","name":"销售经理审批","type":"APPROVAL","approverType":"ROLE","roleCode":"sales_manager","approveMethod":"ANY"},{"nodeId":"finance","name":"财务审批","type":"APPROVAL","approverType":"ROLE","roleCode":"finance","approveMethod":"ANY"}]}',
 '{"scenes":[{"sceneCode":"TASK_ASSIGNED","enabled":true},{"sceneCode":"PROCESS_APPROVE","enabled":true},{"sceneCode":"PROCESS_REJECT","enabled":true}]}',
 0),
('PAYMENT_APPROVAL', '付款审批', '财务', 'ep:credit-card', '适用于付款单申请',
 '{"fields":[{"field":"paymentType","label":"付款类型","type":"select","required":true,"options":[{"value":"SUPPLIER","label":"供应商付款"},{"value":"SALARY","label":"工资发放"},{"value":"OTHER","label":"其他付款"}]},{"field":"amount","label":"金额","type":"number","required":true},{"field":"bankAccount","label":"收款账户","type":"text","required":true},{"field":"reason","label":"付款原因","type":"textarea","required":true}]}',
 '{"nodes":[{"nodeId":"deptManager","name":"部门经理审批","type":"APPROVAL","approverType":"DEPT_LEADER","approveMethod":"ANY"},{"nodeId":"finance","name":"财务审批","type":"APPROVAL","approverType":"ROLE","roleCode":"finance","approveMethod":"ANY"},{"nodeId":"cfo","name":"财务总监审批","type":"APPROVAL","approverType":"ROLE","roleCode":"cfo","approveMethod":"ANY"}]}',
 '{"scenes":[{"sceneCode":"TASK_ASSIGNED","enabled":true},{"sceneCode":"PROCESS_APPROVE","enabled":true},{"sceneCode":"PROCESS_REJECT","enabled":true}]}',
 0);

-- 9. 插入站内信通知模板（使用 INSERT IGNORE 避免重复插入）
INSERT IGNORE INTO `system_notify_template` (`name`, `code`, `nickname`, `content`, `status`, `params`, `type`) VALUES
('审批任务到达', 'APPROVAL_TASK_ASSIGNED', '审批助手', '您有一条新的审批任务：{{bizTitle}}，请及时处理。[查看详情]({{detailUrl}})', 0, '["bizTitle", "taskName", "detailUrl"]', 10),
('审批通过', 'APPROVAL_APPROVED', '审批助手', '您提交的{{bizTitle}}已审批通过。[查看详情]({{detailUrl}})', 0, '["bizTitle", "detailUrl"]', 10),
('审批驳回', 'APPROVAL_REJECTED', '审批助手', '您提交的{{bizTitle}}已被拒绝，原因：{{reason}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "reason", "detailUrl"]', 10),
('审批撤回', 'APPROVAL_WITHDRAWN', '审批助手', '{{startUserName}}已撤回审批：{{bizTitle}}。', 0, '["bizTitle", "startUserName"]', 10),
('审批超时提醒', 'APPROVAL_TASK_TIMEOUT', '审批助手', '您有一条审批任务已超时：{{bizTitle}}，请尽快处理。[查看详情]({{detailUrl}})', 0, '["bizTitle", "taskName", "detailUrl"]', 10),
('审批催办', 'APPROVAL_TASK_URGE', '审批助手', '{{urgeUserName}}催办您处理审批任务：{{bizTitle}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "urgeUserName", "detailUrl"]', 10),
('任务转办', 'APPROVAL_TASK_TRANSFER', '审批助手', '您有一条新的审批任务（由{{transferUserName}}转办）：{{bizTitle}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "transferUserName", "detailUrl"]', 10);
