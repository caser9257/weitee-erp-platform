SELECT jr.id FROM jimu_report jr
WHERE del_flag = '1'
<#if username?? && username?length gt 0>
/* 用户名 */
and jr.CREATE_BY = :username
</#if>
<#if tenantId?? && tenantId?length gt 0>
/* 租户 */
and jr.tenant_id = :tenantId
</#if>