SET NAMES utf8mb4;

DELETE FROM system_post WHERE id >= 8;
UPDATE system_post SET level = NULL WHERE id <= 7;

INSERT INTO system_post (code, name, sort, status, level, create_time, update_time, creator, updater, deleted, tenant_id) VALUES 
('CEO', '首席执行官', 1, 0, '高级', NOW(), NOW(), '1', '1', 0, 1),
('CTO', '技术总监', 2, 0, '高级', NOW(), NOW(), '1', '1', 0, 1),
('DEV_MID', '中级开发工程师', 3, 0, '中级', NOW(), NOW(), '1', '1', 0, 1),
('PM_MID', '产品经理', 4, 0, '中级', NOW(), NOW(), '1', '1', 0, 1),
('UI_LOW', '初级UI设计师', 5, 0, '初级', NOW(), NOW(), '1', '1', 0, 1),
('QA_LOW', '初级测试实习生', 6, 0, '初级', NOW(), NOW(), '1', '1', 0, 1);
