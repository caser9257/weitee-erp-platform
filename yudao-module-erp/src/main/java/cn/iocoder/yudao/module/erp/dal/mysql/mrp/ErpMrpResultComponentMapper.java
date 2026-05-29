package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpMrpResultComponentMapper extends BaseMapperX<ErpMrpResultComponentDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpMrpResultComponentDO::getPlanId, planId);
    }

    default List<ErpMrpResultComponentDO> selectListByResultId(Long resultId) {
        return selectList(new LambdaQueryWrapperX<ErpMrpResultComponentDO>()
                .eq(ErpMrpResultComponentDO::getResultId, resultId)
                .orderByAsc(ErpMrpResultComponentDO::getSequenceNo)
                .orderByAsc(ErpMrpResultComponentDO::getId));
    }
}
