package cn.iocoder.yudao.module.erp.dal.mysql.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.rule.ErpMaterialPlanRulePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpMaterialPlanRuleMapper extends BaseMapperX<ErpMaterialPlanRuleDO> {

    default PageResult<ErpMaterialPlanRuleDO> selectPage(ErpMaterialPlanRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpMaterialPlanRuleDO>()
                .eqIfPresent(ErpMaterialPlanRuleDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpMaterialPlanRuleDO::getSupplyType, reqVO.getSupplyType())
                .eqIfPresent(ErpMaterialPlanRuleDO::getReplenishMode, reqVO.getReplenishMode())
                .eqIfPresent(ErpMaterialPlanRuleDO::getEnableFlag, reqVO.getEnableFlag())
                .orderByDesc(ErpMaterialPlanRuleDO::getId));
    }

    default ErpMaterialPlanRuleDO selectByProductId(Long productId) {
        return selectOne(ErpMaterialPlanRuleDO::getProductId, productId);
    }

}
