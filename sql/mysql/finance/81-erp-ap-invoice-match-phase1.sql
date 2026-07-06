-- =====================================================
-- ERP ap invoice match phase1
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

CREATE TABLE IF NOT EXISTS `erp_ap_invoice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `invoice_no` VARCHAR(64) NOT NULL COMMENT 'invoice no',
    `invoice_date` DATETIME NOT NULL COMMENT 'invoice date',
    `invoice_type` INT NOT NULL COMMENT 'invoice type',
    `total_count` DECIMAL(24, 6) NULL COMMENT 'total count',
    `matched_count` DECIMAL(24, 6) NULL COMMENT 'matched count',
    `total_amount` DECIMAL(24, 6) NOT NULL COMMENT 'total amount',
    `matched_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'matched amount',
    `unmatched_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'unmatched amount',
    `tolerance_amount` DECIMAL(24, 6) NOT NULL DEFAULT 1 COMMENT 'tolerance amount',
    `difference_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'difference amount',
    `match_status` INT NOT NULL DEFAULT 10 COMMENT 'match status',
    `difference_reason` VARCHAR(255) NULL COMMENT 'difference reason',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ap_invoice_supplier_no` (`supplier_id`, `invoice_no`, `deleted`),
    KEY `idx_ap_invoice_no` (`invoice_no`, `deleted`),
    KEY `idx_ap_invoice_supplier_status` (`supplier_id`, `match_status`, `deleted`),
    KEY `idx_ap_invoice_supplier_type` (`supplier_id`, `invoice_type`, `deleted`),
    KEY `idx_ap_invoice_invoice_date` (`invoice_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP AP invoice';

CREATE TABLE IF NOT EXISTS `erp_ap_invoice_match_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `invoice_id` BIGINT NOT NULL COMMENT 'invoice id',
    `ap_statement_id` BIGINT NOT NULL COMMENT 'ap statement id',
    `source_order_id` BIGINT NULL COMMENT 'source order id',
    `source_order_no` VARCHAR(64) NULL COMMENT 'source order no',
    `source_purchase_in_id` BIGINT NOT NULL COMMENT 'source purchase in id',
    `source_purchase_in_no` VARCHAR(64) NOT NULL COMMENT 'source purchase in no',
    `source_purchase_in_item_id` BIGINT NOT NULL COMMENT 'source purchase in item id',
    `product_id` BIGINT NOT NULL COMMENT 'product id',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `match_count` DECIMAL(24, 6) NOT NULL COMMENT 'match count',
    `match_amount` DECIMAL(24, 6) NOT NULL COMMENT 'match amount',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'status',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_ap_invoice_match_item_invoice_status` (`invoice_id`, `status`, `deleted`),
    KEY `idx_ap_invoice_match_item_statement_status` (`ap_statement_id`, `status`, `deleted`),
    KEY `idx_ap_invoice_match_item_purchase_in_status` (`source_purchase_in_id`, `status`, `deleted`),
    KEY `idx_ap_invoice_match_item_purchase_in_item_status` (`source_purchase_in_item_id`, `status`, `deleted`),
    KEY `idx_ap_invoice_match_item_supplier_status` (`supplier_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP AP invoice match item';

SET @finance_root_id := COALESCE(
    (SELECT `parent_id`
     FROM `system_menu`
     WHERE `component` = 'erp/finance/apar/index' AND `deleted` = b'0'
     ORDER BY `id` LIMIT 1),
    (SELECT `id`
     FROM `system_menu`
     WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0'
     ORDER BY `id` LIMIT 1),
    (SELECT `id`
     FROM `system_menu`
     WHERE `parent_id` <> 0 AND `path` = 'finance' AND `deleted` = b'0'
     ORDER BY `id` LIMIT 1)
);

INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '财务管理', '', 1, 390, 0, '/finance', 'ep:money', '', 'FormalFinanceRoot',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NULL;

SET @finance_root_id := COALESCE(
    @finance_root_id,
    (SELECT `id`
     FROM `system_menu`
     WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0'
     ORDER BY `id` LIMIT 1)
);

SET @ap_invoice_menu_id := (
    SELECT `id`
    FROM `system_menu`
    WHERE `component` = 'erp/finance/ap-invoice/index' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '采购发票匹配', '', 2, 24, @finance_root_id, 'ap-invoice', 'ep:document-checked',
       'erp/finance/ap-invoice/index', 'ErpApInvoice',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @ap_invoice_menu_id IS NULL;

SET @ap_invoice_menu_id := (
    SELECT `id`
    FROM `system_menu`
    WHERE `component` = 'erp/finance/ap-invoice/index' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '采购发票匹配',
    `permission` = '',
    `type` = 2,
    `sort` = 24,
    `parent_id` = @finance_root_id,
    `path` = 'ap-invoice',
    `icon` = 'ep:document-checked',
    `component` = 'erp/finance/ap-invoice/index',
    `component_name` = 'ErpApInvoice',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @ap_invoice_menu_id
  AND @finance_root_id IS NOT NULL;

UPDATE `system_menu`
SET `name` = '采购发票匹配查询',
    `parent_id` = @ap_invoice_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:ap-invoice:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '采购发票匹配创建',
    `parent_id` = @ap_invoice_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:ap-invoice:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '采购发票匹配更新',
    `parent_id` = @ap_invoice_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:ap-invoice:update'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '采购发票匹配查询', 'erp:ap-invoice:query', 3, 1, @ap_invoice_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_invoice_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:ap-invoice:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '采购发票匹配创建', 'erp:ap-invoice:create', 3, 2, @ap_invoice_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_invoice_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:ap-invoice:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '采购发票匹配更新', 'erp:ap-invoice:update', 3, 3, @ap_invoice_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @ap_invoice_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:ap-invoice:update' AND `deleted` = b'0'
  );,
    1
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
    SELECT @finance_root_id AS `menu_id`
    UNION
    SELECT @ap_invoice_menu_id
    UNION
    SELECT `id` FROM `system_menu`
    WHERE `permission` IN ('erp:ap-invoice:query', 'erp:ap-invoice:create', 'erp:ap-invoice:update')
      AND `deleted` = b'0'
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT DISTINCT role_menu.`role_id`, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` role_menu
JOIN (
    SELECT @finance_root_id AS `menu_id`
    UNION
    SELECT @ap_invoice_menu_id
    UNION
    SELECT `id` FROM `system_menu`
    WHERE `permission` IN ('erp:ap-invoice:query', 'erp:ap-invoice:create', 'erp:ap-invoice:update')
      AND `deleted` = b'0'
) target ON target.`menu_id` IS NOT NULL
WHERE role_menu.`deleted` = b'0'
  AND role_menu.`menu_id` IN (
      SELECT @finance_root_id
      UNION
      SELECT `id` FROM `system_menu`
      WHERE `permission` IN (
          'erp:ap-statement:query',
          'erp:ap-estimate:query',
          'erp:finance-prepayment:query',
          'erp:finance-payment:query'
      )
        AND `deleted` = b'0'
  )
  AND NOT EXISTS (
      SELECT 1 FROM `system_role_menu` rm
      WHERE rm.`role_id` = role_menu.`role_id`
        AND rm.`menu_id` = target.`menu_id`
        AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
