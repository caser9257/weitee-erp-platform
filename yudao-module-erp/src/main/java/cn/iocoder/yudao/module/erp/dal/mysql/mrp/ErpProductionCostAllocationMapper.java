package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
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

}
