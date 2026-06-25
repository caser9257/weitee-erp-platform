package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpMrpResultMapper extends BaseMapperX<ErpMrpResultDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpMrpResultDO::getPlanId, planId);
    }

    default List<ErpMrpResultDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<ErpMrpResultDO>()
                .eq(ErpMrpResultDO::getPlanId, planId)
                .orderByAsc(ErpMrpResultDO::getTracePathKey)
                .orderByAsc(ErpMrpResultDO::getId));
    }

}
