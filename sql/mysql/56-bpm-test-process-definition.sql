-- 测试用流程定义数据（简化版）
-- 创建日期：2026-06-10
-- 说明：插入测试用的流程模型和流程定义信息

-- 注意：此脚本只插入业务表数据，Flowable 表数据会在应用启动时自动创建

-- 1. 插入流程模型数据
INSERT INTO `bpm_model` (`id`, `name`, `key`, `category`, `description`, `type`, `form_type`, `form_id`, `visible`, `manager_user_ids`, `status`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES
('test-model-001', '测试审批流程', 'test_approval', 'test', '用于测试的简单审批流程', 10, 10, NULL, b'1', '[145]', 1, NOW(), NOW(), '145', '145', b'0');

-- 2. 插入流程定义信息（需要先有流程定义，这里先注释掉）
-- INSERT INTO `bpm_process_definition_info` (`id`, `process_definition_id`, `process_definition_key`, `model_id`, `name`, `description`, `category`, `form_type`, `form_id`, `form_custom_create_path`, `form_custom_view_path`, `visible`, `start_user_ids`, `start_dept_ids`, `manager_user_ids`, `allow_cancel_running_process`, `allow_withdraw_task`, `create_time`, `update_time`, `creator`, `updater`, `deleted`) VALUES
-- ('test-def-info-001', 'test-def-001', 'test_approval', 'test-model-001', '测试审批流程', '用于测试的简单审批流程', 'test', 10, NULL, NULL, NULL, b'1', NULL, NULL, '[145]', b'1', b'1', NOW(), NOW(), '145', '145', b'0');
