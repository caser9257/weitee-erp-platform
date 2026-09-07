-- 200-bpm-test-accounts.sql
-- BPM 审批测试账号创建
-- 目的：创建器件工程师、供应链经理测试账号，并给 admin 分配审批角色，方便开发测试

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
-- USE `ruoyi-vue-pro`;  -- 已连接到正确数据库

-- ============================================================
-- 常量
-- ============================================================
-- 密码 123456 的 BCrypt 哈希
SET @password_hash = '$2a$04$Vk595eYyvcQeKxi6HROnoOocURoMI6vlprElugL6hVoI8qQtBM5Qa';

-- 角色 ID
SET @role_component_engineer = 910005;  -- 器件工程师角色
SET @role_supply_chain_mgr = 910004;    -- 供应链经理角色

-- ============================================================
-- 1. 给 admin (id=1) 分配器件工程师和供应链经理角色
-- ============================================================
INSERT IGNORE INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
VALUES
  (1, @role_component_engineer, '1', NOW(), '1', NOW(), b'0'),
  (1, @role_supply_chain_mgr, '1', NOW(), '1', NOW(), b'0');

-- ============================================================
-- 2. 创建器件工程师测试账号
-- ============================================================
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, status, deleted, create_time, update_time)
VALUES (990001, 'comp_eng', @password_hash, '器件工程师', 'BPM审批测试账号-器件工程师', NULL, '[]', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '器件工程师', remark = 'BPM审批测试账号-器件工程师';

-- 分配器件工程师角色
INSERT IGNORE INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
VALUES (990001, @role_component_engineer, '1', NOW(), '1', NOW(), b'0');

-- ============================================================
-- 3. 创建供应链经理测试账号
-- ============================================================
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, status, deleted, create_time, update_time)
VALUES (990002, 'scm_mgr', @password_hash, '供应链经理', 'BPM审批测试账号-供应链经理', NULL, '[]', 0, b'0', NOW(), NOW())
ON DUPLICATE KEY UPDATE password = @password_hash, nickname = '供应链经理', remark = 'BPM审批测试账号-供应链经理';

-- 分配供应链经理角色
INSERT IGNORE INTO system_user_role (user_id, role_id, creator, create_time, updater, update_time, deleted)
VALUES (990002, @role_supply_chain_mgr, '1', NOW(), '1', NOW(), b'0');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 账号清单
-- ============================================================
-- | 登录名 | 密码 | 角色 | 用途 |
-- |--------|------|------|------|
-- | admin | admin123 | 超级管理员 + 器件工程师 + 供应链经理 | 管理员可发起也可审批 |
-- | comp_eng | 123456 | 器件工程师 | 第一级审批 |
-- | scm_mgr | 123456 | 供应链经理 | 第二级审批 |
