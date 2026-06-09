select sort_no from jimu_report_category where
del_flag = '0'
<#if parentId?? && parentId?length gt 0>
    and parent_id = :parentId
<#else>
    and (parent_id = '0' or parent_id is null)
</#if>
<#if categoryId?? && categoryId?length gt 0>
    and id = :categoryId
</#if>
order by sort_no asc