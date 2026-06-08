package cn.iocoder.yudao.module.erp.service.mrp.listener;

import cn.iocoder.yudao.module.erp.framework.event.ErpSaleOrderApprovedEvent;
import cn.iocoder.yudao.module.erp.service.mrp.ErpSaleOrderMrpFlowService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;

@Component
public class ErpSaleOrderApprovedListener {

    @Resource
    private ErpSaleOrderMrpFlowService saleOrderMrpFlowService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSaleOrderApproved(ErpSaleOrderApprovedEvent event) {
        saleOrderMrpFlowService.releaseApprovedSaleOrder(event.getSaleOrderId());
    }

}
