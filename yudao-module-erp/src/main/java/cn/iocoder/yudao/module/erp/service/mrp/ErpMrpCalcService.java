package cn.iocoder.yudao.module.erp.service.mrp;

public interface ErpMrpCalcService {

    void run(Long planId);

    void runForSaleOrders(Long planId, java.util.List<cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO> saleOrders);

}
