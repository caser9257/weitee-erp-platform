UPDATE jimu_report_ext_data SET
    biz_type = :jimuReportExtData.bizType,
    name = :jimuReportExtData.name,
    descr = :jimuReportExtData.descr,
    tags = :jimuReportExtData.tags,
    data_value = :jimuReportExtData.dataValue,
    metadata = :jimuReportExtData.metadata,
    status = :jimuReportExtData.status,
    update_by = :jimuReportExtData.updateBy,
    update_time = :jimuReportExtData.updateTime
WHERE id = :jimuReportExtData.id

