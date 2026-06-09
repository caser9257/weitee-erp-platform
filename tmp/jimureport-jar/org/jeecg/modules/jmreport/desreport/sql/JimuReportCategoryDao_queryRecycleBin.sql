SELECT * FROM (
      SELECT jrc.*
      FROM (
           SELECT CONCAT('1',jrc.update_time) as ord, jrc.ID, jrc.NAME, jrc.SOURCE_TYPE as type, jrc.UPDATE_TIME
           FROM jimu_report_category jrc
           WHERE jrc.DEL_FLAG = 1 AND jrc.SOURCE_TYPE = 'report'
               <#if (jimuReport.name)?? && jimuReport.name?length gt 0>
               /* 名称 */
               and jrc.NAME  like  :jimuReport.name
               </#if>
               <#if ( jimuReport.createBy )?? && jimuReport.createBy?length gt 0>
               /* 创建人 */
               and jrc.CREATE_BY = :jimuReport.createBy
               </#if>
               <#if ( jimuReport.tenantId )?? && jimuReport.tenantId?length gt 0>
              /* 租户标识 */
               and jrc.tenant_id = :jimuReport.tenantId
              </#if>
           ) jrc
          UNION ALL
          SELECT jr.*
          FROM (
              SELECT CONCAT('0',jr.update_time) as ord, jr.ID, jr.NAME, jr.TYPE, jr.UPDATE_TIME
              FROM jimu_report jr
              WHERE jr.DEL_FLAG = 1 AND jr.TEMPLATE = 0
              <#if ( jimuReport.name )?? && jimuReport.name?length gt 0>
              /* 名称 */
              and jr.NAME like :jimuReport.name
              </#if>
              <#if ( jimuReport.createBy )?? && jimuReport.createBy?length gt 0>
              /* 创建人 */
              and jr.CREATE_BY = :jimuReport.createBy
              </#if>
              <#if ( jimuReport.tenantId )?? && jimuReport.tenantId?length gt 0>
              /* 租户标识 */
               and jr.tenant_id = :jimuReport.tenantId
              </#if>
     ) jr
  ) jm order by jm.ord desc,jm.update_time desc
