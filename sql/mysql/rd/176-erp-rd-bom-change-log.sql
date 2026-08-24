-- 研发 BOM 变更记录：字段级 diff
CREATE TABLE IF NOT EXISTS `erp_rd_bom_change_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `bom_id` bigint NOT NULL COMMENT '研发 BOM 编号',
    `change_type` varchar(32) NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型：UPDATE/SUBMIT/APPROVE/REJECT/CANCEL',
    `change_detail` text NULL COMMENT '变更明细（JSON，含字段级 diff）',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_bom_id` (`bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研发 BOM 变更记录';
