package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
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
