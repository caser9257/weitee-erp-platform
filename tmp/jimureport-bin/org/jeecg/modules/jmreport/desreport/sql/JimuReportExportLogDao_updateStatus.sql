UPDATE jimu_report_export_log
set status      = :jimuReportExportLog.status,
    update_time = :jimuReportExportLog.updateTime,
    err_msg     = :jimuReportExportLog.errMsg
where
    batch_no = :jimuReportExportLog.batchNo
    and status = 'doing'