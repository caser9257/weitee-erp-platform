CREATE TABLE IF NOT EXISTS `erp_purchase_source_batch`
(
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `batch_no`               VARCHAR(64)  NOT NULL COMMENT '来源批次号',
    `product_id`             BIGINT       NOT NULL COMMENT '产品编号',
    `purchase_order_id`      BIGINT       NOT NULL COMMENT '采购订单编号',
    `purchase_order_item_id` BIGINT       NOT NULL COMMENT '采购订单明细编号',
    `supplier_id`            BIGINT       NOT NULL COMMENT '供应商编号',
    `status`                 INT          NOT NULL COMMENT '状态',
    `biz_date`               DATE         NOT NULL COMMENT '业务日期',
    `remark`                 VARCHAR(500) NULL DEFAULT '' COMMENT '备注',
    `creator`                VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_source_batch_no` (`batch_no`, `deleted`),
    KEY `idx_purchase_source_batch_product` (`product_id`, `deleted`),
    KEY `idx_purchase_source_batch_order_item` (`purchase_order_item_id`, `deleted`),
    KEY `idx_purchase_source_batch_supplier` (`supplier_id`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 采购来源批次';
