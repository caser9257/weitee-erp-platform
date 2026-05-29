CREATE TABLE IF NOT EXISTS `bpm_approval_scheme`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '方案编号',
    `code`              VARCHAR(64)  NOT NULL COMMENT '方案编码',
    `name`              VARCHAR(128) NOT NULL COMMENT '方案名称',
    `module_code`       VARCHAR(64)  NOT NULL COMMENT '模块编码',
    `biz_type`          VARCHAR(64)  NOT NULL COMMENT '业务类型',
    `remark`            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `active_version_id` BIGINT       DEFAULT NULL COMMENT '当前生效版本编号',
    `latest_version_id` BIGINT       DEFAULT NULL COMMENT '最新版本编号',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bpm_approval_scheme_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批方案';

CREATE TABLE IF NOT EXISTS `bpm_approval_scheme_version`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '版本编号',
    `scheme_id`         BIGINT       NOT NULL COMMENT '方案编号',
    `version_no`        INT          NOT NULL COMMENT '版本号',
    `status`            TINYINT      NOT NULL COMMENT '状态',
    `source_version_id` BIGINT       DEFAULT NULL COMMENT '来源版本编号',
    `source_type`       VARCHAR(32)  NOT NULL DEFAULT 'CREATE' COMMENT '来源类型',
    `design_json`       MEDIUMTEXT   NOT NULL COMMENT '设计器 JSON',
    `change_summary`    VARCHAR(255) DEFAULT NULL COMMENT '变更说明',
    `published_by`      VARCHAR(64)  DEFAULT NULL COMMENT '发布人',
    `published_time`    DATETIME     DEFAULT NULL COMMENT '发布时间',
    `notify_json`       MEDIUMTEXT   DEFAULT NULL COMMENT '通知配置 JSON',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_bpm_approval_scheme_version_scheme_id` (`scheme_id`),
    KEY `idx_bpm_approval_scheme_version_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批方案版本';

CREATE TABLE IF NOT EXISTS `bpm_approval_rule`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则编号',
    `scheme_version_id` BIGINT       NOT NULL COMMENT '方案版本编号',
    `rule_name`         VARCHAR(128) NOT NULL COMMENT '规则名称',
    `rule_type`         VARCHAR(32)  NOT NULL COMMENT '规则类型',
    `priority`          INT          NOT NULL DEFAULT 1 COMMENT '优先级',
    `is_default`        BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否默认规则',
    `condition_json`    MEDIUMTEXT   DEFAULT NULL COMMENT '命中条件 JSON',
    `process_json`      MEDIUMTEXT   DEFAULT NULL COMMENT '流程 JSON',
    `enabled`           BIT(1)       NOT NULL DEFAULT b'1' COMMENT '是否启用',
    `creator`           VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`           VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_bpm_approval_rule_scheme_version_id` (`scheme_version_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='审批规则';
