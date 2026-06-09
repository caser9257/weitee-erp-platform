SELECT s.item_value as "value",s.item_text as "text" FROM jimu_dict_item s
WHERE dict_id = (SELECT id FROM jimu_dict WHERE dict_code = :dictCode and del_flag = 0)
AND s.status='1'
<#if searchText?? && searchText?length gt 0>
   AND s.item_text like :searchText   
</#if>
ORDER BY s.sort_order ASC