package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductionCostAllocationResultMapper extends BaseMapperX<ErpProductionCostAllocationResultDO> {

    default List<ErpProductionCostAllocationResultDO> selectListByAllocationId(Long allocationId) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostAllocationResultDO>()
                .eq(ErpProductionCostAllocationResultDO::getAllocationId, allocationId)
                .orderByDesc(ErpProductionCostAllocationResultDO::getAllocatedAmount, ErpProductionCostAllocationResultDO::getId));
    }

    default List<ErpProductionCostAllocationResultDO> selectListByGeneratedCostEntryIds(Collection<Long> generatedCostEntryIds) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostAllocationResultDO>()
                .inIfPresent(ErpProductionCostAllocationResultDO::getGeneratedCostEntryId, generatedCostEntryIds)
                .orderByAsc(ErpProductionCostAllocationResultDO::getGeneratedCostEntryId, ErpProductionCostAllocationResultDO::getId));
    }

}
