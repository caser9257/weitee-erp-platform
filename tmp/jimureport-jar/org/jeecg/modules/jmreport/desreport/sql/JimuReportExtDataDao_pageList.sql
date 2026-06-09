SELECT * FROM jimu_report_ext_data
WHERE 1=1
<#if jimuReportExtData.bizType ?exists>
    AND biz_type = :jimuReportExtData.bizType
</#if>
<#if jimuReportExtData.name ?exists>
    AND name LIKE :jimuReportExtData.name
</#if>
<#if jimuReportExtData.status ?exists>
    AND status = :jimuReportExtData.status
</#if>
ORDER BY create_time DESC

