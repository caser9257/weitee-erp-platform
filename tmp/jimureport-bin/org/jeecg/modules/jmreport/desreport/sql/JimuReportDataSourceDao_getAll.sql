SELECT jrds.id,jrds.name,jrds.db_type FROM jimu_report_data_source jrds where 1=1
<#include "JimuReportDataSourceDao_condition.sql">
order by create_time desc