SELECT
    dict.dict_code,
    item.item_text AS "text",
    item.item_value AS "value"
FROM
    jimu_dict_item item
    INNER JOIN jimu_dict dict ON dict.id = item.dict_id
WHERE dict.dict_code IN (
        :dictCodeList
     )
ORDER BY item.sort_order ASC