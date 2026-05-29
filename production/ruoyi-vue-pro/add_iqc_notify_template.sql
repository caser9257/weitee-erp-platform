USE `ruoyi-vue-pro`;

UPDATE `system_notify_template`
SET
  `name` = '【IQC】质检任务指派',
  `nickname` = 'IQC助手',
  `content` = '您收到一条新的 IQC 质检任务：{qualityNo}，关联采购入库单：{purchaseInNo}。',
  `type` = 2,
  `params` = '["qualityNo","purchaseInNo"]',
  `status` = 0,
  `remark` = '采购入库 IQC 单指派质检员时发送站内信',
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0'
WHERE `code` = 'erp_iqc_checker_assigned';

INSERT INTO `system_notify_template`
(`name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  '【IQC】质检任务指派',
  'erp_iqc_checker_assigned',
  'IQC助手',
  '您收到一条新的 IQC 质检任务：{qualityNo}，关联采购入库单：{purchaseInNo}。',
  2,
  '["qualityNo","purchaseInNo"]',
  0,
  '采购入库 IQC 单指派质检员时发送站内信',
  '1',
  NOW(),
  '1',
  NOW(),
  b'0'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_notify_template`
  WHERE `code` = 'erp_iqc_checker_assigned' AND `deleted` = b'0'
);
