-- =====================================================
-- ERP production batch fifo phase1
-- =====================================================

ALTER TABLE `erp_product`
    ADD COLUMN `material_code` VARCHAR(64) NULL COMMENT '物料编码' AFTER `name`,
    ADD COLUMN `batch_control_flag` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否批次管理' AFTER `expiry_day`,
    ADD COLUMN `inspection_required_flag` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否来料检验' AFTER `batch_control_flag`;

CREATE TABLE IF NOT EXISTS `erp_stock_batch`
(
    `id`                 BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `product_id`         BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`       BIGINT         NOT NULL COMMENT '仓库编号',
    `batch_no`           VARCHAR(64)    NOT NULL COMMENT '批次号',
    `inbound_time`       DATETIME       NOT NULL COMMENT '入库时间',
    `produce_date`       DATE           NULL COMMENT '生产日期',
    `expire_date`        DATE           NULL COMMENT '失效日期',
    `total_qty`          DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '当前批次总量',
    `available_qty`      DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '当前批次可用量',
    `locked_qty`         DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '锁定量',
    `virtual_flag`       BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否虚拟批次',
    `source_biz_type`    VARCHAR(32)    NULL COMMENT '来源业务类型',
    `source_biz_id`      BIGINT         NULL COMMENT '来源业务编号',
    `source_biz_item_id` BIGINT         NULL COMMENT '来源业务明细编号',
    `source_biz_no`      VARCHAR(64)    NULL COMMENT '来源业务单号',
    `remark`             VARCHAR(255)   NULL COMMENT '备注',
    `creator`            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`            VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`            BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_batch_product_warehouse_batch` (`product_id`, `warehouse_id`, `batch_no`, `deleted`),
    KEY `idx_stock_batch_available` (`product_id`, `warehouse_id`, `available_qty`, `deleted`),
    KEY `idx_stock_batch_inbound_time` (`product_id`, `warehouse_id`, `inbound_time`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 批次库存';

CREATE TABLE IF NOT EXISTS `erp_stock_batch_record`
(
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `product_id`          BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`        BIGINT         NOT NULL COMMENT '仓库编号',
    `stock_batch_id`      BIGINT         NOT NULL COMMENT '批次库存编号',
    `batch_no`            VARCHAR(64)    NOT NULL COMMENT '批次号',
    `count`               DECIMAL(24, 6) NOT NULL COMMENT '变动数量',
    `after_available_qty` DECIMAL(24, 6) NOT NULL COMMENT '变动后可用量',
    `biz_type`            INT            NOT NULL COMMENT '业务类型',
    `biz_id`              BIGINT         NOT NULL COMMENT '业务编号',
    `biz_item_id`         BIGINT         NULL COMMENT '业务明细编号',
    `biz_no`              VARCHAR(64)    NOT NULL COMMENT '业务单号',
    `remark`              VARCHAR(255)   NULL COMMENT '备注',
    `creator`             VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_batch_record_batch` (`stock_batch_id`, `deleted`),
    KEY `idx_stock_batch_record_biz` (`biz_type`, `biz_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 批次库存流水';

CREATE TABLE IF NOT EXISTS `erp_purchase_in_stock_execute_item_batch`
(
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `execute_item_id`     BIGINT         NOT NULL COMMENT '执行明细编号',
    `purchase_in_item_id` BIGINT         NOT NULL COMMENT '采购入库明细编号',
    `stock_batch_id`      BIGINT         NULL COMMENT '批次库存编号',
    `product_id`          BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`        BIGINT         NOT NULL COMMENT '仓库编号',
    `batch_no`            VARCHAR(64)    NOT NULL COMMENT '批次号',
    `count`               DECIMAL(24, 6) NOT NULL COMMENT '批次数量',
    `inbound_time`        DATETIME       NOT NULL COMMENT '入库时间',
    `produce_date`        DATE           NULL COMMENT '生产日期',
    `expire_date`         DATE           NULL COMMENT '失效日期',
    `remark`              VARCHAR(255)   NULL COMMENT '备注',
    `creator`             VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_purchase_in_execute_item_batch_item` (`execute_item_id`, `deleted`),
    KEY `idx_purchase_in_execute_item_batch_purchase_item` (`purchase_in_item_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='采购入库执行批次明细';

CREATE TABLE IF NOT EXISTS `erp_production_issue`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `issue_no`            VARCHAR(64)  NOT NULL COMMENT '领料单号',
    `production_order_id` BIGINT       NOT NULL COMMENT '生产工单编号',
    `issue_time`          DATETIME     NOT NULL COMMENT '领料时间',
    `status`              INT          NOT NULL DEFAULT 20 COMMENT '状态',
    `issue_amount`        DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '领料金额',
    `remark`              VARCHAR(255) NULL COMMENT '备注',
    `creator`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_production_issue_no` (`issue_no`, `deleted`),
    KEY `idx_production_issue_order` (`production_order_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产领料单';

CREATE TABLE IF NOT EXISTS `erp_production_issue_item`
(
    `id`                     BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `issue_id`               BIGINT         NOT NULL COMMENT '领料单编号',
    `production_material_id` BIGINT         NOT NULL COMMENT '工单物料编号',
    `material_id`            BIGINT         NOT NULL COMMENT '物料编号',
    `warehouse_id`           BIGINT         NOT NULL COMMENT '仓库编号',
    `issue_qty`              DECIMAL(24, 6) NOT NULL COMMENT '领料数量',
    `issue_amount`           DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '领料金额',
    `remark`                 VARCHAR(255)   NULL COMMENT '备注',
    `creator`                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_production_issue_item_issue` (`issue_id`, `deleted`),
    KEY `idx_production_issue_item_material` (`production_material_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产领料明细';

CREATE TABLE IF NOT EXISTS `erp_production_issue_batch`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `issue_item_id`  BIGINT         NOT NULL COMMENT '领料明细编号',
    `stock_batch_id` BIGINT         NOT NULL COMMENT '批次库存编号',
    `batch_no`       VARCHAR(64)    NOT NULL COMMENT '批次号',
    `issue_qty`      DECIMAL(24, 6) NOT NULL COMMENT '领料数量',
    `inbound_time`   DATETIME       NOT NULL COMMENT '入库时间',
    `produce_date`   DATE           NULL COMMENT '生产日期',
    `expire_date`    DATE           NULL COMMENT '失效日期',
    `creator`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_production_issue_batch_item` (`issue_item_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产领料批次明细';

CREATE TABLE IF NOT EXISTS `erp_production_return`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `return_no`           VARCHAR(64)  NOT NULL COMMENT '退料单号',
    `production_order_id` BIGINT       NOT NULL COMMENT '生产工单编号',
    `return_time`         DATETIME     NOT NULL COMMENT '退料时间',
    `status`              INT          NOT NULL DEFAULT 20 COMMENT '状态',
    `remark`              VARCHAR(255) NULL COMMENT '备注',
    `creator`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_production_return_no` (`return_no`, `deleted`),
    KEY `idx_production_return_order` (`production_order_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产退料单';

CREATE TABLE IF NOT EXISTS `erp_production_return_item`
(
    `id`                     BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `return_id`              BIGINT         NOT NULL COMMENT '退料单编号',
    `production_material_id` BIGINT         NOT NULL COMMENT '工单物料编号',
    `material_id`            BIGINT         NOT NULL COMMENT '物料编号',
    `warehouse_id`           BIGINT         NOT NULL COMMENT '仓库编号',
    `return_qty`             DECIMAL(24, 6) NOT NULL COMMENT '退料数量',
    `remark`                 VARCHAR(255)   NULL COMMENT '备注',
    `creator`                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_production_return_item_return` (`return_id`, `deleted`),
    KEY `idx_production_return_item_material` (`production_material_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产退料明细';

CREATE TABLE IF NOT EXISTS `erp_production_return_batch`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `return_item_id`  BIGINT         NOT NULL COMMENT '退料明细编号',
    `issue_batch_id`  BIGINT         NOT NULL COMMENT '领料批次明细编号',
    `stock_batch_id`  BIGINT         NOT NULL COMMENT '批次库存编号',
    `batch_no`        VARCHAR(64)    NOT NULL COMMENT '批次号',
    `return_qty`      DECIMAL(24, 6) NOT NULL COMMENT '退料数量',
    `creator`         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_production_return_batch_item` (`return_item_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='生产退料批次明细';
