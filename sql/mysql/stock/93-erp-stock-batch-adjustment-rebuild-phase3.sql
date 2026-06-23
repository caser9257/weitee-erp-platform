-- =====================================================
-- ERP stock batch adjustment / outbound rebuild phase3
-- 1. create stock batch adjustment master table
-- 2. add menu permissions for stock batch query/update/rebuild
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_stock_batch_adjustment`
(
    `id`                   BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `adjust_no`            VARCHAR(64)    NOT NULL COMMENT '调整单号',
    `stock_batch_id`       BIGINT         NOT NULL COMMENT '批次库存编号',
    `product_id`           BIGINT         NOT NULL COMMENT '产品编号',
    `warehouse_id`         BIGINT         NOT NULL COMMENT '仓库编号',
    `batch_no`             VARCHAR(64)    NOT NULL COMMENT '批次号',
    `adjust_type`          INT            NOT NULL COMMENT '调整类型：1 调增，2 调减',
    `adjust_qty`           DECIMAL(24, 6) NOT NULL COMMENT '调整数量',
    `before_total_qty`     DECIMAL(24, 6) NOT NULL COMMENT '调整前总量',
    `before_available_qty` DECIMAL(24, 6) NOT NULL COMMENT '调整前可用量',
    `after_total_qty`      DECIMAL(24, 6) NULL COMMENT '调整后总量',
    `after_available_qty`  DECIMAL(24, 6) NULL COMMENT '调整后可用量',
    `remark`               VARCHAR(255)   NULL COMMENT '备注',
    `creator`              VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            BIGINT         NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_batch_adjustment_no` (`tenant_id`, `adjust_no`, `deleted`),
    KEY `idx_stock_batch_adjustment_batch` (`tenant_id`, `stock_batch_id`, `deleted`, `create_time`),
    KEY `idx_stock_batch_adjustment_product` (`tenant_id`, `product_id`, `warehouse_id`, `deleted`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 批次库存调整单';

SET @stock_batch_adjustment_no_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_adjustment'
      AND INDEX_NAME = 'uk_stock_batch_adjustment_no'
);
SET @stock_batch_adjustment_no_index_sql := IF(
    @stock_batch_adjustment_no_index_exists > 0,
    'SELECT ''uk_stock_batch_adjustment_no already exists''',
    'ALTER TABLE `erp_stock_batch_adjustment` ADD UNIQUE KEY `uk_stock_batch_adjustment_no` (`tenant_id`, `adjust_no`, `deleted`)'
);
PREPARE stock_batch_adjustment_no_index_stmt FROM @stock_batch_adjustment_no_index_sql;
EXECUTE stock_batch_adjustment_no_index_stmt;
DEALLOCATE PREPARE stock_batch_adjustment_no_index_stmt;

SET @stock_batch_adjustment_batch_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_adjustment'
      AND INDEX_NAME = 'idx_stock_batch_adjustment_batch'
);
SET @stock_batch_adjustment_batch_index_sql := IF(
    @stock_batch_adjustment_batch_index_exists > 0,
    'SELECT ''idx_stock_batch_adjustment_batch already exists''',
    'ALTER TABLE `erp_stock_batch_adjustment` ADD KEY `idx_stock_batch_adjustment_batch` (`tenant_id`, `stock_batch_id`, `deleted`, `create_time`)'
);
PREPARE stock_batch_adjustment_batch_index_stmt FROM @stock_batch_adjustment_batch_index_sql;
EXECUTE stock_batch_adjustment_batch_index_stmt;
DEALLOCATE PREPARE stock_batch_adjustment_batch_index_stmt;

SET @stock_batch_adjustment_product_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_adjustment'
      AND INDEX_NAME = 'idx_stock_batch_adjustment_product'
);
SET @stock_batch_adjustment_product_index_sql := IF(
    @stock_batch_adjustment_product_index_exists > 0,
    'SELECT ''idx_stock_batch_adjustment_product already exists''',
    'ALTER TABLE `erp_stock_batch_adjustment` ADD KEY `idx_stock_batch_adjustment_product` (`tenant_id`, `product_id`, `warehouse_id`, `deleted`, `create_time`)'
);
PREPARE stock_batch_adjustment_product_index_stmt FROM @stock_batch_adjustment_product_index_sql;
EXECUTE stock_batch_adjustment_product_index_stmt;
DEALLOCATE PREPARE stock_batch_adjustment_product_index_stmt;

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
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '批次库存查询', 'erp:stock-batch:query',
       3, 11, @stock_batch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @stock_batch_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-batch:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '批次库存调整', 'erp:stock-batch:update',
       3, 12, @stock_batch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @stock_batch_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-batch:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '批次历史重建', 'erp:stock-batch:rebuild',
       3, 13, @stock_batch_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @stock_batch_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-batch:rebuild' AND `deleted` = b'0'
  );
