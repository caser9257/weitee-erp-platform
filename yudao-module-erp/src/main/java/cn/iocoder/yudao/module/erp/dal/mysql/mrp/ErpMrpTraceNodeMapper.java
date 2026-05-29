package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpTraceNodeDO;
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
