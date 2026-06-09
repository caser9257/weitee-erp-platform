UPDATE jimu_report_category
SET 
    <#if category.id ? exists >
        ID = :category.id,
    </#if> 
    <#if category.name ? exists >
        NAME = :category.name,
    </#if> 

    <#if category.parentId ? exists >
        parent_id = :category.parentId,
    </#if>
    <#if category.izLeaf ? exists >
        iz_leaf = :category.izLeaf,
    </#if>
    <#if category.sourceType ? exists >
        source_type = :category.sourceType,
    </#if>
    <#if category.createBy ? exists >
        CREATE_BY = :category.createBy,
    </#if> 
    <#if category.createTime ? exists >
        CREATE_TIME = :category.createTime,
    </#if>
    <#if category.updateBy ? exists >
        UPDATE_BY = :category.updateBy,
    </#if>
    <#if category.updateTime ? exists >
        UPDATE_TIME = :category.updateTime,
    </#if>
    <#if category.delFlag ? exists >
        DEL_FLAG = :category.delFlag,
    </#if>
    <#if category.tenantId ? exists >
        TENANT_ID = :category.tenantId,
    </#if>
    <#if category.sortNo ? exists >
        SORT_NO = :category.sortNo,
    </#if>
WHERE id = :category.id