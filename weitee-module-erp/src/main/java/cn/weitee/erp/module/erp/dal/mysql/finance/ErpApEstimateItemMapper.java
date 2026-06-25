package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ErpApEstimateItemMapper extends BaseMapperX<ErpApEstimateItemDO> {

    default List<ErpApEstimateItemDO> selectListByEstimateId(Long estimateId) {
        return selectList(new LambdaQueryWrapperX<ErpApEstimateItemDO>()
                .eq(ErpApEstimateItemDO::getEstimateId, estimateId)
                .orderByAsc(ErpApEstimateItemDO::getId));
    }

    default List<ErpApEstimateItemDO> selectListByEstimateIds(Collection<Long> estimateIds) {
        if (estimateIds == null || estimateIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ErpApEstimateItemDO>()
                .in(ErpApEstimateItemDO::getEstimateId, estimateIds)
                .orderByAsc(ErpApEstimateItemDO::getId));
    }

}
