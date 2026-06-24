package cn.iocoder.yudao.module.erp.service.purchase.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderBpmConstants;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderBpmService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @deprecated 已迁移到 BpmApprovalEventDispatcher + PurchaseOrderResultHandler 模式
 * 保留此类仅为兼容旧 BPM 实例，新流程不再使用
 */
@Deprecated
public class ErpPurchaseOrderBpmStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private ErpPurchaseOrderBpmService purchaseOrderBpmService;

    @Override
    protected String getProcessDefinitionKey() {
        return ErpPurchaseOrderBpmConstants.PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        purchaseOrderBpmService.handleProcessInstanceResult(Long.parseLong(event.getBusinessKey()),
                event.getId(), event.getStatus(), event.getReason());
    }

}
