SELECT 'root_categories' AS label, COUNT(*) AS cnt
FROM erp_product_category
WHERE parent_id = 0 AND id BETWEEN 973010 AND 973054;

SELECT id, name, parent_id, sort
FROM erp_product_category
WHERE id BETWEEN 973010 AND 973054
ORDER BY parent_id, sort, id;

SELECT 'products_972' AS label, COUNT(*) AS cnt
FROM erp_product
WHERE id BETWEEN 972000 AND 972499;

SELECT id, name, category_id, purchase_price
FROM erp_product
WHERE id BETWEEN 972001 AND 972010
ORDER BY id;

SELECT 'bom_972' AS label, COUNT(*) AS cnt
FROM erp_bom
WHERE id BETWEEN 972500 AND 972899;

SELECT 'bom_item_972' AS label, COUNT(*) AS cnt
FROM erp_bom_item
WHERE id BETWEEN 972900 AND 973399;

SELECT 'legacy_products_961' AS label, COUNT(*) AS cnt
FROM erp_product
WHERE id BETWEEN 961001 AND 961030;

SELECT 'legacy_bom_961' AS label, COUNT(*) AS cnt
FROM erp_bom
WHERE id BETWEEN 961200 AND 961219;

SELECT 'legacy_bom_item_961' AS label, COUNT(*) AS cnt
FROM erp_bom_item
WHERE id BETWEEN 961300 AND 961399;