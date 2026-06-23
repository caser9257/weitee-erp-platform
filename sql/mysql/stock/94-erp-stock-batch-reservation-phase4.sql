-- =====================================================
-- ERP stock batch reservation phase4
-- 1. ensure stock batch locked_qty column exists
-- 2. create stock batch reservation table
-- 3. add menu permission for reservation operations
-- =====================================================

SET @stock_batch_locked_qty_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch'
      AND COLUMN_NAME = 'locked_qty'
);
SET @stock_batch_locked_qty_column_sql := IF(
    @stock_batch_locked_qty_column_exists > 0,
    'SELECT ''erp_stock_batch.locked_qty already exists''',
    'ALTER TABLE `erp_stock_batch` ADD COLUMN `locked_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT ''锁定数量'' AFTER `available_qty`'
);
PREPARE stock_batch_locked_qty_column_stmt FROM @stock_batch_locked_qty_column_sql;
EXECUTE stock_batch_locked_qty_column_stmt;
DEALLOCATE PREPARE stock_batch_locked_qty_column_stmt;

CREATE TABLE IF NOT EXISTS `erp_stock_batch_reservation`
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
    `reserved_qty`   DECIMAL(24, 6) NOT NULL COMMENT '预占数量',
    `inbound_time`   DATETIME       NULL COMMENT '入库时间',
    `produce_date`   DATE           NULL COMMENT '生产日期',
    `expire_date`    DATE           NULL COMMENT '失效日期',
    `remark`         VARCHAR(255)   NULL COMMENT '备注',
    `creator`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`        VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`      BIGINT         NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_batch_reservation_biz_batch` (`tenant_id`, `biz_type`, `biz_id`, `biz_item_id`, `stock_batch_id`, `deleted`),
    KEY `idx_stock_batch_reservation_biz` (`tenant_id`, `biz_type`, `biz_id`, `deleted`),
    KEY `idx_stock_batch_reservation_batch` (`tenant_id`, `stock_batch_id`, `deleted`),
    KEY `idx_stock_batch_reservation_product` (`tenant_id`, `product_id`, `warehouse_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 批次出库预占明细';

SET @stock_batch_reservation_biz_batch_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_reservation'
      AND INDEX_NAME = 'uk_stock_batch_reservation_biz_batch'
);
SET @stock_batch_reservation_biz_batch_index_sql := IF(
    @stock_batch_reservation_biz_batch_index_exists > 0,
    'SELECT ''uk_stock_batch_reservation_biz_batch already exists''',
    'ALTER TABLE `erp_stock_batch_reservation` ADD UNIQUE KEY `uk_stock_batch_reservation_biz_batch` (`tenant_id`, `biz_type`, `biz_id`, `biz_item_id`, `stock_batch_id`, `deleted`)'
);
PREPARE stock_batch_reservation_biz_batch_index_stmt FROM @stock_batch_reservation_biz_batch_index_sql;
EXECUTE stock_batch_reservation_biz_batch_index_stmt;
DEALLOCATE PREPARE stock_batch_reservation_biz_batch_index_stmt;

SET @stock_batch_reservation_biz_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_reservation'
      AND INDEX_NAME = 'idx_stock_batch_reservation_biz'
);
SET @stock_batch_reservation_biz_index_sql := IF(
    @stock_batch_reservation_biz_index_exists > 0,
    'SELECT ''idx_stock_batch_reservation_biz already exists''',
    'ALTER TABLE `erp_stock_batch_reservation` ADD KEY `idx_stock_batch_reservation_biz` (`tenant_id`, `biz_type`, `biz_id`, `deleted`)'
);
PREPARE stock_batch_reservation_biz_index_stmt FROM @stock_batch_reservation_biz_index_sql;
EXECUTE stock_batch_reservation_biz_index_stmt;
DEALLOCATE PREPARE stock_batch_reservation_biz_index_stmt;

SET @stock_batch_reservation_batch_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_reservation'
      AND INDEX_NAME = 'idx_stock_batch_reservation_batch'
);
SET @stock_batch_reservation_batch_index_sql := IF(
    @stock_batch_reservation_batch_index_exists > 0,
    'SELECT ''idx_stock_batch_reservation_batch already exists''',
    'ALTER TABLE `erp_stock_batch_reservation` ADD KEY `idx_stock_batch_reservation_batch` (`tenant_id`, `stock_batch_id`, `deleted`)'
);
PREPARE stock_batch_reservation_batch_index_stmt FROM @stock_batch_reservation_batch_index_sql;
EXECUTE stock_batch_reservation_batch_index_stmt;
DEALLOCATE PREPARE stock_batch_reservation_batch_index_stmt;

SET @stock_batch_reservation_product_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_reservation'
      AND INDEX_NAME = 'idx_stock_batch_reservation_product'
);
SET @stock_batch_reservation_product_index_sql := IF(
    @stock_batch_reservation_product_index_exists > 0,
    'SELECT ''idx_stock_batch_reservation_product already exists''',
    'ALTER TABLE `erp_stock_batch_reservation` ADD KEY `idx_stock_batch_reservation_product` (`tenant_id`, `product_id`, `warehouse_id`, `deleted`)'
);
PREPARE stock_batch_reservation_product_index_stmt FROM @stock_batch_reservation_product_index_sql;
EXECUTE stock_batch_reservation_product_index_stmt;
DEALLOCATE PREPARE stock_batch_reservation_product_index_stmt;

SET @stock_batch_menu_id := (
    SELECT `id`
    FROM `system_menu`
    WHERE `component` = 'erp/stock/stock/index'
      AND `deleted` = b'0'
    ORDER BY `id`
    LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '批次出库预占', 'erp:stock-batch:reserve',
       3, 14, @stock_batch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @stock_batch_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-batch:reserve' AND `deleted` = b'0'
  );
