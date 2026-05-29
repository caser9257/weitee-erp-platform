package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;

public interface ErpMrpNettingPolicyResolver {

    ErpMrpNettingRuntimePolicy resolve(String businessType);
}
