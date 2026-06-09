SELECT jr.ID,jr.NAME,jr.CODE,jr.TYPE,jr.template,jr.thumb,jrs.preview_url as shareViewUrl FROM jimu_report jr
left join jimu_report_share jrs on jrs.report_id = jr.id
left join jimu_report_category jrc on jrc.id = jr.type
WHERE
(jrc.del_flag = 0 OR (jr.type = '0' and jr.del_flag = 0))
<#if ( jimuReport.name )?? && jimuReport.name ?length gt 0>
    /* 名称 */
and jr.NAME  like  :jimuReport.name
</#if>
<#if ( jimuReport.createBy )?? && jimuReport.createBy ?length gt 0>
/* 创建人 */
and jr.CREATE_BY = :jimuReport.createBy
</#if>
<#if typeList?? && typeList?size gt 0>
    /* 类型 */
    and jr.TYPE in (:typeList)
</#if>
<#if ( jimuReport.delFlag )?? && jimuReport.delFlag ?length gt 0>
    /* 删除标识0-正常,1-已删除 */
    and jr.DEL_FLAG = :jimuReport.delFlag
</#if>
<#if ( jimuReport.template )?? && jimuReport.template ?length gt 0>
    /* 是否是模板 0-是,1-不是 */
    and jr.TEMPLATE = :jimuReport.template
</#if>
<#if ( jimuReport.tenantId )?? && jimuReport.tenantId ?length gt 0>
    /* 租户标识 */
    and jr.tenant_id = :jimuReport.tenantId
</#if>

<#if ( jimuReport.submitForm )?? && jimuReport.submitForm ?length gt 0>
    /* 是否为填报 0否 1是 */
    and jr.submit_form = :jimuReport.submitForm
</#if>
 ORDER BY jr.create_time DESC 
