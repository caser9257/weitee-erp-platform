package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
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
