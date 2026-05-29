package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;

public interface ErpMaterialPlanRuleResolver {

    ErpMaterialPlanRuleDO resolveByProductId(Long productId);

}
