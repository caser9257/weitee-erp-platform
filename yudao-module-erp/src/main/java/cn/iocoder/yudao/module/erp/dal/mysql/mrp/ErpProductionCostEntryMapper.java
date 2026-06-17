package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionCostEntryMapper extends BaseMapperX<ErpProductionCostEntryDO> {

    default PageResult<ErpProductionCostEntryDO> selectPage(ErpProductionCostEntryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionCostEntryDO>()
                .eqIfPresent(ErpProductionCostEntryDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionCostEntryDO::getCostType, reqVO.getCostType())
                .eqIfPresent(ErpProductionCostEntryDO::getAccountingMonth, reqVO.getAccountingMonth())
                .orderByDesc(ErpProductionCostEntryDO::getId));
    }

    default List<ErpProductionCostEntryDO> selectListByProductionOrderId(Long productionOrderId) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostEntryDO>()
                .eq(ErpProductionCostEntryDO::getProductionOrderId, productionOrderId)
                .orderByDesc(ErpProductionCostEntryDO::getId));
    }

    default List<ErpProductionCostEntryDO> selectListByAccountingMonth(String accountingMonth) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostEntryDO>()
                .eqIfPresent(ErpProductionCostEntryDO::getAccountingMonth, accountingMonth)
                .orderByAsc(ErpProductionCostEntryDO::getProductionOrderId, ErpProductionCostEntryDO::getId));
    }

    default List<ErpProductionCostEntryDO> selectListByAccountingMonths(List<String> accountingMonths) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostEntryDO>()
                .in(ErpProductionCostEntryDO::getAccountingMonth, accountingMonths)
                .orderByAsc(ErpProductionCostEntryDO::getProductionOrderId, ErpProductionCostEntryDO::getId));
    }

    default List<ErpProductionCostEntryDO> selectListByPageReqVO(ErpProductionCostEntryPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostEntryDO>()
                .eqIfPresent(ErpProductionCostEntryDO::getProductionOrderId, reqVO.getProductionOrderId())
                .eqIfPresent(ErpProductionCostEntryDO::getCostType, reqVO.getCostType())
                .eqIfPresent(ErpProductionCostEntryDO::getAccountingMonth, reqVO.getAccountingMonth())
                .orderByDesc(ErpProductionCostEntryDO::getProductionOrderId, ErpProductionCostEntryDO::getId));
    }

}
