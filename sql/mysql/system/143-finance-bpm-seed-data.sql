/*
 Target: Seed finance BPM approval scenes, schemes and rules
 Schema: current connection database
 Date: 2026-07-07
 Rule:
 1. Revive logically deleted finance approval scenes when they already exist.
 2. Ensure payment and expense scenes both bind an active scheme.
 3. Ensure default rules point to finance process-definition keys instead of test flows.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

INSERT INTO `bpm_approval_scene`
(`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`,
 `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.finance.payment.submit', '付款单审批', 'erp_finance', 'payment', 'submit', NULL, 1,
       1, '财务模块-付款单 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `bpm_approval_scene`
  WHERE `scene_code` = 'erp.finance.payment.submit'
);

INSERT INTO `bpm_approval_scene`
(`scene_code`, `name`, `module_code`, `biz_type`, `action_code`, `active_scheme_id`, `owner_user_id`,
 `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.finance.expense.submit', '费用报销审批', 'erp_finance', 'expense', 'submit', NULL, 1,
       1, '财务模块-费用报销 BPM 审批', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `bpm_approval_scene`
  WHERE `scene_code` = 'erp.finance.expense.submit'
);

UPDATE `bpm_approval_scene`
SET `name` = '付款单审批',
    `module_code` = 'erp_finance',
    `biz_type` = 'payment',
    `action_code` = 'submit',
    `status` = 1,
    `deleted` = b'0',
    `remark` = '财务模块-付款单 BPM 审批',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `scene_code` = 'erp.finance.payment.submit';

UPDATE `bpm_approval_scene`
SET `name` = '费用报销审批',
    `module_code` = 'erp_finance',
    `biz_type` = 'expense',
    `action_code` = 'submit',
    `status` = 1,
    `deleted` = b'0',
    `remark` = '财务模块-费用报销 BPM 审批',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `scene_code` = 'erp.finance.expense.submit';

SET @payment_scene_id := (
  SELECT `id` FROM `bpm_approval_scene`
  WHERE `scene_code` = 'erp.finance.payment.submit' AND `deleted` = b'0'
  LIMIT 1
);

SET @expense_scene_id := (
  SELECT `id` FROM `bpm_approval_scene`
  WHERE `scene_code` = 'erp.finance.expense.submit' AND `deleted` = b'0'
  LIMIT 1
);

INSERT INTO `bpm_approval_scheme`
(`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`,
 `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.finance.payment.submit.scheme', '付款单审批方案', 'erp_finance', 'payment', @payment_scene_id,
       '付款单默认审批方案', NULL, NULL, 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @payment_scene_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_scheme`
    WHERE `code` = 'erp.finance.payment.submit.scheme'
  );

INSERT INTO `bpm_approval_scheme`
(`code`, `name`, `module_code`, `biz_type`, `scene_id`, `remark`, `active_version_id`, `latest_version_id`,
 `owner_user_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'erp.finance.expense.submit.scheme', '费用报销审批方案', 'erp_finance', 'expense', @expense_scene_id,
       '费用报销默认审批方案', NULL, NULL, 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @expense_scene_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_scheme`
    WHERE `code` = 'erp.finance.expense.submit.scheme'
  );

UPDATE `bpm_approval_scheme`
SET `name` = '付款单审批方案',
    `module_code` = 'erp_finance',
    `biz_type` = 'payment',
    `scene_id` = @payment_scene_id,
    `remark` = '付款单默认审批方案',
    `deleted` = b'0',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `code` = 'erp.finance.payment.submit.scheme';

UPDATE `bpm_approval_scheme`
SET `name` = '费用报销审批方案',
    `module_code` = 'erp_finance',
    `biz_type` = 'expense',
    `scene_id` = @expense_scene_id,
    `remark` = '费用报销默认审批方案',
    `deleted` = b'0',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `code` = 'erp.finance.expense.submit.scheme';

SET @payment_scheme_id := (
  SELECT `id` FROM `bpm_approval_scheme`
  WHERE `code` = 'erp.finance.payment.submit.scheme' AND `deleted` = b'0'
  LIMIT 1
);

SET @expense_scheme_id := (
  SELECT `id` FROM `bpm_approval_scheme`
  WHERE `code` = 'erp.finance.expense.submit.scheme' AND `deleted` = b'0'
  LIMIT 1
);

INSERT INTO `bpm_approval_scheme_version`
(`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`,
 `notify_json`, `change_summary`, `published_by`, `published_time`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @payment_scheme_id, 1, 30, NULL, 'CREATE', '{}',
       NULL, '财务审批初始版本', 'admin', NOW(),
       'admin', NOW(), 'admin', NOW(), b'0'
WHERE @payment_scheme_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_scheme_version`
    WHERE `scheme_id` = @payment_scheme_id AND `deleted` = b'0'
  );

INSERT INTO `bpm_approval_scheme_version`
(`scheme_id`, `version_no`, `status`, `source_version_id`, `source_type`, `design_json`,
 `notify_json`, `change_summary`, `published_by`, `published_time`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @expense_scheme_id, 1, 30, NULL, 'CREATE', '{}',
       NULL, '财务审批初始版本', 'admin', NOW(),
       'admin', NOW(), 'admin', NOW(), b'0'
WHERE @expense_scheme_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_scheme_version`
    WHERE `scheme_id` = @expense_scheme_id AND `deleted` = b'0'
  );

SET @payment_version_id := (
  SELECT `id` FROM `bpm_approval_scheme_version`
  WHERE `scheme_id` = @payment_scheme_id AND `deleted` = b'0'
  ORDER BY `version_no` DESC, `id` DESC
  LIMIT 1
);

SET @expense_version_id := (
  SELECT `id` FROM `bpm_approval_scheme_version`
  WHERE `scheme_id` = @expense_scheme_id AND `deleted` = b'0'
  ORDER BY `version_no` DESC, `id` DESC
  LIMIT 1
);

UPDATE `bpm_approval_scheme_version`
SET `status` = 30,
    `deleted` = b'0',
    `updater` = 'admin',
    `update_time` = NOW(),
    `published_by` = COALESCE(`published_by`, 'admin'),
    `published_time` = COALESCE(`published_time`, NOW())
WHERE `id` IN (@payment_version_id, @expense_version_id);

INSERT INTO `bpm_approval_rule`
(`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`,
 `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @payment_version_id, '默认规则', 'DEFAULT', 1, 1, NULL,
       'erp_finance_payment', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @payment_version_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_rule`
    WHERE `scheme_version_id` = @payment_version_id AND `rule_name` = '默认规则'
  );

INSERT INTO `bpm_approval_rule`
(`scheme_version_id`, `rule_name`, `rule_type`, `priority`, `is_default`, `condition_json`,
 `process_json`, `enabled`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @expense_version_id, '默认规则', 'DEFAULT', 1, 1, NULL,
       'erp_finance_expense', 1, 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @expense_version_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_approval_rule`
    WHERE `scheme_version_id` = @expense_version_id AND `rule_name` = '默认规则'
  );

UPDATE `bpm_approval_rule`
SET `rule_type` = 'DEFAULT',
    `priority` = 1,
    `is_default` = 1,
    `condition_json` = NULL,
    `process_json` = 'erp_finance_payment',
    `enabled` = 1,
    `deleted` = b'0',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `scheme_version_id` = @payment_version_id
  AND `rule_name` = '默认规则';

UPDATE `bpm_approval_rule`
SET `rule_type` = 'DEFAULT',
    `priority` = 1,
    `is_default` = 1,
    `condition_json` = NULL,
    `process_json` = 'erp_finance_expense',
    `enabled` = 1,
    `deleted` = b'0',
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `scheme_version_id` = @expense_version_id
  AND `rule_name` = '默认规则';

UPDATE `bpm_approval_scheme`
SET `active_version_id` = @payment_version_id,
    `latest_version_id` = @payment_version_id,
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `id` = @payment_scheme_id;

UPDATE `bpm_approval_scheme`
SET `active_version_id` = @expense_version_id,
    `latest_version_id` = @expense_version_id,
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `id` = @expense_scheme_id;

UPDATE `bpm_approval_scene`
SET `active_scheme_id` = @payment_scheme_id,
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `id` = @payment_scene_id;

UPDATE `bpm_approval_scene`
SET `active_scheme_id` = @expense_scheme_id,
    `updater` = 'admin',
    `update_time` = NOW()
WHERE `id` = @expense_scene_id;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
