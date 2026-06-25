package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;

public interface ErpMrpNettingPolicyResolver {

    ErpMrpNettingRuntimePolicy resolve(String businessType);
}
