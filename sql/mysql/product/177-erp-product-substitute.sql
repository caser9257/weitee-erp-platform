-- 物料主数据级替代料关联（P7）
CREATE TABLE IF NOT EXISTS `erp_product_substitute` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `product_id` bigint NOT NULL COMMENT '主物料编号',
    `substitute_product_id` bigint NOT NULL COMMENT '替代料编号',
    `priority` int NULL DEFAULT 1 COMMENT '优先级',
    `replace_ratio` decimal(10,4) NULL DEFAULT 1.0000 COMMENT '替换比例',
    `remark` varchar(512) NULL COMMENT '备注',
    `creator` varchar(64) NULL COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) NULL COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_substitute` (`product_id`, `substitute_product_id`),
    INDEX `idx_substitute_product_id` (`substitute_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料替代料关联';
