INSERT INTO jimu_report_ext_data (
    id,
    biz_type,
    name,
    descr,
    tags,
    data_value,
    metadata,
    status,
    create_by,
    create_time,
    update_by,
    update_time
) VALUES (
    :jimuReportExtData.id,
    :jimuReportExtData.bizType,
    :jimuReportExtData.name,
    :jimuReportExtData.descr,
    :jimuReportExtData.tags,
    :jimuReportExtData.dataValue,
    :jimuReportExtData.metadata,
    :jimuReportExtData.status,
    :jimuReportExtData.createBy,
    :jimuReportExtData.createTime,
    :jimuReportExtData.updateBy,
    :jimuReportExtData.updateTime
)

