UPDATE jimu_report_export_job
SET
    <#if jimuReportExportJob.name?exists>
        name = :jimuReportExportJob.name,
    </#if>
    <#if jimuReportExportJob.beginTime?exists>
        begin_time = :jimuReportExportJob.beginTime,
    </#if>
    end_time = :jimuReportExportJob.endTime,
    <#if jimuReportExportJob.execInterval?exists>
        exec_interval = :jimuReportExportJob.execInterval,
    </#if>
    <#if jimuReportExportJob.reportConf?exists>
        report_conf = :jimuReportExportJob.reportConf,
    </#if>
    <#if jimuReportExportJob.lastRunTime?exists>
        last_run_time = :jimuReportExportJob.lastRunTime,
    </#if>
    receiver_email = :jimuReportExportJob.receiverEmail,
    <#if jimuReportExportJob.fileSyncPath?exists>
        file_sync_path = :jimuReportExportJob.fileSyncPath,
    </#if>
    <#if jimuReportExportJob.status?exists>
        status = :jimuReportExportJob.status,
    </#if>
    <#if jimuReportExportJob.createBy?exists>
        create_by = :jimuReportExportJob.createBy,
    </#if>
    <#if jimuReportExportJob.createTime?exists>
        create_time = :jimuReportExportJob.createTime,
    </#if>
    <#if jimuReportExportJob.updateBy?exists>
        update_by = :jimuReportExportJob.updateBy,
    </#if>
    <#if jimuReportExportJob.updateTime?exists>
        update_time = :jimuReportExportJob.updateTime,
    </#if>
    <#if jimuReportExportJob.tenantId?exists>
        tenant_id = :jimuReportExportJob.tenantId,
    </#if>
WHERE id = :jimuReportExportJob.id