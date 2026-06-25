package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRequest;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingResult;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpSupplyContext;

public interface ErpMrpNettingService {

    ErpMrpNettingResult calculate(ErpMrpNettingRuntimePolicy policy, ErpMrpSupplyContext context,
                                  ErpMrpNettingRequest request);
}
