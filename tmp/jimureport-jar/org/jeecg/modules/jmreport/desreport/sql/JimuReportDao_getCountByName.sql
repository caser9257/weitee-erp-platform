SELECT COUNT(name) FROM jimu_report WHERE del_flag = '0' AND NAME = :name
<#if (tenantId )?? && tenantId ?length gt 0>
    /* 租户标识 */
    AND TENANT_ID = :tenantId
</#if>