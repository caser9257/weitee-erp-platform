-- ==============================================================================
-- BPM 审批平台 Schema 整合脚本
-- 创建日期：2026-06-12
-- 说明：本脚本整合并替代以下冲突脚本，形成最终统一 schema：
--   - 54-bpm-approval-platform-incremental.sql（废弃）
--   - 54-bpm-approval-platform-incremental-v2.sql（废弃）
--   - 55-bpm-approval-add-missing-columns.sql（废弃）
--   - 135-bpm-approval-scene-and-snapshot.sql（废弃）
--
-- 设计决策：
--   1. snapshot 表以 135 为基准，status 使用 TINYINT（1=审批中 2=通过 3=驳回 4=撤回）
--   2. snapshot 表补充 DO 需要的字段：approval_id, start_user_id, start_time, end_time
--   3. snapshot 表版本字段统一使用 scheme_version_id（与 DO 一致）
--   4. scene 表补充 owner_user_id 字段
--   5. 唯一键使用 uk_bpm_approval_instance_snapshot_scene_biz (scene_code, biz_id, deleted)
--
-- 幂等性：所有 CREATE 语句使用 IF NOT EXISTS，ALTER 使用存储过程检查列是否存在
-- ==============================================================================

-- ==============================================================================
-- 第一部分：基础表（来自 53，无冲突，此处保留原样）
-- 以下三张表在 53 脚本中已定义，此处不重复创建。
-- 如需完整建表，请执行 53-bpm-approval-platform-foundation.sql。
--   - bpm_approval_scheme
--   - bpm_approval_scheme_version
--   - bpm_approval_rule
-- ==============================================================================

-- ==============================================================================
-- 第二部分：bpm_approval_scheme 补充字段
-- 来源：54 存储过程 + 55 ALTER，合并为幂等存储过程
-- ==============================================================================

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `bpm_approval_consolidation_add_scheme_columns`()
BEGIN
    -- 为 bpm_approval_scheme 添加 owner_user_id
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scheme'
          AND COLUMN_NAME = 'owner_user_id'
    ) THEN
        ALTER TABLE `bpm_approval_scheme`
            ADD COLUMN `owner_user_id` BIGINT NOT NULL DEFAULT 0
            COMMENT '归属用户ID（配置管理员）' AFTER `latest_version_id`;
    END IF;

    -- 为 bpm_approval_scheme 添加 scene_id
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scheme'
          AND COLUMN_NAME = 'scene_id'
    ) THEN
        ALTER TABLE `bpm_approval_scheme`
            ADD COLUMN `scene_id` BIGINT
            COMMENT '关联场景ID' AFTER `biz_type`;
    END IF;

    -- 为 bpm_approval_scheme 添加 owner_user_id 索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scheme'
          AND INDEX_NAME = 'idx_bpm_approval_scheme_owner'
    ) THEN
        ALTER TABLE `bpm_approval_scheme`
            ADD INDEX `idx_bpm_approval_scheme_owner` (`owner_user_id`);
    END IF;

    -- 为 bpm_approval_scheme 添加 scene_id 索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scheme'
          AND INDEX_NAME = 'idx_bpm_approval_scheme_scene'
    ) THEN
        ALTER TABLE `bpm_approval_scheme`
            ADD INDEX `idx_bpm_approval_scheme_scene` (`scene_id`);
    END IF;
END //
DELIMITER ;

CALL `bpm_approval_consolidation_add_scheme_columns`();
DROP PROCEDURE IF EXISTS `bpm_approval_consolidation_add_scheme_columns`;

-- ==============================================================================
-- 第三部分：bpm_approval_scheme_version 补充字段
-- 来源：135 中的 ALTER TABLE
-- ==============================================================================

DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `bpm_approval_consolidation_add_version_columns`()
BEGIN
    -- 为 bpm_approval_scheme_version 添加 notify_json
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scheme_version'
          AND COLUMN_NAME = 'notify_json'
    ) THEN
        ALTER TABLE `bpm_approval_scheme_version`
            ADD COLUMN `notify_json` MEDIUMTEXT DEFAULT NULL
            COMMENT '通知配置 JSON' AFTER `published_time`;
    END IF;
END //
DELIMITER ;

CALL `bpm_approval_consolidation_add_version_columns`();
DROP PROCEDURE IF EXISTS `bpm_approval_consolidation_add_version_columns`;

-- ==============================================================================
-- 第四部分：审批场景表（最终版）
-- 基准：135 + 补充 owner_user_id（来自 54-v2）
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_scene` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '场景编号',
    `scene_code`        VARCHAR(128) NOT NULL COMMENT '场景编码，如 erp.finance.payment.submit',
    `name`              VARCHAR(128) NOT NULL COMMENT '场景名称',
    `module_code`       VARCHAR(64)  NOT NULL COMMENT '模块编码，如 erp_finance',
    `biz_type`          VARCHAR(64)  NOT NULL COMMENT '业务类型，如 payment',
    `action_code`       VARCHAR(64)  NOT NULL COMMENT '动作编码，如 submit',
    `active_scheme_id`  BIGINT       DEFAULT NULL COMMENT '当前生效方案编号',
    `owner_user_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '归属用户ID（配置管理员）',
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态，1-启用 0-禁用',
    `remark`            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_scene_code` (`scene_code`),
    KEY `idx_bpm_approval_scene_active_scheme_id` (`active_scheme_id`),
    KEY `idx_bpm_approval_scene_owner` (`owner_user_id`),
    CONSTRAINT `fk_bpm_approval_scene_active_scheme` FOREIGN KEY (`active_scheme_id`)
        REFERENCES `bpm_approval_scheme` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批场景';

-- 为已有库补充 owner_user_id（135 脚本创建的 scene 表缺少此字段）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `bpm_approval_consolidation_add_scene_columns`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scene'
          AND COLUMN_NAME = 'owner_user_id'
    ) THEN
        ALTER TABLE `bpm_approval_scene`
            ADD COLUMN `owner_user_id` BIGINT NOT NULL DEFAULT 0
            COMMENT '归属用户ID（配置管理员）' AFTER `active_scheme_id`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_scene'
          AND INDEX_NAME = 'idx_bpm_approval_scene_owner'
    ) THEN
        ALTER TABLE `bpm_approval_scene`
            ADD INDEX `idx_bpm_approval_scene_owner` (`owner_user_id`);
    END IF;
END //
DELIMITER ;

CALL `bpm_approval_consolidation_add_scene_columns`();
DROP PROCEDURE IF EXISTS `bpm_approval_consolidation_add_scene_columns`;

-- ==============================================================================
-- 第五部分：审批运行时快照表（最终版）
-- 基准：135（TINYINT status）+ 补充 DO 所需字段
-- 字段来源对照 DO BpmApprovalInstanceSnapshotDO：
--   - approval_id       (String)  ← 54/55 补充
--   - scene_code        (String)  ← 135 基准
--   - biz_id            (String)  ← 135 基准
--   - scheme_id         (Long)    ← 135 基准
--   - scheme_version_id (Long)    ← 135 基准（54 中叫 version_id，以 DO 为准）
--   - rule_id           (Long)    ← 135 基准
--   - process_instance_id        ← 135 基准
--   - process_definition_key     ← 135 基准
--   - context_json      (Map)     ← 135 基准
--   - process_json      (Map)     ← 135 基准
--   - notify_json       (Map)     ← 135 基准
--   - status            (Integer) ← 135 基准，TINYINT 1-审批中 2-通过 3-驳回 4-撤回
--   - result_reason     (String)  ← 135 基准
--   - start_user_id     (Long)    ← 54/55 补充
--   - start_time        (Date)    ← 54/55 补充
--   - end_time          (Date)    ← 54/55 补充
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_instance_snapshot` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '快照编号',
    `approval_id`            VARCHAR(64)  NOT NULL COMMENT '审批ID（业务唯一标识）',
    `scene_code`             VARCHAR(128) NOT NULL COMMENT '场景编码',
    `biz_id`                 VARCHAR(64)  NOT NULL COMMENT '业务单据 ID',
    `scheme_id`              BIGINT       NOT NULL COMMENT '方案编号',
    `scheme_version_id`      BIGINT       NOT NULL COMMENT '方案版本编号',
    `rule_id`                BIGINT       NOT NULL COMMENT '规则编号',
    `process_instance_id`    VARCHAR(64)  DEFAULT NULL COMMENT '流程实例 ID',
    `process_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义 Key',
    `context_json`           MEDIUMTEXT   DEFAULT NULL COMMENT '业务上下文 JSON',
    `process_json`           MEDIUMTEXT   DEFAULT NULL COMMENT '流程 JSON',
    `notify_json`            MEDIUMTEXT   DEFAULT NULL COMMENT '通知配置 JSON',
    `status`                 TINYINT      NOT NULL DEFAULT 1 COMMENT '状态，1-审批中 2-通过 3-驳回 4-撤回',
    `result_reason`          VARCHAR(500) DEFAULT NULL COMMENT '审批结果原因',
    `start_user_id`          BIGINT       NOT NULL DEFAULT 0 COMMENT '发起人ID',
    `start_time`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    `end_time`               DATETIME     DEFAULT NULL COMMENT '结束时间',
    `creator`                VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`              BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_instance_snapshot_scene_biz` (`scene_code`, `biz_id`, `deleted`),
    UNIQUE KEY `uk_bpm_approval_snapshot_approval_id` (`approval_id`),
    KEY `idx_bpm_approval_instance_snapshot_scene_code` (`scene_code`),
    KEY `idx_bpm_approval_instance_snapshot_biz_id` (`biz_id`),
    KEY `idx_bpm_approval_instance_snapshot_process_instance_id` (`process_instance_id`),
    KEY `idx_bpm_approval_snapshot_user` (`start_user_id`),
    KEY `idx_bpm_approval_snapshot_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批运行时快照';

-- 为已有库补充缺失字段（处理 135 或更早版本已创建表的场景）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `bpm_approval_consolidation_add_snapshot_columns`()
BEGIN
    -- 补充 approval_id
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND COLUMN_NAME = 'approval_id'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD COLUMN `approval_id` VARCHAR(64) NOT NULL DEFAULT ''
            COMMENT '审批ID（业务唯一标识）' AFTER `id`;
    END IF;

    -- 补充 approval_id 唯一索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND INDEX_NAME = 'uk_bpm_approval_snapshot_approval_id'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD UNIQUE KEY `uk_bpm_approval_snapshot_approval_id` (`approval_id`);
    END IF;

    -- 补充 start_user_id
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND COLUMN_NAME = 'start_user_id'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD COLUMN `start_user_id` BIGINT NOT NULL DEFAULT 0
            COMMENT '发起人ID' AFTER `result_reason`;
    END IF;

    -- 补充 start_user_id 索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND INDEX_NAME = 'idx_bpm_approval_snapshot_user'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD INDEX `idx_bpm_approval_snapshot_user` (`start_user_id`);
    END IF;

    -- 补充 start_time
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND COLUMN_NAME = 'start_time'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD COLUMN `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
            COMMENT '发起时间' AFTER `start_user_id`;
    END IF;

    -- 补充 end_time
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND COLUMN_NAME = 'end_time'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD COLUMN `end_time` DATETIME DEFAULT NULL
            COMMENT '结束时间' AFTER `start_time`;
    END IF;

    -- 补充 scene_code + biz_id + deleted 唯一约束
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND INDEX_NAME = 'uk_bpm_approval_instance_snapshot_scene_biz'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD UNIQUE KEY `uk_bpm_approval_instance_snapshot_scene_biz` (`scene_code`, `biz_id`, `deleted`);
    END IF;

    -- 补充 status 索引
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'bpm_approval_instance_snapshot'
          AND INDEX_NAME = 'idx_bpm_approval_snapshot_status'
    ) THEN
        ALTER TABLE `bpm_approval_instance_snapshot`
            ADD INDEX `idx_bpm_approval_snapshot_status` (`status`);
    END IF;
END //
DELIMITER ;

CALL `bpm_approval_consolidation_add_snapshot_columns`();
DROP PROCEDURE IF EXISTS `bpm_approval_consolidation_add_snapshot_columns`;

-- ==============================================================================
-- 第六部分：审批任务表（无冲突，使用 IF NOT EXISTS 保证幂等）
-- 来源：54 / 54-v2
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_task` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `task_id`           VARCHAR(64)  NOT NULL COMMENT 'Flowable任务ID',
    `approval_id`       VARCHAR(64)  NOT NULL COMMENT '审批ID',
    `node_id`           VARCHAR(100) NOT NULL COMMENT '节点ID',
    `node_name`         VARCHAR(100) NOT NULL COMMENT '节点名称',
    `assignee_user_id`  BIGINT       DEFAULT NULL COMMENT '审批人ID',
    `status`            VARCHAR(20)  NOT NULL COMMENT '状态（PENDING/APPROVED/REJECTED/RETURNED/TRANSFERRED）',
    `comment`           VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `complete_time`     DATETIME     DEFAULT NULL COMMENT '完成时间',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_task_task_id` (`task_id`),
    KEY `idx_bpm_approval_task_approval` (`approval_id`),
    KEY `idx_bpm_approval_task_assignee` (`assignee_user_id`),
    KEY `idx_bpm_approval_task_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批任务表';

-- ==============================================================================
-- 第七部分：审批记录表（无冲突）
-- 来源：54 / 54-v2
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_record` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `approval_id`       VARCHAR(64)  NOT NULL COMMENT '审批ID',
    `task_id`           VARCHAR(64)  DEFAULT NULL COMMENT '任务ID',
    `action`            VARCHAR(20)  NOT NULL COMMENT '操作（APPROVE/REJECT/RETURN/WITHDRAW/TRANSFER/URGE）',
    `operator_user_id`  BIGINT       NOT NULL COMMENT '操作人ID',
    `comment`           VARCHAR(500) DEFAULT NULL COMMENT '操作意见',
    `target_user_id`    BIGINT       DEFAULT NULL COMMENT '目标用户ID（转办时）',
    `target_node_id`    VARCHAR(100) DEFAULT NULL COMMENT '目标节点ID（驳回时）',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_bpm_approval_record_approval` (`approval_id`),
    KEY `idx_bpm_approval_record_task` (`task_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批记录表';

-- ==============================================================================
-- 第八部分：审批模板表（无冲突）
-- 来源：54 / 54-v2
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_template` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `code`            VARCHAR(100) NOT NULL COMMENT '模板编码',
    `name`            VARCHAR(200) NOT NULL COMMENT '模板名称',
    `category`        VARCHAR(50)  DEFAULT NULL COMMENT '模板分类',
    `icon`            VARCHAR(200) DEFAULT NULL COMMENT '模板图标',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `form_config`     TEXT         DEFAULT NULL COMMENT '表单配置JSON',
    `flow_config`     TEXT         DEFAULT NULL COMMENT '流程配置JSON',
    `notify_config`   TEXT         DEFAULT NULL COMMENT '通知配置JSON',
    `use_count`       BIGINT       DEFAULT 0 COMMENT '使用次数',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `sort`            INT          DEFAULT 0 COMMENT '排序',
    `creator`         VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_template_code` (`code`),
    KEY `idx_bpm_approval_template_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批模板表';

-- ==============================================================================
-- 第九部分：催办记录表（无冲突）
-- 来源：54 / 54-v2
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_urge_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `approval_id`   VARCHAR(64)  NOT NULL COMMENT '审批ID',
    `task_id`       VARCHAR(64)  DEFAULT NULL COMMENT '任务ID',
    `urge_user_id`  BIGINT       NOT NULL COMMENT '催办人ID',
    `urge_message`  VARCHAR(500) DEFAULT NULL COMMENT '催办消息',
    `urge_time`     DATETIME     NOT NULL COMMENT '催办时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_bpm_approval_urge_approval` (`approval_id`),
    KEY `idx_bpm_approval_urge_task` (`task_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '催办记录表';

-- ==============================================================================
-- 第十部分：审批委托配置表（无冲突）
-- 来源：54 / 54-v2
-- ==============================================================================

CREATE TABLE IF NOT EXISTS `bpm_approval_delegation` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '委托ID',
    `user_id`           BIGINT       NOT NULL COMMENT '委托人ID',
    `delegate_user_id`  BIGINT       NOT NULL COMMENT '代理人ID',
    `start_time`        DATETIME     NOT NULL COMMENT '委托开始时间',
    `end_time`          DATETIME     NOT NULL COMMENT '委托结束时间',
    `scene_code`        VARCHAR(100) DEFAULT NULL COMMENT '场景编码（为空表示所有场景）',
    `reason`            VARCHAR(500) DEFAULT NULL COMMENT '委托原因',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_bpm_approval_delegation_user` (`user_id`),
    KEY `idx_bpm_approval_delegation_delegate` (`delegate_user_id`),
    KEY `idx_bpm_approval_delegation_time` (`start_time`, `end_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审批委托配置表';

-- ==============================================================================
-- 第十一部分：预置审批模板 Seed
-- 来源：54 / 54-v2
-- 说明：140 之前的旧增量脚本除了 schema，还承担模板初始化职责。
--      为了让 140 成为可独立执行的最终脚本，这里并入模板 seed。
-- ==============================================================================

INSERT IGNORE INTO `bpm_approval_template`
(`code`, `name`, `category`, `icon`, `description`, `form_config`, `flow_config`, `notify_config`, `status`)
VALUES
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

-- ==============================================================================
-- 第十二部分：审批站内信模板 Seed
-- 来源：54 / 54-v2
-- 说明：system_notify_template 没有唯一索引约束 code，因此这里继续沿用 INSERT IGNORE
--      只保证“新库初始化可用”，不负责清理历史重复数据。
-- ==============================================================================

INSERT IGNORE INTO `system_notify_template`
(`name`, `code`, `nickname`, `content`, `status`, `params`, `type`)
VALUES
('审批任务到达', 'APPROVAL_TASK_ASSIGNED', '审批助手', '您有一条新的审批任务：{{bizTitle}}，请及时处理。[查看详情]({{detailUrl}})', 0, '["bizTitle", "taskName", "detailUrl"]', 10),
('审批通过', 'APPROVAL_APPROVED', '审批助手', '您提交的{{bizTitle}}已审批通过。[查看详情]({{detailUrl}})', 0, '["bizTitle", "detailUrl"]', 10),
('审批驳回', 'APPROVAL_REJECTED', '审批助手', '您提交的{{bizTitle}}已被拒绝，原因：{{reason}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "reason", "detailUrl"]', 10),
('审批撤回', 'APPROVAL_WITHDRAWN', '审批助手', '{{startUserName}}已撤回审批：{{bizTitle}}。', 0, '["bizTitle", "startUserName"]', 10),
('审批超时提醒', 'APPROVAL_TASK_TIMEOUT', '审批助手', '您有一条审批任务已超时：{{bizTitle}}，请尽快处理。[查看详情]({{detailUrl}})', 0, '["bizTitle", "taskName", "detailUrl"]', 10),
('审批催办', 'APPROVAL_TASK_URGE', '审批助手', '{{urgeUserName}}催办您处理审批任务：{{bizTitle}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "urgeUserName", "detailUrl"]', 10),
('任务转办', 'APPROVAL_TASK_TRANSFER', '审批助手', '您有一条新的审批任务（由{{transferUserName}}转办）：{{bizTitle}}。[查看详情]({{detailUrl}})', 0, '["bizTitle", "transferUserName", "detailUrl"]', 10);

-- ==============================================================================
-- 脚本结束
-- ==============================================================================
