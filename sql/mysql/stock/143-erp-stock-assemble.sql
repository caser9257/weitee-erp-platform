-- =====================================================
-- ERP 组装拆卸单：主表、明细表与权限
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_stock_assemble`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no`          VARCHAR(64)    NOT NULL COMMENT '单号',
    `action_type` VARCHAR(32)    NOT NULL COMMENT '作业类型：ASSEMBLE/DISASSEMBLE',
    `warehouse_id` BIGINT        NOT NULL COMMENT '仓库编号',
    `product_id`  BIGINT         NOT NULL COMMENT '成品或被拆产品编号',
    `bom_id`      BIGINT         NOT NULL COMMENT 'BOM 编号',
    `count`       DECIMAL(24, 6) NOT NULL COMMENT '作业数量',
    `total_cost`  DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '作业成本',
    `status`      INT            NOT NULL DEFAULT 0 COMMENT '状态：0草稿、20已审核',
    `remark`      VARCHAR(255)   NULL COMMENT '备注',
    `creator`     VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stock_assemble_no` (`no`, `deleted`),
    KEY `idx_stock_assemble_query` (`action_type`, `warehouse_id`, `product_id`, `status`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 组装拆卸单';

CREATE TABLE IF NOT EXISTS `erp_stock_assemble_item`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '编号',
    `assemble_id`     BIGINT         NOT NULL COMMENT '组装拆卸单编号',
    `product_id`      BIGINT         NOT NULL COMMENT '库存产品编号',
    `count`           DECIMAL(24, 6) NOT NULL COMMENT '变更数量',
    `unit_cost`       DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '单位成本快照',
    `stock_direction` INT            NOT NULL COMMENT '库存方向：-1出库、1入库',
    `remark`          VARCHAR(255)   NULL COMMENT '备注',
    `creator`         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         BIT(1)         NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_stock_assemble_item_assemble` (`assemble_id`, `deleted`),
    KEY `idx_stock_assemble_item_product` (`product_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='ERP 组装拆卸单明细';

SET @assemble_menu_id := (
    SELECT `id` FROM `system_menu`
    WHERE `component` = 'erp/stock/assemble/index' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '查询', 'erp:stock-assemble:query', 3, 1,
       @assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-assemble:query' AND `deleted` = b'0');

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '新增', 'erp:stock-assemble:create', 3, 2,
       @assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-assemble:create' AND `deleted` = b'0');

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '修改', 'erp:stock-assemble:update', 3, 3,
       @assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-assemble:update' AND `deleted` = b'0');

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '审核', 'erp:stock-assemble:update-status', 3, 4,
       @assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-assemble:update-status' AND `deleted` = b'0');

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '删除', 'erp:stock-assemble:delete', 3, 5,
       @assemble_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @assemble_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:stock-assemble:delete' AND `deleted` = b'0');
