/*
 Target: ERP 采购入库 IQC 复检完成站内信模板
 Schema: ruoyi-vue-pro
 Date: 2026-04-14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

INSERT INTO `system_notify_template`
(`id`, `name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920580,
       '【ERP】采购入库复检完成待入库',
       'erp_iqc_stock_in_ready',
       'ERP采购助手',
       '采购入库单 {purchaseInNo} 的复检已完成，结果：{qaResult}。合格数量：{passCount}，不合格数量：{rejectCount}。请及时处理后续入库。',
       2,
       '["purchaseInNo","qaResult","passCount","rejectCount"]',
       0,
       '采购入库 IQC 复检完成且存在可入库数量时发送',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_notify_template`
    WHERE `code` = 'erp_iqc_stock_in_ready'
      AND `deleted` = b'0'
)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`nickname` = VALUES(`nickname`),
`content` = VALUES(`content`),
`type` = VALUES(`type`),
`params` = VALUES(`params`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

INSERT INTO `system_notify_template`
(`id`, `name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920581,
       '【ERP】采购入库复检全部不合格',
       'erp_iqc_recheck_rejected',
       'ERP采购助手',
       '采购入库单 {purchaseInNo} 的复检已完成，结果：{qaResult}。合格数量：{passCount}，不合格数量：{rejectCount}。当前不可入库，请及时处理采购异常。',
       2,
       '["purchaseInNo","qaResult","passCount","rejectCount"]',
       0,
       '采购入库 IQC 复检全部不合格时发送',
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
WHERE NOT EXISTS (
    SELECT 1
    FROM `system_notify_template`
    WHERE `code` = 'erp_iqc_recheck_rejected'
      AND `deleted` = b'0'
)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`nickname` = VALUES(`nickname`),
`content` = VALUES(`content`),
`type` = VALUES(`type`),
`params` = VALUES(`params`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET FOREIGN_KEY_CHECKS = 1;
