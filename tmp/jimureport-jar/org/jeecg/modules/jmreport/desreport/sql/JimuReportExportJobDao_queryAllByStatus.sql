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
    and status = :status
order by create_time desc