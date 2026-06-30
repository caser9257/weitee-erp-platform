-- 修复销售/财务占位菜单的组件路径，指向真实页面

UPDATE system_menu
SET component = 'erp/sale/project-initiation/index'
WHERE id = 931302
  AND component = 'common/menu-placeholder/index';

UPDATE system_menu
SET component = 'erp/sale/customer-supplied-material/index'
WHERE id = 931305
  AND component = 'common/menu-placeholder/index';

UPDATE system_menu
SET component = 'erp/sale/incoming-processing/index'
WHERE id = 931306
  AND component = 'common/menu-placeholder/index';

UPDATE system_menu
SET component = 'erp/finance/research-expense/index'
WHERE id = 930187
  AND component = 'common/menu-placeholder/index';
