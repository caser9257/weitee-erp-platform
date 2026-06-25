package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationRulePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ErpProductionCostAllocationRuleMapper extends BaseMapperX<ErpProductionCostAllocationRuleDO> {

    default PageResult<ErpProductionCostAllocationRuleDO> selectPage(ErpProductionCostAllocationRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpProductionCostAllocationRuleDO>()
                .likeIfPresent(ErpProductionCostAllocationRuleDO::getRuleName, reqVO.getRuleName())
                .eqIfPresent(ErpProductionCostAllocationRuleDO::getCostType, reqVO.getCostType())
                .eqIfPresent(ErpProductionCostAllocationRuleDO::getBasisType, reqVO.getBasisType())
                .eqIfPresent(ErpProductionCostAllocationRuleDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpProductionCostAllocationRuleDO::getId));
    }

    default List<ErpProductionCostAllocationRuleDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ErpProductionCostAllocationRuleDO>()
                .eq(ErpProductionCostAllocationRuleDO::getStatus, status)
                .orderByDesc(ErpProductionCostAllocationRuleDO::getId));
    }

}
