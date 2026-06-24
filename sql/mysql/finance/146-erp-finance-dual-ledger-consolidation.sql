-- =====================================================
-- ERP 双账套最终整合脚本
-- 覆盖范围：
-- 1. 双账套账簿映射表
-- 2. 双账套差异项口径配置表
-- 3. 旧环境差异项计算列兼容补齐
-- 4. 双账套配置、口径配置、结果查询菜单与权限
-- 替代脚本：
-- - 116-erp-finance-dual-ledger-config.sql
-- - 117-erp-finance-dual-ledger-diff-config.sql
-- - 123-erp-finance-dual-ledger-result-menu.sql
-- - 125-erp-finance-dual-ledger-diff-config-columns.sql
-- - 131-erp-finance-dual-ledger-config-permission-fix.sql
-- - 133-erp-finance-dual-ledger-query-permission-hotfix.sql
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `external_ledger_id` BIGINT NOT NULL COMMENT '对外账账簿编号',
    `internal_ledger_id` BIGINT NOT NULL COMMENT '内部账账簿编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_ledger_config_biz_type` (`biz_type`, `deleted`),
    KEY `idx_dual_ledger_config_status` (`status`, `deleted`),
    KEY `idx_dual_ledger_config_external` (`external_ledger_id`, `deleted`),
    KEY `idx_dual_ledger_config_internal` (`internal_ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套账簿映射配置';

CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_diff_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `diff_item_type` INT NOT NULL COMMENT '差异项类型',
    `external_source_type` INT NOT NULL COMMENT '对外账来源类型',
    `external_source_value` INT NULL COMMENT '对外账来源值',
    `internal_source_type` INT NOT NULL COMMENT '内部账来源类型',
    `internal_source_value` INT NULL COMMENT '内部账来源值',
    `calculation_type` INT NOT NULL DEFAULT 3 COMMENT '计算类型',
    `ratio` DECIMAL(10, 4) NULL COMMENT '比例系数',
    `fixed_amount` DECIMAL(18, 2) NULL COMMENT '固定差额',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_ledger_diff_biz_item` (`biz_type`, `diff_item_type`, `deleted`),
    KEY `idx_dual_ledger_diff_status` (`status`, `deleted`),
    KEY `idx_dual_ledger_diff_external` (`external_source_type`, `deleted`),
    KEY `idx_dual_ledger_diff_internal` (`internal_source_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套差异项口径配置';

SET @schema_name := DATABASE();

SET @missing_calculation_type := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'calculation_type'
);

SET @sql := IF(
    @missing_calculation_type = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `calculation_type` INT NOT NULL DEFAULT 3 COMMENT ''计算类型'' AFTER `internal_source_value`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @missing_ratio := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'ratio'
);

SET @sql := IF(
    @missing_ratio = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `ratio` DECIMAL(10,4) NULL COMMENT ''比例系数'' AFTER `calculation_type`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @missing_fixed_amount := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'fixed_amount'
);

SET @sql := IF(
    @missing_fixed_amount = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `fixed_amount` DECIMAL(18,2) NULL COMMENT ''固定差额'' AFTER `ratio`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_finance_dual_ledger_diff_config`
SET `calculation_type` = 3
WHERE `calculation_type` IS NULL;

SET @erp_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (id = 2563 OR path = '/erp' OR name = 'ERP 系统')
    ORDER BY id
    LIMIT 1
);

SET @finance_root_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        path = '/finance'
        OR component_name = 'FormalFinanceRoot'
        OR (parent_id = @erp_root_id AND path = 'finance')
        OR name = '财务管理'
      )
    ORDER BY
      CASE
        WHEN path = '/finance' THEN 0
        WHEN component_name = 'FormalFinanceRoot' THEN 1
        ELSE 2
      END,
      id
    LIMIT 1
);

SET @dual_ledger_config_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-config/index'
        OR component_name IN ('ErpFinanceDualLedgerConfig', 'FormalFinanceDualLedgerConfig')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-config')
      )
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_diff_config_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-diff-config/index'
        OR component_name IN ('ErpFinanceDualLedgerDiffConfig', 'FormalFinanceDualLedgerDiffConfig')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-diff-config')
      )
    ORDER BY id
    LIMIT 1
);

SET @dual_ledger_result_page_id := (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND (
        component = 'erp/finance/dual-ledger-result/index'
        OR component_name IN ('ErpFinanceDualLedgerResult', 'FormalFinanceDualLedgerResult')
        OR (parent_id = @finance_root_id AND path = 'dual-ledger-result')
      )
    ORDER BY id
    LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射', '', 2, 45, @finance_root_id, 'dual-ledger-config', 'ep:connection',
       'erp/finance/dual-ledger-config/index', 'ErpFinanceDualLedgerConfig',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_config_page_id IS NULL;

SET @dual_ledger_config_page_id := COALESCE(@dual_ledger_config_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-config/index'
    ORDER BY id
    LIMIT 1
));

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置', '', 2, 46, @finance_root_id, 'dual-ledger-diff-config', 'ep:operation',
       'erp/finance/dual-ledger-diff-config/index', 'ErpFinanceDualLedgerDiffConfig',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_diff_config_page_id IS NULL;

SET @dual_ledger_diff_config_page_id := COALESCE(@dual_ledger_diff_config_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-diff-config/index'
    ORDER BY id
    LIMIT 1
));

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果查询', '', 2, 70, @finance_root_id, 'dual-ledger-result', 'ep:files',
       'erp/finance/dual-ledger-result/index', 'ErpFinanceDualLedgerResult',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @finance_root_id IS NOT NULL
  AND @dual_ledger_result_page_id IS NULL;

SET @dual_ledger_result_page_id := COALESCE(@dual_ledger_result_page_id, (
    SELECT id
    FROM system_menu
    WHERE deleted = b'0'
      AND component = 'erp/finance/dual-ledger-result/index'
    ORDER BY id
    LIMIT 1
));

SET @dual_ledger_config_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:delete'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:delete'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_result_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-result:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_result_recompute_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-result:recompute'
    ORDER BY id LIMIT 1
);

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射查询', 'erp:finance-dual-ledger-config:query', 3, 1, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射创建', 'erp:finance-dual-ledger-config:create', 3, 2, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_create_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射更新', 'erp:finance-dual-ledger-config:update', 3, 3, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套账簿映射删除', 'erp:finance-dual-ledger-config:delete', 3, 4, @dual_ledger_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_config_page_id IS NOT NULL
  AND @dual_ledger_config_delete_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置查询', 'erp:finance-dual-ledger-diff-config:query', 3, 1, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置创建', 'erp:finance-dual-ledger-diff-config:create', 3, 2, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_create_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置更新', 'erp:finance-dual-ledger-diff-config:update', 3, 3, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_update_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套口径配置删除', 'erp:finance-dual-ledger-diff-config:delete', 3, 4, @dual_ledger_diff_config_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_diff_config_page_id IS NOT NULL
  AND @dual_ledger_diff_delete_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果查询', 'erp:finance-dual-ledger-result:query', 3, 1, @dual_ledger_result_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_result_page_id IS NOT NULL
  AND @dual_ledger_result_query_id IS NULL;

SET @next_menu_id := (SELECT IFNULL(MAX(id), 900000) + 1 FROM system_menu);
INSERT INTO system_menu (
    id, name, permission, type, sort, parent_id, path, icon, component, component_name,
    status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT @next_menu_id, '双账套结果重算', 'erp:finance-dual-ledger-result:recompute', 3, 2, @dual_ledger_result_page_id,
       '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @dual_ledger_result_page_id IS NOT NULL
  AND @dual_ledger_result_recompute_id IS NULL;

SET @dual_ledger_config_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_config_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-config:delete'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_create_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:create'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_update_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:update'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_diff_delete_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-diff-config:delete'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_result_query_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-result:query'
    ORDER BY id LIMIT 1
);
SET @dual_ledger_result_recompute_id := (
    SELECT id FROM system_menu
    WHERE deleted = b'0' AND permission = 'erp:finance-dual-ledger-result:recompute'
    ORDER BY id LIMIT 1
);

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_config_page_id
    UNION ALL SELECT @dual_ledger_diff_config_page_id
    UNION ALL SELECT @dual_ledger_config_query_id
    UNION ALL SELECT @dual_ledger_config_create_id
    UNION ALL SELECT @dual_ledger_config_update_id
    UNION ALL SELECT @dual_ledger_config_delete_id
    UNION ALL SELECT @dual_ledger_diff_query_id
    UNION ALL SELECT @dual_ledger_diff_create_id
    UNION ALL SELECT @dual_ledger_diff_update_id
    UNION ALL SELECT @dual_ledger_diff_delete_id
) t
WHERE r.deleted = b'0'
  AND (r.id = 1 OR r.code IN ('super_admin', 'admin', 'erp_finance_manager'))
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_config_page_id
    UNION ALL SELECT @dual_ledger_diff_config_page_id
    UNION ALL SELECT @dual_ledger_result_page_id
    UNION ALL SELECT @dual_ledger_config_query_id
    UNION ALL SELECT @dual_ledger_diff_query_id
    UNION ALL SELECT @dual_ledger_result_query_id
) t
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_clerk'
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
JOIN (
    SELECT @erp_root_id AS menu_id
    UNION ALL SELECT @finance_root_id
    UNION ALL SELECT @dual_ledger_result_page_id
    UNION ALL SELECT @dual_ledger_result_query_id
) t
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND t.menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = t.menu_id
        AND rm.deleted = b'0'
  );

INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT r.id, @dual_ledger_result_recompute_id, '1', NOW(), '1', NOW(), b'0'
FROM system_role r
WHERE r.deleted = b'0'
  AND r.code = 'erp_finance_manager'
  AND @dual_ledger_result_recompute_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menu rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = @dual_ledger_result_recompute_id
        AND rm.deleted = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
