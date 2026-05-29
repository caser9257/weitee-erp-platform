SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SET @tenant_id = 1;

SET @finance_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/finance' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @ap_invoice_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/finance/ap-invoice/index' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @finance_expense_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/finance/expense/index' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
  SELECT @finance_root_id AS `menu_id`
  UNION
  SELECT @ap_invoice_menu_id
  UNION
  SELECT @finance_expense_menu_id
  UNION
  SELECT `id` FROM `system_menu`
  WHERE `permission` IN ('erp:ap-invoice:query','erp:ap-invoice:create','erp:ap-invoice:update',
                         'erp:finance-expense:query','erp:finance-expense:create','erp:finance-expense:update',
                         'erp:finance-expense:delete','erp:finance-expense:export','erp:finance-expense:update-status')
    AND `deleted` = b'0'
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

SET @scm_root_id := (SELECT `id` FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @plan_rule_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @plan_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @suggest_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0' ORDER BY `id` LIMIT 1);
SET @netting_policy_menu_id := (SELECT `id` FROM `system_menu` WHERE `component` = 'erp/mrp/netting-policy/index' AND `deleted` = b'0' LIMIT 1);

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM (
  SELECT @scm_root_id AS `menu_id`
  UNION
  SELECT @plan_rule_menu_id
  UNION
  SELECT @plan_menu_id
  UNION
  SELECT @suggest_menu_id
  UNION
  SELECT @netting_policy_menu_id
  UNION
  SELECT `id` FROM `system_menu`
  WHERE `permission` IN ('erp:mrp-plan-rule:query','erp:mrp-plan-rule:create','erp:mrp-plan-rule:update',
                         'erp:mrp-plan-rule:delete','erp:mrp-plan:query','erp:mrp-plan:create',
                         'erp:mrp-plan:update','erp:mrp-plan:delete','erp:mrp-suggest:query',
                         'erp:mrp-suggest:create','erp:mrp-suggest:update','erp:mrp-suggest:delete',
                         'erp:mrp-netting-policy:query','erp:mrp-netting-policy:create',
                         'erp:mrp-netting-policy:update','erp:mrp-netting-policy:delete')
    AND `deleted` = b'0'
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
