package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRequest;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingResult;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import cn.iocoder.yudao.module.erp.service.mrp.support.ErpMrpSupplyContext;

public interface ErpMrpNettingService {

    ErpMrpNettingResult calculate(ErpMrpNettingRuntimePolicy policy, ErpMrpSupplyContext context,
                                  ErpMrpNettingRequest request);
}
