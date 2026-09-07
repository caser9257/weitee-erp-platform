/*
  库存任务失败日志表：记录 afterCommit 中库存扣减 / IQC 移可用失败的任务，支持重试闭环
  幂等：CREATE TABLE IF NOT EXISTS
*/
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `erp_stock_task_failure_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `biz_type`     VARCHAR(64)  NOT NULL COMMENT '业务类型：PRODUCTION_DEDUCT 生产领料扣减 / IQC_MOVE_AVAILABLE IQC合格移可用',
    `biz_id`       BIGINT       NOT NULL COMMENT '业务单据编号（生产工单 / 采购入库单）',
    `item_id`      BIGINT       NULL COMMENT '明细编号（IQC 质检明细，生产领料为空）',
    `product_id`   BIGINT       NOT NULL COMMENT '产品编号',
    `warehouse_id` BIGINT       NOT NULL COMMENT '仓库编号',
    `qty`          DECIMAL(24, 6) NOT NULL COMMENT '数量（扣减为正数，重试方向由业务类型决定）',
    `reason`       VARCHAR(512) NULL COMMENT '失败原因',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 待重试、1 已恢复',
    `retry_count`  INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `creator`      VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '库存任务失败日志';

SET FOREIGN_KEY_CHECKS = 1;
