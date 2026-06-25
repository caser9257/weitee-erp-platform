package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpTraceNodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpMrpTraceNodeMapper extends BaseMapperX<ErpMrpTraceNodeDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpMrpTraceNodeDO::getPlanId, planId);
    }

    default List<ErpMrpTraceNodeDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<ErpMrpTraceNodeDO>()
                .eq(ErpMrpTraceNodeDO::getPlanId, planId)
                .orderByAsc(ErpMrpTraceNodeDO::getTracePathKey)
                .orderByAsc(ErpMrpTraceNodeDO::getId));
    }

    default List<ErpMrpTraceNodeDO> selectListByIds(Collection<Long> ids) {
        return selectBatchIds(ids);
    }

}
