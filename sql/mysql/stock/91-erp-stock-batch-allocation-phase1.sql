-- =====================================================
-- ERP stock batch allocation phase1
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_stock_batch_allocation`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `biz_type`       INT            NOT NULL COMMENT '业务类型',
    `biz_id`         BIGINT         NOT NULL COMMENT '业务编号',
    `biz_item_id`    BIGINT         NULL COMMENT '业务明细编号',
    `biz_no`         VARCHAR(64)    NULL COMMENT '业务单号',
    `product_id`     BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`   BIGINT         NOT NULL COMMENT '仓库编号',
    `stock_batch_id` BIGINT         NOT NULL COMMENT '批次库存编号',
    `batch_no`       VARCHAR(64)    NOT NULL COMMENT '批次号',
    `out_qty`        DECIMAL(24, 6) NOT NULL COMMENT '出库数量',
    `inbound_time`   DATETIME       NULL COMMENT '入库时间',
    `produce_date`   DATE           NULL COMMENT '生产日期',
    `expire_date`    DATE           NULL COMMENT '失效日期',
    `remark`         VARCHAR(255)   NULL COMMENT '备注',
    `creator`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_batch_allocation_biz` (`biz_type`, `biz_id`, `deleted`),
    KEY `idx_stock_batch_allocation_batch` (`stock_batch_id`, `deleted`),
    KEY `idx_stock_batch_allocation_product` (`product_id`, `warehouse_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 批次出库分配明细';
