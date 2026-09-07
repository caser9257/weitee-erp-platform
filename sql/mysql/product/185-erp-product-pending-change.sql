-- 物料修改审批：新增待审变更暂存表
-- 背景：
--   1. 物料所有编辑统一走「修改审批」（暂存表模式）：提交时变更内容写入本表，审批通过才落主表；
--      主表在审批期间保持原值，驳回仅丢弃暂存记录，主表全程不被污染。
--   2. product_id 唯一约束保证一个物料同时只有一笔在途修改，天然防并发双开。
--   3. 关键字段冻结（materialCode/standard 被 BOM 引用禁改）由 Service 层校验，不在表结构表达。
-- 幂等：CREATE TABLE IF NOT EXISTS，可重复执行。
-- 注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化，禁止 tenant_id。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_product_pending_change` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `product_id` bigint NOT NULL COMMENT '物料编号',
    `change_data` json NOT NULL COMMENT '变更字段值（JSON，仅变更字段）',
    `changed_fields` varchar(1024) NOT NULL COMMENT '变更字段名清单，逗号分隔',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1待审 2通过 3驳回 4失败',
    `process_instance_id` varchar(64) NULL COMMENT 'BPM 流程实例编号',
    `reason` varchar(512) NULL COMMENT '提交说明/审批结果原因',
    `creator` varchar(64) NULL COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) NULL COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`),
    INDEX `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料待审变更暂存';
