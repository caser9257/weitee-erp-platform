-- 137-tenant-id-merge-and-drop.sql
-- 完整清理：合并数据 → 删专用索引 → 删列
-- 执行时间取决于数据量，建议低峰期执行

SET FOREIGN_KEY_CHECKS = 0;
SET SESSION sql_mode = '';

-- ========================================
-- Step 1: 合并所有 tenant_id 到 1
-- ========================================

-- system 模块
UPDATE system_users SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_role SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_role_menu SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_user_role SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_user_post SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_dept SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_post SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_operate_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_login_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_oauth2_access_token SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_oauth2_refresh_token SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_oauth2_approve SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_oauth2_code SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_sms_code SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_social_user SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_social_user_bind SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_social_client SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_notice SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE system_notify_message SET tenant_id = 1 WHERE tenant_id != 1;

-- erp 模块
UPDATE erp_sale_order SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_order_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_order_audit_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_order_reject_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_out SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_out_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_return SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_sale_return_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_order SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_order_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_order_audit_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_order_reject_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_return SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_return_items SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_product SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_product_category SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_product_unit SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_customer SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_supplier SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_warehouse SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_warehouse_category SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_in SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_in_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_out SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_out_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_move SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_move_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_check SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_check_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_record SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_batch_record SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_batch_adjustment SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_batch_allocation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_batch_reservation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_lot SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_stock_reservation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_bom SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_bom_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_bom_item_substitute SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_account SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_project SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_expense SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_expense_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_payment SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_payment_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_payment_allocate SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_receipt SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_receipt_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_voucher SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_voucher_entry SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_voucher_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_voucher_template SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_voucher_template_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_ledger SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_ledger_mapping SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_ledger_role SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_period SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_prepayment SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_prepayment_allocate SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_subject SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_subject_balance SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_report_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_report_item_subject SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_asset SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_asset_candidate SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_asset_depreciation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_audit_operation_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_dual_ledger_config SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_dual_ledger_diff_config SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_dual_ledger_amount_diff_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_dual_write_config SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_finance_dual_write_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_estimate SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_estimate_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_invoice SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_invoice_match_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_statement SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_ap_statement_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_plan SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_demand SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_result SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_result_component SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_shortage SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_netting_policy SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_netting_policy_line SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_policy_binding SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_stock_reservation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_stock_reservation_summary SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_mrp_trace_node SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_material_plan_rule SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_material_issue SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_material_issue_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_material_return SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_material_return_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_order SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_order_step SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_material SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_completion SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_inbound SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_issue SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_issue_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_issue_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_issue_voucher SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_issue_voucher_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_return SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_return_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_return_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_report SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_report_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_finish_quality SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_cost_allocation SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_cost_allocation_result SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_cost_entry SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_man_hour SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_production_suggest SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_suggest SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_quality SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_quality_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_quality_defect SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_quality_round SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_stock_execute SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_stock_execute_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_in_stock_execute_item_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_purchase_source_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_quality_inspection SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_quality_inspection_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_qc_defect_reason SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_qc_sampling_scheme SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_order SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_inbound SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_issue SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_issue_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_issue_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_return SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_return_batch SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_return_item SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_fee SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_outsource_loss_detail SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_process_route SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_process_route_step SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_work_center SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_device SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_device_change_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE erp_project_role_task SET tenant_id = 1 WHERE tenant_id != 1;

-- infra 模块
UPDATE infra_api_access_log SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE infra_api_error_log SET tenant_id = 1 WHERE tenant_id != 1;

-- bpm 模块
UPDATE bpm_category SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_form SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_oa_leave SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_process_definition_info SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_process_expression SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_process_instance_copy SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_process_listener SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_user_group SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_approval_scene SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_approval_scheme SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_approval_scheme_version SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_approval_rule SET tenant_id = 1 WHERE tenant_id != 1;
UPDATE bpm_approval_instance_snapshot SET tenant_id = 1 WHERE tenant_id != 1;

-- ========================================
-- Step 2: 删除 tenant_id 专用索引
-- ========================================

-- erp 模块专用索引
ALTER TABLE `erp_bom` DROP INDEX `idx_erp_bom_tenant_id`;
ALTER TABLE `erp_bom_item` DROP INDEX `idx_erp_bom_item_tenant_id`;
ALTER TABLE `erp_bom_item_substitute` DROP INDEX `idx_erp_bom_item_substitute_tenant_id`;
ALTER TABLE `erp_mrp_demand` DROP INDEX `idx_erp_mrp_demand_tenant_id`;
ALTER TABLE `erp_mrp_result` DROP INDEX `idx_erp_mrp_result_tenant_id`;
ALTER TABLE `erp_mrp_shortage` DROP INDEX `idx_erp_mrp_shortage_tenant_id`;
ALTER TABLE `erp_purchase_suggest` DROP INDEX `idx_erp_purchase_suggest_tenant_id`;
ALTER TABLE `erp_production_suggest` DROP INDEX `idx_erp_production_suggest_tenant_id`;
ALTER TABLE `erp_sale_order_audit_log` DROP INDEX `idx_erp_sale_order_audit_log_tenant_id`;
ALTER TABLE `erp_sale_order_reject_log` DROP INDEX `idx_erp_sale_order_reject_log_tenant_id`;

-- ========================================
-- Step 3: 删除 tenant_id 列
-- （MySQL 会自动从包含该列的复合索引中移除该列）
-- ========================================

-- system 模块
ALTER TABLE `system_users` DROP COLUMN `tenant_id`;
ALTER TABLE `system_role` DROP COLUMN `tenant_id`;
ALTER TABLE `system_role_menu` DROP COLUMN `tenant_id`;
ALTER TABLE `system_user_role` DROP COLUMN `tenant_id`;
ALTER TABLE `system_user_post` DROP COLUMN `tenant_id`;
ALTER TABLE `system_dept` DROP COLUMN `tenant_id`;
ALTER TABLE `system_post` DROP COLUMN `tenant_id`;
ALTER TABLE `system_operate_log` DROP COLUMN `tenant_id`;
ALTER TABLE `system_login_log` DROP COLUMN `tenant_id`;
ALTER TABLE `system_oauth2_access_token` DROP COLUMN `tenant_id`;
ALTER TABLE `system_oauth2_refresh_token` DROP COLUMN `tenant_id`;
ALTER TABLE `system_oauth2_approve` DROP COLUMN `tenant_id`;
ALTER TABLE `system_oauth2_code` DROP COLUMN `tenant_id`;
ALTER TABLE `system_sms_code` DROP COLUMN `tenant_id`;
ALTER TABLE `system_social_user` DROP COLUMN `tenant_id`;
ALTER TABLE `system_social_user_bind` DROP COLUMN `tenant_id`;
ALTER TABLE `system_social_client` DROP COLUMN `tenant_id`;
ALTER TABLE `system_notice` DROP COLUMN `tenant_id`;
ALTER TABLE `system_notify_message` DROP COLUMN `tenant_id`;

-- erp 模块（核心业务表）
ALTER TABLE `erp_sale_order` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_order_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_order_audit_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_order_reject_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_out` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_out_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_return` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_sale_return_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_order` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_order_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_order_audit_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_order_reject_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_return` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_return_items` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_product` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_product_category` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_product_unit` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_customer` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_supplier` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_warehouse` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_warehouse_category` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_in` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_in_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_out` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_out_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_move` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_move_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_check` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_check_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_record` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_batch_record` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_batch_adjustment` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_batch_allocation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_batch_reservation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_lot` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_stock_reservation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_bom` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_bom_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_bom_item_substitute` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_account` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_project` DROP COLUMN `tenant_id`;

-- erp 财务模块
ALTER TABLE `erp_finance_expense` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_expense_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_payment` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_payment_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_payment_allocate` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_receipt` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_receipt_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_voucher` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_voucher_entry` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_voucher_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_voucher_template` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_voucher_template_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_ledger` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_ledger_mapping` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_ledger_role` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_period` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_prepayment` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_prepayment_allocate` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_subject` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_subject_balance` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_report_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_report_item_subject` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_asset` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_asset_candidate` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_asset_depreciation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_audit_operation_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_dual_ledger_config` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_dual_ledger_diff_config` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_dual_ledger_amount_diff_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_dual_write_config` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_finance_dual_write_log` DROP COLUMN `tenant_id`;

-- erp 应付模块
ALTER TABLE `erp_ap_estimate` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_ap_estimate_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_ap_invoice` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_ap_invoice_match_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_ap_statement` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_ap_statement_item` DROP COLUMN `tenant_id`;

-- erp MRP/生产模块
ALTER TABLE `erp_mrp_plan` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_demand` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_result` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_result_component` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_shortage` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_netting_policy` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_netting_policy_line` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_policy_binding` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_stock_reservation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_stock_reservation_summary` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_mrp_trace_node` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_material_plan_rule` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_material_issue` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_material_issue_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_material_return` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_material_return_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_order` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_order_step` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_material` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_completion` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_inbound` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_issue` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_issue_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_issue_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_issue_voucher` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_issue_voucher_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_return` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_return_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_return_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_report` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_report_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_finish_quality` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_cost_allocation` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_cost_allocation_result` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_cost_entry` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_man_hour` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_production_suggest` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_suggest` DROP COLUMN `tenant_id`;

-- erp 质检/委外/其他
ALTER TABLE `erp_purchase_in_quality` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_quality_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_quality_defect` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_quality_round` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_stock_execute` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_stock_execute_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_in_stock_execute_item_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_purchase_source_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_quality_inspection` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_quality_inspection_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_qc_defect_reason` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_qc_sampling_scheme` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_order` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_inbound` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_issue` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_issue_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_issue_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_return` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_return_batch` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_return_item` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_fee` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_outsource_loss_detail` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_process_route` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_process_route_step` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_work_center` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_device` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_device_change_log` DROP COLUMN `tenant_id`;
ALTER TABLE `erp_project_role_task` DROP COLUMN `tenant_id`;

-- infra 模块
ALTER TABLE `infra_api_access_log` DROP COLUMN `tenant_id`;
ALTER TABLE `infra_api_error_log` DROP COLUMN `tenant_id`;

-- bpm 模块
ALTER TABLE `bpm_category` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_form` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_oa_leave` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_process_definition_info` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_process_expression` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_process_instance_copy` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_process_listener` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_user_group` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_scene` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_scheme` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_scheme_version` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_rule` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_instance_snapshot` DROP COLUMN `tenant_id`;

SET FOREIGN_KEY_CHECKS = 1;
