USE `ruoyi-vue-pro`;
SELECT id, production_order_id, material_id, supply_warehouse_id, required_qty, issued_qty, returned_qty
FROM erp_production_material
ORDER BY id DESC
LIMIT 20;
