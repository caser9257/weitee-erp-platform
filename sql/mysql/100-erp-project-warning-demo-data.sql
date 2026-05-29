/*
 ERP 项目预警演示数据
 作用：
 - 让项目预警页在没有真实业务数据时也能直接看到高/中/低风险项目
 - 让详情抽屉里能回显负责人、阶段、交期和待办任务
 - 仅写测试数据，不改接口契约，不改表结构
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;
SET @password_hash := '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

SET @pc_user_id := 970201;
SET @mc_user_id := 970202;
SET @pm_user_id := 970203;
SET @support_user_id := 970204;

SET @customer_1 := 970101;
SET @customer_2 := 970102;
SET @customer_3 := 970103;

SET @project_1 := 970501;
SET @project_2 := 970502;
SET @project_3 := 970503;
SET @project_4 := 970504;
SET @project_5 := 970505;

SET @task_1 := 970801;
SET @task_2 := 970802;
SET @task_3 := 970803;
SET @task_4 := 970804;
SET @task_5 := 970805;
SET @task_6 := 970806;
SET @task_7 := 970807;
SET @task_8 := 970808;

START TRANSACTION;

DELETE FROM `erp_project_role_task`
WHERE `id` IN (@task_1, @task_2, @task_3, @task_4, @task_5, @task_6, @task_7, @task_8)
   OR `project_id` IN (@project_1, @project_2, @project_3, @project_4, @project_5);

DELETE FROM `erp_project`
WHERE `id` IN (@project_1, @project_2, @project_3, @project_4, @project_5)
   OR `no` IN ('PJ-WARN-20260518-001', 'PJ-WARN-20260518-002', 'PJ-WARN-20260518-003', 'PJ-WARN-20260518-004', 'PJ-WARN-20260518-005');

DELETE FROM `erp_customer`
WHERE `id` IN (@customer_1, @customer_2, @customer_3);

DELETE FROM `system_users`
WHERE `id` IN (@pc_user_id, @mc_user_id, @pm_user_id, @support_user_id)
   OR `username` IN ('pmo_warn_pc', 'pmo_warn_mc', 'pmo_warn_pm', 'pmo_warn_support');

INSERT INTO `system_users`
(`id`, `username`, `password`, `nickname`, `remark`, `dept_id`, `post_ids`, `email`, `mobile`, `sex`,
 `avatar`, `status`, `login_ip`, `login_date`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(@pc_user_id, 'pmo_warn_pc', @password_hash, '陈计划', '项目预警测试账号 - 计划负责人', 103, '[]',
 'pmo_warn_pc@test.local', '13800019901', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@mc_user_id, 'pmo_warn_mc', @password_hash, '林物控', '项目预警测试账号 - 物控负责人', 103, '[]',
 'pmo_warn_mc@test.local', '13800019902', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@pm_user_id, 'pmo_warn_pm', @password_hash, '周项目', '项目预警测试账号 - 项目经理', 103, '[]',
 'pmo_warn_pm@test.local', '13800019903', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@support_user_id, 'pmo_warn_support', @password_hash, '吴协同', '项目预警测试账号 - 协同支持', 103, '[]',
 'pmo_warn_support@test.local', '13800019904', 1, '', 0, '', NULL, '1', NOW(), '1', NOW(), b'0', @tenant_id);

INSERT INTO `erp_customer`
(`id`, `name`, `contact`, `mobile`, `telephone`, `email`, `fax`, `remark`, `status`, `sort`,
 `tax_no`, `tax_percent`, `bank_name`, `bank_account`, `bank_address`, `creator`, `create_time`,
 `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@customer_1, '东海电子有限公司', '陈敏', '13900011001', '021-88001101', 'donghai@test.local', NULL, '项目预警测试客户 A', 0, 1,
 '91310000WARN001', 0.130000, '中国银行', '6217000000000001', '上海浦东', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@customer_2, '华南控制科技有限公司', '刘晨', '13900011002', '0755-88001102', 'huanan@test.local', NULL, '项目预警测试客户 B', 0, 2,
 '91440300WARN002', 0.130000, '工商银行', '6217000000000002', '深圳南山', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@customer_3, '北辰装备制造有限公司', '王磊', '13900011003', '010-88001103', 'beichen@test.local', NULL, '项目预警测试客户 C', 0, 3,
 '91110100WARN003', 0.090000, '建设银行', '6217000000000003', '北京朝阳', '1', NOW(), '1', NOW(), b'0', @tenant_id);

INSERT INTO `erp_project`
(`id`, `no`, `name`, `project_type`, `business_type`, `source_type`, `source_project_id`, `sale_order_id`,
 `project_manager_id`, `plan_coordinator_id`, `material_controller_id`, `owner_dept_id`, `current_stage_code`,
 `risk_level`, `customer_id`, `status`, `pc_status`, `mc_status`, `pc_confirm_time`, `mc_confirm_time`,
 `pc_remark`, `mc_remark`, `delivery_date`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(@project_1, 'PJ-WARN-20260518-001', '东海电子交付项目', 'DELIVERY', 'SELF_RESEARCH', 'SALE_ORDER', NULL, NULL,
 @pm_user_id, @pc_user_id, @mc_user_id, 103, 'PLAN_EXECUTION',
 'HIGH', @customer_1, 1, 'PENDING', 'PENDING', NULL, NULL,
 NULL, NULL, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '高风险演示：计划与物料同时待处理', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@project_2, 'PJ-WARN-20260518-002', '华南控制柜试产项目', 'DELIVERY', 'TOLL_MANUFACTURING', 'SALE_ORDER', NULL, NULL,
 @pm_user_id, @pc_user_id, @support_user_id, 103, 'MATERIAL_RISK',
 'MEDIUM', @customer_2, 1, 'DONE', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL,
 '计划已确认', NULL, DATE_ADD(CURDATE(), INTERVAL 8 DAY), '中风险演示：物料仍待确认', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@project_3, 'PJ-WARN-20260518-003', '北辰装备结项复盘项目', 'PROCESS_VALIDATION', 'CUSTOMER_SUPPLIED', NULL, NULL, NULL,
 @support_user_id, @pc_user_id, @mc_user_id, 103, 'CLOSING_REVIEW',
 'LOW', @customer_3, 1, 'DONE', 'DONE', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY),
 '计划和物料均已确认', '结项资料已同步', DATE_ADD(CURDATE(), INTERVAL 12 DAY), '低风险演示：结项复盘中', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@project_4, 'PJ-WARN-20260518-004', '海城泵体延期项目', 'DELIVERY', 'SELF_RESEARCH', 'SALE_ORDER', NULL, NULL,
 @pm_user_id, @support_user_id, @mc_user_id, 103, 'PLAN_EXECUTION',
 'HIGH', @customer_1, 1, 'PENDING', 'PENDING', NULL, NULL,
 NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '逾期演示：交期已过但责任任务未完成', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@project_5, 'PJ-WARN-20260518-005', '星河自动化验证项目', 'PROCESS_VALIDATION', 'TOLL_MANUFACTURING', NULL, NULL, NULL,
 @pm_user_id, @pc_user_id, @support_user_id, 103, 'MATERIAL_RISK',
 'MEDIUM', @customer_2, 0, 'DONE', 'PENDING', DATE_SUB(NOW(), INTERVAL 11 DAY), NULL,
 '阶段已确认', NULL, DATE_ADD(CURDATE(), INTERVAL 20 DAY), '停用项目：用于筛选和状态展示', '1', NOW(), '1', NOW(), b'0', @tenant_id);

INSERT INTO `erp_project_role_task`
(`id`, `project_id`, `role_code`, `task_type`, `task_status`, `assignee_user_id`, `source_type`, `source_id`,
 `summary`, `due_time`, `finish_time`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
 `deleted`, `tenant_id`)
VALUES
(@task_1, @project_1, 'PC', 'SALE_APPROVED_PLAN_CONFIRM', 'TODO', @pc_user_id, 'SALE_ORDER', 990901,
 '销售订单审批通过，请 PC 确认计划交付节点', DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL, '待计划协调确认', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_2, @project_1, 'MC', 'MRP_SUPPLY_CONFIRM', 'TODO', @mc_user_id, 'MRP_PLAN', 990902,
 'MRP 已生成采购/生产建议，请 MC 确认物料准备策略', DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL, '待物控确认', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_3, @project_2, 'MC', 'MRP_SUPPLY_CONFIRM', 'TODO', @support_user_id, 'MRP_PLAN', 990903,
 '试产物料清单待确认', DATE_ADD(CURDATE(), INTERVAL 5 DAY), NULL, '中风险项目待物控处理', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_4, @project_3, 'PC', 'SALE_APPROVED_PLAN_CONFIRM', 'DONE', @pc_user_id, 'SALE_ORDER', 990904,
 '结项复盘项目计划确认', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), '已完成', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_5, @project_3, 'MC', 'MRP_SUPPLY_CONFIRM', 'DONE', @mc_user_id, 'MRP_PLAN', 990905,
 '结项复盘项目物料确认', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), '已完成', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_6, @project_4, 'PC', 'SALE_APPROVED_PLAN_CONFIRM', 'TODO', @pm_user_id, 'SALE_ORDER', 990906,
 '延期项目计划确认待处理', DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL, '已逾期', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_7, @project_4, 'MC', 'MRP_SUPPLY_CONFIRM', 'TODO', @mc_user_id, 'MRP_PLAN', 990907,
 '延期项目物料准备待处理', DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL, '已逾期', '1', NOW(), '1', NOW(), b'0', @tenant_id),
(@task_8, @project_5, 'MC', 'MRP_SUPPLY_CONFIRM', 'TODO', @support_user_id, 'MRP_PLAN', 990908,
 '验证项目物料准备待处理', DATE_ADD(CURDATE(), INTERVAL 10 DAY), NULL, '用于停用项目详情回显', '1', NOW(), '1', NOW(), b'0', @tenant_id);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
