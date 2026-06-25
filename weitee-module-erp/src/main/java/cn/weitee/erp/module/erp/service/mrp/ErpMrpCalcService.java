package cn.weitee.erp.module.erp.service.mrp;

public interface ErpMrpCalcService {

    void run(Long planId);

    void runForSaleOrders(Long planId, java.util.List<cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO> saleOrders);

}
