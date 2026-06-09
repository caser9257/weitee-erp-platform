SELECT jrc.id FROM jimu_report_category jrc
WHERE jrc.del_flag = 1 AND jrc.SOURCE_TYPE = 'report'
<#if username?? && username?length gt 0>
/* 用户名 */
and jrc.CREATE_BY = :username
</#if>
<#if tenantId?? && tenantId?length gt 0>
/* 租户 */
and jrc.tenant_id = :tenantId
</#if>