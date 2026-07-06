ALTER TABLE `erp_purchase_in`
    ADD COLUMN `stock_in_count` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '累计已入库数量' AFTER `qa_reject_count`;

ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `stock_in_count` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '累计已入库数量' AFTER `qa_reject_count`;

UPDATE `erp_purchase_in`
SET `stock_in_count` = CASE
                           WHEN `stock_in_status` = 20 THEN COALESCE(`qa_pass_count`, 0)
                           ELSE 0
    END,
    `stock_in_status` = CASE
                            WHEN COALESCE(`qa_pass_count`, 0) <= 0 THEN 30
                            WHEN `stock_in_status` = 20 THEN 20
                            ELSE 10
        END;

UPDATE `erp_purchase_in_items` item
    INNER JOIN `erp_purchase_in` pin ON pin.id = item.in_id
SET item.stock_in_count = CASE
                              WHEN pin.stock_in_status = 20 THEN COALESCE(item.qa_pass_count, 0)
                              ELSE 0
    END;

CREATE TABLE IF NOT EXISTS `erp_purchase_in_stock_execute`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no`             VARCHAR(64)  NOT NULL COMMENT '执行单号',
    `purchase_in_id` BIGINT       NOT NULL COMMENT '采购入库编号',
    `status`         INT          NOT NULL DEFAULT 20 COMMENT '状态',
    `remark`         VARCHAR(255) NULL DEFAULT NULL COMMENT '备注',
    `creator`        VARCHAR(64)  NULL DEFAULT '' COMMENT '创建者',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        VARCHAR(64)  NULL DEFAULT '' COMMENT '更新者',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_purchase_in_id` (`purchase_in_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='采购入库执行单';

CREATE TABLE IF NOT EXISTS `erp_purchase_in_stock_execute_item`
(
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `execute_id`          BIGINT         NOT NULL COMMENT '执行单编号',
    `purchase_in_id`      BIGINT         NOT NULL COMMENT '采购入库编号',
    `purchase_in_item_id` BIGINT         NOT NULL COMMENT '采购入库明细编号',
    `product_id`          BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`        BIGINT         NOT NULL COMMENT '仓库编号',
    `count`               DECIMAL(24, 6) NOT NULL COMMENT '本次入库数量',
    `remark`              VARCHAR(255)   NULL DEFAULT NULL COMMENT '备注',
    `creator`             VARCHAR(64)    NULL DEFAULT '' COMMENT '创建者',
    `create_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             VARCHAR(64)    NULL DEFAULT '' COMMENT '更新者',
    `update_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_execute_id` (`execute_id`),
    KEY `idx_purchase_in_id` (`purchase_in_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='采购入库执行单明细';
