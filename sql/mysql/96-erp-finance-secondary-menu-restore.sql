/*
 Target: Restore finance secondary menus backed by real frontend pages
 Schema: ruoyi-vue-pro
 Date: 2026-05-07
 Scope:
   1. Only restore page menus under /finance that have real frontend pages
   2. Hide placeholder-only finance menus introduced by final menu alignment
   3. Keep the change limited to the /finance tree
*/

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;
SET @finance_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/finance'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_finance_restore_targets`;
CREATE TEMPORARY TABLE `tmp_finance_restore_targets` (
  `fixed_id` BIGINT NOT NULL,
  `actual_id` BIGINT NULL,
  `path` VARCHAR(64) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `sort_no` INT NOT NULL,
  `icon` VARCHAR(100) NOT NULL,
  `component` VARCHAR(255) NOT NULL,
  `component_name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`fixed_id`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `tmp_finance_restore_targets`
(`fixed_id`,`actual_id`,`path`,`name`,`sort_no`,`icon`,`component`,`component_name`)
VALUES
  (930184,NULL,'cost','项目成本分析',10,'ep:pie-chart','erp/finance/cost/index','ProjectFinanceCost'),
  (930182,NULL,'apar','应收应付账款池',20,'ep:wallet','erp/finance/apar/index','ProjectFinanceApar'),
  (932301,NULL,'ap-estimate','采购暂估入库',30,'ep:clock','erp/finance/ap-estimate/index','ProjectFinanceApEstimate'),
  (932302,NULL,'ledger','财务账簿',40,'ep:collection','erp/finance/ledger/index','ErpFinanceLedger'),
  (932309,NULL,'dual-ledger-config','双账套账簿映射',45,'ep:connection','erp/finance/dual-ledger-config/index','ErpFinanceDualLedgerConfig'),
  (932310,NULL,'dual-ledger-diff-config','双账套口径配置',46,'ep:operation','erp/finance/dual-ledger-diff-config/index','ErpFinanceDualLedgerDiffConfig'),
  (932303,NULL,'period','会计期间',50,'ep:calendar','erp/finance/period/index','ErpFinancePeriod'),
  (932304,NULL,'subject','财务科目',60,'ep:document','erp/finance/subject/index','ErpFinanceSubject'),
  (932305,NULL,'report-item','报表项目',70,'ep:list','erp/finance/report-item/index','ErpFinanceReportItem'),
  (932306,NULL,'reports','财务报表',80,'ep:data-analysis','erp/finance/reports/index','ErpFinanceReports'),
  (932307,NULL,'voucher','财务凭证',90,'ep:tickets','erp/finance/voucher/index','ErpFinanceVoucher'),
  (932311,NULL,'voucher-template','凭证模板',95,'ep:files','erp/finance/voucher-template/index','ErpFinanceVoucherTemplate'),
  (932308,NULL,'general-ledger','总账',100,'ep:collection-tag','erp/finance/general-ledger/index','ErpFinanceGeneralLedger'),
  (930185,NULL,'receipt','项目收款管理',100,'ep:expand','erp/finance/receipt/index','ErpFinanceReceipt'),
  (930186,NULL,'payment','项目付款管理',110,'ep:caret-right','erp/finance/payment/index','ErpFinancePayment'),
  (930188,NULL,'prepayment','预付款',115,'ep:wallet-filled','erp/finance/prepayment/index','ErpFinancePrepayment'),
  (930181,NULL,'account','结算账户',120,'fa:universal-access','erp/finance/account/index','ErpAccount'),
  (930183,NULL,'assets','固定资产台账',130,'ep:coin','erp/finance/assets/index','ProjectFinanceAssets');

UPDATE `tmp_finance_restore_targets` t
LEFT JOIN (
  SELECT `component`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
    AND `component` IS NOT NULL
    AND `component` <> ''
  GROUP BY `component`
) component_menu
  ON component_menu.`component` COLLATE utf8mb4_unicode_ci = t.`component`
LEFT JOIN (
  SELECT `parent_id`, `path`, MIN(`id`) AS `id`
  FROM `system_menu`
  WHERE `deleted` = b'0'
  GROUP BY `parent_id`, `path`
) path_menu
  ON path_menu.`parent_id` = @finance_root_id
 AND path_menu.`path` COLLATE utf8mb4_unicode_ci = t.`path`
SET t.`actual_id` = COALESCE(component_menu.`id`, path_menu.`id`);

UPDATE `system_menu` sm
JOIN `tmp_finance_restore_targets` t ON t.`actual_id` = sm.`id`
SET sm.`name` = t.`name`,
    sm.`type` = 2,
    sm.`sort` = t.`sort_no`,
    sm.`parent_id` = @finance_root_id,
    sm.`path` = t.`path`,
    sm.`icon` = t.`icon`,
    sm.`component` = t.`component`,
    sm.`component_name` = t.`component_name`,
    sm.`status` = 0,
    sm.`visible` = b'1',
    sm.`keep_alive` = b'1',
    sm.`always_show` = b'1',
    sm.`updater` = '1',
    sm.`update_time` = NOW()
WHERE sm.`deleted` = b'0'
  AND @finance_root_id IS NOT NULL;

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT t.`fixed_id`, t.`name`, '', 2, t.`sort_no`, @finance_root_id, t.`path`, t.`icon`, t.`component`, t.`component_name`,
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `tmp_finance_restore_targets` t
WHERE t.`actual_id` IS NULL
  AND @finance_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu` sm
    WHERE sm.`deleted` = b'0'
      AND sm.`parent_id` = @finance_root_id
      AND sm.`path` COLLATE utf8mb4_unicode_ci = t.`path`
  );

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `parent_id` = @finance_root_id
  AND `type` = 2
  AND `path` IN ('project-cost', 'production-cost', 'statement', 'analysis')
  AND `component` = 'common/menu-placeholder/index';

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, sm.`id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `system_menu` sm
WHERE sm.`deleted` = b'0'
  AND (
    sm.`id` = @finance_root_id
    OR (
      sm.`parent_id` = @finance_root_id
      AND sm.`type` = 2
      AND sm.`status` = 0
      AND sm.`visible` = b'1'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = sm.`id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_finance_restore_targets`;

SET FOREIGN_KEY_CHECKS = 1;
