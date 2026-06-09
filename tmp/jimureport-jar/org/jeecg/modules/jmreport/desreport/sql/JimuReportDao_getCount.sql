SELECT COUNT(*) FROM jimu_report WHERE del_flag = '0'
<#if ( submitForm )?? && submitForm ?length gt 0>
    /* 名称 */
    and submit_form = :submitForm
</#if>
