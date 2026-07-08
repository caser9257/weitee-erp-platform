SET NAMES utf8mb4;

INSERT INTO `bpm_process_definition_info`
(`process_definition_id`, `model_id`, `model_type`, `category`, `description`, `form_type`,
 `form_custom_create_path`, `form_custom_view_path`, `sort`, `visible`, `start_user_ids`,
 `manager_user_ids`, `allow_cancel_running_process`, `allow_withdraw_task`, `creator`,
 `create_time`, `updater`, `update_time`, `deleted`)
SELECT `ID_`, 'auto-erp_stock_in_approval', 10, 'test', '其它入库审批', 20,
       '/scm/stock-in', '/scm/stock-in', UNIX_TIMESTAMP(NOW(3)) * 1000, b'1', '',
       '145', b'1', b'1', '145', NOW(), '145', NOW(), b'0'
FROM `ACT_RE_PROCDEF`
WHERE `KEY_` = 'erp_stock_in_approval'
  AND `SUSPENSION_STATE_` = 1
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_process_definition_info`
    WHERE `process_definition_id` = `ACT_RE_PROCDEF`.`ID_`
      AND `deleted` = b'0'
  )
ORDER BY `VERSION_` DESC
LIMIT 1;

INSERT INTO `bpm_process_definition_info`
(`process_definition_id`, `model_id`, `model_type`, `category`, `description`, `form_type`,
 `form_custom_create_path`, `form_custom_view_path`, `sort`, `visible`, `start_user_ids`,
 `manager_user_ids`, `allow_cancel_running_process`, `allow_withdraw_task`, `creator`,
 `create_time`, `updater`, `update_time`, `deleted`)
SELECT `ID_`, 'auto-erp_stock_out_approval', 10, 'test', '其它出库审批', 20,
       '/scm/stock-out', '/scm/stock-out', UNIX_TIMESTAMP(NOW(3)) * 1000 + 1, b'1', '',
       '145', b'1', b'1', '145', NOW(), '145', NOW(), b'0'
FROM `ACT_RE_PROCDEF`
WHERE `KEY_` = 'erp_stock_out_approval'
  AND `SUSPENSION_STATE_` = 1
  AND NOT EXISTS (
    SELECT 1 FROM `bpm_process_definition_info`
    WHERE `process_definition_id` = `ACT_RE_PROCDEF`.`ID_`
      AND `deleted` = b'0'
  )
ORDER BY `VERSION_` DESC
LIMIT 1;
