package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultComponentDO;
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
