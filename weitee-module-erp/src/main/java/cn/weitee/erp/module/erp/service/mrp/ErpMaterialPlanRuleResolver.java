package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;

public interface ErpMaterialPlanRuleResolver {

    ErpMaterialPlanRuleDO resolveByProductId(Long productId);

}
