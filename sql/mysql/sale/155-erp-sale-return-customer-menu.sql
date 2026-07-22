-- 销售退货与客户管理菜单补充脚本
-- 幂等写法：动态解析父节点 + 定点回查真实菜单 ID + INSERT / UPDATE 双保险
-- 日期: 2026-07-22

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- ========== 1. 动态解析一级父节点 ==========

SET @sales_root_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0' AND parent_id = 0 AND path = '/sales'
  ORDER BY id
  LIMIT 1
);

-- ========== 2. 销售退货页面菜单 ==========

SET @sale_return_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931312)
    AND (
      id = 931312
      OR component = 'erp/sale/return/index'
      OR (parent_id = @sales_root_id AND path = 'return')
    )
  ORDER BY
    CASE
      WHEN id = 931312 THEN 0
      WHEN component = 'erp/sale/return/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931312, '销售退货', 'erp:sale-return:query', 2, 30, @sales_root_id,
       'return', 'ep:minus', 'erp/sale/return/index', 'ErpSaleReturnPage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @sale_return_menu_id IS NULL;

SET @sale_return_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931312)
    AND (
      id = 931312
      OR component = 'erp/sale/return/index'
      OR (parent_id = @sales_root_id AND path = 'return')
    )
  ORDER BY
    CASE
      WHEN id = 931312 THEN 0
      WHEN component = 'erp/sale/return/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '销售退货',
    permission = 'erp:sale-return:query',
    type = 2,
    sort = 30,
    path = 'return',
    icon = 'ep:minus',
    component = 'erp/sale/return/index',
    component_name = 'ErpSaleReturnPage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @sale_return_menu_id
  AND @sales_root_id IS NOT NULL;

-- ========== 3. 客户管理页面菜单 ==========

SET @customer_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931313)
    AND (
      id = 931313
      OR component = 'erp/sale/customer/index'
      OR (parent_id = @sales_root_id AND path = 'customer')
    )
  ORDER BY
    CASE
      WHEN id = 931313 THEN 0
      WHEN component = 'erp/sale/customer/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 931313, '客户管理', 'erp:customer:query', 2, 35, @sales_root_id,
       'customer', 'ep:user', 'erp/sale/customer/index', 'ErpCustomerPage',
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sales_root_id IS NOT NULL
  AND @customer_menu_id IS NULL;

SET @customer_menu_id := (
  SELECT id
  FROM system_menu
  WHERE (deleted = b'0' OR id = 931313)
    AND (
      id = 931313
      OR component = 'erp/sale/customer/index'
      OR (parent_id = @sales_root_id AND path = 'customer')
    )
  ORDER BY
    CASE
      WHEN id = 931313 THEN 0
      WHEN component = 'erp/sale/customer/index' THEN 1
      ELSE 2
    END,
    id
  LIMIT 1
);

UPDATE system_menu
SET parent_id = @sales_root_id,
    name = '客户管理',
    permission = 'erp:customer:query',
    type = 2,
    sort = 35,
    path = 'customer',
    icon = 'ep:user',
    component = 'erp/sale/customer/index',
    component_name = 'ErpCustomerPage',
    status = 0,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'1',
    deleted = b'0',
    updater = 'admin',
    update_time = NOW()
WHERE id = @customer_menu_id
  AND @sales_root_id IS NOT NULL;

-- ========== 4. 按钮权限菜单 ==========

-- 销售退货按钮
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131201, '查询', 'erp:sale-return:query', 3, 1, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131201 OR (deleted = b'0' AND permission = 'erp:sale-return:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131202, '创建', 'erp:sale-return:create', 3, 2, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131202 OR (deleted = b'0' AND permission = 'erp:sale-return:create'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131203, '更新', 'erp:sale-return:update', 3, 3, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131203 OR (deleted = b'0' AND permission = 'erp:sale-return:update'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131204, '删除', 'erp:sale-return:delete', 3, 4, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131204 OR (deleted = b'0' AND permission = 'erp:sale-return:delete'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131205, '更新状态', 'erp:sale-return:update-status', 3, 5, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131205 OR (deleted = b'0' AND permission = 'erp:sale-return:update-status'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131206, '导出', 'erp:sale-return:export', 3, 6, @sale_return_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @sale_return_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131206 OR (deleted = b'0' AND permission = 'erp:sale-return:export'));

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '查询', permission = 'erp:sale-return:query',
    type = 3, sort = 1, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131201 AND @sale_return_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '创建', permission = 'erp:sale-return:create',
    type = 3, sort = 2, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131202 AND @sale_return_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '更新', permission = 'erp:sale-return:update',
    type = 3, sort = 3, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131203 AND @sale_return_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '删除', permission = 'erp:sale-return:delete',
    type = 3, sort = 4, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131204 AND @sale_return_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '更新状态', permission = 'erp:sale-return:update-status',
    type = 3, sort = 5, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131205 AND @sale_return_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @sale_return_menu_id, name = '导出', permission = 'erp:sale-return:export',
    type = 3, sort = 6, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131206 AND @sale_return_menu_id IS NOT NULL;

-- 客户管理按钮
INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131301, '查询', 'erp:customer:query', 3, 1, @customer_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131301 OR (deleted = b'0' AND permission = 'erp:customer:query' AND type = 3));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131302, '创建', 'erp:customer:create', 3, 2, @customer_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131302 OR (deleted = b'0' AND permission = 'erp:customer:create'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131303, '更新', 'erp:customer:update', 3, 3, @customer_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131303 OR (deleted = b'0' AND permission = 'erp:customer:update'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131304, '删除', 'erp:customer:delete', 3, 4, @customer_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131304 OR (deleted = b'0' AND permission = 'erp:customer:delete'));

INSERT INTO system_menu (
  id, name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT 93131305, '导出', 'erp:customer:export', 3, 5, @customer_menu_id, '', '', '', NULL,
       0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM system_menu WHERE id = 93131305 OR (deleted = b'0' AND permission = 'erp:customer:export'));

UPDATE system_menu
SET parent_id = @customer_menu_id, name = '查询', permission = 'erp:customer:query',
    type = 3, sort = 1, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131301 AND @customer_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @customer_menu_id, name = '创建', permission = 'erp:customer:create',
    type = 3, sort = 2, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131302 AND @customer_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @customer_menu_id, name = '更新', permission = 'erp:customer:update',
    type = 3, sort = 3, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131303 AND @customer_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @customer_menu_id, name = '删除', permission = 'erp:customer:delete',
    type = 3, sort = 4, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131304 AND @customer_menu_id IS NOT NULL;

UPDATE system_menu
SET parent_id = @customer_menu_id, name = '导出', permission = 'erp:customer:export',
    type = 3, sort = 5, path = '', icon = '', component = '', component_name = NULL,
    status = 0, visible = b'1', keep_alive = b'1', always_show = b'1', deleted = b'0',
    updater = 'admin', update_time = NOW()
WHERE id = 93131305 AND @customer_menu_id IS NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;