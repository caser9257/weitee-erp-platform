package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductionCostAllocationMapper extends BaseMapperX<ErpProductionCostAllocationDO> {

    default PageResult<ErpProductionCostAllocationDO> selectPage(ErpProductionCostAllocationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionCostAllocationDO>()
                .likeIfPresent(ErpProductionCostAllocationDO::getAllocationNo, reqVO.getAllocationNo())
                .eqIfPresent(ErpProductionCostAllocationDO::getAccountingMonth, reqVO.getAccountingMonth())
                .eqIfPresent(ErpProductionCostAllocationDO::getCostType, reqVO.getCostType())
                .eqIfPresent(ErpProductionCostAllocationDO::getRuleId, reqVO.getRuleId())
                .eqIfPresent(ErpProductionCostAllocationDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProductionCostAllocationDO::getId));
    }

    default ErpProductionCostAllocationDO selectByAccountingMonthAndCostType(String accountingMonth, Integer costType) {
        return selectOne(new LambdaQueryWrapperX<ErpProductionCostAllocationDO>()
                .eq(ErpProductionCostAllocationDO::getAccountingMonth, accountingMonth)
                .eq(ErpProductionCostAllocationDO::getCostType, costType)
                .orderByDesc(ErpProductionCostAllocationDO::getId));
    }

    default Long selectCountExecutedByAccountingMonth(String accountingMonth) {
        return selectCount(new LambdaQueryWrapperX<ErpProductionCostAllocationDO>()
                .eq(ErpProductionCostAllocationDO::getAccountingMonth, accountingMonth)
                .eq(ErpProductionCostAllocationDO::getStatus, ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus()));
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpProductionCostAllocationDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<ErpProductionCostAllocationDO>()
                .eq(ErpProductionCostAllocationDO::getId, id)
                .eq(ErpProductionCostAllocationDO::getStatus, status));
    }

}
