-- 研发 BOM 重复身份盘点（只读，不修改数据）
-- 业务身份：产品 + 去首尾空格后的 BOM 编码 + 版本；空版本统一视为“草稿”。
SELECT
    product_id,
    TRIM(bom_code) AS bom_code,
    COALESCE(NULLIF(TRIM(version), ''), '__DRAFT__') AS version_key,
    COUNT(*) AS duplicate_count,
    GROUP_CONCAT(id ORDER BY id) AS bom_ids
FROM `erp_rd_bom`
WHERE deleted = b'0'
GROUP BY product_id, TRIM(bom_code), COALESCE(NULLIF(TRIM(version), ''), '__DRAFT__')
HAVING COUNT(*) > 1
ORDER BY product_id, bom_code, version_key;
