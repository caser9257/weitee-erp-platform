select id, name, submit_form, is_multi_sheet from jimu_report
where 
<#if username?? && username ?length gt 0 >
    create_by=:username and
</#if>
<#if tenantId?? && tenantId ?length gt 0 >
    tenant_id=:tenantId and
</#if>
<#if reportId?? && reportId ?length gt 0 >
    id!=:reportId and
</#if>
<#if template??>
   template = :template and
</#if>
    del_flag=0
order by create_time desc