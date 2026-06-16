-- 产品成本趋势分析页面菜单种子
-- 路径：/erp/finance/cost-product-trend
-- type: 0=目录, 1=菜单, 2=按钮

-- 插入菜单（如果不存在）
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
SELECT 142000, 
       (SELECT id FROM system_menu WHERE name = '财务管理' AND deleted = 0 LIMIT 1),
       '产品成本趋势',
       'erp:finance-cost-product-trend:query',
       'cost-product-trend',
       'erp/finance/cost-product/trend',
       'ErpProductCostTrend',
       'ep:trend-charts',
       26,
       0,
       1,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE path = 'cost-product-trend' AND deleted = 0
);

-- 授权给超级管理员角色
INSERT INTO system_role_menu (role_id, menu_id)
SELECT 1, 142000
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = 142000
);
