package cn.iocoder.yudao.module.erp.service.mrp.listener;

import cn.iocoder.yudao.module.erp.framework.event.ErpProductionQualityPassedEvent;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderDeliveryReadyService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

@Component
public class ErpProductionQualityPassedListener {

    @Resource
    private ErpSaleOrderDeliveryReadyService saleOrderDeliveryReadyService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductionQualityPassed(ErpProductionQualityPassedEvent event) {
        saleOrderDeliveryReadyService.recalculate(event.getSourceOrderId());
    }

}
