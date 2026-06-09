-- 审批场景表
CREATE TABLE IF NOT EXISTS `bpm_approval_scene`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '场景编号',
    `scene_code`        VARCHAR(128) NOT NULL COMMENT '场景编码，如 erp.finance.payment.submit',
    `name`              VARCHAR(128) NOT NULL COMMENT '场景名称',
    `module_code`       VARCHAR(64)  NOT NULL COMMENT '模块编码，如 erp_finance',
    `biz_type`          VARCHAR(64)  NOT NULL COMMENT '业务类型，如 payment',
    `action_code`       VARCHAR(64)  NOT NULL COMMENT '动作编码，如 submit',
    `active_scheme_id`  BIGINT       DEFAULT NULL COMMENT '当前生效方案编号',
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
    CONSTRAINT `fk_bpm_approval_scene_active_scheme` FOREIGN KEY (`active_scheme_id`)
        REFERENCES `bpm_approval_scheme` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批场景';

-- 运行时快照表
CREATE TABLE IF NOT EXISTS `bpm_approval_instance_snapshot`
(
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '快照编号',
    `scene_code`            VARCHAR(128) NOT NULL COMMENT '场景编码',
    `biz_id`                VARCHAR(64)  NOT NULL COMMENT '业务单据 ID',
    `scheme_id`             BIGINT       NOT NULL COMMENT '方案编号',
    `scheme_version_id`     BIGINT       NOT NULL COMMENT '方案版本编号',
    `rule_id`               BIGINT       NOT NULL COMMENT '规则编号',
    `process_instance_id`   VARCHAR(64)  DEFAULT NULL COMMENT '流程实例 ID',
    `process_definition_key` VARCHAR(128) DEFAULT NULL COMMENT '流程定义 Key',
    `context_json`          MEDIUMTEXT   DEFAULT NULL COMMENT '业务上下文 JSON',
    `process_json`          MEDIUMTEXT   DEFAULT NULL COMMENT '流程 JSON',
    `notify_json`           MEDIUMTEXT   DEFAULT NULL COMMENT '通知配置 JSON',
    `status`                TINYINT      NOT NULL DEFAULT 1 COMMENT '状态，1-审批中 2-通过 3-驳回 4-撤回',
    `result_reason`         VARCHAR(500) DEFAULT NULL COMMENT '审批结果原因',
    `creator`               VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`               VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`               BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`             BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_instance_snapshot_scene_biz` (`scene_code`, `biz_id`, `deleted`),
    KEY `idx_bpm_approval_instance_snapshot_scene_code` (`scene_code`),
    KEY `idx_bpm_approval_instance_snapshot_biz_id` (`biz_id`),
    KEY `idx_bpm_approval_instance_snapshot_process_instance_id` (`process_instance_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批运行时快照';

-- 为已有数据库补充 notify_json 字段（新建库已由 53 号 SQL 包含）
ALTER TABLE `bpm_approval_scheme_version`
    ADD COLUMN `notify_json` MEDIUMTEXT DEFAULT NULL COMMENT '通知配置 JSON' AFTER `published_time`;

-- 为已有数据库补充 scene_code + biz_id 唯一约束（防止重复提交审批）
-- 注意：1）执行前需确认不存在重复数据，如有重复需先清理
--       2）包含 deleted 字段以兼容软删除场景（已完结审批重新提交时先软删除旧快照）
ALTER TABLE `bpm_approval_instance_snapshot`
    ADD UNIQUE KEY `uk_bpm_approval_instance_snapshot_scene_biz` (`scene_code`, `biz_id`, `deleted`);
