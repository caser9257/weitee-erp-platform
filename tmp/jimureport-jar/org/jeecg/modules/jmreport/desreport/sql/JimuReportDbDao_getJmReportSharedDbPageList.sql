SELECT jrd.id,jrd.iz_shared_source,jrd.db_type,jrd.db_code,jrd.create_time,jrd.db_ch_name FROM jimu_report_db jrd where 
(jrd.JIMU_REPORT_ID = '' or jrd.JIMU_REPORT_ID is null) and jrd.IZ_SHARED_SOURCE = 1
<#if name?? && name?length gt 0>
    AND jrd.DB_CH_NAME like :name
</#if>
ORDER BY CREATE_TIME DESC