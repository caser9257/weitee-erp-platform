SELECT * FROM(
     SELECT jrc.*
     FROM (
          SELECT CONCAT('1',jrc.create_time) as ord, jrc.ID, jrc.NAME, null as code, jrc.SOURCE_TYPE as type, 3 as template, null as thumb, null as shareViewUrl, jrc.create_time
          FROM jimu_report_category jrc
          WHERE jrc.DEL_FLAG = 0 AND jrc.SOURCE_TYPE = 'report'
          <#if (jimuReport.name)?? && jimuReport.name?length gt 0>
          /* 名称 */
          and jrc.name  like  :jimuReport.name
          </#if>
          <#if parentId?? && parentId?length gt 0>
          /* 父级id */
          and jrc.parent_id = :parentId
          <#else>
          and (jrc.parent_id IS NULL OR jrc.parent_id = '0')
          </#if>
          ) jrc
     UNION ALL
     SELECT jr.*
     FROM (
          SELECT CONCAT('0',jr.create_time) as ord,jr.ID,jr.NAME,jr.CODE,jr.TYPE,jr.template,jr.thumb,jrs.preview_url as shareViewUrl, jr.create_time FROM jimu_report jr                                                                                                 left join jimu_report_share jrs on jrs.report_id = jr.id
          WHERE 1=1
         <#if ( jimuReport.name )?? && jimuReport.name ?length gt 0>
          /* 名称 */
          and jr.NAME  like  :jimuReport.name
          </#if>
          <#if ( jimuReport.createBy )?? && jimuReport.createBy ?length gt 0>
          /* 创建人 */
          and jr.CREATE_BY = :jimuReport.createBy
          </#if>
          <#if typeList?? && typeList?size gt 0>
            /* 类型 */
            and jr.TYPE in (:typeList)
          </#if>
          <#if ( jimuReport.delFlag )?? && jimuReport.delFlag ?length gt 0>
            /* 删除标识0-正常,1-已删除 */
            and jr.DEL_FLAG = :jimuReport.delFlag
          </#if>
          <#if ( jimuReport.template )?? && jimuReport.template ?length gt 0>
            /* 是否是模板 0-是,1-不是 */
            and jr.TEMPLATE = :jimuReport.template
          </#if>
          <#if ( jimuReport.tenantId )?? && jimuReport.tenantId ?length gt 0>
            /* 租户标识 */
            and jr.tenant_id = :jimuReport.tenantId
          </#if>
   ) jr    
) jm order by jm.ord desc,jm.create_time desc
