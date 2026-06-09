select jr.name as reportName,jrs.id,jrs.report_id,jrs.preview_lock_status,jrs.preview_url,jrs.preview_lock,jrs.last_update_time,jrs.term_of_validity,jrs.status from jimu_report_share jrs
join jimu_report jr on jrs.report_id = jr.id and jr.del_flag = 0
where
    jrs.status = '0'
<#if reportName?? && reportName?length gt 0>
    and jr.name like :reportName
</#if> 
<#if username?? && username?length gt 0>
    and jr.create_by = :username
</#if>

<#if tenantId?? && tenantId?length gt 0>
    and jr.tenant_id = :tenantId
</#if>
order by jrs.last_update_time desc

