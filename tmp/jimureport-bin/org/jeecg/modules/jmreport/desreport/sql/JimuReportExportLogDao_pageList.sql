SELECT
    jmlog.id,
    jmlog.batch_no,
    jmlog.export_channel,
    jmlog.export_from,
    jmlog.from_id,
    jmlog.export_type,
    jmlog.report_id,
    jmlog.download_path,
    jmlog.status,
    jmlog.err_msg,
    jmlog.create_by,
    jmlog.create_time,
    jmlog.update_time,
    jmjob.name as from_job_name
FROM jimu_report_export_log jmlog
LEFT JOIN jimu_report_export_job jmjob ON jmlog.from_id = jmjob.id
WHERE
    1=1
    <#if ( jimuReportExportLog.batchNo )?? && jimuReportExportLog.batchNo ?length gt 0>
    and jmlog.batch_no  like  :jimuReportExportLog.batchNo
    </#if>
    <#if ( jimuReportExportLog.fromId )?? && jimuReportExportLog.fromId ?length gt 0>
    and jmlog.from_id  =  :jimuReportExportLog.fromId
    </#if>
    <#if ( jimuReportExportLog.tenantId )?? && jimuReportExportLog.tenantId ?length gt 0>
    and jmlog.tenant_id = :jimuReportExportLog.tenantId
    </#if>
order by jmlog.create_time desc