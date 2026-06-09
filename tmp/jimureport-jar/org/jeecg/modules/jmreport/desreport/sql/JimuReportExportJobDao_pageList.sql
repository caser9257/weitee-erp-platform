SELECT
    id,
    name,
    begin_time,
    end_time,
    exec_interval,
    report_conf,
    last_run_time,
    receiver_email,
    file_sync_path,
    status,
    create_by,
    create_time,
    update_by,
    update_time,
    tenant_id
FROM
    jimu_report_export_job
WHERE
    1=1
    <#if ( jimuReportExportJob.name )?? && jimuReportExportJob.name ?length gt 0>
    and name like  :jimuReportExportJob.name
    </#if>
    <#if ( jimuReportExportJob.tenantId )?? && jimuReportExportJob.tenantId ?length gt 0>
     and tenant_id = :jimuReportExportJob.tenantId 
    </#if>
order by create_time desc