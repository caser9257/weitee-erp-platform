package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpMrpShortageMapper extends BaseMapperX<ErpMrpShortageDO> {

    default void deleteByPlanId(Long planId) {
        delete(ErpMrpShortageDO::getPlanId, planId);
    }

    default List<ErpMrpShortageDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<ErpMrpShortageDO>()
                .eq(ErpMrpShortageDO::getPlanId, planId)
                .orderByAsc(ErpMrpShortageDO::getTracePathKey)
                .orderByAsc(ErpMrpShortageDO::getRequiredDate)
                .orderByAsc(ErpMrpShortageDO::getId));
    }

}
