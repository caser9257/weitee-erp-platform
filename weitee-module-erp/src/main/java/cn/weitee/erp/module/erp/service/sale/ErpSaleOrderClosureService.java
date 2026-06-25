package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.service.sale.bo.ErpSaleOrderClosureSummaryBO;

import java.util.Collection;
import java.util.Map;

public interface ErpSaleOrderClosureService {

    ErpSaleOrderClosureSummaryBO getClosureSummary(Long saleOrderId);

    Map<Long, ErpSaleOrderClosureSummaryBO> getClosureSummaryMap(Collection<Long> saleOrderIds);
}
