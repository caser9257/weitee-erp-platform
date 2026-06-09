    <#if ( category.id )?? && category.id ?length gt 0>
        /* 主键 */
        and jrc.ID = :category.id
    </#if>
    <#if ( category.name )?? && category.name ?length gt 0>
        /* 名称 */
        and jrc.name = :category.name
    </#if>
    <#if ( category.parentId )?? && category.parentId ?length gt 0 && category.parentId?string != "0">
        /* 父级id */
        and jrc.parent_id = :category.parentId
    <#else>
        and (jrc.parent_id IS NULL OR jrc.parent_id = '0')
    </#if>
    <#if ( category.izLeaf )?? && category.izLeaf ?length gt 0>
        /* 是否为叶子节点 */
        and jrc.iz_leaf = :category.izLeaf
    </#if>
    <#if ( category.sourceType )?? && category.sourceType ?length gt 0>
        /* 来源类型( report 积木报表 screen 大屏  drag 仪表盘) */
        and jrc.source_type = :category.sourceType
    </#if>
    <#if ( category.createBy )?? && category.createBy ?length gt 0>
        /* 创建人 */
        and jrc.CREATE_BY = :category.createBy
    </#if>
    <#if ( category.createTime )??>
        /* 创建时间 */
        and jrc.CREATE_TIME = :category.createTime
    </#if>
    <#if ( category.updateBy )?? && category.updateBy ?length gt 0>
        /* 修改人 */
        and jrc.UPDATE_BY = :category.updateBy
    </#if>
    <#if ( category.updateTime )??>
        /* 修改时间 */
        and jrc.UPDATE_TIME = :category.updateTime
    </#if>
    <#if ( category.delFlag )?? && category.delFlag ?length gt 0>
        /* 删除标识0-正常,1-已删除 */
        and jrc.DEL_FLAG = :category.delFlag
    </#if>
    <#if ( category.tenantId )?? && category.tenantId ?length gt 0>
        /* 租户标识 */
        and jrc.TENANT_ID = :category.tenantId
    </#if>
