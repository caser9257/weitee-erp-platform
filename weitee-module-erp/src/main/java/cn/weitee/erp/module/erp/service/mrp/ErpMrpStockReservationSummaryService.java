package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;

import java.util.Collection;
import java.util.List;

public interface ErpMrpStockReservationSummaryService {

    List<ErpMrpStockReservationSummaryDO> getActiveSummaryList();

    void refreshSummaryByProductIds(Collection<Long> productIds);

}
